/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.dungeon_game.core.data;

import com.dungeon_game.core.api.RenderProcessor;
import com.dungeon_game.core.logic.GameState;
import com.dungeon_game.core.model.Imagen;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 *
 * @author GABRIEL SALGADO
 */
public class ChangeSala {

    private static ChangeSala instance;

    private Imagen circle;
    private boolean playing = false;

    private int maxRadius;
    private int radius = 20;
    private float opacity = 255;

    // Incrementamos el growSpeed y el fadeSpeed en un 20%
    private int growSpeed = (int) (8 * 1.5); // 9.6 redondeado a 10
    private float fadeSpeed = 2.2f * 1.5f;  // 2.64

    private int centerX, centerY;

    private ChangeSala() {}

    public static ChangeSala getInstance() {
        if (instance == null) instance = new ChangeSala();
        return instance;
    }

    public void startAnimation(int screenW, int screenH) {
        if (playing) return;

        playing = true;

        centerX = screenW / 2;
        centerY = screenH / 2;

        maxRadius = (int) Math.sqrt(screenW * screenW + screenH * screenH);

        // Posición inicial centrada
        circle = new Imagen(
                centerX - radius,
                centerY - radius,
                radius * 2,
                radius * 2,
                1,
                null,
                null,
                (int) opacity
        );

        circle.setImage(generateCircle(radius, (int) opacity));

        RenderProcessor.getInstance().setElement(circle);
    }

    public void update() {
        if (!playing) return;

        // Aumentamos el radio en función del growSpeed
        radius += growSpeed;
        if (radius > maxRadius) radius = maxRadius;

        // Disminuimos la opacidad en función del fadeSpeed
        opacity -= fadeSpeed;
        if (opacity < 0) opacity = 0;

        // 🔥 CENTRAR EL CÍRCULO SIEMPRE
        circle.renderX = centerX - radius;
        circle.renderY = centerY - radius;

        // Tamaño actualizado
        circle.width = radius * 2;
        circle.height = radius * 2;

        circle.setOpacity((int) opacity);

        circle.setImage(generateCircle(radius, (int) opacity));

        // Si el radio es máximo y la opacidad es 0, terminamos la animación
        if (radius >= maxRadius && opacity <= 0) {
            playing = false;
            RenderProcessor.getInstance().eliminarElemento(circle);
            GameState.getInstance().setCambioSala(false);
            instance = null;
        }
    }

    private BufferedImage generateCircle(int radius, int opacity) {
        int size = radius * 2;

        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setComposite(AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER,
                opacity / 255f
        ));

        g.setColor(Color.BLACK);
        g.fillOval(0, 0, size, size);

        g.dispose();
        return img;
    }
}