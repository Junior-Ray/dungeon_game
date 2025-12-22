/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.dungeon_game.server.chat;

import com.dungeon_game.server.net.ConnectionManager;
import com.dungeon_game.server.session.PlayerSession;
import com.dungeon_game.server.session.SessionManager;
import java.util.List;

/**
 *
 * @author USUARIO
 */
public class GameChatService {
    private final SessionManager sessionManager;
    private final ConnectionManager connectionManager;

    public GameChatService(SessionManager sessionManager, ConnectionManager connectionManager) {
        this.sessionManager = sessionManager;
        this.connectionManager = connectionManager;
    }

    // ========== API PÚBLICA ==========

    /** Chat global: visible para todos los jugadores conectados */
    public void sendGlobal(PlayerSession from, String rawText) {
        String text = limpiar(rawText);
        if (text.isEmpty()) {
            sendSystemMessage(from, "No puedes enviar un mensaje vacío.");
            return;
        }

        String formatted = "[GLOBAL] " + from.getPlayerId() + ": " + text;


        connectionManager.broadcast(formatted);
    }

    /** Chat de party: solo miembros de la misma party */
    public void sendParty(PlayerSession from, String rawText) {
        String text = limpiar(rawText);
        if (text.isEmpty()) {
            sendSystemMessage(from, "No puedes enviar un mensaje vacío.");
            return;
        }

        String partyId = from.getPartyId();
        if (partyId == null) {
            sendSystemMessage(from, "No estás en una party.");
            return;
        }

        List<PlayerSession> miembros = sessionManager.getSessionsByPartyId(partyId);
        if (miembros.isEmpty()) {
            sendSystemMessage(from, "Tu party no tiene más miembros conectados.");
            return;
        }

        String formatted = "[PARTY] " + from.getPlayerId() + ": " + text;
        for (PlayerSession ps : miembros) {
            connectionManager.sendTo(ps.getPlayerId(), formatted);
        }
    }

    /** Whisper / mensaje privado 1 a 1 */
    public void sendWhisper(PlayerSession from, String targetName, String rawText) {
        String text = limpiar(rawText);
        if (text.isEmpty()) {
            sendSystemMessage(from, "No puedes enviar un mensaje vacío.");
            return;
        }

        if (targetName == null || targetName.isBlank()) {
            sendSystemMessage(from, "Uso: WHISPER <nombreJugador> <mensaje>");
            return;
        }

        PlayerSession target = sessionManager.getSessionByName(targetName);
        if (target == null) {
            sendSystemMessage(from, "El usuario '" + targetName + "' no está conectado.");
            return;
        }

        String toReceiver = "[PM] " + from.getPlayerId() + " → tú: " + text;
        String toSender  = "[PM] tú → " + target.getPlayerId() + ": " + text;

        connectionManager.sendTo(target.getPlayerId(), toReceiver);
        connectionManager.sendTo(from.getPlayerId(), toSender);
    }

    /** Mensaje del sistema para un solo jugador */
    public void sendSystemMessage(PlayerSession to, String text) {
        String msg = "[SERVER] " + text;
        connectionManager.sendTo(to.getPlayerId(), msg);
    }

    // ========== Helpers internos ==========

    private String limpiar(String raw) {
        if (raw == null) return "";
        return raw.trim();
    }
}
