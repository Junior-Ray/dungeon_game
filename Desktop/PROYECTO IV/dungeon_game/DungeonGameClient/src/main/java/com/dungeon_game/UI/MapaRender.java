/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.dungeon_game.UI;

import com.dungeon_game.core.api.CroppedImage;
import com.dungeon_game.core.api.DriverRender;
import com.dungeon_game.core.api.VisualRenderable;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Queue;

/**
 *
 * @author GABRIEL SALGADO
 *
 */
public class MapaRender {

    private final int CANVAS_WIDTH;
    private final int CANVAS_HEIGHT;

    private final BufferedImage finalImage;
    private final Graphics2D g2d;

    public MapaRender(int width, int height) {
        this.CANVAS_WIDTH = width;
        this.CANVAS_HEIGHT = height;

        // 🔹 Crear UNA SOLA VEZ
        this.finalImage = new BufferedImage(
                CANVAS_WIDTH,
                CANVAS_HEIGHT,
                BufferedImage.TYPE_INT_ARGB
        );

        this.g2d = finalImage.createGraphics();

        // 🔹 Hints UNA SOLA VEZ
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    public BufferedImage renderSala() {

        // 🔹 Limpiar buffer (en lugar de crear uno nuevo)
        g2d.setComposite(AlphaComposite.Clear);
        g2d.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        g2d.setComposite(AlphaComposite.SrcOver);


        Queue<VisualRenderable> driver = DriverRender.getInstance().obtenerCola();
        while (!driver.isEmpty()) {

            VisualRenderable renderData = driver.poll();
            Image img = getImage(renderData);
            if (img == null) continue;

            int x = renderData.getRenderX();
            int y = renderData.getRenderY();
            int w = renderData.getWidth();
            int h = renderData.getHeight();

            float alpha = Math.max(0f, Math.min(1f, renderData.getOpacity() / 255f));
            g2d.setComposite(
                    AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha)
            );

            g2d.drawImage(img, x, y, w, h, null);
        }

        g2d.setComposite(AlphaComposite.SrcOver);

        return finalImage;
    }

    private Image getImage(VisualRenderable obj) {
        String visualId = obj.getVisualId();
        if (visualId == null) return obj.getImage();

        Image img = AssetManager.getInstance().getImage(visualId);

        if (obj instanceof CroppedImage) {
            CroppedImage.Scrap o = ((CroppedImage) obj).getScrap();
            img = AssetManager.getInstance().getImage(
                    img, o.getX(), o.getY(), o.getWitdh(), o.getHeight()
            );
        }
        return img;
    }
}
