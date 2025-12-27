/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.dungeon_game.core.data;

/**
 *
 * @author USUARIO
 */



import com.dungeon_game.core.api.DriverRender;
import com.dungeon_game.core.api.Updater;


/**
 * Animación vertical: mueve un objeto en Y (sube/baja).
 * Compatible con tus dos mundos: RenderableVisual y VisualRender.
 */
public class SlideVertical implements Updater {

    @FunctionalInterface
    public interface Listener { void onFinish(); }

    private final VisualRender target;
    private final int endY;
    private final int speed;
    private boolean finished = false;
    private Listener listener;

    public SlideVertical(VisualRender target, int endY, int speed) {
        this.target = target;
        this.endY = endY;
        this.speed = Math.max(1, speed);
    }

    public void setListener(Listener l) { this.listener = l; }

    @Override
    public void update() {
        if (finished) return;

        int x = target.getRenderX();
        int y = target.getRenderY();

        if (y == endY) {
            finish();
            return;
        }

        int nextY;
        if (y < endY) nextY = Math.min(endY, y + speed);
        else          nextY = Math.max(endY, y - speed);

        // 1) quitar hitbox viejo
        SpatialGrid.getInstance().limpiar(target);

        // 2) mover (IMPORTANTE: también actualiza dir)
        target.moveTo(x, nextY);

        // 3) pintar hitbox nuevo
        SpatialGrid.getInstance().setElement(target);

        // refrescar frame
        DriverRender.getInstance().string();

        if (nextY == endY) finish();
    }

    private void finish() {
        finished = true;
        if (listener != null) listener.onFinish();
    }
}
