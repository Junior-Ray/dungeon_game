/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.dungeon_game.core.components;

/**
 *
 * @author USUARIO
 */

import com.dungeon_game.core.api.InputMouse;
import com.dungeon_game.core.data.VisualRender;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class UIPanel extends VisualRender {

    private final List<VisualRender> children = new ArrayList<>();
    private Color background = new Color(0, 0, 0, 150); // semi-transparente

    public UIPanel(int renderX, int renderY,
                   int width, int height,
                   int layer, String visualId,
                   Point[] vertices, Point dir) {
        super(renderX, renderY, width, height, layer, visualId, vertices, dir);
    }

    // --- Gestión de hijos ---

    public void addChild(VisualRender child) {
        children.add(child);
    }

    public void removeChild(VisualRender child) {
        children.remove(child);
    }

    public List<VisualRender> getChildren() {
        return children;
    }

    public void setBackground(Color bg) {
        this.background = bg;
    }

    // --- UPDATE: delega interacción a los hijos que sean AbstractUIComponent ---

    public void update() {
        InputMouse input = InputMouse.getInstance();
        Point mouse = input.getMousePosition();
        int mx = mouse.x;
        int my = mouse.y;

        for (VisualRender vr : children) {
            if (vr instanceof AbstractUIComponent ui) {
                boolean contains = isMouseInside(ui, mx, my);
                ui.updateState(contains);
            }
        }
    }

    private boolean isMouseInside(VisualRender vr, int mx, int my) {
        int x = vr.getRenderX();
        int y = vr.getRenderY();
        int w = vr.getWidth();
        int h = vr.getHeight();

        return mx >= x && mx <= x + w &&
               my >= y && my <= y + h;
    }

    // --- RENDER: dibuja el panel y luego todos sus hijos ---

    @Override
    public void render() {
        int w = getWidth();
        int h = getHeight();

        // Imagen local del panel
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                             RenderingHints.VALUE_ANTIALIAS_ON);

        // 1) Fondo del panel (coordenadas locales 0..w, 0..h)
        g2d.setColor(background);
        g2d.fillRect(0, 0, w, h);

        // 2) Borde
        g2d.setColor(Color.BLACK);
        g2d.drawRect(0, 0, w - 1, h - 1);

        // 3) Dibujar hijos dentro del panel
        for (VisualRender vr : children) {
            // Primero, cada hijo construye su propia imagen interna
            vr.render();
            Image childImg = vr.getImage();
            if (childImg == null) continue;

            // Si las coords de los hijos son *globales*, las volvemos locales:
            int childLocalX = vr.getRenderX() - getRenderX();
            int childLocalY = vr.getRenderY() - getRenderY();

            // Si ya son relativas al panel, simplemente usa:
            // int childLocalX = vr.getRenderX();
            // int childLocalY = vr.getRenderY();

            g2d.drawImage(childImg, childLocalX, childLocalY, null);
        }

        g2d.dispose();

        // Guardamos la imagen del panel para que el motor la dibuje
        this.setImage(image);
    }
    
}
