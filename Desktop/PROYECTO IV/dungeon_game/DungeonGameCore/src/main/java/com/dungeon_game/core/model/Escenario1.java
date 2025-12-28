/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.dungeon_game.core.model;

import com.dungeon_game.core.api.RenderProcessor;
import com.dungeon_game.core.api.Updater;

import com.dungeon_game.core.logic.InterpreterEvent;
import com.dungeon_game.core.logic.UIFocus;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author GABRIEL SALGADO
 */
public class Escenario1 extends Sala {

    private List<Updater> lista=new ArrayList<>();
    private static Escenario1 instance;

    private Escenario1() {
    }

    public static Escenario1 getInstance() {
        if (instance == null) {
            instance = new Escenario1();
        }
        return instance;
    }

    @Override
    public void cargarIniciales() {
        Imagen fondo = new Imagen(0, 0, 1280, 720, 0, "Paisaje", null, 255);

        Point[] ver = new Point[4];
        ver[0] = new Point(0, 0);
        ver[1] = new Point(19, 0);
        ver[2] = new Point(19, 20);
        ver[3] = new Point(0, 20);

        Character pj = new Character(ver, new Point(64, 30));
        UIFocus.setFocus(pj);
        InterpreterEvent.getInstance().setPrioridad(pj);
        
        ver = new Point[4];
        ver[0] = new Point(0, 0);
        ver[1] = new Point(127, 0);
        ver[2] = new Point(127, 20);
        ver[3] = new Point(0, 20);
        Imagen  obj = new Imagen(0,700,1280,200,2,null, null,255);
        Blocks tierra= new Blocks(ver, new Point(0,51), obj);
        
        RenderProcessor.getInstance().setElement(fondo);
        RenderProcessor.getInstance().setElement(tierra);
        InterpreterEvent.getInstance().setCapa(4);
        RenderProcessor.getInstance().setElement(pj);
        lista.add(pj);

    }
    @Override
    public void commonUpdate(){
        for(Updater obj: lista){
            obj.update();
        }
    }
    @Override
    public void eliminarSala() {
        instance = null;
    }
    

}
