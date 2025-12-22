/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.dungeon_game.core.components;

import com.dungeon_game.core.api.InputMouse;


import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class UIContextMenu extends AbstractUIComponent {

    public static class MenuItem {
        public final String text;
        public final ClickAction action;

        public MenuItem(String text, ClickAction action) {
            this.text = text;
            this.action = action;
        }
    }

    private final List<MenuItem> items = new ArrayList<>();
    private boolean visible = false;
    private int itemHeight = 20;

    public UIContextMenu(int width, int layer, String visualId) {
        // La posición la setearás cuando llames a showAt(...)
        super(0, 0, width, 0, layer, visualId, null, null);
    }

    public void addItem(String text, ClickAction action) {
        items.add(new MenuItem(text, action));
        setHeight(items.size() * itemHeight);
    }

    public void showAt(Point p) {
        setRenderPosition(p.x, p.y);  // ✅ usa el método protegido
        this.visible = true;
    }

    public void hide() {
        this.visible = false;
    }

    public boolean isVisible() {
        return visible;
    }

    @Override
    public void updateState(boolean contains) {
        if (!visible) return;
        super.updateState(contains);
    }

    @Override
    protected void onEnter() { }

    @Override
    protected void onExit() { }

    @Override
    public void onClick() {
        // Determinar qué opción se clickeó
        InputMouse input = InputMouse.getInstance();
        Point mouse = input.getMousePosition();
        int mx = mouse.x;
        int my = mouse.y;

        int x = getRenderX();
        int y = getRenderY();

        if (mx < x || mx > x + getWidth()) return;
        if (my < y || my > y + getHeight()) return;

        int index = (my - y) / itemHeight;
        if (index >= 0 && index < items.size()) {
            MenuItem item = items.get(index);
            if (item.action != null) {
                item.action.execute();
            }
        }

        // después del click, cerramos
        hide();
    }

    public void render() {
        if (!visible) {
            // Limpia la imagen p
            setImage(null);
            return;
        }

        int w = getWidth();
        int h = getHeight();

        // Imagen local del menú
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                             RenderingHints.VALUE_ANTIALIAS_ON);

        // Fondo
        g2d.setColor(new Color(245, 245, 245));
        g2d.fillRect(0, 0, w, h);

        // Borde
        g2d.setColor(Color.BLACK);
        g2d.drawRect(0, 0, w - 1, h - 1);

        // Texto de los items
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2d.setColor(Color.BLACK);

        for (int i = 0; i < items.size(); i++) {
            int iy = i * itemHeight;   // coordenada local
            g2d.drawString(items.get(i).text, 5, iy + 14);
        }

        g2d.dispose();

        // Guardamos la imagen en el componente
        this.setImage(image);
    }
}
