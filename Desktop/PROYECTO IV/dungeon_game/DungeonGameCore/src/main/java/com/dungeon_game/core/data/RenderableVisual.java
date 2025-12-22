/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.dungeon_game.core.data;

import com.dungeon_game.core.api.VisualRenderable;
import java.awt.Image;

/**
 *
 * @author GABRIEL SALGADO
 */
public abstract class RenderableVisual extends Updatable implements VisualRenderable {
    // --- CAMPOS PROTEGIDOS ---
    protected String visualId;
    protected Image image;

    protected int renderX;
    protected int renderY;

    protected int width;
    protected int height;

    protected int layer;

    /**
     * Opacidad en rango 0–255
     * 0 = totalmente transparente
     * 255 = totalmente opaco
     */
    protected int opacity=255;


    // --- CONSTRUCTOR PRINCIPAL ---
    public RenderableVisual(
            String visualId,
            Image image,
            int renderX,
            int renderY,
            int width,
            int height,
            int layer,
            int opacity
    ) {
        this.visualId = visualId;
        this.image = image;

        this.renderX = renderX;
        this.renderY = renderY;

        this.width = width;
        this.height = height;

        this.layer = layer;

        // validación de opacidad
        if (opacity < 0) opacity = 0;
        if (opacity > 255) opacity = 255;
        this.opacity = opacity;
    }
    public RenderableVisual(RenderableVisual obj){
        this.height=obj.height;
        this.image=obj.image;
        this.layer=obj.layer;
        this.opacity=obj.opacity;
        this.renderX=obj.renderX;
        this.renderY=obj.renderY;
        this.visualId=obj.visualId;
        this.width=obj.width;
    }
    // --- IMPLEMENTACIÓN DE INTERFAZ ---

    @Override
    public String getVisualId() {
        return visualId;
    }

    @Override
    public Image getImage() {
        return image;
    }

    @Override
    public int getRenderX() {
        return renderX;
    }

    @Override
    public int getRenderY() {
        return renderY;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getLayer() {
        return layer;
    }

    @Override
    public int getOpacity() {
        return opacity;
    }


    // --- SETTERS OPCIONALES (si quieres que puedan cambiar en runtime) ---

    public void setPosition(int x, int y) {
        this.renderX = x;
        this.renderY = y;
    }

    public void setSize(int w, int h) {
        this.width = w;
        this.height = h;
    }

    public void setLayer(int layer) {
        this.layer = layer;
    }

    public void setOpacity(int opacity) {
        if (opacity < 0) opacity = 0;
        if (opacity > 255) opacity = 255;
        this.opacity = opacity;
    }

    public void setImage(Image img) {
        this.image = img;
    }

    public void setVisualId(String id) {
        this.visualId = id;
    }
    
}
