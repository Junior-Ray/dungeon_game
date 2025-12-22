/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.dungeon_game.core.model;

import com.dungeon_game.core.data.RenderableVisual;

/**
 *
 * @author GABRIEL SALGADO
 */
public class Imagen extends RenderableVisual {

    public Imagen( int renderX, int renderY, int width, int height, int layer,String visualId, java.awt.Image image, int opacity) {
        super(visualId, image, renderX, renderY, width, height, layer, opacity);
    }

    public Imagen(RenderableVisual obj) {
        super(obj);
    }

    @Override
    public void commonUpdate(){
       
    }

    @Override
    public void onUpdate() {
        
    }
}
