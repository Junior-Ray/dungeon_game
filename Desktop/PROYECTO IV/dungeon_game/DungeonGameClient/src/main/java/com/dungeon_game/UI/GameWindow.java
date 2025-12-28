/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.dungeon_game.UI;

import com.dungeon_game.client.ClientBootstrap;
import com.dungeon_game.client.DefaultTransportFactory;
import com.dungeon_game.client.audio.JavaAudioPlayer;
import com.dungeon_game.client.net.NetworkAuthService;
import com.dungeon_game.client.net.SessionStorage;
import com.dungeon_game.core.api.RenderProcessor;
import com.dungeon_game.core.audio.AudioManager;
import com.dungeon_game.core.auth.AuthManager;
import com.dungeon_game.core.logic.GameState;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;

/**
 *
 * @author GABRIEL SALGADO
 */
public class GameWindow {
    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;
    
    private JFrame frame;
    private GameCanvas canvas;
    public GameWindow(String title){
        AudioManager audioManager = AudioManager.getInstance();
        audioManager.setAudioPlayer(new JavaAudioPlayer());
        // INYECCIÓN DE DEPENDENCIAS (Obligatorio)
        // Conectamos el cerebro (Client) con el cuerpo (Core)
        AuthManager.setService(new NetworkAuthService());
        GameState.getInstance().setTokenStorage(new SessionStorage());
        
        GameState.getInstance().setTransportFactory(new DefaultTransportFactory());
        GameState.getInstance().iniciarGraficos();
        
        ClientBootstrap.configure();
        canvas = new GameCanvas(WIDTH, HEIGHT);
        
        frame = new JFrame(title);
        
        frame.add(canvas);
        frame.pack();
        
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
         frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {

                // ⬇️ Tu código aquí (antes de cerrar)
                //System.out.println("Guardando datos antes de salir...");

                // Ejemplo: detener el hilo del juego
                canvas.stop();

                // Ejemplo: guardar configuraciones
                RenderProcessor.getInstance().eliminarTodo();

                // Ahora sí cerrar la app
                frame.dispose();
                System.exit(0);
            }
        });
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        
        canvas.start();
        
    }
    public static void main(String[] args) {
        new GameWindow("Sombra Carmesí");
    }
    public GameCanvas getCanvas() {
        return canvas;
    }
    public static int getWidth(){
        return WIDTH;
    }
    public static int getHeight(){
        return HEIGHT;
    }
}
