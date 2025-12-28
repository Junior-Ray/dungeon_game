/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.dungeon_game.core.data;

/**
 *
 * @author GABRIEL SALGADO
 */
public interface ClickOn{

    public interface ClickAction {

        void execute();
    }
    

    public abstract void setOnClickAction(ClickAction action) ;
    public abstract void executeClickAction();
    public abstract void onClick();
}
