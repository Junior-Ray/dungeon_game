/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.dungeon_game.core.logic;

import com.dungeon_game.core.api.InputMouse;
import com.dungeon_game.core.components.AbstractUIComponent;
import com.dungeon_game.core.data.ClickOn;
import com.dungeon_game.core.data.SpatialGrid;
import com.dungeon_game.core.data.VisualRender;
import java.awt.Point;

/**
 *
 * @author GABRIEL SALGADO
 */
public class InterpreterEvent {
    private static InterpreterEvent instance;
    private VisualRender actual;
    private static final int MIN_LAYER = 0;
    private static final int MAX_LAYER = 9;
    private int minActiveLayer = MIN_LAYER;
    private VisualRender prioridad = null;
    private int capa=1;
    
    private InterpreterEvent(){};
    
    public static InterpreterEvent getInstance(){
        if(instance == null) instance = new InterpreterEvent();
        return instance;
    }
    
    /**
     * El método principal llamado en cada ciclo del Game Loop.
     * El orden es crucial: La posición debe actualizarse primero.
     */
    public void update(){
        // 1. Manejar la posición del mouse (actualiza el estado 'actual')
        handleMousePosition();
        
        // 2. Procesar el evento de click (usa el 'actual' de este frame)
        consumeClickEvent();
        
        // 3. Procesar el estado de retención del botón
        isLeftMouseDown();
    }
    
    private void consumeClickEvent(){
        if(InputMouse.getInstance().consumeClickEvent()){
           // System.out.println("Capa: "+capa);
            // Si el click ocurre fuera, ignoramos el evento por completo.
            if (!InputMouse.getInstance().isMouseInWindow()) {
                return; 
            }
            // Usamos la referencia 'actual' que fue establecida en handleMousePosition()
            if (actual instanceof ClickOn c) {
                // Si el objeto actual (en hover) es un componente UI, es seguro llamarlo.
                //System.out.println("Que raro");
                UIFocus.setFocus(actual);
                c.executeClickAction();
                
               
            } else if (actual != null) {
                // Caso donde se clica un objeto no-UI (e.g., terreno, unidad)
            } else {
                // Caso donde se clica el vacío (e.g., deseleccionar todo)
               
            }
            if (UIFocus.getFocus() != prioridad) {
                UIFocus.setFocus(prioridad);
                return;
            }
            if(UIFocus.getFocus() instanceof ClickOn c) c.executeClickAction();
        }
    }

    public VisualRender getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(VisualRender prioridad) {
        this.prioridad = prioridad;
    }
    
    /**
     * Maneja la posición del mouse, el hover, y la salida de la ventana.
     
    private void handleMousePosition(){
        Point dir = InputMouse.getInstance().getMousePosition();
        VisualRender obj = null;
        
        // Solo buscamos un objeto si el mouse está dentro de la ventana.
        if (InputMouse.getInstance().isMouseInWindow()) {
            // Buscamos el objeto en la capa 1 (UI)
            obj = SpatialGrid.getInstance().getElement(dir, capa);
            //System.out.println((obj!=null)?obj.toStringVertices():null);
            //System.out.println((obj!=null)? obj.toStringVertices():null);
        }
        
        if (actual != obj) {
            // if(actual!=null) System.out.println("Actual: "+ actual.getVisualId());
            // A. Desactivar el estado 'hover' del objeto ANTERIOR ('actual')
            if (actual instanceof AbstractUIComponent) {
               
                ((AbstractUIComponent) actual).updateState(false);
            }
            
            // B. Actualizar la referencia 'actual' al nuevo objeto (puede ser null)
            actual = obj;
        }
        
        // C. Activar el estado 'hover' del objeto ACTUAL
        if (actual instanceof AbstractUIComponent) {
            ((AbstractUIComponent) actual).updateState(true);
        }
    }
    * */
    private void handleMousePosition(){
        Point dir = InputMouse.getInstance().getMousePosition();
        VisualRender obj = null;
        SpatialGrid grid = SpatialGrid.getInstance();
        
        // Solo buscamos un objeto si el mouse está dentro de la ventana.
        if (InputMouse.getInstance().isMouseInWindow()) {
            // Buscamos el objeto en la capa entre 0 y 9 (UI)
            
            
            for (int layer = MAX_LAYER; layer >= minActiveLayer; layer--) {
                
                VisualRender cand = grid.getElement(dir, layer);
                obj = grid.getElement(dir, layer);
                
                if (cand == null) continue;

                // Guardamos el primer hit
                obj = cand;
                System.out.println((obj!=null)?obj.getVisualId():null);
                if (cand instanceof AbstractUIComponent) {
                    break;
                }
            }
        }
        
        if (actual != obj) {
            // if(actual!=null) System.out.println("Actual: "+ actual.getVisualId());
            // A. Desactivar el estado 'hover' del objeto ANTERIOR ('actual')
            if (actual instanceof AbstractUIComponent) {
               
                // Si el objeto anterior existía y era un UI Component, desactivamos su estado
                ((AbstractUIComponent) actual).updateState(false);
            }
            
            // B. Actualizar la referencia 'actual' al nuevo objeto (puede ser null)
            actual = obj;
        }
        
        // C. Activar el estado 'hover' del objeto ACTUAL
        if (actual instanceof AbstractUIComponent) {
            ((AbstractUIComponent) actual).updateState(true);
        }
    }
    
    private void isLeftMouseDown(){
        if(InputMouse.getInstance().isLeftMouseDown()){
            // Aquí iría la lógica para arrastrar, mantener presionado, o cargar ataques.
        }
    }

    public VisualRender getActual() {
        return actual;
    }

    public void setActual(VisualRender actual) {
        this.actual = actual;
    }

    public int getCapa() {
        return capa;
    }

    public void setCapa(int capa) {
        this.capa = capa;
    }
    
    
    public void setMinActiveLayer(int layer) {
        if (layer < MIN_LAYER) layer = MIN_LAYER;
        if (layer > MAX_LAYER) layer = MAX_LAYER;
        this.minActiveLayer = layer;
    }
    
}
