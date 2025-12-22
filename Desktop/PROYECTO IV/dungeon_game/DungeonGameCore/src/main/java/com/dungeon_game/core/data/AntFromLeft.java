/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.dungeon_game.core.data;

import com.dungeon_game.core.api.CroppedImage;
import com.dungeon_game.core.api.CroppedImage.Scrap;


/**
 *INTENTO DE ANIMACIONES PARA COMPONENTES GENERALES PERO EXISTEN PROBLEMAS
 *PUES LA ESTRUCTURA ESTA HORRIBLE
 * @author GABRIEL SALGADO
 */
public class AntFromLeft extends Animation  {
    private int limite = 0;
    private int w = 0;
    private boolean ant = true;

    public AntFromLeft(RenderableVisual vr) {
        super(vr);
         cropped= new CroppedImage(vr,new Scrap(0, 0, 0, vr.height) );
        this.limite = vr.renderX;
        vr.renderX = vr.renderX + vr.width;
    }

    public AntFromLeft(VisualRender vr) {
        super(vr);
         cropped= new CroppedImage(vr.getObjeto(),new Scrap(0, 0, 0, vr.getHeight()) );
        this.limite = vr.getObjeto().renderX;
        vr.setRenderX(vr.getRenderX() + vr.getWidth());
    }   

    @Override
    public void update() {
        if (ant) {
            
            RenderableVisual obj = (target instanceof VisualRender) ? ((VisualRender) target).getObjeto() : (RenderableVisual) target;
            obj.setPosition((int)obj.renderX - obj.width/4, obj.renderY);
            w+=obj.width/10;
            cropped.getScrap().setWitdh(w);
            //Validar si completo
            if (obj.getRenderX()<=limite) {
                obj.setPosition(limite, obj.renderY);
                stop();
                ant = false;
            }
            
        }

    }

    public boolean isAnt() {
        return ant;
    }

    public void setAnt(boolean ant) {
        this.ant = ant;
    }

    @Override
    public void start() {
        
    }
    

}
