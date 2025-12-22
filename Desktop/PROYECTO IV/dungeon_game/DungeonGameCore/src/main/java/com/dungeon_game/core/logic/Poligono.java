/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.dungeon_game.core.logic;

import java.awt.Point;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 *
 * @author GABRIEL SALGADO
 */
public class Poligono {

    private Point[] vertices;
    private byte id;
    private byte matriz[][];
    private int witdh;
    private int height;

    public Poligono(Point[] vertices, byte id, byte[][] matriz) {
        this.vertices = vertices;
        this.id = id;
        this.matriz = matriz;

        // matriz.length es el número de filas (Y/alto)
        this.height = matriz.length;
        // matriz[0].length es el número de columnas (X/ancho)
        this.witdh = matriz[0].length;
    }

    public Poligono(Point[] vertices) {
        this.vertices = vertices;
        // Si no se proporciona matriz, las dimensiones quedan sin inicializar 
    }

    public void dibujarVertices() {
        for (Point p : vertices) {
            matriz[p.y][p.x] = id;
        }
    }

    public void dibujarBordePoligono() {
        int n = vertices.length;

        for (int i = 0; i < n; i++) {
            Point p1 = vertices[i];
            int j = (i + 1) % n;
            Point p2 = vertices[j];

            dibujarLineaBresenham(p1.x, p1.y, p2.x, p2.y);
        }
    }

    public Point centroideInterior() {

        // 1. Centroide geométrico clásico
        double area = 0;
        double cx = 0, cy = 0;
        int n = vertices.length;

        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            double cross = vertices[i].x * vertices[j].y
                    - vertices[j].x * vertices[i].y;

            area += cross;
            cx += (vertices[i].x + vertices[j].x) * cross;
            cy += (vertices[i].y + vertices[j].y) * cross;
        }

        area *= 0.5;
        cx /= (6 * area);
        cy /= (6 * area);

        int ix = (int) Math.round(cx);
        int iy = (int) Math.round(cy);

        // 2. Si el centroide ya está dentro, devolverlo
        if (pointInPolygon(ix, iy)) {
            return new Point(ix, iy);
        }

        // 3. Buscar un punto interior cercano (espiral)
        for (int r = 1; r < 80; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dy = -r; dy <= r; dy++) {

                    int px = ix + dx;
                    int py = iy + dy;

                    if (pointInPolygon(px, py)) {
                        return new Point(px, py);
                    }
                }
            }
        }

        // 4. Muy improbable: devolver primer vértice (polígono degenerado)
        return new Point(vertices[0]);
    }

    /**
     * Dibuja una línea usando el algoritmo de Bresenham
     */
    private void dibujarLineaBresenham(int x0, int y0, int x1, int y1) {

        int dx = Math.abs(x0 - x1);
        int dy = Math.abs(y0 - y1);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;

        int x = x0;
        int y = y0;

        while (true) {
            if (dentro(x, y)) {

                matriz[y][x] = id;
            }

            if (x == x1 && y == y1) {
                break;
            }

            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x += sx;
            }
            if (e2 < dx) {
                err += dx;
                y += sy;
            }
        }
    }

    /**
     * Rellena el interior del polígono usando DFS desde un punto semilla
     *
     * @param seedX punto interior X
     * @param seedY punto interior Y
     * @param historialPosiciones historial de objetos anteriores
     */
    public void rellenarConDFS(int seedX, int seedY, Map<Byte, Map<Point, Byte>> historialPosiciones) {
        // Convertir a coordenadas de matriz
        int x = seedX;
        int y = seedY;

        // Inicializar historial para este ID si no existe
        if (!historialPosiciones.containsKey(id)) {
            historialPosiciones.put(id, new HashMap<>());
        }

        // Usar un Stack para DFS iterativo
        Stack<Point> stack = new Stack<>();
        Set<Point> visitados = new HashSet<>();

        stack.push(new Point(x, y));

        int celdasDibujadas = 0;

        while (!stack.isEmpty()) {
            Point p = stack.pop();

            // Si ya lo visitamos, continuar
            if (visitados.contains(p)) {
                continue;
            }

            // Si está fuera de límites, continuar
            if (!dentro(p.x, p.y)) {
                continue;
            }

            // Si ya tiene el ID que queremos, continuar (evita sobrescribir)
            if (matriz[p.y][p.x] == id) {
                visitados.add(p);
                continue;
            }

            // Marcar como visitado
            visitados.add(p);

            // Guardar el ID anterior
            byte idActual = matriz[p.y][p.x];
            if (idActual != 0 && idActual != id) {
                historialPosiciones.get(id).put(new Point(p.x, p.y), idActual);
            }

            // Escribir el nuevo ID
            matriz[p.y][p.x] = id;
            celdasDibujadas++;

            // Agregar vecinos (4-conectividad: arriba, abajo, izq, der)
            stack.push(new Point(p.x + 1, p.y));
            stack.push(new Point(p.x - 1, p.y));
            stack.push(new Point(p.x, p.y + 1));
            stack.push(new Point(p.x, p.y - 1));
        }
    }

    private boolean dentro(int x, int y) {
        return x >= 0 && y >= 0 && x < witdh && y < height;
    }


    //PUNTO DENTRO DE UN POLIGONO: Ray Casting
    private boolean pointInPolygon(int x, int y) {
        boolean inside = false;
        int n = vertices.length;

        for (int i = 0, j = n - 1; i < n; j = i++) {
            int xi = vertices[i].x, yi = vertices[i].y;
            int xj = vertices[j].x, yj = vertices[j].y;

            boolean intersect = ((yi > y) != (yj > y))
                    && (x < (double) (xj - xi) * (y - yi) / (double) (yj - yi) + xi);
            if (intersect) {
                inside = !inside;
            }
        }

        return inside;
    }


    /**
     * Limpia usando Flood Fill con DFS (Depth-First Search)
     *
     * @param seedX Coordenada X semilla (punto dentro del polígono, en
     * coordenadas de pantalla)
     * @param seedY Coordenada Y semilla (punto dentro del polígono, en
     * coordenadas de pantalla)
     * @param historialPosiciones evaluar posiciones anteiores
     */
    public void limpiarFloodFillDFS(int seedX, int seedY, Map<Byte, Map<Point, Byte>> historialPosiciones) {
        // Convertir coordenadas de pantalla a matriz
        int x = seedX;
        int y = seedY;

        // Verificar que la celda semilla tiene el ID correcto
        if (matriz[y][x] != id) {
    //        System.err.println("ERROR DFS: Semilla no contiene ID esperado. Esperado: " + id
    //                + ", Actual: " + matriz[y][x] + " en (" + x + "," + y + ")");
            return;
        }

        // Usar un Stack para DFS iterativo (evitar stack overflow)
        Stack<Point> stack = new Stack<>();
        Set<Point> visitados = new HashSet<>();

        stack.push(new Point(x, y));

        int celdasLimpiadas = 0;

        while (!stack.isEmpty()) {
            Point p = stack.pop();

            // Si ya lo visitamos, continuar
            if (visitados.contains(p)) {
                continue;
            }

            // Si está fuera de límites, continuar
            if (!dentro(p.x, p.y)) {
                continue;
            }

            // Si no tiene el ID que buscamos, continuar
            if (matriz[p.y][p.x] != id) {
                continue;
            }

            // Marcar como visitado
            visitados.add(p);

            byte idAnterior = 0;
            Point key = new Point(p.x, p.y);
            if (historialPosiciones.containsKey(id)
                    && historialPosiciones.get(id).containsKey(key)) {
                idAnterior = historialPosiciones.get(id).get(key);
            }

            // Escribir el ID anterior (limpiar)
            matriz[p.y][p.x] = idAnterior;
            celdasLimpiadas++;

            // Agregar vecinos (4-conectividad: arriba, abajo, izq, der)
            stack.push(new Point(p.x + 1, p.y));
            stack.push(new Point(p.x - 1, p.y));
            stack.push(new Point(p.x, p.y + 1));
            stack.push(new Point(p.x, p.y - 1));
        }

        // Limpiar el historial de este ID
        historialPosiciones.remove(id);

    }
}
