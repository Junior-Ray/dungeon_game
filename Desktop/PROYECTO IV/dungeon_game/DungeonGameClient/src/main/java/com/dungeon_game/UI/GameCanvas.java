/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.dungeon_game.UI;

import com.dungeon_game.control.InputManager;
import com.dungeon_game.control.InputTeclado;

import com.dungeon_game.core.api.Update;
import com.dungeon_game.core.audio.AudioManager;
import com.dungeon_game.core.audio.MusicTrack;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;

/**
 *
 * @author GABRIEL SALGADO
 */
public class GameCanvas extends Canvas implements Runnable {

    private static int CANVAS_WIDTH;
    private static int CANVAS_HEIGHT;

    //Variable de estado, (Volatile debido a que formara parte del hilo)
    private volatile boolean running = false;
    private Thread gameThread;
    private final MapaRender renderer;

    public GameCanvas(int width, int height) {
        this.CANVAS_WIDTH = width;
        this.CANVAS_HEIGHT = height;
        // Inicialización del InputManager y añadirlo como listener

        this.addMouseListener(InputManager.getInstance());
        this.addMouseMotionListener(InputManager.getInstance());
        //listener para teclado
        this.addKeyListener(InputTeclado.getInstance());
        this.setFocusable(true); 
        // Inicializar el MapaRender 
        this.renderer = new MapaRender(CANVAS_WIDTH, CANVAS_HEIGHT);
        // Configuración del Canvas
        this.setPreferredSize(new Dimension(width, height));
        this.setMaximumSize(new Dimension(width, height));
        this.setMinimumSize(new Dimension(width, height));
        setFocusable(true);

    }

    //INICIA EL JUEGO
    public synchronized void start() {
        if (running) {
            return; // Evita iniciar el hilo dos veces
        }
        running = true;
        gameThread = new Thread(this, "GameThread");
        gameThread.start();
    }

    //Detiene el juego evitando errores
    public synchronized void stop() {
        if (!running) {
            return; //No detiene cuando no esta en renderizando
        }
        running = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // PARTE DEL LOOP PARA EL RENDERIZADO y ACTUALIZACION
    @Override
    public void run() {
        this.requestFocusInWindow();
        //1. Crear el BufferStrategy
        this.createBufferStrategy(3);
        BufferStrategy bs = this.getBufferStrategy();
        //game loop
        long lastTime = System.nanoTime();
        final double amountOfTicks = 60;
        double ns = 1000000000 / amountOfTicks;
        double delta = 0;

        while (running) {
            long now = System.nanoTime();
            long timeElapsed = now - lastTime;
            lastTime = now;
            delta += timeElapsed / ns;

            if (delta >= 1) {
                update();
                //2. Obtener el contenido del gráfico del Buffer
                Graphics g = bs.getDrawGraphics();
                //3. Limpiar y Dibujar
                // Renderiza la lista OM y obtiene la imagen compuesta (BufferedImage)
                g.drawImage(renderer.renderSala(), 0, 0, getWidth(), getHeight(), null);

                g.dispose();
                bs.show();

                delta--;
            }
        }
    }

    // MÉTODO PARA LA LÓGICA DEL JUEGO 
    private void update() {
        //Primero actualizamos Inputs
        InputManager.getInstance().update();
        InputTeclado.getInstance().update();
        //Actualizamos logica
        Update.getInstance().update();
        // Aquí iría la comunicación con el Core/Servidor para obtener la ListaOM actualizada
        // Ejemplo: this.listaOM = ServerConnection.getRenderableObjects();

    }

}
