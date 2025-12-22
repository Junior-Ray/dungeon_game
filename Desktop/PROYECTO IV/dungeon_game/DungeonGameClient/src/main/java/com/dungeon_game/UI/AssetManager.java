/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.dungeon_game.UI;


import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author GABRIEL SALGADO
 */
public class AssetManager {

    private static AssetManager instance;

    private final Map<String, Image> imageCache = new HashMap();

    private AssetManager() {
    }

    public static AssetManager getInstance() {
        if (instance == null) {
            instance = new AssetManager();
        }
        return instance;
    }
 
    public Image getImage(String visualId) {
        if (imageCache.containsKey(visualId)) {
            return imageCache.get(visualId);
        }

        String path = "imagenes/" + visualId + ".png";
        if (path != null) {
            Image image = ImageLoader.load(path);

            if (image != null) {
                imageCache.put(visualId, image);
                return image;
            }
        }

        return null;
    }

    public Image getImage(Image image, int x, int y, int width, int height) {

        Image fullImage = image;

        if (fullImage == null) {
            return null;
        }
        if(width<=0|| height<=0) return null;
        BufferedImage original;
        if (fullImage instanceof BufferedImage) {
            original = (BufferedImage) fullImage;
        } else {
            original = new BufferedImage(
                    fullImage.getWidth(null),
                    fullImage.getHeight(null),
                    BufferedImage.TYPE_INT_ARGB
            );
            Graphics2D g = original.createGraphics();
            g.drawImage(fullImage, 0, 0, null);
            g.dispose();
        }
        // 2. Crear un nuevo BufferedImage solo para la porción
        BufferedImage sub = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = sub.createGraphics();
        g2.drawImage(original,
                0, 0, width, height,
                x, y, x + width, y + height,
                null);
        g2.dispose();

        return sub;
    }
}
