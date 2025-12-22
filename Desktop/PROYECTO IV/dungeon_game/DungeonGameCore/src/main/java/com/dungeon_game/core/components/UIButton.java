package com.dungeon_game.core.components;

import com.dungeon_game.core.data.RenderableVisual;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class UIButton extends AbstractUIComponent {

    private String text;

    public UIButton(int x, int y, int width, int height, int layer,
                    String visualId, Point[] vertices, Point dir,
                    String text) {
        super(x, y, width, height, layer, visualId, vertices, dir);
        this.text = text;
    }

    public UIButton(String text, Point[] vertices, Point dir, RenderableVisual renderable) {
        super(vertices, dir, renderable);
        this.text = text;
    }

    public void render() {  

        BufferedImage image = new BufferedImage(
                getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                             RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Fondo según estado
        if (isPressed) {
            g2d.setColor(Color.DARK_GRAY);
        } else if (isHovered) {
            g2d.setColor(Color.GRAY);
        } else {
            g2d.setColor(Color.LIGHT_GRAY);
        }
        g2d.fillRect(0, 0, w, h);

        // Borde
        g2d.setColor(Color.BLACK);
        g2d.drawRect(0, 0, w - 1, h - 1);

        // Texto
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2d.drawString(text, 8, h / 2);

        g2d.dispose();

        // Guardas la imagen en el componente
        this.setImage(image);
    }
    public void moveTo(int x, int y) {

        // 2) mover en píxeles (para dibujar)
        setRenderX(x);
        setRenderY(y);
        setDireccion(null);
        setDireccion(new Point(x/10,y/10));

    }

    @Override
    protected void onEnter() {
        // Sonidito o animación ligera
    }

    @Override
    protected void onExit() {
        // Reset de cosas si quieres
    }

    @Override
    public void onClick() {
        // Efecto visual/sonoro del click (lo funcional ya va en clickAction)
    }
}
