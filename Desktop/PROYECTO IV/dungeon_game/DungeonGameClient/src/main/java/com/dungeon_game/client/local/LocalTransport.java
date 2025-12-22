/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.dungeon_game.client.local;

import com.dungeon_game.core.net.GameTransport;
import java.util.Locale;
import java.util.function.Consumer;

/**
 *
 * @author USUARIO
 */
public class LocalTransport implements GameTransport{

 
    private final Consumer<String> messageListener;
    private boolean connected = false;
    private String playerId = "aventurero";

    public LocalTransport(Consumer<String> messageListener) {
        // Esto es parecido a lo que haces en DungeonGameServer
      
        this.messageListener = messageListener;
    }

    @Override
    public boolean connect() {
        connected = true;
        // Mensajes “como si fuera” el servidor
        messageListener.accept("WELCOME DungeonGameServer (OFFLINE)");
        messageListener.accept("Usa: HELLO, WHERE, MOVE <idSala>, QUIT (modo offline)");
        return true;
    }

    @Override
    public void sendCommand(String cmd) {
        if (!connected) return;

        // Por ahora implementamos algunos comandos básicos “fake”.
        // Luego, si quieres, aquí llamas a la misma lógica que usa el servidor
        // (por ejemplo, algún CommandHandler o similar).
        String trimmed = cmd.trim();
        if (trimmed.isEmpty()) return;

        String[] parts = trimmed.split("\\s+");
        String op = parts[0].toUpperCase(Locale.ROOT);

        switch (op) {
            case "HELLO" -> {
                String nombre = (parts.length >= 2) ? parts[1] : "aventurero";
                messageListener.accept("Hola " + nombre + ", estás en modo OFFLINE (singleplayer).");
            }
            case "WHERE" -> {
                // Aquí podrías usar el GameEngine real para saber la sala.
                messageListener.accept("Estás en la sala inicial (OFFLINE).");
            }
            case "MOVE" -> {
                if (parts.length < 2) {
                    messageListener.accept("Uso: MOVE <idSala> (OFFLINE)");
                } else {
                    String sala = parts[1];
                    // Aquí podrías actualizar algún estado interno o usar el engine.
                    messageListener.accept("Te mueves a la sala " + sala + " (OFFLINE).");
                }
            }
            case "QUIT" -> {
                messageListener.accept("Cerrando sesión OFFLINE...");
                close();
            }
            default -> {
                messageListener.accept("Comando '" + cmd + "' aún no implementado en modo OFFLINE.");
            }
        }
    }

    @Override
    public void close() {
        if (!connected) return;
        connected = false;
        messageListener.accept("Modo OFFLINE finalizado.");
        // Si en algún momento creas hilos o loops locales, los cierras aquí.
    }
}
