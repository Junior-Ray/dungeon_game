package com.dungeon_game.client.audio;

import com.dungeon_game.core.api.AudioPlayer;
import com.dungeon_game.core.audio.MusicTrack;
import com.dungeon_game.core.audio.SoundEffect;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JavaAudioPlayer implements AudioPlayer {

    // --- CACHÉ DE SONIDOS ---
    // Guardamos los datos crudos (bytes) y el formato. 
    // Esto permite crear múltiples clips del mismo sonido simultáneamente.
    private final Map<SoundEffect, byte[]> soundDataCache = new EnumMap<>(SoundEffect.class);
    private final Map<SoundEffect, AudioFormat> soundFormatCache = new EnumMap<>(SoundEffect.class);

    // --- SISTEMA DE HILOS ---
    // Usamos un Pool de hilos 'Cached'. Crea hilos nuevos si es necesario, 
    // pero reutiliza los existentes. Ideal para sonidos cortos y frecuentes.
    private final ExecutorService soundPool;

    // --- ESTADO DE MÚSICA ---
    private Clip currentMusicClip;
    private long musicPausePosition = 0; // Para pausar y reanudar correctamente

    // --- VOLUMENES (0.0f a 1.0f) ---
    private float musicVolume = 0.7f;
    private float soundVolume = 1.0f;

    public JavaAudioPlayer() {
        System.out.println("[AudioPlayer] Inicializando sistema de audio...");
        this.soundPool = Executors.newCachedThreadPool();
        
        // Carga inicial de todos los efectos para evitar lag durante el juego
        loadAllSoundEffects();
    }

    // ==========================================
    //              GESTIÓN DE RECURSOS
    // ==========================================

    private void loadAllSoundEffects() {
        long start = System.currentTimeMillis();
        int loadedCount = 0;

        for (SoundEffect effect : SoundEffect.values()) {
            if (loadSoundToMemory(effect)) {
                loadedCount++;
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("[AudioPlayer] Cargados " + loadedCount + " efectos en " + (end - start) + "ms.");
    }

    private boolean loadSoundToMemory(SoundEffect effect) {
        String path = "/audio/sounds/" + effect.getFileName() + ".wav";
        URL url = getClass().getResource(path);

        if (url == null) {
            System.err.println("⚠️ [Audio] Archivo no encontrado: " + path);
            return false;
        }

        try (AudioInputStream originalStream = AudioSystem.getAudioInputStream(url)) {
            AudioFormat baseFormat = originalStream.getFormat();
            
            // Leemos todos los bytes del stream a la memoria
            // Usamos BufferedInputStream para asegurar lectura eficiente
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[4096];
            int nRead;
            
            while ((nRead = originalStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            
            soundDataCache.put(effect, buffer.toByteArray());
            soundFormatCache.put(effect, baseFormat);
            return true;

        } catch (UnsupportedAudioFileException | IOException e) {
            System.err.println("❌ [Audio] Error cargando " + effect.name() + ": " + e.getMessage());
            return false;
        }
    }

    // ==========================================
    //              MÚSICA
    // ==========================================

    @Override
    public void playMusic(MusicTrack track, boolean loop) {
        // 1. Detener música anterior si existe
        stopMusic();

        // 2. Cargar nueva pista (Streaming, no caché completo)
        String path = "/audio/music/" + track.getFileName() + ".wav";
        URL url = getClass().getResource(path);

        if (url == null) {
            System.err.println("⚠️ [Audio] Música no encontrada: " + track.name());
            return;
        }

        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(url.openStream()));
            currentMusicClip = AudioSystem.getClip();
            currentMusicClip.open(ais);

            // Ajustar volumen antes de empezar
            applyVolumeToClip(currentMusicClip, musicVolume);

            if (loop) {
                currentMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
            } else {
                currentMusicClip.start();
            }

        } catch (Exception e) {
            System.err.println("❌ [Audio] Error reproduciendo música: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void stopMusic() {
        if (currentMusicClip != null) {
            if (currentMusicClip.isRunning()) {
                currentMusicClip.stop();
            }
            currentMusicClip.close(); // Liberar recursos del sistema
            currentMusicClip = null;
            musicPausePosition = 0;
        }
    }

    @Override
    public void pauseMusic() {
        if (currentMusicClip != null && currentMusicClip.isRunning()) {
            musicPausePosition = currentMusicClip.getMicrosecondPosition();
            currentMusicClip.stop();
        }
    }

    @Override
    public void resumeMusic() {
        if (currentMusicClip != null && !currentMusicClip.isRunning()) {
            currentMusicClip.setMicrosecondPosition(musicPausePosition);
            currentMusicClip.start();
        }
    }

    @Override
    public void setMusicVolume(float volume) {
        this.musicVolume = clampVolume(volume);
        if (currentMusicClip != null && currentMusicClip.isOpen()) {
            applyVolumeToClip(currentMusicClip, musicVolume);
        }
    }
    
    // Necesario si lo pide la interfaz (opcional según tu API)
    public float getMusicVolume() { return musicVolume; }

    // ==========================================
    //              EFECTOS DE SONIDO (SFX)
    // ==========================================

    @Override
    public void playSound(SoundEffect effect) {
        // 1. Optimización rápida: Si está mudo o no existe el sonido, no hacemos nada.
        if (soundVolume < 0.01f || !soundDataCache.containsKey(effect)) {
            return;
        }

        // 2. "Fire and Forget": Lanzamos la tarea a un hilo secundario
        soundPool.submit(() -> {
            try {
                // Recuperamos los datos crudos de la RAM
                byte[] data = soundDataCache.get(effect);
                AudioFormat format = soundFormatCache.get(effect);

                // Solicitamos una línea de audio al sistema
                DataLine.Info info = new DataLine.Info(Clip.class, format);
                
                // Verificación de seguridad por si el hardware de audio está saturado
                if (!AudioSystem.isLineSupported(info)) return;

                Clip sfxClip = (Clip) AudioSystem.getLine(info);
                sfxClip.open(format, data, 0, data.length);

                // --- A. APLICAR VOLUMEN ---
                applyVolumeToClip(sfxClip, soundVolume);

                // --- B. APLICAR PITCH ALEATORIO (Variación de tono) ---
                // Verificamos si el driver de audio soporta cambiar la frecuencia
                if (sfxClip.isControlSupported(FloatControl.Type.SAMPLE_RATE)) {
                    FloatControl rateControl = (FloatControl) sfxClip.getControl(FloatControl.Type.SAMPLE_RATE);
                    
                    float baseRate = format.getSampleRate(); // Ej: 44100.0 Hz
                    float variationRange = 0.05f; // ±5% de variación (Subtil y natural)

                    // Fórmula: Random entre (1.0 - range) y (1.0 + range)
                    // Math.random() devuelve 0.0 a 1.0
                    float randomFactor = 1.0f + ((float) Math.random() * variationRange * 2) - variationRange;
                    
                    float newRate = baseRate * randomFactor;
                    
                    // Aplicamos la nueva velocidad (esto cambia el tono)
                    rateControl.setValue(newRate);
                }

                // --- C. LIMPIEZA ---
                // Agregamos un listener para cerrar el clip apenas termine de sonar
                sfxClip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        sfxClip.close();
                    }
                });

                // --- D. REPRODUCIR ---
                sfxClip.start();

            } catch (Exception e) {
                // Capturamos cualquier error de audio silenciosamente para no detener el juego
                // System.err.println("Error reproduciento SFX: " + effect.name());
            }
        });
    }

    @Override
    public void setSoundVolume(float volume) {
        this.soundVolume = clampVolume(volume);
        // Nota: No actualizamos los clips de SFX que ya están sonando porque
        // suelen ser muy cortos. El nuevo volumen aplicará al siguiente sonido.
    }
    
    // Necesario si lo pide la interfaz
    public float getSoundVolume() { return soundVolume; }

    // ==========================================
    //              UTILIDADES / DISPOSE
    // ==========================================

    /**
     * Convierte volumen lineal (0.0 - 1.0) a Decibeles para Java Sound API.
     */
    private void applyVolumeToClip(Clip clip, float volume) {
        if (clip == null) return;
        try {
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            
            // Fórmula: dB = 20 * log10(volumen)
            // Usamos Math.max para evitar log(0) = -Infinity
            float dB = (float) (Math.log(Math.max(0.0001f, volume)) / Math.log(10.0) * 20.0);
            
            // Aseguramos que el valor esté dentro del rango permitido por el hardware
            float min = gainControl.getMinimum();
            float max = gainControl.getMaximum();
            
            gainControl.setValue(Math.max(min, Math.min(dB, max)));
            
        } catch (IllegalArgumentException e) {
            // El control MASTER_GAIN no está soportado por este clip (raro, pero posible)
        }
    }

    private float clampVolume(float vol) {
        return Math.max(0.0f, Math.min(1.0f, vol));
    }
    
    @Override
    public boolean isMusicPlaying() {
        // Verificamos dos cosas:
        // 1. Que el objeto clip exista (no sea null)
        // 2. Que esté activamente corriendo (isRunning devuelve false si está en pause o stop)
        return currentMusicClip != null && currentMusicClip.isRunning();
    }

    @Override
    public void dispose() {
        System.out.println("[AudioPlayer] Liberando recursos...");
        stopMusic();
        soundDataCache.clear();
        soundFormatCache.clear();
        soundPool.shutdownNow(); // Forzamos el cierre de los hilos de sonido
    }
    
}