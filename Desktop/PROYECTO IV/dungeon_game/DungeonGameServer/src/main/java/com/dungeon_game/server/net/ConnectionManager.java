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
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author USUARIO
 */
public class ConnectionManager {
    //Lo mas importante GAAAAAAAA
    private int port;
    private ServerContext context;
    
    private final ConcurrentHashMap<String, PlayerThread> playersById  = new ConcurrentHashMap<>();
    private volatile boolean running = true; // :0
    
    
    private final GameChatService chatService;
    
    
    private ServerSocket serverSocket;
    
    public ConnectionManager(int port, ServerContext context){
        this.port = port;
        this.context  = context ;
        this.chatService = new GameChatService(context.getSessionManager(), this);
    }
    
    public GameChatService getChatService() {
        return chatService;
    }
    
    public void start(){
        try {
            this.serverSocket = new ServerSocket(port);
            System.out.println("[Server] Escuchando en el puerto " + port);

            while (running) {
                Socket socket = serverSocket.accept();
                System.out.println("[Server] Nueva conexión desde " + socket.getRemoteSocketAddress());

                PlayerThread playerThread = new PlayerThread(socket, context, this);
                new Thread(playerThread, "PlayerThread-" + socket.getRemoteSocketAddress()).start();
            }

        } catch (IOException e) {
            if (running) { // si estaba corriendo de verdad, es error real
                System.err.println("[Server] Error en ConnectionManager: " + e.getMessage());
                e.printStackTrace();
            }
        } finally {
            try { if (serverSocket != null) serverSocket.close(); } catch (Exception ignored) {}
        }
    }
    public void stop() {
        running = false;
        try { if (serverSocket != null) serverSocket.close(); } catch (Exception ignored) {}
        // No cierro players aquí por simplicidad, se podría mejorar
    }

    public void registerPlayer(String playerId, PlayerThread pt) {
        if (playerId == null || playerId.isBlank() || pt == null) return;

        PlayerThread prev = playersById.put(playerId, pt);
        
        if (prev != null && prev != pt) {
            System.out.println("[Server] Reemplazando conexión previa de " + playerId);
            prev.requestStop(); // 👈 lo agregamos en PlayerThread
        }
        System.out.println("[Server][registerPlayer] " + playerId + " replaced=" + (prev != null));

    }
     public void unregisterPlayer(String playerId, PlayerThread pt) {
        if (playerId == null || playerId.isBlank() || pt == null) return;

        boolean removed = playersById.remove(playerId, pt); // remove seguro
        if (removed) {
            System.out.println("[Server] unregister ok: " + playerId);
        }
    }
     public void broadcast(String message) {
        for (PlayerThread pt : playersById.values()) {
            pt.sendLine(message);
        }
    }

    public void sendTo(String playerId, String message) {
        PlayerThread pt = playersById.get(playerId);

        if (pt != null) {
            pt.sendLine(message);
            System.out.println("[Server] sendTo " + playerId + " => " + message);
        } else {
            System.out.println("[Server][WARN] sendTo failed, no thread for " + playerId);
        }
    }
    public boolean isOnline(String playerId) {
        return playerId != null && playersById.containsKey(playerId);
    }

    public List<String> getOnlinePlayerIds() {
        return new ArrayList<>(playersById.keySet());
    }
}
