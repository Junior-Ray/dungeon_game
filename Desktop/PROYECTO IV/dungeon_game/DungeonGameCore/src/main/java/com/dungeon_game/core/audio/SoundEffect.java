/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.dungeon_game.core.audio;

public enum SoundEffect {
    // UI Sounds
    BUTTON_CLICK("ui_click"),
    BUTTON_HOVER("ui_hover"),
    MENU_OPEN("menu_open"),
    MENU_CLOSE("menu_close"),
    
    // Combat Sounds
    SWORD_SLASH("sword_slash"),
    SWORD_HIT("sword_hit"),
    ENEMY_HIT("enemy_damage"),
    ENEMY_DEATH("enemy_death"),
    PLAYER_DAMAGE("player_hurt"),
    PLAYER_DEATH("player_death"),
    
    // Environment Sounds
    DOOR_OPEN("door_open"),
    DOOR_CLOSE("door_close"),
    CHEST_OPEN("chest_open"),
    FOOTSTEP("footstep"),
    
    // Item Sounds
    ITEM_PICKUP("item_get"),
    ITEM_EQUIP("item_equip"),
    POTION_DRINK("potion_drink"),
    
    // Special Sounds
    LEVEL_UP("level_up"),
    COIN_PICKUP("coin"),
    SPELL_CAST("spell_cast"),
    HEAL("heal");
    
    private final String fileName;
    
    SoundEffect(String fileName) {
        this.fileName = fileName;
    }
    
    public String getFileName() {
        return fileName;
    }
}