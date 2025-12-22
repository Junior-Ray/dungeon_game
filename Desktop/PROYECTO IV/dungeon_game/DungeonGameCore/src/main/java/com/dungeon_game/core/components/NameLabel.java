/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.dungeon_game.core.components;

import com.dungeon_game.core.model.Imagen;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 *
 * @author USUARIO
 */
public class NameLabel extends Imagen{
    
    private String text;

    /**
     * @param args the command line arguments
     */
    public NameLabel(String text, int x, int y, int layer){
        super(x,y,160,24,layer,null,null, 255);
        this.text = text;
        render();
    }
    public NameLabel(String text, int x, int y, int layer,int opacity ){
        super(x,y,160,24,layer,null,null, opacity);
        this.text = text;
        render();
    }
    public NameLabel(String text, int x, int y, int layer,String visualID, int opacity ){
        super(x,y,160,24,layer,visualID,null, opacity);
        this.text = text;
        render();
    }
    public void render() {
        BufferedImage img = new BufferedImage(
                getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                           RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Fondo semi-transparente
        g.setColor(new Color(0, 0, 0, 120));
        g.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

        // Texto
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 14));

        FontMetrics fm = g.getFontMetrics();
        int textX = (getWidth() - fm.stringWidth(text)) / 2;
        int textY = (getHeight() + fm.getAscent()) / 2 - 2;

        g.drawString(text, textX, textY);
        g.dispose();

        setImage(img);
    }
}
