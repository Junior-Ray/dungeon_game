/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.dungeon_game.core.api;

/**
 *
 * @author GABRIEL SALGADO
 */
public class ControlDelta {
    private static ControlDelta instance;
    private double delta=0;
    private ControlDelta(){}
    public static ControlDelta getInstance(){
        if(instance ==null) instance = new ControlDelta();
        return instance;
    }
    public void update(double delta){
        this.delta=delta;
    }
    public double getDelta(){
        return delta;
    }
}
