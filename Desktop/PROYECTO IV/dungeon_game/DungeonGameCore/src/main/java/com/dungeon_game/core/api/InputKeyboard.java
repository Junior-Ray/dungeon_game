/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.dungeon_game.core.api;


import com.dungeon_game.core.components.InputTextArea;
import com.dungeon_game.core.components.TextInputComponent;
import com.dungeon_game.core.data.VisualRender;
import com.dungeon_game.core.logic.UIFocus;
import java.awt.event.KeyEvent;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 * @author GABRIEL SALGADO
 */
public class InputKeyboard {
    private static InputKeyboard instance;
    
    // Almacena estados de teclas (presionado: true)
    private final ConcurrentHashMap<Integer, Boolean> keyStates
            = new ConcurrentHashMap<>();
    
    // Evento de tecla presionada (un solo disparo por frame)
    private final AtomicBoolean keyPressedEvent = new AtomicBoolean(false);
    
    // Último caracter tipeado (consumible)
    private final AtomicReference<Character> lastTypedCharacter
            = new AtomicReference<>(null);
    
    // Almacena el último keyCode presionado para teclas especiales
    private final AtomicReference<Integer> lastKeyCode = new AtomicReference<>(null);
    
    private InputKeyboard() {
    }
    
    public static InputKeyboard getInstance() {
        if (instance == null) {
            instance = new InputKeyboard();
        }
        return instance;
    }
    
    /**
     * Retorna si la tecla está presionada actualmente
     */
    public boolean isKeyDown(int keyCode) {
        return keyStates.getOrDefault(keyCode, false);
    }
    
    /**
     * Retorna true una única vez cuando ocurre un keyPressed
     */
    public boolean consumeKeyPressedEvent() {
        return keyPressedEvent.compareAndSet(true, false);
    }
    
    /**
     * Retorna el caracter tipeado y lo consume
     */
    public Character consumeTypedCharacter() {
        return lastTypedCharacter.getAndSet(null);
    }
    
    /**
     * Retorna el último keyCode y lo consume
     */
    public Integer consumeKeyCode() {
        return lastKeyCode.getAndSet(null);
    }
    
    /**
     * Limpiar (por ejemplo si se pierde el foco)
     */
    public void clearAll() {
        keyStates.forEach((k, v) -> keyStates.put(k, false));
        lastTypedCharacter.set(null);
        keyPressedEvent.set(false);
        lastKeyCode.set(null);
    }
    
    /**
    * Procesa caracteres tipeados (llamado desde update)
    */
   public void onKeyTyped(char c) {
       VisualRender f = UIFocus.getFocus();
       if(f!=null)f.addChar(c);
   }

   // ----------------------
   // MÉTODO UPDATE (desde CLIENTE)
   // ----------------------
   /**
    * Recibe los datos crudos del cliente. Este método se ejecuta desde
    * InputTeclado.update()
    */
    public void update(ConcurrentHashMap<Integer, Boolean> rawKeys,
            boolean rawKeyPressedEvent,
            Character rawTypedChar,
            Integer rawKeyCode) {

        // 1. Copia los estados de teclas
        rawKeys.forEach((k, v) -> keyStates.put(k, v));

        // 2. Evento de un solo disparo al presionar
        if (rawKeyPressedEvent) {
            keyPressedEvent.set(true);
        }

        // 3. Copia el caracter tipeado (si lo hay)
        if (rawTypedChar != null) {
            lastTypedCharacter.set(rawTypedChar);
        }

        // 4. Copia el keyCode (para teclas especiales)
        if (rawKeyCode != null) {
            lastKeyCode.set(rawKeyCode);
        }

        // 5. Procesar eventos
        if (consumeKeyPressedEvent()) {
            VisualRender f = UIFocus.getFocus();
            Integer keyCode = consumeKeyCode();

            if (f instanceof TextInputComponent input && keyCode != null) {
                switch (keyCode) {
                    case KeyEvent.VK_LEFT -> { input.moveCursorLeft(); return; }
                    case KeyEvent.VK_RIGHT -> { input.moveCursorRight(); return; }
                    case KeyEvent.VK_HOME -> { input.moveCursorHome(); return; }
                    case KeyEvent.VK_END -> { input.moveCursorEnd(); return; }
                    case KeyEvent.VK_DELETE -> { input.deleteAtCursor(); return; }
                }

                // Casos especiales solo para InputTextArea
                if (f instanceof InputTextArea textArea) {
                    if (keyCode == KeyEvent.VK_UP) {
                        textArea.moveCursorUp();
                        return;
                    } else if (keyCode == KeyEvent.VK_DOWN) {
                        textArea.moveCursorDown();
                        return;
                    }
                }
            }

            // Procesar caracteres normales
            Character c = consumeTypedCharacter();
            if (c != null) {
                onKeyTyped(c);
            }
        }
    }
}