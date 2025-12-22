/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.dungeon_game.control;

import com.dungeon_game.core.api.InputMouse;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 * @author GABRIEL SALGADO
 */
public class InputManager extends MouseAdapter {
    private static InputManager  instance;
    
    // Almacena la posición del mouse de forma atómica para ser segura entre hilos
    private final AtomicReference<Point> mousePos = new AtomicReference<>(new Point(0, 0));
    
    // Almacena el estado del clic izquierdo (presionado) de forma atómica
    private final AtomicBoolean leftMouseDown = new AtomicBoolean(false);
    
    // NUEVO: Almacena si el mouse está actualmente dentro de los límites del componente.
    private final AtomicBoolean mouseInWindow = new AtomicBoolean(false);
    
    private InputManager(){};
    
    public static InputManager getInstance(){
        if(instance==null) instance = new InputManager();
        return instance;
    }
    
    /**
     * Envía el estado del input al módulo InputMouse para que sea procesado por 
     * el Game Loop.
     */
    public void update(){
        InputMouse.getInstance().update(isLeftMouseDown(), getMousePosition(), isMouseInWindow());
    }
    
    /**
     * Retorna la posición actual del mouse en la ventana.
     * @return Point con las coordenadas (x, y).
     */
    private Point getMousePosition() {
        return mousePos.get();
    }

    /**
     * Retorna si el botón izquierdo del mouse está presionado.
     * @return true si está presionado.
     */
    private boolean isLeftMouseDown() {
        return leftMouseDown.get();
    }
    
    /**
     * Retorna si el mouse está actualmente dentro del componente.
     * @return true si el mouse está dentro de la ventana.
     */
    private boolean isMouseInWindow() {
        return mouseInWindow.get();
    }
    
    // --- Implementación de MouseAdapter ---

    @Override
    public void mouseMoved(MouseEvent e) {
        // Actualiza la posición del mouse en cada movimiento.
        mousePos.set(e.getPoint());
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        // También actualiza la posición durante un arrastre.
        mousePos.set(e.getPoint());
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            leftMouseDown.set(true);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            leftMouseDown.set(false);
        }
    }
    
    @Override
    public void mouseEntered(MouseEvent e) {
        // El mouse ha entrado, la posición actual es válida.
        mouseInWindow.set(true);
        // Capturamos la posición de entrada, aunque mouseMoved se activará inmediatamente después.
        mousePos.set(e.getPoint());
    }
    
    @Override
    public void mouseExited(MouseEvent e) {
        // El mouse ha salido. Esta es la posición más reciente antes de salir.
        // Guardamos esta última posición, como solicitaste.
        mousePos.set(e.getPoint());
        
        // Indicamos que el mouse ya no está en la ventana (¡Esto es clave para el hover!)
        mouseInWindow.set(false); 
        // La lógica en InputMouse/Interpreter debe usar este 'false' para desactivar los hovers.
    }
    
}