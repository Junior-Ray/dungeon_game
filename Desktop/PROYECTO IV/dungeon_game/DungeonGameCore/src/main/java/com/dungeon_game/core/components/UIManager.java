/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.dungeon_game.core.components;


import com.dungeon_game.core.data.VisualRender;

import java.awt.Graphics2D;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class UIManager {

    private final List<VisualRender> components = new ArrayList<>();

    public void add(VisualRender vr) {
        components.add(vr);
        // ordenar por layer
        components.sort(Comparator.comparingInt(VisualRender::getLayer));
    }

    public void remove(VisualRender vr) {
        components.remove(vr);
    }

    public void update() {
        for (int i = components.size() - 1; i >= 0; i--) {
            VisualRender vr = components.get(i);
            if (vr instanceof AbstractUIComponent ui) {
                // cálculo de contains lo puedes tener aquí o en otra función
                // para la beta basta con un rectángulo simple:
                var mouse = com.dungeon_game.core.api.InputMouse
                        .getInstance().getMousePosition();
                
                boolean contains =
                        mouse.x >= ui.getRenderX() &&
                        mouse.x <= ui.getRenderX() + ui.getWidth() &&
                        mouse.y >= ui.getRenderY() &&
                        mouse.y <= ui.getRenderY() + ui.getHeight();

                ui.updateState(contains);
            }
        }
    }

    public void render(Graphics2D g) {
        for (VisualRender vr : components) {
            vr.render();

            Image img = vr.getImage();
            if (img != null) {
                g.drawImage(img, vr.getRenderX(), vr.getRenderY(), null);
            }
        }
    }

}
