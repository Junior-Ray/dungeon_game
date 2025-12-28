/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.dungeon_game.core.api;


/**
 *
 * @author GABRIEL SALGADO
 */
public class Changed {
     private static Changed instance;
     private boolean change= false;
     private Changed(){};
     public static Changed getInstance(){
         if(instance== null) instance = new Changed();
         return instance;
     }
     public boolean update(){
         if(change){
             change=false;
             return true;
         }
         return false;
     }

    public boolean isChange() {
        return change;
    }

    public void setChange(boolean change) {
        this.change = change;
    }
     
}
