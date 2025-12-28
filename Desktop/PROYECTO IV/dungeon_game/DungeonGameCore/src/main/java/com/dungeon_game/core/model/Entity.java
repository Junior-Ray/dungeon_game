/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.dungeon_game.core.model;

import com.dungeon_game.core.api.Updater;
import com.dungeon_game.core.data.RenderableVisual;
import com.dungeon_game.core.data.VisualRender;
import java.awt.Point;


/**
 *
 * @author GABRIEL SALGADO
 */
public abstract class Entity extends VisualRender implements Updater{
    protected final double gravity = 9.81;

    public Entity(Point[] vertices, Point dir, RenderableVisual renderable) {
        super(vertices, dir, renderable);
        
    }

    @Override
    public void render() {
       
    }
    @Override
    public void offFocus(){
        
    }
    public void addChar(char c){}
     
    public abstract void interactuable(Entity other);
    
}
