/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.dungeon_game.core.data;

import com.dungeon_game.core.logic.Poligono;
import java.awt.Point;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

/**
 *
 * @author GABRIEL SALGADO
 *
 */
public class SpatialGrid {

    private byte[][][] matrizNodo = new byte[10][72][128];
    private Map<Byte, VisualRender> hash = new HashMap<>();
    private Queue<Byte> idsLibres = new ArrayDeque<>();
    private byte nextId = 1;

    private static Map<Byte, Map<Point, Byte>> historialPosiciones = new HashMap<>();
    private static SpatialGrid instance;

    private SpatialGrid() {}

    public static SpatialGrid getInstance() {
        if (instance == null) {
            instance = new SpatialGrid();
        }
        return instance;
    }


    public void reset() {

        // 1. Limpiar matriz
        for (int layer = 0; layer < matrizNodo.length; layer++) {
            for (int y = 0; y < matrizNodo[layer].length; y++) {
                Arrays.fill(matrizNodo[layer][y], (byte) 0);
            }
        }

        // 2. Limpiar historial
        historialPosiciones.clear();

        // 3. Limpiar hash
        hash.clear();

        // 4. Resetear IDs reutilizables
        idsLibres.clear();
        nextId = 1;   

       
    }


    private byte generateId() {
        if (!idsLibres.isEmpty()) {
            return idsLibres.poll();
        }
        return nextId++;  
    }

    private void releaseId(byte id) {
        if (id > 0) {
            idsLibres.offer(id);
            hash.remove(id);
        }
    }


    public void setElement(VisualRender obj) {

        if (obj.getId() == 0) {
            byte idNuevo = generateId();
            obj.setId(idNuevo);
            hash.put(idNuevo, obj);
        }

        int layer = obj.getLayer();
        draw(obj.getVertices(), layer, obj.getId());
    }

    private void draw(Point[] vertices, int layer, byte id) {
        if (vertices == null) return;

        Poligono p = new Poligono(vertices, id, matrizNodo[layer]);

        p.dibujarVertices();
        p.dibujarBordePoligono();
        Point centroide = p.centroideInterior();
        p.rellenarConDFS(centroide.x, centroide.y, historialPosiciones);
    }


    public void limpiar(VisualRender obj) {

        byte id = obj.getId();
        if (id == 0) return;

        int capa = obj.getLayer();
        Point[] vertices = obj.getVertices();
        if (vertices == null) return;

        Poligono p = new Poligono(vertices, id, matrizNodo[capa]);

        Point centro = vertices[0];
        p.limpiarFloodFillDFS(centro.x, centro.y, historialPosiciones);
    }

 
    public VisualRender getElement(Point dir, int layer) {
        byte id = matrizNodo[layer][dir.y][dir.x];
        return (id != 0) ? hash.get(id) : null;
    }

    public VisualRender[] getElemnts(Point dir) {
        int nLayer = matrizNodo.length;
        VisualRender[] vector = new VisualRender[nLayer];
        int j = 0;

        for (int i = 0; i < nLayer; i++) {
            byte id = matrizNodo[i][dir.y][dir.x];
            if (id != 0) {
                vector[j++] = hash.get(id);
            }
        }
        return vector;
    }


    public void eliminar(VisualRender obj) {
        limpiar(obj);
        releaseId(obj.getId());
        obj.setId((byte) 0);
    }
}