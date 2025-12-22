/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.dungeon_game.core.components;

import com.dungeon_game.core.api.InputMouse;
import com.dungeon_game.core.data.VisualRender;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class UIScrollPanel extends VisualRender {

    private final List<VisualRender> children = new ArrayList<>();

    private Color background = new Color(0, 0, 0, 150);
    private Color borderColor = Color.BLACK;

    // Scroll en píxeles
    private int scrollY = 0;
    private int contentHeight = 0;   // alto total de los hijos
    private int scrollStep = 20;     // cuánto se mueve por “paso”

    public UIScrollPanel(int x, int y, int width, int height,
                         int layer, String visualId,
                         Point[] vertices, Point dir) {
        super(x, y, width, height, layer, visualId, vertices, dir);
    }

    // ---------- GESTIÓN DE HIJOS ----------

    public void addChild(VisualRender child) {
        children.add(child);
        recalcContentHeight();
    }

    public void removeChild(VisualRender child) {
        children.remove(child);
        recalcContentHeight();
    }

    private void recalcContentHeight() {
        int max = 0;
        for (VisualRender vr : children) {
            int bottom = vr.getRenderY() + vr.getHeight();
            if (bottom > max) max = bottom;
        }
        contentHeight = max;
        clampScroll();
    }

    // ---------- SCROLL ----------

    public void scrollBy(int dy) {
        scrollY += dy;
        clampScroll();
    }

    public void setScrollStep(int scrollStep) {
        this.scrollStep = scrollStep;
    }

    private void clampScroll() {
        int maxScroll = Math.max(0, contentHeight - getHeight());
        if (scrollY < 0) scrollY = 0;
        if (scrollY > maxScroll) scrollY = maxScroll;
    }

    // ---------- UPDATE (hover / click de hijos) ----------

    public void update() {
        InputMouse input = InputMouse.getInstance();
        Point mouse = input.getMousePosition();
        int mx = mouse.x;
        int my = mouse.y;

        // (Opcional) si tienes rueda de mouse, aquí la usarías:
        // int wheel = input.consumeWheelDelta(); // método que tú implementes
        // if (isMouseInsidePanel(mx, my)) {
        //     scrollBy(-wheel * scrollStep);
        // }

        // Mouse dentro del panel → transformamos a coords locales
        if (!isMouseInsidePanel(mx, my)) {
            // si quieres, puedes hacer que los hijos no reaccionen
            return;
        }

        int localX = mx - getRenderX();
        int localY = my - getRenderY() + scrollY; // sumamos scroll

        for (VisualRender vr : children) {
            if (vr instanceof AbstractUIComponent ui) {
                boolean contains = isMouseInsideChild(ui, localX, localY);
                ui.updateState(contains);
            }
        }
    }

    private boolean isMouseInsidePanel(int mx, int my) {
        return mx >= getRenderX() && mx <= getRenderX() + getWidth()
            && my >= getRenderY() && my <= getRenderY() + getHeight();
    }

    private boolean isMouseInsideChild(VisualRender vr, int localX, int localY) {
        int x = vr.getRenderX();
        int y = vr.getRenderY();
        int w = vr.getWidth();
        int h = vr.getHeight();
        return localX >= x && localX <= x + w
            && localY >= y && localY <= y + h;
    }

    // ---------- RENDER ----------

    @Override
    public void render() {
        int pw = getWidth();
        int ph = getHeight();

        BufferedImage image = new BufferedImage(pw, ph, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                             RenderingHints.VALUE_ANTIALIAS_ON);

        // 1) Fondo del panel (coordenadas locales)
        g2d.setColor(background);
        g2d.fillRect(0, 0, pw, ph);

        // 2) Borde
        g2d.setColor(borderColor);
        g2d.drawRect(0, 0, pw - 1, ph - 1);

        // 3) Clip para que nada se dibuje fuera del panel
        Shape oldClip = g2d.getClip();
        g2d.setClip(0, 0, pw, ph);

        // 4) Dibujar hijos con scroll aplicado
        for (VisualRender vr : children) {
            vr.render();  // el hijo actualiza su propia imagen
            Image childImg = vr.getImage();
            if (childImg == null) continue;

            // Asumiendo que las coords de los hijos son relativas al panel:
            int childLocalX = vr.getRenderX();
            int childLocalY = vr.getRenderY() - scrollY;  // aplicamos scroll

            // Si las coords de hijos fueran globales, usarías:
            // int childLocalX = vr.getRenderX() - getRenderX();
            // int childLocalY = vr.getRenderY() - getRenderY() - scrollY;

            // Solo dibujamos si alguna parte está dentro del viewport (opcional, optimización)
            if (childLocalY + vr.getHeight() >= 0 && childLocalY <= ph) {
                g2d.drawImage(childImg, childLocalX, childLocalY, null);
            }
        }

        // 5) Restaurar clip
        g2d.setClip(oldClip);

        // 6) DIBUJAR BARRA DE SCROLL dentro del panel (coordenadas locales)
        drawScrollbar(g2d);

        g2d.dispose();

        this.setImage(image);
    }


    private void drawScrollbar(Graphics2D g) {
        if (contentHeight <= getHeight()) return; // no hace falta

        int px = getRenderX();
        int py = getRenderY();
        int pw = getWidth();
        int ph = getHeight();

        int barWidth = 8;
        int barX = px + pw - barWidth - 2;

        float ratio = (float) getHeight() / (float) contentHeight;
        int barHeight = Math.max(20, (int) (ph * ratio));

        int maxScroll = contentHeight - getHeight();
        float scrollRatio = (maxScroll == 0) ? 0f : (float) scrollY / maxScroll;
        int barY = py + (int) ((ph - barHeight) * scrollRatio);

        g.setColor(new Color(200, 200, 200, 180));
        g.fillRect(barX, barY, barWidth, barHeight);
    }
}
