/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.dungeon_game.server.session;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author USUARIO
 */
public class SessionManager {

    private final Map<String, PlayerSession> sesiones = new ConcurrentHashMap<>();

    /**
     * Crea una nueva sesión para el jugador dado, o devuelve la existente
     * si ya estaba registrada.
     */
    public PlayerSession getOrCreateSession(String playerId) {
        return sesiones.computeIfAbsent(playerId, PlayerSession::new);
    }

    /**
     * Obtiene la sesión de un jugador, o null si no existe.
     */
    public PlayerSession getSession(String playerId) {
        return sesiones.get(playerId);
    }

    /**
     * Elimina la sesión (por ejemplo, cuando se desconecta).
     */
    public void removeSession(String playerId) {
        sesiones.remove(playerId);
    }

    /**
     * Devuelve todas las sesiones actuales (por ejemplo, para WHO/PLAYERS).
     */
    public Collection<PlayerSession> getAllSessions() {
        return sesiones.values();
    }
    public List<String> getResumenSesiones() {
        List<String> lista = new ArrayList<>();
        for (PlayerSession ps : sesiones.values()) {
            lista.add(ps.getPlayerId() + "(" + ps.getMode().name().toLowerCase() + ")");
        }
        return lista;
    }
    
    public List<PlayerSession> getSessionsByPartyId(String partyId) {
        List<PlayerSession> lista = new ArrayList<>();
        if (partyId == null) return lista;

        for (PlayerSession ps : sesiones.values()) {
            if (partyId.equals(ps.getPartyId())) {
                lista.add(ps);
            }
        }
        return lista;
    }
    /**
     * Busca una sesión por "nombre" del jugador.
     * Si tu playerId ya es el username, esto es equivalente a getSession(playerId).
     */
    public PlayerSession getSessionByName(String name) {
        if (name == null) return null;
        // Si playerId es el username:
        return getSession(name.trim());

        // Si en el futuro tienes un campo "displayName" separado, aquí lo recorres:
        /*
        String lower = name.toLowerCase(Locale.ROOT);
        for (PlayerSession ps : sesiones.values()) {
            if (ps.getPlayerId().toLowerCase(Locale.ROOT).equals(lower)) {
                return ps;
            }
        }
        return null;
        */
    }
}
