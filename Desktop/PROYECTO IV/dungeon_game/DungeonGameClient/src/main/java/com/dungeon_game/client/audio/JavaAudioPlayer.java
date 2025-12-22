package com.dungeon_game.client.audio;

import com.dungeon_game.core.api.AudioPlayer;
import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class JavaAudioPlayer implements AudioPlayer {
    
    private Clip musicClip;
    private Map<String, Clip> soundCache = new HashMap<>();
    
    private float musicVolume = 0.7f;
    private float soundVolume = 1.0f;
    
    private static final int MAX_SOUND_CACHE = 20;
    
    public JavaAudioPlayer() {
        System.out.println("[AudioPlayer] Inicializado");
    }
    
    @Override
    public void playMusic(String musicId, boolean loop) {
        stopMusic();
        
        try {
            URL url = getClass().getResource("/audio/music/" + musicId + ".wav");
            if (url == null) {
                System.err.println("[AudioPlayer] Música no encontrada: " + musicId);
                return;
            }
            
            AudioInputStream ais = AudioSystem.getAudioInputStream(url);
            musicClip = AudioSystem.getClip();
            musicClip.open(ais);
            
            setMusicVolume(musicVolume);
            
            if (loop) {
                musicClip.loop(Clip.LOOP_CONTINUOUSLY);
            } else {
                musicClip.start();
            }
            
            System.out.println("[AudioPlayer] Reproduciendo música: " + musicId);
            
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("[AudioPlayer] Error al cargar música: " + musicId);
            e.printStackTrace();
        }
    }
    
    @Override
    public void stopMusic() {
        if (musicClip != null) {
            if (musicClip.isRunning()) {
                musicClip.stop();
            }
            musicClip.close();
            musicClip = null;
        }
    }
    
    @Override
    public void pauseMusic() {
        if (musicClip != null && musicClip.isRunning()) {
            musicClip.stop();
        }
    }
    
    @Override
    public void resumeMusic() {
        if (musicClip != null && !musicClip.isRunning()) {
            musicClip.start();
        }
    }
    
    @Override
    public void setMusicVolume(float volume) {
        this.musicVolume = Math.max(0f, Math.min(1f, volume));
        if (musicClip != null && musicClip.isOpen()) {
            setClipVolume(musicClip, musicVolume);
        }
    }
    
    @Override
    public float getMusicVolume() {
        return musicVolume;
    }
    
    @Override
    public void playSound(String soundId) {
        try {
            URL url = getClass().getResource("/audio/sounds/" + soundId + ".wav");
            if (url == null) {
                System.err.println("[AudioPlayer] Sonido no encontrado: " + soundId);
                return;
            }
            
            AudioInputStream ais = AudioSystem.getAudioInputStream(url);
            Clip clip = AudioSystem.getClip();
            clip.open(ais);
            
            setClipVolume(clip, soundVolume);
            clip.start();
            
            // Auto-cerrar cuando termine
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });
            
        } catch (Exception e) {
            System.err.println("[AudioPlayer] Error al reproducir sonido: " + soundId);
            // No imprimir stack trace para sonidos (evitar spam)
        }
    }
    
    @Override
    public void setSoundVolume(float volume) {
        this.soundVolume = Math.max(0f, Math.min(1f, volume));
    }
    
    @Override
    public float getSoundVolume() {
        return soundVolume;
    }
    
    @Override
    public boolean isMusicPlaying() {
        return musicClip != null && musicClip.isRunning();
    }
    
    /**
     * Ajusta el volumen de un Clip usando control de ganancia (decibeles)
     */
    private void setClipVolume(Clip clip, float volume) {
        if (clip == null || !clip.isOpen()) return;
        
        try {
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            
            // Convertir volumen lineal (0-1) a decibeles
            // Fórmula: dB = 20 * log10(volume)
            // Agregar piso mínimo para evitar -infinito
            float dB = (float)(Math.log(Math.max(0.0001f, volume)) / Math.log(10.0) * 20.0);
            
            // Clamp al rango permitido por el control
            float min = gainControl.getMinimum();
            float max = gainControl.getMaximum();
            dB = Math.max(min, Math.min(dB, max));
            
            gainControl.setValue(dB);
            
        } catch (IllegalArgumentException e) {
            System.err.println("[AudioPlayer] No se pudo ajustar volumen del clip");
        }
    }
    
    /**
     * Liberar recursos
     */
    public void dispose() {
        stopMusic();
        soundCache.values().forEach(Clip::close);
        soundCache.clear();
    }
}