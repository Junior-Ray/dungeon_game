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

/**
 *
 * @author GABRIEL SALGADO
 *
 */
public class MapaRender {

    private static int CANVAS_WIDTH = 1280;
    private static int CANVAS_HEIGHT = 720;

    public MapaRender(int width, int height) {
        this.CANVAS_WIDTH = width;
        this.CANVAS_HEIGHT = height;

    }

    public BufferedImage renderSala() {
        BufferedImage finalImage = new BufferedImage(
                CANVAS_WIDTH,
                CANVAS_HEIGHT,
                BufferedImage.TYPE_INT_ARGB
        );

        Graphics2D g2d = finalImage.createGraphics();

        // Calidad alta
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        DriverRender driver = DriverRender.getInstance();

        while (driver.hasNext()) {

            VisualRenderable renderData = driver.nextElement();
            Image img=getImage(renderData);

            if (img == null) continue;

            int screenX = renderData.getRenderX();
            int screenY = renderData.getRenderY();
            int screenW = renderData.getWidth();
            int screenH = renderData.getHeight();
            int opacity = renderData.getOpacity(); // 0 - 255

            float alpha = Math.max(0f, Math.min(1f, opacity / 255f));

            // Aplica opacidad
            g2d.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER,
                    alpha
            ));

            // Dibujar imagen con opacidad
            g2d.drawImage(img, screenX, screenY, screenW, screenH, null);
        }

        // Reset a transparencia normal
        g2d.setComposite(AlphaComposite.SrcOver);

        // Fin
        driver.resetTransverse();
        g2d.dispose();

        return finalImage;
    }
    private Image getImage(VisualRenderable obj){
        String visualId = obj.getVisualId();
        if(visualId==null) return obj.getImage();
        Image img =AssetManager.getInstance().getImage(visualId);
        
        if(obj instanceof CroppedImage){
            CroppedImage.Scrap o = ((CroppedImage) obj).getScrap();
            img= AssetManager.getInstance().getImage(img, o.getX(), o.getY(), o.getWitdh(), o.getHeight());
        }
        
        
        return img;
    }
}
