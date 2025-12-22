/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.dungeon_game.core.logic;

import com.dungeon_game.core.model.Sala;

/**
 *
 * @author GABRIEL SALGADO
 */
public class NodoSala {
    private Sala info;
    private NodoSala siguiente;
    private NodoSala anterior;
    public NodoSala(Sala info) {
        this.info = info;
    }

    public Sala getInfo() {
        return info;
    }

    public void setInfo(Sala info) {
        this.info = info;
    }

    public NodoSala getSiguiente() {
        return siguiente;
    }

    public void setSiguiente(NodoSala siguiente) {
        this.siguiente = siguiente;
    }

    public NodoSala getAnterior() {
        return anterior;
    }

    public void setAnterior(NodoSala anterior) {
        this.anterior = anterior;
    }

    
    
}
