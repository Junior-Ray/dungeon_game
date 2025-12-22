/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.dungeon_game.server.core;

/**
 *
 * @author USUARIO
 */
public class GameLoop implements Runnable {

    private final GameEngine engine;
    private final int ticksPerSecond;
    private volatile boolean running = true;

    public GameLoop(GameEngine engine, int ticksPerSecond) {
        this.engine = engine;
        this.ticksPerSecond = ticksPerSecond;
    }

    public void stopLoop() {
        running = false;
    }

    @Override
    public void run() {
        final long tickDurationMs = 1000L / ticksPerSecond;
        long lastTime = System.currentTimeMillis();

        System.out.println("[GameLoop] Iniciado con " + ticksPerSecond + " TPS");

        while (running) {
            long now = System.currentTimeMillis();
            long delta = now - lastTime;
            lastTime = now;

            engine.update(delta);

            long elapsed = System.currentTimeMillis() - now;
            long sleep = tickDurationMs - elapsed;
            if (sleep > 0) {
                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException ignored) {}
            }
        }

        System.out.println("[GameLoop] Detenido");
    }
}