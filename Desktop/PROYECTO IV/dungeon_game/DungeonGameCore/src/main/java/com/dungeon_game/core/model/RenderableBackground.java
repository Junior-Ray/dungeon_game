/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.dungeon_game.core.model;

import com.dungeon_game.core.api.VisualRenderable;
import java.awt.Image;

/**
 *
 * @author GABRIEL SALGADO
 */
public class RenderableBackground implements VisualRenderable {
    // El ID que mapeamos en el AssetManager (FONDO_INICIO -> imagenes/Paisaje.jpg)
    
    private final String visualId;
    private int opacity = 255;
    
    // El fondo siempre va en la capa más baja para ser dibujado primero
    private final int renderLayer = 0; 
    
    public RenderableBackground(String visualId) {
        this.visualId = visualId;
    }
    
    @Override
    public String getVisualId() {
        return visualId;
    }
    
    @Override
    public int getRenderX() {
        return 0; // El fondo siempre comienza en (0, 0)
    }

    @Override
    public int getRenderY() {
        return 0; // El fondo siempre comienza en (0, 0)
    }

    @Override
    public int getLayer() {
        return renderLayer;
    }
    @Override 
    public int getWidth(){
        return 1280;
    }
    @Override
    public int getHeight(){
        return 720;  
    }
    @Override
    public Image getImage(){
        return null;
    }
    @Override
    public int getOpacity(){
        return opacity;
    }
}
