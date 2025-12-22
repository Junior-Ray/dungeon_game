/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.dungeon_game.core.api;



import com.dungeon_game.core.data.SpatialGrid;
import com.dungeon_game.core.data.VisualRender;
import java.awt.Point;

/**
 *
 * @author GABRIEL SALGADO
 */
public class RenderProcessor {
    private static RenderProcessor instance;
    private RenderProcessor(){};
    public static RenderProcessor getInstance(){
        if(instance == null) instance = new RenderProcessor();
        return instance;
    }
    public void setElement(VisualRenderable obj){
        if(obj instanceof VisualRender vr){
            SpatialGrid.getInstance().setElement(vr);
        }
        DriverRender.getInstance().setElement(obj);
    }
    public void eliminarElemento(VisualRenderable obj){
         boolean eli = DriverRender.getInstance().eliminarNodo(obj);
        if(obj instanceof VisualRender vr && eli){
            SpatialGrid.getInstance().eliminar(vr);
            //System.out.println("Hibox eliminada: "+obj.getVisualId());
        }
        
    }
    public void eliminarTodo(){
        DriverRender.getInstance().clean();
        SpatialGrid.getInstance().reset();
    };
    public void updateHitbox(VisualRender obj, Point render, Point dir){
        SpatialGrid.getInstance().limpiar(obj);
        obj.getObjeto().setPosition(render.x, render.y);
        obj.setDireccion(dir);
        SpatialGrid.getInstance().setElement(obj);
    }
}
