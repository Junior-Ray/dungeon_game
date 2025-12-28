package com.dungeon_game.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 * Panel visualizador de matriz 128x72 con hitbox de personaje
 * Escala: 10 píxeles por celda
 */
public class MatrizVisualizerPanel extends JPanel {
    
    // Dimensiones
    private static final int ANCHO_MATRIZ = 128;
    private static final int ALTO_MATRIZ = 72;
    private static final int ESCALA = 10;
    private static final int ANCHO_PANEL = ANCHO_MATRIZ * ESCALA; // 1280
    private static final int ALTO_PANEL = ALTO_MATRIZ * ESCALA;   // 720
    private static final int ANCHO_LISTA_VERTICES = 250; // Ancho del panel lateral
    private static final int ANCHO_TOTAL = ANCHO_PANEL + ANCHO_LISTA_VERTICES; // 1530
    
    // Lista de imágenes
    private List<BufferedImage> imagenes;
    private List<String> nombresImagenes;
    private List<Dimension> dimensionesImagenes; // Dimensión deseada para cada imagen
    private int indiceImagenActual = 0;
    
    // Vértices del polígono (hitbox) - guardados por índice de imagen
    private Map<Integer, List<Point>> verticesPorImagen;
    
    // Hitbox secundaria (ataque/espada) - visual, no editable
    private Map<Integer, List<Point>> hitboxSecundariaPorImagen;
    
    // Información de hover
    private Point puntoHover = null;
    private Point verticeHoverRelativo = null; // Coordenadas relativas a la imagen
    
    // Modo de edición
    private boolean modoEdicionVertices = true;
    
    public MatrizVisualizerPanel() {
        setPreferredSize(new Dimension(ANCHO_TOTAL, ALTO_PANEL));
        setBackground(Color.BLACK);
        setFocusable(true);
        
        imagenes = new ArrayList<>();
        nombresImagenes = new ArrayList<>();
        dimensionesImagenes = new ArrayList<>();
        verticesPorImagen = new HashMap<>();
        hitboxSecundariaPorImagen = new HashMap<>();
        
        // Agregar listeners
        agregarListeners();
        
        // Cargar imágenes de ejemplo (puedes modificar esto)
        cargarImagenesEjemplo();
    }
    
    private void cargarImagenesEjemplo() {
        // Crear imágenes de ejemplo
        for (int i = 0; i < 3; i++) {
            BufferedImage img = new BufferedImage(400, 400, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            g.setColor(new Color(100 + i * 50, 150, 200 - i * 30, 180));
            g.fillOval(50, 50, 300, 300);
            g.dispose();
            imagenes.add(img);
            nombresImagenes.add("Ejemplo " + (i + 1));
            dimensionesImagenes.add(new Dimension(400, 400)); // Tamaño por defecto
            verticesPorImagen.put(i, new ArrayList<>());
        }
    }
    
    /**
     * Método para cargar imágenes desde archivos externos
     * @param rutasImagenes Lista de rutas de archivos
     * @param dimensiones Lista de dimensiones deseadas (ancho, alto) para cada imagen. Si es null, usa tamaño original
     */
    public void cargarImagenes(List<String> rutasImagenes, List<Dimension> dimensiones) {
        imagenes.clear();
        nombresImagenes.clear();
        dimensionesImagenes.clear();
        verticesPorImagen.clear();
        
        for (int i = 0; i < rutasImagenes.size(); i++) {
            String ruta = rutasImagenes.get(i);
            try {
                BufferedImage img = ImageIO.read(new File(ruta));
                imagenes.add(img);
                nombresImagenes.add(new File(ruta).getName());
                
                // Si se proporciona dimensión específica, usarla; si no, usar tamaño original de la imagen
                if (dimensiones != null && i < dimensiones.size() && dimensiones.get(i) != null) {
                    dimensionesImagenes.add(dimensiones.get(i));
                } else {
                    // Usar tamaño original de la imagen
                    dimensionesImagenes.add(new Dimension(img.getWidth(), img.getHeight()));
                }
                
                verticesPorImagen.put(i, new ArrayList<>());
                hitboxSecundariaPorImagen.put(i, new ArrayList<>());
            } catch (IOException e) {
                System.err.println("Error cargando imagen: " + ruta);
                e.printStackTrace();
            }
        }
        indiceImagenActual = 0;
        repaint();
    }
    
    /**
     * Sobrecarga: Cargar imágenes usando sus dimensiones originales
     */
    public void cargarImagenes(List<String> rutasImagenes) {
        cargarImagenes(rutasImagenes, null);
    }
    
    /**
     * Método para cargar imágenes desde recursos (carpeta resources)
     * @param rutasRecursos Lista de rutas relativas desde resources
     * @param dimensiones Lista de dimensiones deseadas para cada imagen. Si es null, usa tamaño original
     */
    public void cargarImagenesDesdeRecursos(List<String> rutasRecursos, List<Dimension> dimensiones) {
        imagenes.clear();
        nombresImagenes.clear();
        dimensionesImagenes.clear();
        verticesPorImagen.clear();
        
        for (int i = 0; i < rutasRecursos.size(); i++) {
            String ruta = rutasRecursos.get(i);
            try {
                BufferedImage img = ImageIO.read(getClass().getClassLoader().getResourceAsStream(ruta));
                if (img != null) {
                    imagenes.add(img);
                    String nombre = ruta.substring(ruta.lastIndexOf('/') + 1);
                    nombresImagenes.add(nombre);
                    
                    // Si se proporciona dimensión específica, usarla; si no, usar tamaño original de la imagen
                    if (dimensiones != null && i < dimensiones.size() && dimensiones.get(i) != null) {
                        dimensionesImagenes.add(dimensiones.get(i));
                    } else {
                        // Usar tamaño original de la imagen
                        dimensionesImagenes.add(new Dimension(img.getWidth(), img.getHeight()));
                    }
                    
                    verticesPorImagen.put(i, new ArrayList<>());
                    hitboxSecundariaPorImagen.put(i, new ArrayList<>());
                } else {
                    System.err.println("No se pudo cargar el recurso: " + ruta);
                }
            } catch (IOException e) {
                System.err.println("Error cargando recurso: " + ruta);
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                System.err.println("Recurso no encontrado: " + ruta);
                e.printStackTrace();
            }
        }
        indiceImagenActual = 0;
        repaint();
    }
    
    /**
     * Sobrecarga: Cargar imágenes desde recursos usando sus dimensiones originales
     */
    public void cargarImagenesDesdeRecursos(List<String> rutasRecursos) {
        cargarImagenesDesdeRecursos(rutasRecursos, null);
    }
    
    /**
     * Método alternativo para cargar una sola imagen desde recursos
     * @param rutaRecurso Ruta del recurso
     * @param dimension Dimensión deseada. Si es null, usa tamaño original
     */
    public void cargarImagenRecurso(String rutaRecurso, Dimension dimension) {
        try {
            BufferedImage img = ImageIO.read(getClass().getClassLoader().getResourceAsStream(rutaRecurso));
            if (img != null) {
                int indice = imagenes.size();
                imagenes.add(img);
                String nombre = rutaRecurso.substring(rutaRecurso.lastIndexOf('/') + 1);
                nombresImagenes.add(nombre);
                
                if (dimension != null) {
                    dimensionesImagenes.add(dimension);
                } else {
                    // Usar tamaño original de la imagen
                    dimensionesImagenes.add(new Dimension(img.getWidth(), img.getHeight()));
                }
                
                verticesPorImagen.put(indice, new ArrayList<>());
                hitboxSecundariaPorImagen.put(indice, new ArrayList<>());
                repaint();
            }
        } catch (IOException e) {
            System.err.println("Error cargando recurso: " + rutaRecurso);
            e.printStackTrace();
        }
    }
    
    /**
     * Sobrecarga: Cargar una imagen usando su tamaño original
     */
    public void cargarImagenRecurso(String rutaRecurso) {
        cargarImagenRecurso(rutaRecurso, null);
    }
    
    /**
     * Establece la dimensión de visualización para una imagen específica
     */
    public void setDimensionImagen(int indice, Dimension dimension) {
        if (indice >= 0 && indice < dimensionesImagenes.size()) {
            dimensionesImagenes.set(indice, dimension);
            repaint();
        }
    }
    
    /**
     * Obtiene la dimensión de visualización de una imagen específica
     */
    public Dimension getDimensionImagen(int indice) {
        if (indice >= 0 && indice < dimensionesImagenes.size()) {
            return dimensionesImagenes.get(indice);
        }
        return null;
    }
    
    /**
     * Obtiene la dimensión original de la imagen (sin escalar)
     */
    public Dimension getDimensionOriginalImagen(int indice) {
        if (indice >= 0 && indice < imagenes.size()) {
            BufferedImage img = imagenes.get(indice);
            return new Dimension(img.getWidth(), img.getHeight());
        }
        return null;
    }
    
    /**
     * Restaura una imagen a su tamaño original
     */
    public void restaurarTamañoOriginal(int indice) {
        if (indice >= 0 && indice < imagenes.size() && indice < dimensionesImagenes.size()) {
            BufferedImage img = imagenes.get(indice);
            dimensionesImagenes.set(indice, new Dimension(img.getWidth(), img.getHeight()));
            repaint();
        }
    }
    
    private void agregarListeners() {
        // Mouse Motion Listener para hover
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                // Solo considerar hover en el área de la matriz (no en el panel de lista)
                if (e.getX() >= ANCHO_PANEL) {
                    puntoHover = null;
                    verticeHoverRelativo = null;
                    repaint();
                    return;
                }
                
                // Convertir coordenadas de píxel a matriz
                int matrizX = e.getX() / ESCALA;
                int matrizY = e.getY() / ESCALA;
                
                if (matrizX >= 0 && matrizX < ANCHO_MATRIZ && matrizY >= 0 && matrizY < ALTO_MATRIZ) {
                    puntoHover = new Point(matrizX, matrizY);
                    
                    // Calcular coordenadas relativas a la imagen actual
                    if (!imagenes.isEmpty() && indiceImagenActual < imagenes.size()) {
                        Dimension dimDeseada = dimensionesImagenes.get(indiceImagenActual);
                        int imgX = (ANCHO_PANEL - dimDeseada.width) / 2;
                        int imgY = (ALTO_PANEL - dimDeseada.height) / 2;
                        
                        // Convertir de píxel a coordenadas de imagen
                        int relX = e.getX() - imgX;
                        int relY = e.getY() - imgY;
                        
                        // Si está dentro de la imagen, calcular coordenadas de matriz relativas
                        if (relX >= 0 && relX < dimDeseada.width && relY >= 0 && relY < dimDeseada.height) {
                            int matrizRelX = relX / ESCALA;
                            int matrizRelY = relY / ESCALA;
                            verticeHoverRelativo = new Point(matrizRelX, matrizRelY);
                        } else {
                            verticeHoverRelativo = null;
                        }
                    } else {
                        verticeHoverRelativo = null;
                    }
                } else {
                    puntoHover = null;
                    verticeHoverRelativo = null;
                }
                repaint();
            }
        });
        
        // Mouse Listener para agregar vértices
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Solo procesar clicks en el área de la matriz
                if (e.getX() >= ANCHO_PANEL) {
                    return;
                }
                
                if (modoEdicionVertices && e.getButton() == MouseEvent.BUTTON1) {
                    // Click izquierdo: agregar vértice a la imagen actual
                    if (!imagenes.isEmpty() && indiceImagenActual < imagenes.size()) {
                        // Obtener dimensión y posición de la imagen actual
                        Dimension dimDeseada = dimensionesImagenes.get(indiceImagenActual);
                        int imgX = (ANCHO_PANEL - dimDeseada.width) / 2;
                        int imgY = (ALTO_PANEL - dimDeseada.height) / 2;
                        
                        // Convertir coordenadas de pantalla a coordenadas relativas a la imagen
                        int relX = e.getX() - imgX;
                        int relY = e.getY() - imgY;
                        
                        // Verificar que el click está dentro de la imagen
                        if (relX >= 0 && relX < dimDeseada.width && relY >= 0 && relY < dimDeseada.height) {
                            // Convertir a coordenadas de matriz relativas a la imagen
                            int matrizX = relX / ESCALA;
                            int matrizY = relY / ESCALA;
                            
                            List<Point> verticesActuales = verticesPorImagen.get(indiceImagenActual);
                            verticesActuales.add(new Point(matrizX, matrizY));
                            repaint();
                        }
                    }
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    // Click derecho: eliminar último vértice de la imagen actual
                    List<Point> verticesActuales = verticesPorImagen.get(indiceImagenActual);
                    if (verticesActuales != null && !verticesActuales.isEmpty()) {
                        verticesActuales.remove(verticesActuales.size() - 1);
                        repaint();
                    }
                }
            }
        });
        
        // Key Listener para cambiar imágenes
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (imagenes.isEmpty()) return;
                
                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    // Flecha izquierda: imagen anterior
                    indiceImagenActual--;
                    if (indiceImagenActual < 0) {
                        indiceImagenActual = imagenes.size() - 1;
                    }
                    repaint();
                } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    // Flecha derecha: imagen siguiente
                    indiceImagenActual++;
                    if (indiceImagenActual >= imagenes.size()) {
                        indiceImagenActual = 0;
                    }
                    repaint();
                } else if (e.getKeyCode() == KeyEvent.VK_C) {
                    // C: limpiar vértices de la imagen actual
                    List<Point> verticesActuales = verticesPorImagen.get(indiceImagenActual);
                    verticesActuales.clear();
                    repaint();
                } else if (e.getKeyCode() == KeyEvent.VK_A) {
                    // A: limpiar TODOS los vértices de todas las imágenes
                    for (List<Point> verts : verticesPorImagen.values()) {
                        verts.clear();
                    }
                    repaint();
                } else if (e.getKeyCode() == KeyEvent.VK_E) {
                    // E: toggle modo edición
                    modoEdicionVertices = !modoEdicionVertices;
                    repaint();
                }
            }
        });
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 1. Dibujar imagen actual centrada con la dimensión especificada
        if (!imagenes.isEmpty() && indiceImagenActual < imagenes.size()) {
            BufferedImage imgActual = imagenes.get(indiceImagenActual);
            Dimension dimDeseada = dimensionesImagenes.get(indiceImagenActual);
            
            // Calcular posición centrada
            int x = (ANCHO_PANEL - dimDeseada.width) / 2;
            int y = (ALTO_PANEL - dimDeseada.height) / 2;
            
            // Dibujar imagen escalada a la dimensión deseada
            g2d.drawImage(imgActual, x, y, dimDeseada.width, dimDeseada.height, null);
            
            // Dibujar borde de la imagen para referencia (opcional)
            g2d.setColor(new Color(0, 255, 0, 100));
            g2d.setStroke(new BasicStroke(1));
            g2d.drawRect(x, y, dimDeseada.width, dimDeseada.height);
        }
        
        // 2. Dibujar grilla (opcional, semi-transparente)
        g2d.setColor(new Color(100, 100, 100, 30));
        for (int x = 0; x <= ANCHO_MATRIZ; x++) {
            g2d.drawLine(x * ESCALA, 0, x * ESCALA, ALTO_PANEL);
        }
        for (int y = 0; y <= ALTO_MATRIZ; y++) {
            g2d.drawLine(0, y * ESCALA, ANCHO_PANEL, y * ESCALA);
        }
        
        // 3. Dibujar polígono (hitbox) de la imagen actual
        List<Point> verticesActuales = verticesPorImagen.get(indiceImagenActual);
        if (verticesActuales != null && verticesActuales.size() > 1 && !imagenes.isEmpty()) {
            Dimension dimDeseada = dimensionesImagenes.get(indiceImagenActual);
            int imgX = (ANCHO_PANEL - dimDeseada.width) / 2;
            int imgY = (ALTO_PANEL - dimDeseada.height) / 2;
            
            // Líneas del polígono
            g2d.setColor(new Color(255, 0, 0, 200));
            g2d.setStroke(new BasicStroke(2));
            for (int i = 0; i < verticesActuales.size(); i++) {
                Point p1 = verticesActuales.get(i);
                Point p2 = verticesActuales.get((i + 1) % verticesActuales.size());
                
                // Convertir coordenadas relativas a la imagen a coordenadas absolutas
                int x1 = imgX + p1.x * ESCALA + ESCALA / 2;
                int y1 = imgY + p1.y * ESCALA + ESCALA / 2;
                int x2 = imgX + p2.x * ESCALA + ESCALA / 2;
                int y2 = imgY + p2.y * ESCALA + ESCALA / 2;
                
                g2d.drawLine(x1, y1, x2, y2);
            }
            
            // Rellenar polígono semi-transparente
            if (verticesActuales.size() > 2) {
                int[] xPoints = new int[verticesActuales.size()];
                int[] yPoints = new int[verticesActuales.size()];
                for (int i = 0; i < verticesActuales.size(); i++) {
                    Point p = verticesActuales.get(i);
                    xPoints[i] = imgX + p.x * ESCALA + ESCALA / 2;
                    yPoints[i] = imgY + p.y * ESCALA + ESCALA / 2;
                }
                g2d.setColor(new Color(255, 0, 0, 50));
                g2d.fillPolygon(xPoints, yPoints, verticesActuales.size());
            }
        }
        
        // 4. Dibujar vértices de la imagen actual
        g2d.setColor(Color.YELLOW);
        if (verticesActuales != null && !imagenes.isEmpty()) {
            Dimension dimDeseada = dimensionesImagenes.get(indiceImagenActual);
            int imgX = (ANCHO_PANEL - dimDeseada.width) / 2;
            int imgY = (ALTO_PANEL - dimDeseada.height) / 2;
            
            for (int i = 0; i < verticesActuales.size(); i++) {
                Point v = verticesActuales.get(i);
                int x = imgX + v.x * ESCALA + ESCALA / 2;
                int y = imgY + v.y * ESCALA + ESCALA / 2;
                
                g2d.fillOval(x - 4, y - 4, 8, 8);
                
                // Dibujar número del vértice
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 10));
                g2d.drawString(String.valueOf(i), x + 8, y - 8);
                g2d.setColor(Color.YELLOW);
            }
        }
        
        // 5. Destacar celda hover
        if (puntoHover != null) {
            g2d.setColor(new Color(255, 255, 0, 100));
            g2d.fillRect(
                puntoHover.x * ESCALA,
                puntoHover.y * ESCALA,
                ESCALA, ESCALA
            );
        }
        
        // 6. Dibujar hitbox secundaria (ataque/espada) - Solo visual
        List<Point> hitboxSecundaria = hitboxSecundariaPorImagen.get(indiceImagenActual);
        if (hitboxSecundaria != null && hitboxSecundaria.size() > 1 && !imagenes.isEmpty()) {
            Dimension dimDeseada = dimensionesImagenes.get(indiceImagenActual);
            int imgX = (ANCHO_PANEL - dimDeseada.width) / 2;
            int imgY = (ALTO_PANEL - dimDeseada.height) / 2;
            
            // Líneas del polígono secundario (color azul/cyan)
            g2d.setColor(new Color(0, 200, 255, 200));
            g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{5, 5}, 0)); // Línea punteada
            for (int i = 0; i < hitboxSecundaria.size(); i++) {
                Point p1 = hitboxSecundaria.get(i);
                Point p2 = hitboxSecundaria.get((i + 1) % hitboxSecundaria.size());
                
                int x1 = imgX + p1.x * ESCALA + ESCALA / 2;
                int y1 = imgY + p1.y * ESCALA + ESCALA / 2;
                int x2 = imgX + p2.x * ESCALA + ESCALA / 2;
                int y2 = imgY + p2.y * ESCALA + ESCALA / 2;
                
                g2d.drawLine(x1, y1, x2, y2);
            }
            
            // Rellenar polígono secundario semi-transparente
            if (hitboxSecundaria.size() > 2) {
                int[] xPoints = new int[hitboxSecundaria.size()];
                int[] yPoints = new int[hitboxSecundaria.size()];
                for (int i = 0; i < hitboxSecundaria.size(); i++) {
                    Point p = hitboxSecundaria.get(i);
                    xPoints[i] = imgX + p.x * ESCALA + ESCALA / 2;
                    yPoints[i] = imgY + p.y * ESCALA + ESCALA / 2;
                }
                g2d.setColor(new Color(0, 200, 255, 30));
                g2d.fillPolygon(xPoints, yPoints, hitboxSecundaria.size());
            }
            
            // Dibujar vértices de hitbox secundaria (color cyan)
            g2d.setColor(new Color(0, 255, 255));
            for (int i = 0; i < hitboxSecundaria.size(); i++) {
                Point v = hitboxSecundaria.get(i);
                int x = imgX + v.x * ESCALA + ESCALA / 2;
                int y = imgY + v.y * ESCALA + ESCALA / 2;
                
                g2d.fillOval(x - 4, y - 4, 8, 8);
                
                // Dibujar número del vértice
                g2d.setColor(Color.CYAN);
                g2d.setFont(new Font("Arial", Font.BOLD, 10));
                g2d.drawString("A" + i, x + 8, y - 8);
                g2d.setColor(new Color(0, 255, 255));
            }
        }
        
        // 7. Dibujar información de hover
        if (puntoHover != null) {
            String info;
            if (verticeHoverRelativo != null) {
                // Mostrar coordenadas relativas a la imagen
                info = String.format("Imagen: (%d, %d) | Píxel: (%d, %d) | Matriz global: (%d, %d)",
                    verticeHoverRelativo.x, verticeHoverRelativo.y,
                    verticeHoverRelativo.x * ESCALA, verticeHoverRelativo.y * ESCALA,
                    puntoHover.x, puntoHover.y);
            } else {
                // Fuera de la imagen, solo coordenadas globales
                info = String.format("Matriz global: (%d, %d) | Píxel: (%d, %d)",
                    puntoHover.x, puntoHover.y,
                    puntoHover.x * ESCALA, puntoHover.y * ESCALA);
            }
            
            g2d.setColor(new Color(0, 0, 0, 180));
            g2d.fillRect(5, 5, 600, 25);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Monospaced", Font.BOLD, 12));
            g2d.drawString(info, 10, 22);
        }
        
        // 7. Información general
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRect(5, ALTO_PANEL - 105, 450, 100);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        String nombreImg = !nombresImagenes.isEmpty() ? nombresImagenes.get(indiceImagenActual) : "Sin nombre";
        verticesActuales = verticesPorImagen.get(indiceImagenActual);
        int numVertices = verticesActuales != null ? verticesActuales.size() : 0;
        Dimension dimActual = dimensionesImagenes.get(indiceImagenActual);
        
        g2d.drawString("Imagen [" + indiceImagenActual + "]: " + nombreImg + " (" + (indiceImagenActual + 1) + "/" + imagenes.size() + ")", 10, ALTO_PANEL - 85);
        g2d.drawString("Dimensión: " + dimActual.width + "x" + dimActual.height + " px | Vértices: " + numVertices, 10, ALTO_PANEL - 70);
        g2d.drawString("← / → : Cambiar imagen", 10, ALTO_PANEL - 50);
        g2d.drawString("Click: Agregar | RClick: Quitar último", 10, ALTO_PANEL - 35);
        g2d.drawString("Rojo: Hitbox | Cyan: Ataque (visual)", 10, ALTO_PANEL - 20);
        
        // 8. Panel lateral derecho con lista de vértices
        g2d.setColor(new Color(30, 30, 35));
        g2d.fillRect(ANCHO_PANEL, 0, ANCHO_LISTA_VERTICES, ALTO_PANEL);
        
        // Borde del panel lateral
        g2d.setColor(new Color(100, 100, 100));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(ANCHO_PANEL, 0, ANCHO_PANEL, ALTO_PANEL);
        
        // Título del panel lateral - Hitbox principal
        g2d.setColor(new Color(255, 100, 100));
        g2d.setFont(new Font("Monospaced", Font.BOLD, 14));
        g2d.drawString("HITBOX PRINCIPAL", ANCHO_PANEL + 10, 25);
        
        // Información de la imagen
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 11));
        g2d.setColor(new Color(150, 150, 150));
        if (!imagenes.isEmpty()) {
            dimActual = dimensionesImagenes.get(indiceImagenActual);
            int matrizAncho = dimActual.width / ESCALA;
            int matrizAlto = dimActual.height / ESCALA;
            g2d.drawString("Imagen: " + matrizAncho + "x" + matrizAlto + " celdas", ANCHO_PANEL + 10, 45);
        }
        
        // Lista de vértices hitbox principal
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 11));
        int yOffset = 65;
        
        if (verticesActuales != null && !verticesActuales.isEmpty()) {
            for (int i = 0; i < verticesActuales.size(); i++) {
                Point v = verticesActuales.get(i);
                
                // Alternar color de fondo
                if (i % 2 == 0) {
                    g2d.setColor(new Color(40, 40, 45));
                    g2d.fillRect(ANCHO_PANEL + 5, yOffset - 12, ANCHO_LISTA_VERTICES - 10, 16);
                }
                
                // Número del vértice
                g2d.setColor(Color.YELLOW);
                g2d.drawString("[" + i + "]", ANCHO_PANEL + 10, yOffset);
                
                // Coordenadas
                g2d.setColor(Color.WHITE);
                String coords = "(" + v.x + "," + v.y + ")";
                g2d.drawString(coords, ANCHO_PANEL + 45, yOffset);
                
                // Coordenadas en píxeles
                g2d.setColor(new Color(150, 150, 255));
                String pixels = "px:(" + (v.x * ESCALA) + "," + (v.y * ESCALA) + ")";
                g2d.drawString(pixels, ANCHO_PANEL + 120, yOffset);
                
                yOffset += 18;
                
                if (yOffset > ALTO_PANEL / 2 - 40) {
                    g2d.setColor(new Color(150, 150, 150));
                    g2d.drawString("... +" + (verticesActuales.size() - i - 1), ANCHO_PANEL + 10, yOffset);
                    break;
                }
            }
        } else {
            g2d.setColor(new Color(100, 100, 100));
            g2d.drawString("Sin vértices", ANCHO_PANEL + 10, yOffset);
        }
        
        // Separador
        yOffset = ALTO_PANEL / 2 + 10;
        g2d.setColor(new Color(100, 100, 100));
        g2d.drawLine(ANCHO_PANEL + 10, yOffset - 20, ANCHO_PANEL + ANCHO_LISTA_VERTICES - 10, yOffset - 20);
        
        // Título - Hitbox secundaria (ataque)
        g2d.setColor(new Color(100, 220, 255));
        g2d.setFont(new Font("Monospaced", Font.BOLD, 14));
        g2d.drawString("HITBOX ATAQUE", ANCHO_PANEL + 10, yOffset);
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 10));
        g2d.setColor(new Color(120, 120, 120));
        g2d.drawString("(solo visual)", ANCHO_PANEL + 10, yOffset + 15);
        
        // Lista de vértices hitbox secundaria
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 11));
        yOffset += 35;
        
        List<Point> hitboxSec = hitboxSecundariaPorImagen.get(indiceImagenActual);
        if (hitboxSec != null && !hitboxSec.isEmpty()) {
            for (int i = 0; i < hitboxSec.size(); i++) {
                Point v = hitboxSec.get(i);
                
                // Alternar color de fondo
                if (i % 2 == 0) {
                    g2d.setColor(new Color(40, 45, 50));
                    g2d.fillRect(ANCHO_PANEL + 5, yOffset - 12, ANCHO_LISTA_VERTICES - 10, 16);
                }
                
                // Número del vértice
                g2d.setColor(Color.CYAN);
                g2d.drawString("[A" + i + "]", ANCHO_PANEL + 10, yOffset);
                
                // Coordenadas
                g2d.setColor(Color.WHITE);
                String coords = "(" + v.x + "," + v.y + ")";
                g2d.drawString(coords, ANCHO_PANEL + 50, yOffset);
                
                // Coordenadas en píxeles
                g2d.setColor(new Color(150, 200, 255));
                String pixels = "px:(" + (v.x * ESCALA) + "," + (v.y * ESCALA) + ")";
                g2d.drawString(pixels, ANCHO_PANEL + 120, yOffset);
                
                yOffset += 18;
                
                if (yOffset > ALTO_PANEL - 30) {
                    g2d.setColor(new Color(150, 150, 150));
                    g2d.drawString("... +" + (hitboxSec.size() - i - 1), ANCHO_PANEL + 10, yOffset);
                    break;
                }
            }
        } else {
            g2d.setColor(new Color(100, 100, 100));
            g2d.drawString("Sin vértices", ANCHO_PANEL + 10, yOffset);
        }
    }
    
    /**
     * Obtiene los vértices de una imagen específica por índice
     */
    public Point[] getVerticesPorIndice(int indice) {
        List<Point> verts = verticesPorImagen.get(indice);
        if (verts != null) {
            return verts.toArray(new Point[0]);
        }
        return new Point[0];
    }
    
    /**
     * Obtiene los vértices de la imagen actual
     */
    public Point[] getVerticesActuales() {
        return getVerticesPorIndice(indiceImagenActual);
    }
    
    /**
     * Obtiene TODOS los vértices de todas las imágenes
     */
    public Map<Integer, Point[]> getTodosLosVertices() {
        Map<Integer, Point[]> resultado = new HashMap<>();
        for (Map.Entry<Integer, List<Point>> entry : verticesPorImagen.entrySet()) {
            resultado.put(entry.getKey(), entry.getValue().toArray(new Point[0]));
        }
        return resultado;
    }
    
    /**
     * Establece vértices para una imagen específica por índice
     */
    public void setVerticesPorIndice(int indice, Point[] nuevosVertices) {
        List<Point> verts = verticesPorImagen.get(indice);
        if (verts != null) {
            verts.clear();
            for (Point p : nuevosVertices) {
                verts.add(new Point(p));
            }
            repaint();
        }
    }
    
    /**
     * Establece vértices para la imagen actual
     */
    public void setVerticesActuales(Point[] nuevosVertices) {
        setVerticesPorIndice(indiceImagenActual, nuevosVertices);
    }
    
    /**
     * Limpia todos los vértices de una imagen específica
     */
    public void limpiarVerticesPorIndice(int indice) {
        List<Point> verts = verticesPorImagen.get(indice);
        if (verts != null) {
            verts.clear();
            repaint();
        }
    }
    
    /**
     * Limpia todos los vértices de la imagen actual
     */
    public void limpiarVerticesActuales() {
        limpiarVerticesPorIndice(indiceImagenActual);
    }
    
    /**
     * Limpia todos los vértices de todas las imágenes
     */
    public void limpiarTodosLosVertices() {
        for (List<Point> verts : verticesPorImagen.values()) {
            verts.clear();
        }
        repaint();
    }
    
    /**
     * Obtiene el índice de la imagen actual
     */
    public int getIndiceImagenActual() {
        return indiceImagenActual;
    }
    
    /**
     * Establece la imagen actual por índice
     */
    public void setIndiceImagenActual(int indice) {
        if (indice >= 0 && indice < imagenes.size()) {
            indiceImagenActual = indice;
            repaint();
        }
    }
    
    /**
     * Obtiene el número total de imágenes
     */
    public int getCantidadImagenes() {
        return imagenes.size();
    }
    
    /**
     * Obtiene el nombre de una imagen por índice
     */
    public String getNombreImagenPorIndice(int indice) {
        if (indice >= 0 && indice < nombresImagenes.size()) {
            return nombresImagenes.get(indice);
        }
        return null;
    }
    
    /**
     * Establece la hitbox secundaria (ataque) para una imagen específica - SOLO VISUAL
     */
    public void setHitboxSecundariaPorIndice(int indice, Point[] vertices) {
        List<Point> hitboxSec = hitboxSecundariaPorImagen.get(indice);
        if (hitboxSec != null) {
            hitboxSec.clear();
            for (Point p : vertices) {
                hitboxSec.add(new Point(p));
            }
            repaint();
        }
    }
    
    /**
     * Obtiene la hitbox secundaria de una imagen específica
     */
    public Point[] getHitboxSecundariaPorIndice(int indice) {
        List<Point> hitboxSec = hitboxSecundariaPorImagen.get(indice);
        if (hitboxSec != null) {
            return hitboxSec.toArray(new Point[0]);
        }
        return new Point[0];
    }
    
    /**
     * Limpia la hitbox secundaria de una imagen específica
     */
    public void limpiarHitboxSecundaria(int indice) {
        List<Point> hitboxSec = hitboxSecundariaPorImagen.get(indice);
        if (hitboxSec != null) {
            hitboxSec.clear();
            repaint();
        }
    }
    
    // Método main para testing
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Matriz Visualizer - Hitbox Editor");
            MatrizVisualizerPanel panel = new MatrizVisualizerPanel();
            
            // Ejemplo 1: Cargar imágenes usando sus dimensiones originales
            List<String> rutasRecursos = new ArrayList<>();
            // IDLE
            rutasRecursos.add("imagenes/jp/idle_01.png");
            rutasRecursos.add("imagenes/jp/idle_02.png");
            rutasRecursos.add("imagenes/jp/idle_03.png");
            
            // JUMP
            rutasRecursos.add("imagenes/jp/jump_01.png");
            rutasRecursos.add("imagenes/jp/jump_02.png");
            rutasRecursos.add("imagenes/jp/jump_03.png");
            
            // RUN
            rutasRecursos.add("imagenes/jp/run_01.png");
            rutasRecursos.add("imagenes/jp/run_02.png");
            rutasRecursos.add("imagenes/jp/run_03.png");
            rutasRecursos.add("imagenes/jp/run_04.png");
            rutasRecursos.add("imagenes/jp/run_05.png");
            rutasRecursos.add("imagenes/jp/run_06.png");
            
            //ATACK DASH
            rutasRecursos.add("imagenes/jp/attack_dash_01.png");
            rutasRecursos.add("imagenes/jp/attack_dash_02.png");
            rutasRecursos.add("imagenes/jp/attack_dash_03.png");
            rutasRecursos.add("imagenes/jp/attack_dash_04.png");
            rutasRecursos.add("imagenes/jp/attack_dash_05.png");
            rutasRecursos.add("imagenes/jp/attack_dash_06.png");
            rutasRecursos.add("imagenes/jp/attack_dash_07.png");
            
            //ATTACK 1
            rutasRecursos.add("imagenes/jp/attack1_01.png");
            rutasRecursos.add("imagenes/jp/attack1_02.png");
            rutasRecursos.add("imagenes/jp/attack1_03.png");
            rutasRecursos.add("imagenes/jp/attack1_04.png");
            rutasRecursos.add("imagenes/jp/attack1_05.png");
            rutasRecursos.add("imagenes/jp/attack1_06.png");
            
            //ATTACK 2
            rutasRecursos.add("imagenes/jp/attack2_01.png");
            rutasRecursos.add("imagenes/jp/attack2_02.png");
            rutasRecursos.add("imagenes/jp/attack2_03.png");
            rutasRecursos.add("imagenes/jp/attack2_04.png");
            rutasRecursos.add("imagenes/jp/attack2_05.png");
            rutasRecursos.add("imagenes/jp/attack2_06.png");
            
            
            
            // Opción A: Sin especificar dimensiones (usa tamaño original)
            panel.cargarImagenesDesdeRecursos(rutasRecursos);
            
            // Opción B: Con dimensiones personalizadas para algunas imágenes
//            List<Dimension> dimensiones = new ArrayList<>();
//            dimensiones.add(null);                         // img0: tamaño original
//            dimensiones.add(new Dimension(400, 450));      // img1: 400x450 personalizado
//            dimensiones.add(null);                         // img2: tamaño original
            // panel.cargarImagenesDesdeRecursos(rutasRecursos, dimensiones);
            
            // Ejemplo 2: Establecer vértices manualmente para cada imagen
            
            //IDLE
            // Vértices para imagen índice 0 
            Point[] verticesImg0 = {
                new Point(8, 3),
                new Point(10, 1),
                new Point(12, 3),
                new Point(15, 10),
                new Point(14,18),
                new Point(12, 18),
                new Point(12, 16),
                new Point(10, 16),
                new Point(10, 18),
                new Point(8,18)
            };
            panel.setVerticesPorIndice(0, verticesImg0);
            
            // Vértices para imagen índice 1 
            Point[] verticesImg1 = {
                new Point(8, 3),
                new Point(12, 2),
                new Point(15, 13),
                new Point(14,18),
                new Point(12, 18),
                new Point(12, 16),
                new Point(10, 16),
                new Point(10, 18),
                new Point(8,18)
            };
            panel.setVerticesPorIndice(1, verticesImg1);
            
            // Vértices para imagen índice 2 
            Point[] verticesImg2 = {
                new Point(8, 3),
                new Point(11, 2),
                new Point(15, 11),
                new Point(14,18),
                new Point(12, 18),
                new Point(12, 16),
                new Point(10, 16),
                new Point(10, 18),
                new Point(8,18)
            };
            panel.setVerticesPorIndice(2, verticesImg2);
            
            // JUMP
            
            // Vértices para imagen índice 3 
            Point[] verticesImg3 = {
                new Point(9, 2),
                new Point(12, 1),
                new Point(17, 12),
                new Point(14,16),
                new Point(14, 18),
                new Point(13, 19),
                new Point(12, 16),
                new Point(11, 15),
                new Point(10,16),
                new Point(9, 17),
                new Point(7, 13),
                new Point(9,9)
            };
            panel.setVerticesPorIndice(3, verticesImg3);
            
            // Vértices para imagen índice 4
            Point[] verticesImg4 = {
                new Point(8, 3),
                new Point(11, 1),
                new Point(18, 11),
                new Point(15,14),
                new Point(15, 17),
                new Point(13, 17),
                new Point(12, 15),
                new Point(11, 17),
                new Point(8,17),
                new Point(9,9)
            };
            panel.setVerticesPorIndice(4, verticesImg4);
            
            // Vértices para imagen índice 5
            Point[] verticesImg5 = {
                new Point(8, 4),
                new Point(10, 2),
                new Point(18, 9),
                new Point(17,11),
                new Point(15, 8),
                new Point(13, 10),
                new Point(14, 16),
                new Point(13, 17),
                new Point(11,14),
                new Point(11,18),
                new Point(9,19)
            };
            panel.setVerticesPorIndice(5, verticesImg5);
            
            // RUN
            
            // Vértices para imagen índice 6
            Point[] verticesImg6 = {
                new Point(2, 2),
                new Point(7, 1),
                new Point(10, 9),
                new Point(13, 14),
                new Point(7,15),
                new Point(6, 16),
                new Point(4,16)
            };
            panel.setVerticesPorIndice(6, verticesImg6);
            
            // Vértices para imagen índice 7
            Point[] verticesImg7 = {
                new Point(3, 3),
                new Point(6, 3),
                new Point(10, 10),
                new Point(10, 15),
                new Point(9,17),
                new Point(6, 17),
                new Point(7, 15),
                new Point(3,13)
            };
            panel.setVerticesPorIndice(7, verticesImg7);
            
            // Vértices para imagen índice 8
            Point[] verticesImg8 = {
                new Point(3, 2),
                new Point(7, 2),
                new Point(10, 9),
                new Point(11, 13),
                new Point(13,15),
                new Point(11, 17),
                new Point(10, 15),
                new Point(6,16),
                new Point(4,15)
            };
            panel.setVerticesPorIndice(8, verticesImg8);
            
            // Vértices para imagen índice 9
            Point[] verticesImg9 = {
                new Point(3, 2),
                new Point(7, 2),
                new Point(10, 8),
                new Point(11, 13),
                new Point(13,14),
                new Point(12, 16),
                new Point(10, 14),
                new Point(7,15),
                new Point(6,16),
                new Point(4,16)
            };
            panel.setVerticesPorIndice(9, verticesImg9);
            
            // Vértices para imagen índice 10
            Point[] verticesImg10 = {
                new Point(3, 4),
                new Point(6, 3),
                new Point(10, 10),
                new Point(10, 14),
                new Point(10,16),
                new Point(6, 17),
                new Point(3, 13)
            };
            panel.setVerticesPorIndice(10, verticesImg10);
            
            // Vértices para imagen índice 11
            Point[] verticesImg11 = {
                new Point(3, 2),
                new Point(6, 1),
                new Point(11, 9),
                new Point(11, 13),
                new Point(13,15),
                new Point(12, 17),
                new Point(10, 15),
                new Point(7,15),
                new Point(6,16),
                new Point(4,16),
                new Point(5,14),
                new Point(3,13)
            };
            panel.setVerticesPorIndice(11, verticesImg11);
            
            
            // ATTACK DASH
            
            // Vértices para imagen índice 12 
            Point[] verticesImg12 = {
                new Point(19, 4),
                new Point(22, 3),
                new Point(25, 6),
                new Point(28, 7),
                new Point(30, 10),
                new Point(28, 12),
                new Point(30, 16),
                new Point(28, 16),
                new Point(27, 14),
                new Point(23, 15),
                new Point(23, 16),
                new Point(20, 16)
            };
            panel.setVerticesPorIndice(12, verticesImg12);
            
            // Vértices para imagen índice 13 
            Point[] verticesImg13 = {
                new Point(20, 5),
                new Point(22, 3),
                new Point(25, 6),
                new Point(28, 7),
                new Point(30, 10),
                new Point(28, 12),
                new Point(30, 16),
                new Point(28, 16),
                new Point(27, 14),
                new Point(23, 15),
                new Point(23, 16),
                new Point(20, 16)
            };
            panel.setVerticesPorIndice(13, verticesImg13);
            
            // Vértices para imagen índice 14 
            Point[] verticesImg14 = {
                new Point(12, 3),
                new Point(15, 3),
                new Point(17, 6),
                new Point(23,6),
                new Point(25, 9),
                new Point(23,12),
                new Point(25, 14),
                new Point(25, 15),
                new Point(21, 15),
                new Point(13, 9),
                new Point(7,10),
                new Point(5, 8),
                new Point(11, 7)
            };
            panel.setVerticesPorIndice(14, verticesImg14);
            
            Point[] ataque14 = {
                new Point(4, 8),
                new Point(1, 10),
                new Point(1, 13),
                new Point(4,16),
                new Point(9, 17),
                new Point(13,18),
                new Point(6, 15),
                new Point(4, 12),
                new Point(6, 10)
            };
            panel.setHitboxSecundariaPorIndice(14, ataque14);
            
            
            // Vértices para imagen índice 15 
            Point[] verticesImg15 = {
                new Point(12, 3),
                new Point(15, 3),
                new Point(17, 6),
                new Point(23,6),
                new Point(25, 9),
                new Point(23,12),
                new Point(25, 14),
                new Point(25, 15),
                new Point(21, 15),
                new Point(13, 9),
                new Point(7,9),
                new Point(5, 7),
                new Point(11, 7)
            };
            panel.setVerticesPorIndice(15, verticesImg15);
            
            Point[] ataque15 = {
                new Point(4, 7),
                new Point(0, 10),
                new Point(0, 13),
                new Point(2,16),
                new Point(8, 18),
                new Point(3,14),
                new Point(3, 11),
                new Point(6, 9)
            };
            panel.setHitboxSecundariaPorIndice(15, ataque15);
            
            // Vértices para imagen índice 16 
            Point[] verticesImg16 = {
                new Point(12, 3),
                new Point(15, 3),
                new Point(17, 6),
                new Point(23,6),
                new Point(25, 9),
                new Point(23,12),
                new Point(25, 14),
                new Point(24, 15),
                new Point(21, 15),
                new Point(13, 9),
                new Point(8,11),
                new Point(5, 8),
                new Point(11, 7)
            };
            panel.setVerticesPorIndice(16, verticesImg16);
            
            Point[] ataque16 = {
                new Point(5, 8),
                new Point(0, 8),
                new Point(0, 12),
                new Point(2,14),
                new Point(2, 11),
                new Point(5,10)
            };
            panel.setHitboxSecundariaPorIndice(16, ataque16);
            
            // Vértices para imagen índice 17 
            Point[] verticesImg17 = {
                new Point(10, 3),
                new Point(13, 2),
                new Point(22, 15),
                new Point(21,16),
                new Point(19, 16),
                new Point(18, 15),
                new Point(14, 14),
                new Point(14, 16),
                new Point(11,16),
                new Point(10, 11),
                new Point(12, 9)
            };
            panel.setVerticesPorIndice(17, verticesImg17);
            
            Point[] ataque17 = {
                new Point(11, 9),
                new Point(9, 10),
                new Point(10, 12),
                new Point(12,10)
            };
            panel.setHitboxSecundariaPorIndice(17, ataque17);
            
            // Vértices para imagen índice 18 
            Point[] verticesImg18 = {
                new Point(11, 5),
                new Point(13, 2),
                new Point(22, 15),
                new Point(21,16),
                new Point(19, 16),
                new Point(18, 15),
                new Point(14, 14),
                new Point(14, 16),
                new Point(11,16),
                new Point(11, 14),
                new Point(13, 9)
            };
            panel.setVerticesPorIndice(18, verticesImg18);
            
            // Vértices para imagen índice 19 
            Point[] verticesImg19 = {
                new Point(21, 3),
                new Point(24, 2),
                new Point(27, 6),
                new Point(28,9),
                new Point(27, 18),
                new Point(25, 19),
                new Point(24, 17),
                new Point(22, 17),
                new Point(21,18),
                new Point(19, 18),
                new Point(20, 13),
                new Point(18,11),
                new Point(21,8)
            };
            panel.setVerticesPorIndice(19, verticesImg19);
            
            // Vértices para imagen índice 20 
            Point[] verticesImg20 = {
                new Point(13, 4),
                new Point(21, 5),
                new Point(22, 8),
                new Point(26, 9),
                new Point(28, 13),
                new Point(27, 18),
                new Point(25, 18),
                new Point(22, 17),
                new Point(20, 17),
                new Point(20, 18),
                new Point(17, 18),
                new Point(17, 16),
                new Point(20,13)
            };
            panel.setVerticesPorIndice(20, verticesImg20);
            
            Point[] ataque20 = {
                new Point(0, 1),
                new Point(1, 11),
                new Point(6, 17),
                new Point(11, 19),
                new Point(17, 19),
                new Point(25, 15),
                new Point(15, 14),
                new Point(10, 11),
                new Point(9, 5),
                new Point(12, 6),
                new Point(12, 4)
            };
            panel.setHitboxSecundariaPorIndice(20, ataque20);
            
            // Vértices para imagen índice 21 
            Point[] verticesImg21 = {
                new Point(17, 5),
                new Point(21, 4),
                new Point(22, 9),
                new Point(26, 8),
                new Point(28, 18),
                new Point(25, 18),
                new Point(24, 17),
                new Point(20, 17),
                new Point(20, 18),
                new Point(18, 18),
                new Point(19, 13)
            };
            panel.setVerticesPorIndice(21, verticesImg21);
            
            Point[] ataque21 = {
                new Point(0, 1),
                new Point(0, 8),
                new Point(7, 15),
                new Point(14, 15),
                new Point(9, 6),
                new Point(13, 6),
                new Point(13, 5)
            };
            panel.setHitboxSecundariaPorIndice(21, ataque21);
            
            // Vértices para imagen índice 22 
            Point[] verticesImg22 = {
                new Point(17, 5),
                new Point(21, 4),
                new Point(22, 9),
                new Point(26, 8),
                new Point(28, 18),
                new Point(25, 18),
                new Point(24, 17),
                new Point(20, 17),
                new Point(20, 18),
                new Point(18, 18),
                new Point(19, 13)
            };
            panel.setVerticesPorIndice(22, verticesImg22);
            
            Point[] ataque22 = {
                new Point(0, 1),
                new Point(13, 5),
                new Point(13, 7),
                new Point(2, 4)
            };
            panel.setHitboxSecundariaPorIndice(22, ataque22);
            
            // Vértices para imagen índice 23
            Point[] verticesImg23 = {
                new Point(17, 5),
                new Point(21, 4),
                new Point(22, 9),
                new Point(26, 8),
                new Point(28, 18),
                new Point(25, 18),
                new Point(24, 17),
                new Point(20, 17),
                new Point(20, 18),
                new Point(18, 18),
                new Point(19, 13)
            };
            panel.setVerticesPorIndice(23, verticesImg23);
            
            Point[] ataque23 = {
                new Point(0, 1),
                new Point(13, 5),
                new Point(13, 7),
                new Point(2, 4)
            };
            panel.setHitboxSecundariaPorIndice(23, ataque23);
            
            // Vértices para imagen índice 24
            Point[] verticesImg24 = {
                new Point(17, 5),
                new Point(21, 4),
                new Point(22, 9),
                new Point(26, 8),
                new Point(28, 18),
                new Point(25, 18),
                new Point(24, 17),
                new Point(20, 17),
                new Point(20, 18),
                new Point(18, 18),
                new Point(19, 13)
            };
            panel.setVerticesPorIndice(24, verticesImg24);
            
            Point[] ataque24 = {
                new Point(0, 1),
                new Point(13, 5),
                new Point(13, 7),
                new Point(2, 4)
            };
            panel.setHitboxSecundariaPorIndice(24, ataque24);
            
            // Vértices para imagen índice 25
            Point[] verticesImg25 = {
                new Point(18, 3),
                new Point(22, 2),
                new Point(23, 6),
                new Point(28, 11),
                new Point(27, 17),
                new Point(25, 17),
                new Point(24, 16),
                new Point(21, 16),
                new Point(20, 17),
                new Point(18, 17),
                new Point(20, 12)
            };
            panel.setVerticesPorIndice(25, verticesImg25);
            
            // Vértices para imagen índice 26
            Point[] verticesImg26 = {
                new Point(20, 5),
                new Point(25, 3),
                new Point(27, 6),
                new Point(28, 9),
                new Point(28, 17),
                new Point(26, 17),
                new Point(25, 16),
                new Point(22, 16),
                new Point(21, 18),
                new Point(18, 18),
                new Point(20, 13)
            };
            panel.setVerticesPorIndice(26, verticesImg26);
            
            Point[] ataque26 = {
                new Point(2, 0),
                new Point(0, 7),
                new Point(1, 14),
                new Point(8, 20),
                new Point(20, 16),
                new Point(12, 10),
                new Point(11, 4)
            };
            panel.setHitboxSecundariaPorIndice(26, ataque26);
            
            // Vértices para imagen índice 27
            Point[] verticesImg27 = {
                new Point(20, 5),
                new Point(25, 3),
                new Point(27, 6),
                new Point(28, 9),
                new Point(28, 17),
                new Point(26, 17),
                new Point(25, 16),
                new Point(22, 16),
                new Point(21, 18),
                new Point(18, 18),
                new Point(20, 13)
            };
            panel.setVerticesPorIndice(27, verticesImg27);
            
            // Vértices para imagen índice 27
            Point[] verticesImg28 = {
                new Point(20, 5),
                new Point(25, 3),
                new Point(27, 6),
                new Point(28, 9),
                new Point(28, 17),
                new Point(26, 17),
                new Point(25, 16),
                new Point(22, 16),
                new Point(21, 18),
                new Point(18, 18),
                new Point(20, 13)
            };
            panel.setVerticesPorIndice(28, verticesImg28);
            
            // Vértices para imagen índice 27
            Point[] verticesImg29 = {
                new Point(20, 5),
                new Point(25, 3),
                new Point(27, 6),
                new Point(28, 9),
                new Point(28, 17),
                new Point(26, 17),
                new Point(25, 16),
                new Point(22, 16),
                new Point(21, 18),
                new Point(18, 18),
                new Point(20, 13)
            };
            panel.setVerticesPorIndice(29, verticesImg29);
            
            // Vértices para imagen índice 27
            Point[] verticesImg30 = {
                new Point(20, 5),
                new Point(25, 3),
                new Point(27, 6),
                new Point(28, 9),
                new Point(28, 17),
                new Point(26, 17),
                new Point(25, 16),
                new Point(22, 16),
                new Point(21, 18),
                new Point(18, 18),
                new Point(20, 13)
            };
            panel.setVerticesPorIndice(30, verticesImg30);
            
            frame.add(panel);
            frame.pack();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            
            panel.requestFocusInWindow();
            
            // Ejemplos de uso adicionales:
            // Dimension original = panel.getDimensionOriginalImagen(0);  // Tamaño real del archivo
            // Dimension actual = panel.getDimensionImagen(0);             // Tamaño de visualización
            // panel.restaurarTamañoOriginal(0);                           // Volver a tamaño original
            
            // Obtener vértices de una imagen específica
            // Point[] vertices = panel.getVerticesPorIndice(0);
            
            // Obtener todos los vértices de todas las imágenes
            // Map<Integer, Point[]> todosVertices = panel.getTodosLosVertices();
        });
    }
}