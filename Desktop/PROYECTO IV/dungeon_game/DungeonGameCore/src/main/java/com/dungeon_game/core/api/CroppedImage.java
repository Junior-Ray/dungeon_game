/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.dungeon_game.core.api;

import com.dungeon_game.core.data.RenderableVisual;

/**
 *
 * @author GABRIEL SALGADO
 */
public class CroppedImage extends RenderableVisual {

    private Scrap scrap;

    public static  class Scrap {

        private int x;
        private int y;
        private int w;
        private int h;

        public Scrap(int x, int y, int w, int h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public int getWitdh() {
            return w;
        }

        public void setWitdh(int witdh) {
            this.w = witdh;
        }

        public int getHeight() {
            return h;
        }

        public void setHeight(int height) {
            this.h = height;
        }

    }

    public CroppedImage(RenderableVisual vr, Scrap scrap) {
        super(vr);
        this.scrap = scrap;
    }

    public Scrap getScrap() {
        return scrap;
    }

    public void setScrap(Scrap scrap) {
        this.scrap = scrap;
    }
    @Override
    public void commonUpdate(){
        
    }
}
