/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.dungeon_game.core.components;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class FriendCard extends AbstractUIComponent {

    private final String name;
    private final String statusText;     // "CONECTADO", "DESCONECTADO"
    private final boolean online;

    private Color baseColorOnline   = new Color(230, 240, 255);
    private Color baseColorOffline  = new Color(245, 245, 245);
    private Color hoverColor        = new Color(210, 220, 240);
    private Color borderColor       = new Color(40, 40, 40);

    public FriendCard(int x, int y, int width, int height,
                      int layer, String visualId,
                      Point[] vertices, Point dir,
                      String name, boolean online) {
        super(x, y, width, height, layer, visualId, vertices, dir);
        this.name = name;
        this.online = online;
        this.statusText = online ? "CONECTADO" : "DESCONECTADO";
    }

    // Dibujo básico (sin imagen de personaje)
    @Override
    public void render() {
        int w = getWidth();
        int h = getHeight();

        // Creamos la imagen interna
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                             RenderingHints.VALUE_ANTIALIAS_ON);

        // Fondo según hover/estado
        Color bg = online ? new Color(230, 240, 255) : new Color(245, 245, 245);
        if (isHovered) {
            bg = new Color(210, 220, 240);
        }
        g2d.setColor(bg);
        g2d.fillRect(0, 0, w, h);

        // Borde
        g2d.setColor(borderColor);
        g2d.drawRect(0, 0, w - 1, h - 1);

        // Nombre
        g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2d.setColor(Color.BLACK);
        g2d.drawString(name, 10, 20);

        // Estado
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2d.setColor(online ? new Color(0, 140, 0) : new Color(120, 120, 120));
        g2d.drawString(statusText, 10, 40);

        g2d.dispose();

        // Guardamos la imagen en el componente
        this.setImage(image);
    }

    @Override
    protected void onEnter() {
        // si quieres sonido o algo, va aquí
    }

    @Override
    protected void onExit() {
        // limpiar cosas si quieres
    }

    @Override
    public void onClick() {
        // Efecto visual/sonoro (la lógica de abrir menú va en ClickAction)
        // Por ejemplo: una pequeña animación o sonido
    }

    public String getFriendName() {
        return name;
    }

    public boolean isOnline() {
        return online;
    }
}
