/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.dungeon_game.core.model;

import com.dungeon_game.core.data.RenderableVisual;
import java.awt.Point;

/**
 *
 * @author GABRIEL SALGADO
 */
public class Blocks extends Entity {

    @Override
    public void interactuable(Entity other) {
       
    }

    public Blocks(Point[] vertices, Point dir, RenderableVisual renderable) {
        super(vertices, dir, renderable);
    }
    
}
