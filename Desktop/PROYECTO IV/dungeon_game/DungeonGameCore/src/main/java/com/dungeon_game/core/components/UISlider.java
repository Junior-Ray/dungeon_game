package com.dungeon_game.core.components;

import com.dungeon_game.core.api.InputMouse;
import java.awt.*;
import java.awt.image.BufferedImage;

public class UISlider extends AbstractUIComponent {

    private final int maxLevel = 10;
    private int level = 5;
    private final int SCALE_FACTOR = 10; // ⭐ IMPORTANTE: mismo factor que InputMouse

    private final Color barBackground = new Color(100, 100, 100);
    private final Color barFill = new Color(200, 50, 50);
    private final Color knobNormal = new Color(220, 220, 220);
    private final Color knobHover = new Color(255, 255, 255);
    private final Color knobBorder = Color.BLACK;
    private final Color textColor = Color.BLACK;

    public UISlider(int x, int y, int width, int height, int layer,
                    String visualId, Point[] vertices, Point dir, int initialLevel) {
        super(x, y, width, height, layer, visualId, 
              vertices != null ? vertices : generateDefaultHitbox(width, height), 
              dir);
        this.level = Math.min(maxLevel, Math.max(0, initialLevel));
    }

    private static Point[] generateDefaultHitbox(int width, int height) {
        int logicalWidth = width / 10;
        int logicalHeight = height / 10;
        
        return new Point[]{
            new Point(0, 0),
            new Point(logicalWidth, 0),
            new Point(logicalWidth, logicalHeight),
            new Point(0, logicalHeight)
        };
    }

    @Override
    public void render() {
        int w = getWidth();
        int h = getHeight();

        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // === 1. BARRA DE FONDO ===
        int barHeight = 10;
        int barY = (h - barHeight) / 2;
        
        g2d.setColor(barBackground);
        g2d.fillRoundRect(0, barY, w, barHeight, barHeight, barHeight);

        // === 2. BARRA DE PROGRESO ===
        int fillWidth = (int)((level / (float)maxLevel) * w);
        if (fillWidth > 0) {
            g2d.setColor(barFill);
            g2d.fillRoundRect(0, barY, fillWidth, barHeight, barHeight, barHeight);
        }

        // === 3. CÍRCULO (KNOB) ===
        int knobSize = Math.min(h - 4, 32);
        int knobX = (int)(level * w / (float)maxLevel) - knobSize / 2;
        knobX = Math.max(0, Math.min(knobX, w - knobSize));
        int knobY = (h - knobSize) / 2;

        // Sombra
        g2d.setColor(new Color(0, 0, 0, 80));
        g2d.fillOval(knobX + 3, knobY + 3, knobSize, knobSize);

        // Círculo
        if (isHovered) {
            g2d.setColor(knobHover);
        } else {
            g2d.setColor(knobNormal);
        }
        g2d.fillOval(knobX, knobY, knobSize, knobSize);

        // Borde
        g2d.setColor(knobBorder);
        g2d.setStroke(new BasicStroke(2.5f));
        g2d.drawOval(knobX, knobY, knobSize, knobSize);

        // Destello
        g2d.setColor(new Color(255, 255, 255, 150));
        g2d.fillOval(knobX + 6, knobY + 6, knobSize / 3, knobSize / 3);

        // === 4. TEXTO ===
        g2d.setColor(textColor);
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        String text = String.valueOf(level);
        FontMetrics fm = g2d.getFontMetrics();
        int textX = w + 15;
        int textY = (h + fm.getAscent()) / 2 - 2;
        g2d.drawString(text, textX, textY);

        g2d.dispose();
        setImage(img);
    }

    @Override
    public void updateState(boolean contains) {
        super.updateState(contains);

        InputMouse input = InputMouse.getInstance();

        if (contains && input.isLeftMouseDown()) {
            Point mouse = input.getMousePosition();
            
            // ⭐ CORRECCIÓN: Convertir mouse de unidades lógicas a píxeles
            int mouseXPixels = mouse.x * SCALE_FACTOR;
            int mouseYPixels = mouse.y * SCALE_FACTOR;
            
            // Calcular posición relativa
            int localX = mouseXPixels - getRenderX();
            
            // Clamp al rango válido
            localX = Math.max(0, Math.min(localX, getWidth()));
            
            // Calcular nivel
            int newLevel = calculateLevelFromPosition(localX);
            
            if (newLevel != level) {
                level = newLevel;
                render();
                executeClickAction();
            }
        }
    }

    private int calculateLevelFromPosition(int localX) {
        int w = getWidth();
        float percentage = (float)localX / (float)w;
        int calculatedLevel = Math.round(percentage * maxLevel);
        return Math.max(0, Math.min(maxLevel, calculatedLevel));
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        int oldLevel = this.level;
        this.level = Math.min(maxLevel, Math.max(0, level));
        
        if (oldLevel != this.level) {
            render();
        }
    }

    public float getVolumeFloat() {
        return level / (float)maxLevel;
    }

    @Override
    protected void onEnter() {
        render();
    }

    @Override
    protected void onExit() {
        render();
    }

    @Override
    public void onClick() {
    }
}