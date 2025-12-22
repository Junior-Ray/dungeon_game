/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.dungeon_game.core.api;


import java.awt.Image;

/**
 *
 * @author GABRIEL SALGADO
 */
public interface VisualRenderable {
    //Identificador de la imagen
    String getVisualId();
    Image getImage();
   //Posicion
   int getRenderX();
   int getRenderY();
   int getWidth();
   int getHeight();
   //Capa
   int getLayer();
   int getOpacity();
   
}

