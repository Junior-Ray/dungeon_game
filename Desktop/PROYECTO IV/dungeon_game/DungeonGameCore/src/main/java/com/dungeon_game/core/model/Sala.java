/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.dungeon_game.core.model;

import com.dungeon_game.core.data.Updatable;

/**
 *
 * @author GABRIEL SALGADO
 */
public abstract class Sala extends Updatable {
    public Sala(){
       
    }
    public abstract void cargarIniciales();
    public abstract void eliminarSala();
}
