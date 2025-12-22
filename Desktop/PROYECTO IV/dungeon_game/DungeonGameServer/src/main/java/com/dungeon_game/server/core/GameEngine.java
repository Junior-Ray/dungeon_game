/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.dungeon_game.server.core;

import com.dungeon_game.core.logic.GameState;
import com.dungeon_game.core.structures.DungeonGraph;
import com.dungeon_game.core.structures.NodoSala;
import com.dungeon_game.core.structures.TipoSala;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author USUARIO
 */
public class GameEngine {

    private DungeonGraph mapa;
    private Map<String, GameState> jugadores = new ConcurrentHashMap<>(); //Esto se tiene que explicar despues
    public GameEngine() {
        this.mapa = crearMapaInicial();
        System.out.println("[GameEngine] Mapa inicial creado:");
        System.out.println(mapa);
    }
    private DungeonGraph crearMapaInicial() {
        DungeonGraph g = new DungeonGraph();

        NodoSala s1 = new NodoSala("S1", TipoSala.INICIO, 0, 0, 0, false, false);
        NodoSala s2 = new NodoSala("S2", TipoSala.NORMAL, 0, 1, 0, false, false);
        NodoSala s3 = new NodoSala("S3", TipoSala.NORMAL, 0, 2, 0, false, false);
        NodoSala s4 = new NodoSala("S4", TipoSala.JEFE,   0, 3, 0, true,  false); // bloqueada por ahora

        g.agregarSala(s1);
        g.agregarSala(s2);
        g.agregarSala(s3);
        g.agregarSala(s4);

        g.conectarSalas("S1", "S2");
        g.conectarSalas("S2", "S3");
        g.conectarSalas("S3", "S4");

        return g;
    }

    // =============== Gestión de jugadores =================

    public void registrarJugador(String playerId) {
        jugadores.computeIfAbsent(playerId, id -> {
            NodoSala inicio = mapa.getSala("S1");
            System.out.println("[GameEngine] Registrando jugador " + id + " en S1");
            //return new GameState(mapa, inicio);
            return new GameState();
        });
    }

    public boolean moverJugador(String playerId, String idDestino) {
        GameState state = jugadores.get(playerId);
        if (state == null) {
            return false;
        }
        return state.moverA(idDestino);
    }

    public String getSalaActualDe(String playerId) {
        GameState state = jugadores.get(playerId);
        if (state == null) {
            return "DESCONOCIDA";
        }
        return state.getSalaActual().getIdSala();
    }
    public String getResumenJugadores() {
        StringBuilder sb = new StringBuilder();
        boolean primero = true;

        for (Map.Entry<String, GameState> e : jugadores.entrySet()) {
            if (!primero) {
                sb.append(", ");
            }
            primero = false;

            String playerId = e.getKey();
            GameState state = e.getValue();
            String sala = state.getSalaActual().getIdSala();

            sb.append(playerId).append(":").append(sala);
        }

        if (primero) {
            // significa que no había jugadores
            return "(sin jugadores)";
        }

        return sb.toString();
    }
    public void resetearPosicion(String playerId) {
        GameState state = jugadores.get(playerId);
        if (state != null) {
            NodoSala inicio = mapa.getSala("S1");
            state.forzarMoverA(inicio); // Necesitamos añadir este método en GameState
        }
    }

    // =============== Game loop hook =======================

    /**
     * Actualiza el mundo del juego.
     * @param deltaMs tiempo transcurrido desde el último tick, en ms.
     */
    public void update(long deltaMs) {
        // Aquí más adelante:
        // - mover mobs
        // - IA
        // - eventos de tiempo, etc.
    }
    
}
