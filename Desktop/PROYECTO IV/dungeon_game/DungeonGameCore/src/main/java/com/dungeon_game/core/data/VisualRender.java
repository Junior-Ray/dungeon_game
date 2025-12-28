/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.dungeon_game.core.data;

import com.dungeon_game.core.api.Updater;
import com.dungeon_game.core.api.VisualRenderable;
import com.dungeon_game.core.model.Imagen;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;

/**
 *
 * @author GABRIEL SALGADO
 */
public abstract class VisualRender extends Interactuable implements VisualRenderable, Updater {

    protected RenderableVisual objeto;
    
    protected byte id = 0;   // 0 = sin asignar aún

    public byte getId() {
        return id;
    }

    public void setId(byte id) {
        this.id = id;
    }

    public VisualRender(int renderX, int renderY, int width, int height, int layer, String visualId, Point[] vertices, Point dir) {
        super(vertices, dir);
        objeto = new Imagen(renderX, renderY, width, height, layer, visualId, null, 255);
    }

    public VisualRender(int renderX, int renderY, int width, int height, int layer, String visualId, Image imagen, Point[] vertices, Point dir) {
        super(vertices, dir);
        objeto = new Imagen(renderX, renderY, width, height, layer, visualId, imagen, 255);
    }

    public VisualRender(int renderX, int renderY, int width, int height, int layer, Image imagen, Point[] vertices, Point dir) {
        super(vertices, dir);
        objeto = new Imagen(renderX, renderY, width, height, layer, null, imagen, 255);
    }

    public VisualRender( Point[] vertices, Point dir, RenderableVisual renderable) {
        super(vertices, dir);
        objeto = renderable;
    }
    public void moveTo(int x, int y) {
        setRenderPosition(x, y);
        setDireccion(new Point(x / 10, y / 10));
    }

    @Override
    public Point[] getVertices() {
        return super.getVertices();
    }

    @Override
    public Point getDireccion() {
        return super.getDireccion();
    }

    @Override
    public void setDireccion(Point dir) {
        super.setDireccion(dir);
    }

    @Override
    public int getLayer() {
        return objeto.layer;
    }

    @Override
    public int getHeight() {
        return objeto.height;
    }

    @Override
    public int getWidth() {
        return objeto.width;
    }

    @Override
    public int getRenderY() {
        return objeto.renderY;
    }

    @Override
    public int getRenderX() {
        return objeto.renderX;
    }

    @Override
    public String getVisualId() {
        return objeto.visualId;
    }

    @Override
    public Image getImage() {
        return objeto.image;
    }

    public abstract void render();

    //Protegios, o sea solo subclases lo usaran
    protected void setRenderX(int x) {
        this.objeto.renderX = x;
    }

    protected void setRenderY(int y) {
        this.objeto.renderY = y;
    }

    protected void setRenderPosition(int x, int y) {
        this.objeto.renderX = x;
        this.objeto.renderY = y;
    }

    protected void setHeight(int height) {
        this.objeto.height = height;
    }

    protected void setImage(BufferedImage image) {
        this.objeto.image = image;
    }

    @Override
    public int getOpacity() {
        return objeto.opacity;
    }

    public void setOpacity(int opacity) {
        this.objeto.opacity = opacity;
    }

    public RenderableVisual getObjeto() {
        return objeto;
    }

    public void setObjeto(RenderableVisual objeto) {
        this.objeto = objeto;
    }
    public void offFocus(){
        
    }
    public void addChar(char c){
    };
    public void update(){
        
        objeto.update();
    }
}
