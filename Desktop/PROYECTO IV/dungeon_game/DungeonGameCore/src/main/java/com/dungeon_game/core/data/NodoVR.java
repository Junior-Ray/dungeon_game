/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.dungeon_game.core.data;

import com.dungeon_game.core.api.VisualRenderable;

/**
 *
 * @author GABRIEL SALGADO
 */
public class NodoVR {
     private VisualRenderable info;
    private NodoVR sgte;
    private NodoVR ant;

    public NodoVR(VisualRenderable info) {
        this.info = info;
    }

    public VisualRenderable getInfo() {
        return info;
    }

    public void setInfo(VisualRenderable info) {
        this.info = info;
    }

    public NodoVR getSgte() {
        return sgte;
    }

    public void setSgte(NodoVR sgte) {
        this.sgte = sgte;
    }

    public NodoVR getAnt() {
        return ant;
    }

    public void setAnt(NodoVR ant) {
        this.ant = ant;
    }
    
}
