/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.dungeon_game.core.audio;

public enum MusicTrack {
    INTRO("intro_theme"),
    MAIN_MENU("menu_theme"),
    GAMEPLAY("dungeon_ambient"),
    BATTLE("combat_theme"),
    BOSS_BATTLE("boss_theme"),
    VICTORY("victory_fanfare"),
    GAME_OVER("game_over"),
    CREDITS("credits_theme");
    
    private final String fileName;
    
    MusicTrack(String fileName) {
        this.fileName = fileName;
    }
    
    public String getFileName() {
        return fileName;
    }
}