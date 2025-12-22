/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.dungeon_game.core.api;

public interface AudioPlayer {
    void playMusic(String musicId, boolean loop);
    void stopMusic();
    void pauseMusic();
    void resumeMusic();
    void setMusicVolume(float volume); // 0.0f a 1.0f
    float getMusicVolume();
    
    void playSound(String soundId);
    void setSoundVolume(float volume);
    float getSoundVolume();
    
    boolean isMusicPlaying();
}