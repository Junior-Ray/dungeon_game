/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.dungeon_game.core.data;

import java.awt.Point;

/**
 *
 * @author GABRIEL SALGADO
 */
public abstract class Interactuable {
    private Point[] vertices;
    private Point dir ;

    public Interactuable(Point[] vertices, Point dir) {
        this.vertices = vertices;
        this.dir = dir;
    }
    
    public void setDireccion(Point dir){
        this.dir=dir;
    }
    public Point getDireccion(){
        return dir;
    }
    public Point[] getVertices(){
        Point []resultado= new Point[vertices.length];
        int i=0;
        for(Point p: vertices){
            resultado[i++]= new Point(p.x+dir.x,p.y+dir.y);
        }
        return resultado;
    }
   public String toStringVertices(){
       String resultado ="Vertices: ";
       Point[] ver = getVertices();
       for(Point v: ver){
           resultado +="["+v.x+" , " +v.y+" ]";
       }
       return resultado;
   }
}