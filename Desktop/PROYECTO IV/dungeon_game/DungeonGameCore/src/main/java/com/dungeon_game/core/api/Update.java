/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.dungeon_game.core.api;

import com.dungeon_game.core.logic.GameState;
import com.dungeon_game.core.logic.InterpreterEvent;


/**
 *
 * @author GABRIEL SALGADO
 */
public class Update {
    private static Update instance;
    private Update(){};
    public static Update getInstance(){
        if(instance == null) instance = new Update();
        return instance;
    }
    public void update(){
        //Primero actualizamos interpretacion de los inputs
        InterpreterEvent.getInstance().update();
        //Despues actualizamos logica
        GameState.getInstance().update();
        
    }
}
