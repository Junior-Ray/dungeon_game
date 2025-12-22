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
public class ListaSala {
    private NodoSala cabeza;
    private NodoSala cola;
    private int size = 0;
    public void ListaSala (){};
    public void agregar(Sala data) {
        NodoSala nuevo = new NodoSala(data);

        if (cabeza == null) {
            cabeza = cola = nuevo;
        } else {
            cola.setSiguiente(nuevo);
            nuevo.setAnterior(cola);
            cola = nuevo;
        }

        size++;
    }

    public boolean eliminar(Sala data) {
        NodoSala actual = cabeza;

        while (actual != null) {
            if (actual.getInfo().equals(data)) {

                if (actual.getAnterior() != null) {
                    actual.getAnterior().setSiguiente(actual.getSiguiente());
                } else {
                    cabeza = actual.getSiguiente();
                }

                if (actual.getSiguiente() != null) {
                    actual.getSiguiente().setAnterior(actual.getAnterior());
                } else {
                    cola = actual.getAnterior();
                }

                size--;
                return true;
            }
            actual = actual.getSiguiente();
        }
        return false;
    }

    public NodoSala getCabeza() {
        return cabeza;
    }

    public NodoSala getCola() {
        return cola;
    }

    public int size() {
        return size;
    }
}

