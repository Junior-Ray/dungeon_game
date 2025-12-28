/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.dungeon_game.core.audio;

public enum SoundEffect {
    // UI Sounds
    BUTTON_CLICK("ui_click"),
    BUTTON_HOVER("ui_hover"),
    ENTRY_LOBBY("entry_lobby"),
    MENU_CLOSE("menu_close"),
    
    // Combat Sounds
    ATTACK_LIGHT("atk_light"),
    ATTACK_HEAVY("atk_heavy"),
    ATTACK_DASH("atk_dash"),
    ENEMY_HIT("enemy_damage"),
    ENEMY_DEATH("enemy_death"),
    PLAYER_DAMAGE("player_hurt"),
    PLAYER_DEATH("player_death"),
    
    // Environment Sounds
    JUMP("jump"),
    LANDING("landing"),
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