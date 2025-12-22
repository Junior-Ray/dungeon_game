/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.dungeon_game.core.components;

import com.dungeon_game.core.api.Updater;
import com.dungeon_game.core.model.Imagen;
import java.util.List;

/**
 *
 * @author USUARIO
 */
public class SpriteImageAnimator implements Updater {

    private final Imagen target;
    private final List<String> frames;
    private final long frameDurationMs;
    private final boolean loop;

    private int index = 0;
    private boolean playing = true;
    private long lastTimeMs;

    public SpriteImageAnimator(Imagen target, List<String> frames, long frameDurationMs, boolean loop) {
        if (target == null) throw new IllegalArgumentException("target null");
        if (frames == null || frames.isEmpty()) throw new IllegalArgumentException("frames vacío");
        if (frameDurationMs <= 0) throw new IllegalArgumentException("frameDurationMs debe ser > 0");

        this.target = target;
        this.frames = frames;
        this.frameDurationMs = frameDurationMs;
        this.loop = loop;

        apply(frames.get(0));
        this.lastTimeMs = System.currentTimeMillis();
    }

    public void play() { playing = true; lastTimeMs = System.currentTimeMillis(); }
    public void pause() { playing = false; }
    public void stop() { playing = false; index = 0; apply(frames.get(0)); }

    @Override
    public void update() {
        if (!playing) return;

        long now = System.currentTimeMillis();
        long dt = now - lastTimeMs;
        if (dt < frameDurationMs) return;

        long steps = dt / frameDurationMs;
        lastTimeMs += steps * frameDurationMs;

        int next = index + (int) steps;

        if (loop) index = next % frames.size();
        else {
            index = Math.min(next, frames.size() - 1);
            if (index == frames.size() - 1) playing = false;
        }

        apply(frames.get(index));
    }

    private void apply(String frameId) {
        // IMPORTANTE: aquí depende de tu clase Imagen
        // Necesitas un setter tipo setVisualId / setSprite / setIdRecurso.
        target.setVisualId(frameId);

        // Si tu motor requiere que se “re-renderice” el nodo, llama algo tipo:
        // target.render();
        // DriverRender.getInstance().string();
    }
}