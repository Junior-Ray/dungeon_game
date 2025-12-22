/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.dungeon_game.core.components;

import com.dungeon_game.core.api.InputMouse;
import com.dungeon_game.core.data.RenderableVisual;
import com.dungeon_game.core.data.VisualRender;
import java.awt.Point;


/**
 *
 * @author GABRIEL SALGADO
 */

public abstract class AbstractUIComponent extends VisualRender {

   
    @FunctionalInterface
    public static interface ClickAction {
        /**
         * Define la lógica que se ejecutará al activarse el componente.
         */
        void execute();
    }

    // --- ESTADO INTERNO DEL CLIENTE ---
    protected boolean enabled = true;
    protected boolean isHovered = false;
    protected boolean isPressed = false;
    private ClickAction clickAction; // La función a ejecutar al hacer click

    public AbstractUIComponent(int renderX, int renderY, int width, int height, int layer, String visualId, Point[] vertices, Point dir) {
        super(renderX, renderY, width, height, layer, visualId, vertices, dir);
    }

    public AbstractUIComponent(Point[] vertices, Point dir, RenderableVisual renderable) {
        super(vertices, dir, renderable);
    }

    /**
     * Asigna la función (lambda o clase) que se ejecutará al hacer click.
     * Esta es la clave para la flexibilidad.
     * @param action La interfaz ClickAction con el código a ejecutar.
     */
    public void setOnClickAction(ClickAction action) {
        this.clickAction = action;
    }

    public void updateState(boolean contains) {
        InputMouse input = InputMouse.getInstance();

        // 1. Verificación de HOVER (Hitbox)
        boolean wasHovered = isHovered;
        
        // La validación de 'contains' usa las propiedades de la hitbox del Core
        // (renderX, width, height o los vértices)
        isHovered = contains;
        
        // 2. Ejecutar lógica abstracta de HOVER si el estado cambió
        if (isHovered && !wasHovered) {
            onEnter(); // Se acaba de entrar al área
        } else if (!isHovered && wasHovered) {
            onExit();  // Se acaba de salir del área
        }
        
        // 4. Implementación del estado 'Pressed' (Opcional, si lo necesitas visualmente)
        this.isPressed = isHovered && input.isLeftMouseDown();
    }
    public void executeClickAction() {
		// 1. Ejecuta el método abstracto de acción visual/sonora
		onClick();	
		
		// 2. Ejecuta la acción delegada (la función que el usuario programó)
		if (this.clickAction != null) {
			this.clickAction.execute();
		}
    }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            isHovered = false;
            isPressed = false;
        }
    }
    // --- MÉTODOS HOOKS ABSTRACTOS PARA EL EQUIPO ---
    public void offFocus(){}
    protected abstract void onEnter();

    protected abstract void onExit();
    
    public abstract void onClick();
    public void update(){
        
    }
}
