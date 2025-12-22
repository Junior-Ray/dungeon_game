/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.dungeon_game.core.components;

import com.dungeon_game.core.data.VisualRender;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class UILabel extends VisualRender {

    private String text;
    private Color color = Color.WHITE;
    private Font font = new Font("Arial", Font.PLAIN, 16);

    public enum Align {
        LEFT, CENTER, RIGHT
    }

    private Align align = Align.LEFT;

    public UILabel(int renderX, int renderY,
                   int layer, String visualId,
                   String text,
                   Point[] vertices, Point dir) {
        // width/height puedes dejarlos 0 o calcularlos más tarde si quieres
        super(renderX, renderY, 0, 0, layer, visualId, vertices, dir);
        this.text = text;
    }

    // --- setters opcionales ---

    public void setText(String text) {
        this.text = text;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public void setAlign(Align align) {
        this.align = align;
    }

    // --- dibujado ---

    @Override
    public void render() {
        int w = getWidth();
        int h = getHeight();

        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                             RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2d.setFont(font);
        g2d.setColor(color);

        FontMetrics fm = g2d.getFontMetrics();
        int textWidth  = fm.stringWidth(text);

        int x;
        // alineación horizontal dentro del rectángulo del label
        switch (align) {
            case CENTER -> x = (w - textWidth) / 2;
            case RIGHT  -> x = w - textWidth;
            case LEFT   -> x = 0;
            default     -> x = 0;
        }

        // Línea base del texto: aquí lo centro verticalmente en el alto del label
        int y = (h + fm.getAscent() - fm.getDescent()) / 2;

        g2d.drawString(text, x, y);

        g2d.dispose();

        this.setImage(image);
    }
    public void update(){
        
    }

}
