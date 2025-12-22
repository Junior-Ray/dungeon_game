package com.dungeon_game.control;

import com.dungeon_game.core.api.InputKeyboard;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Sistema de input de teclado con soporte para key repeat
 * Soporta repetición tanto de teclas especiales como caracteres normales
 * 
 * @author GABRIEL SALGADO
 */
public class InputTeclado implements KeyListener {

    private static InputTeclado instance;

    // Almacena el estado de las teclas
    private final ConcurrentHashMap<Integer, Boolean> keyStates = new ConcurrentHashMap<>();

    // Último caracter tipeado
    private final AtomicReference<Character> lastTypedCharacter = new AtomicReference<>(null);

    // Último keyCode presionado
    private final AtomicReference<Integer> lastKeyCode = new AtomicReference<>(null);

    // Bandera de evento de tecla presionada
    private volatile boolean keyPressedEvent = false;

    // --- KEY REPEAT SYSTEM ---
    private final ConcurrentHashMap<Integer, Long> keyPressTime = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, Long> lastRepeatTime = new ConcurrentHashMap<>();
    
    // Almacena el último carácter asociado a cada keyCode (para repetición)
    private final ConcurrentHashMap<Integer, Character> keyCodeToChar = new ConcurrentHashMap<>();
    
    // Delay inicial antes de que empiece la repetición (ms)
    private static final long INITIAL_REPEAT_DELAY = 500; // x segundos
    
    // Intervalo entre repeticiones (ms)
    private static final long REPEAT_INTERVAL = 100; // n repeticiones por segundo
    
    // Teclas especiales que soportan repetición
    private static final int[] SPECIAL_REPEATABLE_KEYS = {
        KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_UP, KeyEvent.VK_DOWN,
        KeyEvent.VK_BACK_SPACE, KeyEvent.VK_DELETE, KeyEvent.VK_HOME, KeyEvent.VK_END
    };

    private InputTeclado() {
    }

    public static InputTeclado getInstance() {
        if (instance == null) {
            instance = new InputTeclado();
        }
        return instance;
    }

    // --- MÉTODOS DE ESTADO ---
    
    public boolean isKeyPressed(int keyCode) {
        return keyStates.getOrDefault(keyCode, false);
    }

    public Character consumeTypedCharacter() {
        return lastTypedCharacter.getAndSet(null);
    }

    public Integer consumeKeyCode() {
        return lastKeyCode.getAndSet(null);
    }

    public boolean consumeKeyPressedEvent() {
        if (keyPressedEvent) {
            keyPressedEvent = false;
            return true;
        }
        return false;
    }

    public void clearAllStates() {
        keyStates.forEach((k, v) -> keyStates.put(k, false));
        lastTypedCharacter.set(null);
        lastKeyCode.set(null);
        keyPressedEvent = false;
        keyPressTime.clear();
        lastRepeatTime.clear();
        keyCodeToChar.clear();
    }
    
    /**
     * Verifica si una tecla especial soporta repetición
     */
    private boolean isSpecialRepeatableKey(int keyCode) {
        for (int key : SPECIAL_REPEATABLE_KEYS) {
            if (key == keyCode) return true;
        }
        return false;
    }
    
    /**
     * Verifica si una tecla es alfanumérica o símbolo (para repetición de caracteres)
     */
    private boolean isCharacterKey(int keyCode) {
        // Letras A-Z
        if (keyCode >= KeyEvent.VK_A && keyCode <= KeyEvent.VK_Z) return true;
        
        // Números 0-9
        if (keyCode >= KeyEvent.VK_0 && keyCode <= KeyEvent.VK_9) return true;
        
        // Numpad
        if (keyCode >= KeyEvent.VK_NUMPAD0 && keyCode <= KeyEvent.VK_NUMPAD9) return true;
        
        // Símbolos comunes
        if (keyCode == KeyEvent.VK_SPACE) return true;
        if (keyCode == KeyEvent.VK_COMMA) return true;
        if (keyCode == KeyEvent.VK_PERIOD) return true;
        if (keyCode == KeyEvent.VK_SLASH) return true;
        if (keyCode == KeyEvent.VK_SEMICOLON) return true;
        if (keyCode == KeyEvent.VK_QUOTE) return true;
        if (keyCode == KeyEvent.VK_OPEN_BRACKET) return true;
        if (keyCode == KeyEvent.VK_CLOSE_BRACKET) return true;
        if (keyCode == KeyEvent.VK_BACK_SLASH) return true;
        if (keyCode == KeyEvent.VK_MINUS) return true;
        if (keyCode == KeyEvent.VK_EQUALS) return true;
        
        return false;
    }
    
    /**
     * Verifica si una tecla soporta repetición (especiales o caracteres)
     */
    private boolean isRepeatableKey(int keyCode) {
        return isSpecialRepeatableKey(keyCode) || isCharacterKey(keyCode);
    }

    /**
     * Procesa la repetición de teclas mantenidas presionadas
     */
    private void processKeyRepeat() {
        long currentTime = System.currentTimeMillis();
        
        keyStates.forEach((keyCode, isPressed) -> {
            if (!isPressed || !isRepeatableKey(keyCode)) {
                return;
            }
            
            Long pressTime = keyPressTime.get(keyCode);
            if (pressTime == null) {
                return;
            }
            
            long timeSincePress = currentTime - pressTime;
            
            // Si ha pasado el delay inicial, empezar a repetir
            if (timeSincePress >= INITIAL_REPEAT_DELAY) {
                Long lastRepeat = lastRepeatTime.get(keyCode);
                
                // Si es la primera repetición o ya pasó el intervalo
                if (lastRepeat == null || (currentTime - lastRepeat) >= REPEAT_INTERVAL) {
                    // Disparar evento de repetición
                    keyPressedEvent = true;
                    lastKeyCode.set(keyCode);
                    lastRepeatTime.put(keyCode, currentTime);
                    
                    // Para teclas especiales
                    if (isSpecialRepeatableKey(keyCode)) {
                        if (keyCode == KeyEvent.VK_BACK_SPACE) {
                            lastTypedCharacter.set('\b');
                        }
                        // Otras teclas especiales se manejan por keyCode
                    } 
                    // Para teclas de caracteres
                    else if (isCharacterKey(keyCode)) {
                        Character associatedChar = keyCodeToChar.get(keyCode);
                        if (associatedChar != null) {
                            lastTypedCharacter.set(associatedChar);
                        }
                    }
                }
            }
        });
    }

    public void update() {
        // Procesar repetición de teclas mantenidas
        processKeyRepeat();
        
        InputKeyboard.getInstance().update(
                new ConcurrentHashMap<>(keyStates),
                consumeKeyPressedEvent(),
                consumeTypedCharacter(),
                consumeKeyCode()
        );
    }

    // --- IMPLEMENTACIÓN DE KEY LISTENER ---

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();

        // Solo actualizamos si la tecla no estaba ya presionada
        if (!keyStates.getOrDefault(keyCode, false)) {
            keyStates.put(keyCode, true);
            keyPressedEvent = true;
            lastKeyCode.set(keyCode);
            
            // Registrar el tiempo de presión para key repeat
            if (isRepeatableKey(keyCode)) {
                keyPressTime.put(keyCode, System.currentTimeMillis());
                lastRepeatTime.remove(keyCode); // Limpiar repetición anterior
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        keyStates.put(keyCode, false);
        
        // Limpiar timers de repetición
        keyPressTime.remove(keyCode);
        lastRepeatTime.remove(keyCode);
        keyCodeToChar.remove(keyCode);  // Limpiar el carácter asociado
    }

    @Override
    public void keyTyped(KeyEvent e) {
        char c = e.getKeyChar();
        int keyCode = e.getKeyCode();

        // FILTRO: Aceptar solo caracteres válidos
        if (isValidTypedCharacter(c)) {
            lastTypedCharacter.set(c);
            
            // Asociar el carácter con el keyCode para repetición
            // Obtener el keyCode de la tecla que generó este carácter
            // NOTA: keyTyped no tiene keyCode confiable, pero podemos usar
            // la última tecla presionada si es una tecla de carácter
            Integer lastKey = lastKeyCode.get();
            if (lastKey != null && isCharacterKey(lastKey)) {
                keyCodeToChar.put(lastKey, c);
            }
        }
    }

    private boolean isValidTypedCharacter(char c) {
        // 1. Especiales permitidos explícitamente
        if (c == '\b') {
            return true; // Backspace
        }
        if (c == '\n' || c == '\r') {
            return true; // Enter
        }
        if (c == ' ') {
            return true; // Espacio
        }
        
        // 2. Ignorar completamente ISOControl (TAB, ESC, etc.)
        if (Character.isISOControl(c)) {
            return false;
        }

        // 3. Aceptar caracteres imprimibles ASCII (letras, números, símbolos)
        if (c >= 32 && c <= 126) {
            return true;
        }

        // 4. Aceptar caracteres UNICODE (á, ñ, etc.)
        if (!Character.isISOControl(c)) {
            return true;
        }

        return false;
    }
}