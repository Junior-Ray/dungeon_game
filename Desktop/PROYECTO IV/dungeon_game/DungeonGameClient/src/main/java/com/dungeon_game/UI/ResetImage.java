/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.dungeon_game.UI;

import com.dungeon_game.core.api.Changed;

/**
 *
 * @author GABRIEL SALGADO
 */
public class ResetImage {
    private static ResetImage instance;
    private ResetImage(){}
    public static ResetImage getInstance(){
        if(instance == null) instance = new ResetImage();
        return instance;
    }
    public void update(){
        if(Changed.getInstance().update()){
            AssetManager.getInstance().reset();
        }
    }
}
