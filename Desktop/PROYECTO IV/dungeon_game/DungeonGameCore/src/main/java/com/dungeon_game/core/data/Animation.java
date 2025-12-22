/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.dungeon_game.core.data;

import com.dungeon_game.core.api.CroppedImage;
import com.dungeon_game.core.api.RenderProcessor;
import com.dungeon_game.core.api.Updater;
import com.dungeon_game.core.api.VisualRenderable;

/**
 *
 * @author GABRIEL SALGADO
 */
public abstract class Animation implements Updater{
    @FunctionalInterface
    public interface AnimationListener {
        void onFinish();
    }
    protected VisualRenderable target; // el objeto original
    protected CroppedImage cropped;  // la imagen temporal
    private AnimationListener listener;

    public Animation(VisualRenderable target) {
        this.target = target;
    }
    public final void stop() {
        finish();
    }

    public abstract void start();

    public abstract void update();

    protected void finish() {
        RenderProcessor.getInstance().eliminarElemento(cropped);
        RenderProcessor.getInstance().setElement(target);

        if (listener != null) {
            listener.onFinish();
        }
    }

    public void setListener(AnimationListener l) {
        this.listener = l;
    }

    public VisualRenderable getTarget() {
        return target;
    }

    public void setTarget(VisualRenderable target) {
        this.target = target;
    }

    public CroppedImage getCropped() {
        return cropped;
    }

    public void setCropped(CroppedImage cropped) {
        this.cropped = cropped;
    }
    
}
