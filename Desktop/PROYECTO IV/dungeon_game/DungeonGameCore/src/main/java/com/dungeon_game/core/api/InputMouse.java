package com.dungeon_game.core.api;

import com.dungeon_game.core.components.AbstractUIComponent;
import com.dungeon_game.core.components.InputTextArea;
import com.dungeon_game.core.logic.UIFocus;
import java.awt.Point;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * API de mouse con soporte para clicks, posición y rueda (wheel)
 * 
 * @author GABRIEL SALGADO
 */
public class InputMouse {
    // Instancia del Singleton
    private static InputMouse instance;
    
    // Almacena la posición escalada del mouse de forma atómica
    private final AtomicReference<Point> mousePosicion = new AtomicReference<>(new Point(0, 0));
    
    // Almacena el estado de si el botón izquierdo está presionado (MouseDown)
    private final AtomicBoolean leftMouseDown = new AtomicBoolean(false);
    
    // Almacena el evento de click que se procesa una sola vez por update (ClickRelease)
    private final AtomicBoolean clickEventFired = new AtomicBoolean(false);
    
    // Almacena si el mouse está actualmente dentro de la ventana de la aplicación
    private final AtomicBoolean mouseInWindow = new AtomicBoolean(false);
    
    // NUEVO: Almacena la rotación de la rueda del mouse
    private final AtomicInteger wheelRotation = new AtomicInteger(0);
    
    /**
     * Factor de escalado (ej. 10 píxeles por unidad de Core/Hitbox)
     */
    private final int SCALE_FACTOR = 10; // Debe ser de 10
    
    // Constructor privado para el patrón Singleton
    private InputMouse() {
    }
    
    /**
     * Retorna la única instancia del InputMouse (Singleton).
     */
    public static InputMouse getInstance() {
        if (instance == null) {
            instance = new InputMouse();
        }
        return instance;
    }
    
    /**
     * Retorna el estado actual de si el botón izquierdo está presionado.
     */
    public boolean isLeftMouseDown() {
        return leftMouseDown.get();
    }
    
    /**
     * Retorna si el mouse está actualmente dentro de la ventana de la aplicación.
     */
    public boolean isMouseInWindow() {
        return mouseInWindow.get();
    }
    
    /**
     * Retorna la última posición del mouse escalada (coordenadas de Core).
     */
    public Point getMousePosition() {
        return mousePosicion.get();
    }
    
    /**
     * Retorna true si un evento de click (soltar botón) ha ocurrido desde la
     * última llamada a este método, y consume/resetea el evento inmediatamente.
     */
    public boolean consumeClickEvent() {
        return clickEventFired.compareAndSet(true, false);
    }
    
    /**
     * NUEVO: Consume la rotación de la rueda del mouse
     * @return Rotación acumulada (negativo = arriba, positivo = abajo)
     */
    public int consumeWheelRotation() {
        return wheelRotation.getAndSet(0);
    }
    
    /**
     * Actualiza el estado del mouse con datos brutos del Hilo de Eventos/Cliente.
     *
     * @param isMouseDown Estado del botón (presionado: true, soltado: false).
     * @param currentPos Posición real (no escalada) del mouse en píxeles.
     * @param isInWindow Indica si el cursor está dentro de los límites de la ventana.
     
     */
    public void update(boolean isMouseDown, Point currentPos, boolean isInWindow) {
        // 1. Manejar el estado de "presionado" y el evento de click
        if (leftMouseDown.get() && !isMouseDown) {
            clickEventFired.set(true);
        }
        leftMouseDown.set(isMouseDown);
        
        // 2. Almacenar el estado de si el mouse está en la ventana
        mouseInWindow.set(isInWindow);
        
        // 3. Calcular la posición escalada
        int x = currentPos.x / SCALE_FACTOR;
        int y = currentPos.y / SCALE_FACTOR;
        Point escalado = new Point(x, y);
        
        // 4. Actualizar la posición escalada de forma atómica
        mousePosicion.set(escalado);
        
    }
    
}