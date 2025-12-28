/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.dungeon_game.core.api;

import com.dungeon_game.core.audio.MusicTrack;
import com.dungeon_game.core.audio.SoundEffect;

public interface AudioPlayer {
    // Música
    void playMusic(MusicTrack track, boolean loop);
    void stopMusic();
    void pauseMusic();
    void resumeMusic();
    void setMusicVolume(float volume);
    
    // Sonidos (SFX) - Ahora usa el Enum SoundEffect
    void playSound(SoundEffect effect);
    void setSoundVolume(float volume);
    
    boolean isMusicPlaying();
    void dispose();
}