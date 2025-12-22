package com.dungeon_game.core.audio;

import com.dungeon_game.core.api.AudioPlayer;

public class AudioManager {
    private static AudioManager instance;
    private AudioPlayer player;
    
    private float musicVolume = 0.7f;
    private float soundVolume = 1.0f;
    private float masterVolume = 1.0f;
    
    private MusicTrack currentTrack = null;
    private boolean musicEnabled = true;
    private boolean soundEnabled = true;
    
    private boolean isFading = false;
    
    // 👇 NUEVO: Estado de pausa automática
    private boolean isPausedByFocusLoss = false;
    
    private AudioManager() {}
    
    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }
    
    public void setAudioPlayer(AudioPlayer player) {
        this.player = player;
        if (player != null) {
            player.setMusicVolume(musicVolume * masterVolume);
            player.setSoundVolume(soundVolume * masterVolume);
        }
    }
    
    // ===== MÚSICA =====
    
    public void playMusic(MusicTrack track) {
        playMusic(track, true);
    }
    
    public void playMusic(MusicTrack track, boolean loop) {
        if (!musicEnabled || player == null || track == null) return;
        
        if (currentTrack == track && player.isMusicPlaying()) {
            return;
        }
        
        currentTrack = track;
        isPausedByFocusLoss = false; // Reset flag
        player.playMusic(track.getFileName(), loop);
    }
    
    public void playMusic(String trackName, boolean loop) {
        playMusic(MusicTrack.valueOf(trackName.toUpperCase()), loop);
    }
    
    public void transitionToMusic(MusicTrack newTrack) {
        if (player == null || newTrack == null) return;
        
        if (currentTrack == newTrack && player.isMusicPlaying()) {
            return;
        }
        
        if (!player.isMusicPlaying()) {
            playMusic(newTrack);
            fadeInMusic(500);
            return;
        }
        
        fadeOutMusic(300, () -> {
            playMusic(newTrack);
            fadeInMusic(500);
        });
    }
    
    private void fadeOutMusic(int durationMs, Runnable onComplete) {
        if (isFading) return;
        
        isFading = true;
        new Thread(() -> {
            float originalVolume = musicVolume * masterVolume;
            int steps = 10;
            int delay = durationMs / steps;
            
            for (int i = steps; i >= 0; i--) {
                float vol = originalVolume * (i / (float) steps);
                if (player != null) {
                    player.setMusicVolume(vol);
                }
                try { Thread.sleep(delay); } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            
            if (player != null) {
                player.stopMusic();
            }
            
            isFading = false;
            
            if (onComplete != null) {
                onComplete.run();
            }
        }).start();
    }
    
    private void fadeInMusic(int durationMs) {
        if (isFading) return;
        
        isFading = true;
        new Thread(() -> {
            float targetVolume = musicVolume * masterVolume;
            int steps = 10;
            int delay = durationMs / steps;
            
            for (int i = 0; i <= steps; i++) {
                float vol = targetVolume * (i / (float) steps);
                if (player != null) {
                    player.setMusicVolume(vol);
                }
                try { Thread.sleep(delay); } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            
            isFading = false;
        }).start();
    }
    
    public void stopMusic() {
        if (player != null) {
            player.stopMusic();
        }
        currentTrack = null;
        isPausedByFocusLoss = false;
    }
    
    public void pauseMusic() {
        if (player != null) {
            player.pauseMusic();
        }
    }
    
    public void resumeMusic() {
        if (player != null) {
            player.resumeMusic();
        }
    }
    
    // 👇 NUEVO: Métodos para manejo de foco de ventana
    
    /**
     * Llamar cuando la ventana pierde el foco
     */
    public void onWindowFocusLost() {
        if (player != null && player.isMusicPlaying()) {
            player.pauseMusic();
            isPausedByFocusLoss = true;
            System.out.println("[AudioManager] Música pausada (ventana sin foco)");
        }
    }
    
    /**
     * Llamar cuando la ventana recupera el foco
     */
    public void onWindowFocusGained() {
        if (player != null && isPausedByFocusLoss && musicEnabled) {
            player.resumeMusic();
            isPausedByFocusLoss = false;
            System.out.println("[AudioManager] Música reanudada (ventana con foco)");
        }
    }
    
    public void setMusicVolume(float volume) {
        this.musicVolume = Math.max(0f, Math.min(1f, volume));
        if (player != null && !isFading) {
            player.setMusicVolume(this.musicVolume * masterVolume);
        }
    }
    
    public float getMusicVolume() {
        return musicVolume;
    }
    
    public void setMusicEnabled(boolean enabled) {
        this.musicEnabled = enabled;
        if (!enabled) {
            stopMusic();
        } else if (currentTrack != null) {
            playMusic(currentTrack);
        }
    }
    
    public boolean isMusicEnabled() {
        return musicEnabled;
    }
    
    // ===== SONIDOS =====
    
    public void playSound(SoundEffect sound) {
        if (!soundEnabled || player == null || sound == null) return;
        player.playSound(sound.getFileName());
    }
    
    public void setSoundVolume(float volume) {
        this.soundVolume = Math.max(0f, Math.min(1f, volume));
        if (player != null) {
            player.setSoundVolume(this.soundVolume * masterVolume);
        }
    }
    
    public float getSoundVolume() {
        return soundVolume;
    }
    
    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
    }
    
    public boolean isSoundEnabled() {
        return soundEnabled;
    }
    
    // ===== VOLUMEN MAESTRO =====
    
    public void setMasterVolume(float volume) {
        this.masterVolume = Math.max(0f, Math.min(1f, volume));
        if (player != null) {
            player.setMusicVolume(this.musicVolume * masterVolume);
            player.setSoundVolume(this.soundVolume * masterVolume);
        }
    }
    
    public float getMasterVolume() {
        return masterVolume;
    }
    
    // ===== GETTERS =====
    
    public MusicTrack getCurrentTrack() {
        return currentTrack;
    }
    
    public boolean isPlaying() {
        return player != null && player.isMusicPlaying();
    }
}