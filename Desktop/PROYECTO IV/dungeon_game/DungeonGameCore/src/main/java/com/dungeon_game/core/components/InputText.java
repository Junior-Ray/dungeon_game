/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.dungeon_game.core.components;

import com.dungeon_game.core.data.RenderableVisual;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * InputText con soporte para diferentes modos de entrada y cursor navegable:
 * - NORMAL: Cualquier carácter
 * - NUMERIC: Solo números (0-9, punto decimal, signo negativo)
 * - PASSWORD: Muestra asteriscos en lugar del texto real
 * - ALPHANUMERIC: Solo letras y números
 * 
 * Navegación con flechas izquierda/derecha, Home, End
 * Inserción/borrado en cualquier posición
 * 
 * @author GABRIEL SALGADO
 */
public class InputText extends AbstractUIComponent implements TextInputComponent {

    private StringBuilder text = new StringBuilder();
    private boolean focused = false;
    private boolean allowEnter = false;
    
    // --- CURSOR NAVEGABLE ---
    private int cursorPosition = 0;  // Posición actual del cursor (0 = inicio)
    
    // --- MODOS DE ENTRADA ---
    private InputMode mode = InputMode.NORMAL;
    
    // --- PASSWORD MODE ---
    private boolean passwordVisible = false;
    private char passwordChar = '•';  // Caracter para ocultar (• ● * etc.)
    
    // --- NUMERIC MODE ---
    private boolean allowDecimal = true;
    private boolean allowNegative = true;
    
    // Cursor visual
    private boolean cursorVisible = false;
    private long lastBlinkTime = 0;
    private static final long CURSOR_DELAY = 500;
    
    // Scroll horizontal para textos largos
    private int scrollOffset = 0;

    /**
     * Modos de entrada disponibles
     */
    public enum InputMode {
        NORMAL,         // Cualquier carácter
        NUMERIC,        // Solo números (0-9, ., -)
        PASSWORD,       // Muestra asteriscos
        ALPHANUMERIC,   // Solo letras y números
        EMAIL           // Formato email básico
    }

    public InputText(
            int x, int y, int width, int height, int layer,
            String visualId, Point[] vertices, Point dir
    ) {
        super(x, y, width, height, layer, visualId, vertices, dir);
        setOnClickAction(() -> focused = true);
    }

    public InputText(Point[] vertices, Point dir, RenderableVisual renderable) {
        super(vertices, dir, renderable);
        setOnClickAction(() -> focused = true);
    }

    // ==================== NAVEGACIÓN DEL CURSOR ====================
    
    /**
     * Mueve el cursor una posición a la izquierda
     */
    public void moveCursorLeft() {
        if (!focused) return;
        
        if (cursorPosition > 0) {
            cursorPosition--;
            resetCursorBlink();
            render();
        }
    }
    
    /**
     * Mueve el cursor una posición a la derecha
     */
    public void moveCursorRight() {
        if (!focused) return;
        
        if (cursorPosition < text.length()) {
            cursorPosition++;
            resetCursorBlink();
            render();
        }
    }
    
    /**
     * Mueve el cursor al inicio del texto (Home)
     */
    public void moveCursorHome() {
        if (!focused) return;
        
        cursorPosition = 0;
        resetCursorBlink();
        render();
    }
    
    /**
     * Mueve el cursor al final del texto (End)
     */
    public void moveCursorEnd() {
        if (!focused) return;
        
        cursorPosition = text.length();
        resetCursorBlink();
        render();
    }
    
    /**
     * Establece la posición del cursor manualmente
     */
    public void setCursorPosition(int position) {
        if (position < 0) position = 0;
        if (position > text.length()) position = text.length();
        
        cursorPosition = position;
        resetCursorBlink();
        render();
    }
    
    public int getCursorPosition() {
        return cursorPosition;
    }
    
    /**
     * Reinicia el parpadeo del cursor (lo hace visible)
     */
    private void resetCursorBlink() {
        cursorVisible = true;
        lastBlinkTime = System.currentTimeMillis();
    }

    // ==================== MANEJO DE ENTRADA ====================
    
    public void addChar(char c) {
        if (!focused) {
            return;
        }

        // 1. BACKSPACE - Borra el carácter ANTES del cursor
        if (c == '\b') {
            if (cursorPosition > 0) {
                text.deleteCharAt(cursorPosition - 1);
                cursorPosition--;
                resetCursorBlink();
                render();
            }
            return;
        }

        // 2. DELETE - Borra el carácter EN la posición del cursor
        if (c == 127) { // código ASCII de DELETE
            if (cursorPosition < text.length()) {
                text.deleteCharAt(cursorPosition);
                resetCursorBlink();
                render();
            }
            return;
        }

        // 3. ENTER
        if (c == '\n' || c == '\r') {
            if (allowEnter) {
                text.insert(cursorPosition, '\n');
                cursorPosition++;
                resetCursorBlink();
                render();
            }
            return;
        }

        // 4. Ignorar caracteres de control
        if (Character.isISOControl(c)) {
            return;
        }

        // 5. VALIDAR según el modo antes de agregar
        if (isCharacterAllowed(c)) {
            text.insert(cursorPosition, c);
            cursorPosition++;
            resetCursorBlink();
            render();
        }
    }
    
    /**
     * Borra el carácter en la posición del cursor (equivalente a DELETE)
     */
    public void deleteAtCursor() {
        if (!focused) return;
        
        if (cursorPosition < text.length()) {
            text.deleteCharAt(cursorPosition);
            resetCursorBlink();
            render();
        }
    }
    
    /**
     * Borra el carácter antes del cursor (equivalente a BACKSPACE)
     */
    public void backspaceAtCursor() {
        if (!focused) return;
        
        if (cursorPosition > 0) {
            text.deleteCharAt(cursorPosition - 1);
            cursorPosition--;
            resetCursorBlink();
            render();
        }
    }
    
    /**
     * Valida si un carácter es permitido según el modo actual
     */
    private boolean isCharacterAllowed(char c) {
        switch (mode) {
            case NORMAL:
                return true;  // Cualquier carácter
                
            case NUMERIC:
                // Números, punto decimal y signo negativo
                if (Character.isDigit(c)) return true;
                if (c == '.' && allowDecimal && !text.toString().contains(".")) return true;
                if (c == '-' && allowNegative && cursorPosition == 0 && !text.toString().startsWith("-")) return true;
                return false;
                
            case PASSWORD:
                // Permitir cualquier carácter en password
                return true;
                
            case ALPHANUMERIC:
                // Solo letras y números
                return Character.isLetterOrDigit(c);
                
            case EMAIL:
                // Caracteres válidos para email
                if (Character.isLetterOrDigit(c)) return true;
                if (c == '@' || c == '.' || c == '_' || c == '-') return true;
                return false;
                
            default:
                return true;
        }
    }

    // ==================== UPDATE & RENDER ====================
    
    @Override
    public void update() {
        if (!focused) {
            return;
        }

        // Cursor blink
        long now = System.currentTimeMillis();
        if (now - lastBlinkTime > CURSOR_DELAY) {
            cursorVisible = !cursorVisible;
            lastBlinkTime = now;
            render();
        }
    }

    @Override
    public void render() {
        BufferedImage img = new BufferedImage(
                getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB
        );

        Graphics2D g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Fondo transparente
        g2d.setComposite(AlphaComposite.Clear);
        g2d.fillRect(0, 0, w, h);
        g2d.setComposite(AlphaComposite.SrcOver);

        // Configurar texto
        g2d.setColor(Color.BLACK);
        Font font = new Font("SansSerif", Font.PLAIN, 14);
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();

        // Determinar qué texto mostrar según el modo
        String visibleText = getDisplayText();
        
        // Calcular posición del cursor en píxeles
        String textBeforeCursor = visibleText.substring(0, Math.min(cursorPosition, visibleText.length()));
        int cursorX = fm.stringWidth(textBeforeCursor);
        
        // Ajustar scroll si el cursor está fuera del área visible
        int padding = 5;
        int maxVisibleWidth = w - (padding * 2);
        
        if (cursorX - scrollOffset > maxVisibleWidth) {
            scrollOffset = cursorX - maxVisibleWidth + 20;
        } else if (cursorX - scrollOffset < 0) {
            scrollOffset = Math.max(0, cursorX - 20);
        }
        
        // Dibujar texto con scroll
        int textY = (int) (h * 0.70);
        g2d.setColor(Color.BLACK);
        g2d.drawString(visibleText, padding - scrollOffset, textY);
        
        // Dibujar cursor si está visible y enfocado
        if (focused && cursorVisible) {
            g2d.setColor(Color.BLUE);
            int cursorDrawX = padding + cursorX - scrollOffset;
            g2d.fillRect(cursorDrawX, textY - fm.getAscent(), 2, fm.getHeight());
        }

        g2d.dispose();
        this.setImage(img);
    }
    
    /**
     * Obtiene el texto a mostrar según el modo
     */
    private String getDisplayText() {
        String actualText = text.toString();
        
        // Modo PASSWORD: Mostrar asteriscos si no es visible
        if (mode == InputMode.PASSWORD && !passwordVisible) {
            StringBuilder masked = new StringBuilder();
            for (int i = 0; i < actualText.length(); i++) {
                masked.append(passwordChar);
            }
            return masked.toString();
        }
        
        return actualText;
    }

    // ==================== MÉTODOS HEREDADOS ====================
    
    @Override
    protected void onEnter() {
        // hover visual opcional
    }

    @Override
    protected void onExit() {
        // cuando deja el hover
    }

    @Override
    public void onClick() {
        // Cuando se hace clic, mover el cursor al final
        cursorPosition = text.length();
        resetCursorBlink();
        render();
    }

    @Override
    public void offFocus() {
        focused = false;
        cursorVisible = false;
        render();
    }

    // ==================== GETTERS & SETTERS ====================
    
    public String getText() {
        return text.toString();
    }

    public void clear() {
        text.setLength(0);
        cursorPosition = 0;
        scrollOffset = 0;
        render();
    }
    
    public void setText(String newText) {
        text = new StringBuilder(newText);
        cursorPosition = text.length();
        scrollOffset = 0;
        render();
    }
    
    public void setAllowEnter(boolean allow) {
        this.allowEnter = allow;
    }
    
    public boolean isFocused() {
        return focused;
    }
    
    public void setFocused(boolean focused) {
        this.focused = focused;
        if (focused) {
            resetCursorBlink();
        } else {
            cursorVisible = false;
        }
        render();
    }
    
    // --- CONFIGURACIÓN DE MODO ---
    
    /**
     * Establece el modo de entrada
     */
    public void setMode(InputMode mode) {
        this.mode = mode;
        render();
    }
    
    public InputMode getMode() {
        return mode;
    }
    
    // --- PASSWORD MODE ---
    
    /**
     * Alterna la visibilidad del password (para botón de mostrar/ocultar)
     */
    public void togglePasswordVisibility() {
        if (mode == InputMode.PASSWORD) {
            passwordVisible = !passwordVisible;
            render();
        }
    }
    
    /**
     * Establece si el password es visible
     */
    public void setPasswordVisible(boolean visible) {
        if (mode == InputMode.PASSWORD) {
            this.passwordVisible = visible;
            render();
        }
    }
    
    public boolean isPasswordVisible() {
        return passwordVisible;
    }
    
    /**
     * Cambia el carácter usado para ocultar el password (• ● * etc.)
     */
    public void setPasswordChar(char c) {
        this.passwordChar = c;
        if (mode == InputMode.PASSWORD) {
            render();
        }
    }
    
    // --- NUMERIC MODE ---
    
    /**
     * Permite o no punto decimal en modo numérico
     */
    public void setAllowDecimal(boolean allow) {
        this.allowDecimal = allow;
    }
    
    /**
     * Permite o no números negativos en modo numérico
     */
    public void setAllowNegative(boolean allow) {
        this.allowNegative = allow;
    }
    
    /**
     * Obtiene el valor numérico (solo si está en modo NUMERIC)
     */
    public Double getNumericValue() {
        if (mode != InputMode.NUMERIC) {
            return null;
        }
        
        try {
            return Double.parseDouble(text.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Obtiene el valor como entero (solo si está en modo NUMERIC)
     */
    public Integer getIntegerValue() {
        if (mode != InputMode.NUMERIC) {
            return null;
        }
        
        try {
            return Integer.parseInt(text.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    // --- VALIDACIÓN ---
    
    /**
     * Verifica si el contenido actual es válido según el modo
     */
    public boolean isValid() {
        String content = text.toString();
        
        switch (mode) {
            case NUMERIC:
                try {
                    Double.parseDouble(content);
                    return true;
                } catch (NumberFormatException e) {
                    return content.isEmpty(); // Vacío es válido
                }
                
            case EMAIL:
                // Validación básica de email
                return content.isEmpty() || 
                       (content.contains("@") && content.contains("."));
                
            default:
                return true;
        }
    }
}