/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.dungeon_game.core.api;

import com.dungeon_game.core.data.Interactuable;
import com.dungeon_game.core.data.NodoVR;
import com.dungeon_game.core.data.VisualRender;

/**
 *
 * @author GABRIEL SALGADO
 */
public class DriverRender {

    private static NodoVR primero;
    private static DriverRender instance;
    private static NodoVR transverse;

    private DriverRender() {
    }

    ;
    public static DriverRender getInstance() {
        if (instance == null) {
            instance = new DriverRender();
        }
        return instance;
    }

    public void setElement(VisualRenderable actual) {
        NodoVR nuevo = new NodoVR(actual);

        // Caso 1: lista vacía
        if (primero == null) {
            primero = nuevo;
            transverse = primero;
            return;
        }

        // Caso 2: insertar al inicio
        if (primero.getInfo().getLayer() > nuevo.getInfo().getLayer()) {
            nuevo.setSgte(primero);
            primero.setAnt(nuevo);
            primero = nuevo;
            return;
        }

        // Recorrer la lista
        NodoVR p = primero;

        while (p.getSgte() != null
                && p.getSgte().getInfo().getLayer() <= nuevo.getInfo().getLayer()) {
            p = p.getSgte();
        }

        // Insertar entre p y p.getSgte()
        nuevo.setSgte(p.getSgte());
        nuevo.setAnt(p);

        if (p.getSgte() != null) {
            p.getSgte().setAnt(nuevo);
        }

        p.setSgte(nuevo);
    }

    public boolean eliminarNodo(VisualRenderable obj) {
        if (primero.getInfo() == obj) {
            primero = primero.getSgte();
            primero.setAnt(null);
            return true;
        }
        NodoVR p = primero;
        do {
            p = p.getSgte();
            if (p.getInfo() == obj) {
                p.getAnt().setSgte(p.getSgte());
                if(p.getSgte()!=null)p.getSgte().setAnt(p.getAnt());
                return true;
            }
        }while (p.getSgte() != null);
        return false;
    }

    public void clean() {
        while (primero != null) {
            primero = primero.getSgte();
        }
    }

    public boolean hasNext() {
        return transverse != null;
    }

    public VisualRenderable nextElement() {
        if (transverse == null) {
            return null;
        }

        NodoVR actual = transverse;
        transverse = transverse.getSgte();
        return actual.getInfo();
    }

    public void resetTransverse() {
        transverse = primero;
    }

    public void string() {
        NodoVR t = primero;
        String resultado = "NUMERO: \n";
        while (t != null) {
            resultado+="\n " + t.getInfo().getVisualId()+t.getInfo().getLayer();
            if (t.getInfo() instanceof VisualRender) {
                Interactuable obj = (Interactuable) t.getInfo();

                resultado += obj.toStringVertices();
            }

            t = t.getSgte();
        }
        System.out.println("ORDEN: ID" + resultado);
    }
}
