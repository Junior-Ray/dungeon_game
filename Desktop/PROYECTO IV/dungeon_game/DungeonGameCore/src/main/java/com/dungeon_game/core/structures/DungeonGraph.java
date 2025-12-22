/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.dungeon_game.core.structures;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 *
 * @author USUARIO
 */
public class DungeonGraph {

    private final Map<String, NodoSala> salas;
    
    private final Map<String, List<String>> adyacencias; //Vecinos p
    
    public DungeonGraph() {
        this.salas = new HashMap<>();
        this.adyacencias = new HashMap<>();
    }
    
    public void agregarSala(NodoSala sala) {
        if (sala == null) {
            throw new IllegalArgumentException("La sala no puede ser nula");
        }
        String id = sala.getIdSala();
        if (salas.containsKey(id)) {
            throw new IllegalArgumentException("Ya existe una sala con id: " + id);
        }

        salas.put(id, sala);
        adyacencias.put(id, new ArrayList<>());
    }
    public boolean eliminarSala(String idSala) {
        if (!salas.containsKey(idSala)) {
            return false;
        }

        // Eliminar la sala del mapa principal
        salas.remove(idSala);

        // Eliminar la lista de adyacencias de esa sala
        adyacencias.remove(idSala);

        // Eliminar referencias a esta sala en las listas de otros nodos
        for (List<String> vecinos : adyacencias.values()) {
            vecinos.remove(idSala);
        }

        return true;
    }
    public boolean existeSala(String idSala) {
        return salas.containsKey(idSala);
    }
    
    /**
     * Obtiene la sala con el id indicado.
     *
     * @param idSala id de la sala.
     * @return NodoSala o null si no existe.
     */
    public NodoSala getSala(String idSala) {
        return salas.get(idSala);
    }
    
    /**
     * Devuelve una vista inmodificable de todas las salas registradas.
     */
    public Collection<NodoSala> getTodasLasSalas() {
        return Collections.unmodifiableCollection(salas.values());
    }
    
    public int size() {
        return salas.size();
    }
    
    public void conectarSalas(String idA, String idB) {
        if (idA == null || idB == null) {
            throw new IllegalArgumentException("Los ids de sala no pueden ser nulos");
        }
        if (idA.equals(idB)) {
            throw new IllegalArgumentException("No se puede conectar una sala consigo misma: " + idA);
        }
        if (!salas.containsKey(idA) || !salas.containsKey(idB)) {
            throw new IllegalArgumentException("Ambas salas deben existir para conectarlas. idA=" +
                    idA + ", idB=" + idB);
        }

        List<String> vecinosA = adyacencias.get(idA);
        List<String> vecinosB = adyacencias.get(idB);

        if (!vecinosA.contains(idB)) {
            vecinosA.add(idB);
        }
        if (!vecinosB.contains(idA)) {
            vecinosB.add(idA);
        }
    }
    
    
    public void desconectarSalas(String idA, String idB) {
        List<String> vecinosA = adyacencias.get(idA);
        List<String> vecinosB = adyacencias.get(idB);

        if (vecinosA != null) {
            vecinosA.remove(idB);
        }
        if (vecinosB != null) {
            vecinosB.remove(idA);
        }
    }
    
    public boolean estanConectadas(String idA, String idB) {
        List<String> vecinosA = adyacencias.get(idA);
        if (vecinosA == null) return false;
        return vecinosA.contains(idB);
    }
    
    /**
     * Devuelve los vecinos (salas conectadas directamente) de la sala dada.
     *
     * @param idSala id de la sala.
     * @return lista inmodificable de NodoSala vecinos (puede estar vacía).
     */
    public List<NodoSala> getVecinos(String idSala) {
        List<String> idsVecinos = adyacencias.get(idSala);
        if (idsVecinos == null) {
            return Collections.emptyList();
        }

        List<NodoSala> vecinos = new ArrayList<>(idsVecinos.size());
        for (String id : idsVecinos) {
            NodoSala s = salas.get(id);
            if (s != null) {
                vecinos.add(s);
            }
        }
        return Collections.unmodifiableList(vecinos);
    }
    
    /**
     * Devuelve los ids de todas las salas vecinas de la sala indicada.
     * Útil para algoritmos de pathfinding o persistencia.
     */
    public List<String> getIdsVecinos(String idSala) {
        List<String> ids = adyacencias.get(idSala);
        if (ids == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(ids);
    }
    // =====================================================
    //         UTILIDAD PARA LÓGICA DE MOVIMIENTO / BFS
    // =====================================================

    /**
     * Indica si es válido moverse directamente de una sala a otra:
     * - Deben estar conectadas.
     * - La sala de destino no debe estar bloqueada.
     *
     * (Esta es una validación básica; la lógica de juego podría
     * añadir más reglas encima de esto.)
     */
    public boolean esMovimientoDirectoValido(String idOrigen, String idDestino) {
        if (!estanConectadas(idOrigen, idDestino)) {
            return false;
        }
        NodoSala destino = salas.get(idDestino);
        if (destino == null) {
            return false;
        }
        return !destino.isBloqueada();
    }

    /**
     * Verifica si existe algún camino (no necesariamente directo) entre
     * dos salas, usando un BFS sencillo.
     *
     * Esto NO devuelve la ruta, solo responde sí/no.
     */
    public boolean existeCamino(String idOrigen, String idDestino) {
        if (!salas.containsKey(idOrigen) || !salas.containsKey(idDestino)) {
            return false;
        }
        if (idOrigen.equals(idDestino)) {
            return true;
        }

        Set<String> visitados = new HashSet<>();
        Queue<String> cola = new ArrayDeque<>();

        cola.add(idOrigen);
        visitados.add(idOrigen);

        while (!cola.isEmpty()) {
            String actual = cola.poll();
            List<String> vecinos = adyacencias.get(actual);
            if (vecinos == null) continue;

            for (String vecino : vecinos) {
                if (!visitados.contains(vecino)) {
                    if (vecino.equals(idDestino)) {
                        return true;
                    }
                    visitados.add(vecino);
                    cola.add(vecino);
                }
            }
        }

        return false;
    }

    // =====================================================
    //                  UTILIDAD / DEPURACIÓN
    // =====================================================

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("DungeonGraph{\n");
        for (String id : salas.keySet()) {
            sb.append("  ").append(id)
              .append(" -> ").append(adyacencias.get(id))
              .append("\n");
        }
        sb.append('}');
        return sb.toString();
    }
    
    
    
}
