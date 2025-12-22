/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.dungeon_game.client.net;


import com.dungeon_game.core.net.GameTransport;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.function.Consumer;

/**
 *
 * @author USUARIO
 */
public class NetworkClient implements GameTransport{

   private final String host;
    private final int port;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private Thread listenerThread;
    private volatile boolean running = false;

    // Un callback para cuando llegue un mensaje del server
    private final Consumer<String> onServerMessage;

    public NetworkClient(String host, int port, Consumer<String> onServerMessage) {
        this.host = host;
        this.port = port;
        this.onServerMessage = onServerMessage;
    }

    public boolean connect() {
        try {
            socket = new Socket(host, port);
            in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            running = true;
            
            
            System.out.println("[Client] CONNECT OK -> " + host + ":" + port);
            
            // Hilo que escucha al servidor
            listenerThread = new Thread(this::listenLoop, "ClientListener");
            listenerThread.start();

            return true;
        } catch (IOException e) {
            System.err.println("[Client] No se pudo conectar al servidor: " + e.getMessage());
            return false;
        }
    }

    private void listenLoop() {
        try {
            String line;
            while (running && (line = in.readLine()) != null) {
                System.out.println("[Client] RX <- " + line);
                if (onServerMessage != null) {
                    
                    onServerMessage.accept(line);
                }
            }
        } catch (IOException e) {
            System.err.println("[Client] Error en listener: " + e.getMessage());
        } finally {
            System.out.println("[Client] Listener END");
            close();
        }
    }

    public void sendCommand(String command) {
        if (out != null) {
            out.println(command);
        }
    }

    public void close() {
        running = false;
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {}
    }
}
