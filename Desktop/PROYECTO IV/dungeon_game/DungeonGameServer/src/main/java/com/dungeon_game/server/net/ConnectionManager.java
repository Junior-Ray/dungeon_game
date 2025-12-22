/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.dungeon_game.server.net;


import com.dungeon_game.server.ServerContext;
import com.dungeon_game.server.chat.GameChatService;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author USUARIO
 */
public class ConnectionManager {

    private int port;
    private ServerContext context;
    private List<PlayerThread> players = new ArrayList<>();
    private volatile boolean running = true; // :0
    
    
    private final GameChatService chatService;
    
    public ConnectionManager(int port, ServerContext context){
        this.port = port;
        this.context  = context ;
        this.chatService = new GameChatService(context.getSessionManager(), this);
    }
    
    public GameChatService getChatService() {
        return chatService;
    }
    
    public void start(){
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("[Server] Escuchando en el puerto " + port);

            while (running) {
                Socket socket = serverSocket.accept();
                System.out.println("[Server] Nueva conexión desde " + socket.getRemoteSocketAddress());

                PlayerThread playerThread = new PlayerThread(socket, context, this);
                players.add(playerThread);
                new Thread(playerThread, "PlayerThread-" + players.size()).start();
            }

        } catch (IOException e) {
            System.err.println("[Server] Error en ConnectionManager: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public void stop() {
        running = false;
        // No cierro players aquí por simplicidad, se podría mejorar
    }
    public synchronized void broadcast(String message) {
        for (PlayerThread pt : players) {
            pt.sendLine(message);
        }
    }
    public synchronized void sendTo(String playerId, String message) {
        boolean sent = false;
        for (PlayerThread pt : players) {
            String pid = pt.getPlayerId();
            if (pid != null && pid.equals(playerId)) {
                pt.sendLine(message);
                System.out.println("[Server] sendTo " + pid + " => " + message);
                sent = true;
                break;

            }
        }
        if (!sent) {
            System.out.println("[Server][WARN] sendTo failed, no thread for " + playerId);
        }
    }
    public synchronized void removePlayer(PlayerThread pt) {
        players.remove(pt);
    }
}
