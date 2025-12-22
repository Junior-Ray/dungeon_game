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
import java.util.ArrayList;
import java.util.List;

/**
 * InputTextArea con cursor manejable:
 * - Flechas ←→ mueven el cursor horizontal
 * - Flechas ↑↓ mueven el cursor vertical (entre líneas)
 * - Crece hacia arriba como WhatsApp (texto siempre en la base)
 * - Fondo opcional (transparente por defecto)
 * 
 * @author GABRIEL SALGADO
 */
public class InputTextArea extends AbstractUIComponent implements TextInputComponent {

    private StringBuilder text = new StringBuilder();
    private boolean focused = false;
    
    // Configuración visual
    private final int maxVisibleLines = 4;
    private final int lineHeight = 16;
    private final int padding = 5;
    private final int minHeight = lineHeight + (padding * 2);
    
    // Escala del sistema
    private static final int SCALE = 10;
    
    // Lista de líneas renderizables
    private List<String> lines = new ArrayList<>();
    
    // Cursor manejable
    private int cursorPosition = 0;
    private boolean cursorVisible = false;
    private long lastBlinkTime = 0;
    private static final long CURSOR_DELAY = 450;
    
    // Scroll
    private int scrollOffset = 0;
    
    // Estilo
    private Color textColor = Color.BLACK;
    private Color cursorColor = Color.BLACK;
    private Color backgroundColor = null;  // null = transparente
    private Font font = new Font("SansSerif", Font.PLAIN, 14);
    
    // Geometría base (posición inferior fija)
    private final int baseY;

    /**
     * Constructor principal
     */
    public InputTextArea(int x, int y, int width, int height, int layer,
                        String visualId, Point[] vertices, Point dir) {
        super(x, y, width, height, layer, visualId, vertices, dir);
        // baseY es la LÍNEA BASE inferior (donde aparece la última línea)
        this.baseY = y + height;
        
        lines.add("");
        
        setOnClickAction(() -> {
            focused = true;
            cursorPosition = text.length();  // Cursor al final al hacer click
            resetBlink();
            scrollToCursor();
            render();
        });
        
        render();
    }

    /**
     * Constructor con RenderableVisual
     */
    public InputTextArea(Point[] vertices, Point dir, RenderableVisual renderable) {
        super(vertices, dir, renderable);
        // baseY es la LÍNEA BASE inferior (donde aparece la última línea)
        // Calcular desde dir.y (en unidades Core) + altura
        int yInPixels = dir.y * SCALE;
        this.baseY = yInPixels + renderable.getHeight();
        
        lines.add("");
        
        setOnClickAction(() -> {
            focused = true;
            cursorPosition = text.length();  // Cursor al final al hacer click
            resetBlink();
            scrollToCursor();
            render();
        });
        
        render();
    }
    
    private void resetBlink() {
        cursorVisible = true;
        lastBlinkTime = System.currentTimeMillis();
    }

    // ==================== ENTRADA DE TEXTO ====================
    
    public void addChar(char c) {
        if (!focused) return;

        if (c == '\b') {
            if (cursorPosition > 0) {
                text.deleteCharAt(cursorPosition - 1);
                cursorPosition--;
                updateLines();
                scrollToCursor();
                resetBlink();
                render();
            }
            return;
        }

        // ❌ COMENTAR O ELIMINAR ESTE BLOQUE:
        /*
        if (c == '\n' || c == '\r') {
            text.insert(cursorPosition, '\n');
            cursorPosition++;
            updateLines();
            scrollToCursor();
            resetBlink();
            render();
            return;
        }
        */

        if (c == '\n' || c == '\r') {
            return;  // No hacer nada
        }

        if (Character.isISOControl(c)) return;

        text.insert(cursorPosition, c);
        cursorPosition++;
        updateLines();
        scrollToCursor();
        resetBlink();
        render();
    }

    // ==================== NAVEGACIÓN DEL CURSOR ====================
    
    public void moveCursorLeft() {
        if (cursorPosition > 0) {
            cursorPosition--;
            scrollToCursor();
            resetBlink();
            render();
        }
    }
    
    public void moveCursorRight() {
        if (cursorPosition < text.length()) {
            cursorPosition++;
            scrollToCursor();
            resetBlink();
            render();
        }
    }
    
    public void moveCursorUp() {
        CursorLinePosition pos = getCursorLinePosition();
        if (pos.lineIndex > 0) {
            int targetCol = Math.min(pos.columnInLine, lines.get(pos.lineIndex - 1).length());
            cursorPosition = getPositionFromLineColumn(pos.lineIndex - 1, targetCol);
            scrollToCursor();
            resetBlink();
            render();
        }
    }
    
    public void moveCursorDown() {
        CursorLinePosition pos = getCursorLinePosition();
        if (pos.lineIndex < lines.size() - 1) {
            int targetCol = Math.min(pos.columnInLine, lines.get(pos.lineIndex + 1).length());
            cursorPosition = getPositionFromLineColumn(pos.lineIndex + 1, targetCol);
            scrollToCursor();
            resetBlink();
            render();
        }
    }
    
    public void moveCursorToLineStart() {
        CursorLinePosition pos = getCursorLinePosition();
        cursorPosition = getPositionFromLineColumn(pos.lineIndex, 0);
        scrollToCursor();
        resetBlink();
        render();
    }
    
    public void moveCursorToLineEnd() {
        CursorLinePosition pos = getCursorLinePosition();
        cursorPosition = getPositionFromLineColumn(pos.lineIndex, lines.get(pos.lineIndex).length());
        scrollToCursor();
        resetBlink();
        render();
    }
    
    public void deleteAtCursor() {
        if (cursorPosition < text.length()) {
            text.deleteCharAt(cursorPosition);
            updateLines();
            scrollToCursor();
            resetBlink();
            render();
        }
    }
    
    // Métodos de compatibilidad
    public int getCursorPosition() { return cursorPosition; }
    public void setCursorPosition(int position) {
        cursorPosition = Math.max(0, Math.min(position, text.length()));
        scrollToCursor();
        resetBlink();
        render();
    }
    public void onMouseWheel(int wheelRotation) {}

    @Override
    public void moveCursorHome() {}

    @Override
    public void moveCursorEnd() {}

    // ==================== LÓGICA DE LÍNEAS ====================
    
    private static class CursorLinePosition {
        int lineIndex;
        int columnInLine;
        
        CursorLinePosition(int lineIndex, int columnInLine) {
            this.lineIndex = lineIndex;
            this.columnInLine = columnInLine;
        }
    }
    
    private CursorLinePosition getCursorLinePosition() {
        if (lines.isEmpty()) {
            lines.add("");
        }
        
        int textPosition = 0;
        String fullText = text.toString();
        
        for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
            String currentLine = lines.get(lineIndex);
            int lineLength = currentLine.length();
            
            if (cursorPosition >= textPosition && cursorPosition <= textPosition + lineLength) {
                int column = cursorPosition - textPosition;
                return new CursorLinePosition(lineIndex, column);
            }
            
            textPosition += lineLength;
            
            if (textPosition < fullText.length() && fullText.charAt(textPosition) == '\n') {
                textPosition++;
            }
        }
        
        int lastLineIndex = lines.size() - 1;
        return new CursorLinePosition(lastLineIndex, lines.get(lastLineIndex).length());
    }
    
    private int getPositionFromLineColumn(int targetLineIndex, int targetColumn) {
        if (targetLineIndex >= lines.size()) {
            return text.length();
        }
        
        String fullText = text.toString();
        int textPosition = 0;
        
        for (int lineIndex = 0; lineIndex < targetLineIndex && lineIndex < lines.size(); lineIndex++) {
            String currentLine = lines.get(lineIndex);
            textPosition += currentLine.length();
            
            if (textPosition < fullText.length() && fullText.charAt(textPosition) == '\n') {
                textPosition++;
            }
        }
        
        int columnInLine = Math.min(targetColumn, lines.get(targetLineIndex).length());
        textPosition += columnInLine;
        
        return Math.min(textPosition, text.length());
    }
    
    private void updateLines() {
        lines.clear();
        
        if (text.length() == 0) {
            lines.add("");
            return;
        }
        
        String fullText = text.toString();
        String[] manualLines = fullText.split("\n", -1);
        
        BufferedImage tempImg = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = tempImg.createGraphics();
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        
        int availableWidth = getWidth() - (padding * 2);
        
        for (String manualLine : manualLines) {
            if (manualLine.isEmpty()) {
                lines.add("");
                continue;
            }
            
            int start = 0;
            while (start < manualLine.length()) {
                int end = manualLine.length();
                String substring = manualLine.substring(start, end);
                
                while (fm.stringWidth(substring) > availableWidth && end > start + 1) {
                    end--;
                    substring = manualLine.substring(start, end);
                }
                
                if (start == end) {
                    end = start + 1;
                }
                
                lines.add(manualLine.substring(start, end));
                start = end;
            }
        }
        
        g.dispose();
        
        if (lines.isEmpty()) {
            lines.add("");
        }
    }
    
    private void scrollToCursor() {
        CursorLinePosition pos = getCursorLinePosition();
        
        // Si el cursor está ANTES del inicio del viewport, ajustar scroll hacia arriba
        if (pos.lineIndex < scrollOffset) {
            scrollOffset = pos.lineIndex;
        }
        
        // Si el cursor está DESPUÉS del final del viewport, ajustar scroll hacia abajo
        if (pos.lineIndex >= scrollOffset + maxVisibleLines) {
            scrollOffset = pos.lineIndex - maxVisibleLines + 1;
        }
        
        // Limitar el scroll
        int maxScroll = Math.max(0, lines.size() - maxVisibleLines);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
    }

    // ==================== RENDERIZADO ====================
    
    @Override
    public void update() {
        if (!focused) return;

        long now = System.currentTimeMillis();
        if (now - lastBlinkTime > CURSOR_DELAY) {
            cursorVisible = !cursorVisible;
            lastBlinkTime = now;
            render();
        }
    }

    @Override
    public void render() {
        int visibleLines = Math.min(lines.size(), maxVisibleLines);
        int newHeight = (visibleLines * lineHeight) + (padding * 2);
        newHeight = Math.max(newHeight, minHeight);
        
        // FORMATO CHAT WhatsApp: última línea en la base, crece hacia arriba
        // baseY es la posición INFERIOR fija (donde está la última línea)
        int newY = baseY - newHeight;
        
        final int MAX_Y_PIXELS = 720;
        
        // Validar que no se salga por arriba
        if (newY < 0) {
            newY = 0;
            newHeight = baseY;
        }
        
        // Validar que baseY no exceda el límite
        if (baseY > MAX_Y_PIXELS) {
            newY = MAX_Y_PIXELS - newHeight;
            if (newY < 0) {
                newY = 0;
                newHeight = MAX_Y_PIXELS;
            }
        }
        
        // IMPORTANTE: Actualizar AMBOS sistemas de posición
        // 1. renderX/renderY (para dibujar) en píxeles
        setRenderPosition(getRenderX(), newY);
        
        // 2. dir (para hitbox) en unidades Core
        int dirX = getRenderX() / SCALE;
        int dirY = newY / SCALE;
        dirX = Math.max(0, Math.min(dirX, 127));
        dirY = Math.max(0, Math.min(dirY, 71));
        setDireccion(new Point(dirX, dirY));
        
        // 3. Altura
        setHeight(newHeight);
        
        // Crear imagen
        BufferedImage img = new BufferedImage(getWidth(), newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Fondo
        g.setComposite(AlphaComposite.Clear);
        g.fillRect(0, 0, getWidth(), newHeight);
        g.setComposite(AlphaComposite.SrcOver);
        
        // Si hay color de fondo, dibujarlo
        if (backgroundColor != null) {
            g.setColor(backgroundColor);
            g.fillRect(0, 0, getWidth(), newHeight);
        }

        // Texto
        g.setColor(textColor);
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        
        // IMPORTANTE: Mostrar líneas según el scrollOffset (controlado por el cursor)
        // NO siempre las últimas, sino las que están en el viewport
        int totalLines = lines.size();
        
        // Si hay pocas líneas, mostrar todas desde el inicio
        if (totalLines <= maxVisibleLines) {
            scrollOffset = 0;
        }
        
        int startLine = scrollOffset;
        int endLine = Math.min(startLine + maxVisibleLines, totalLines);
        
        for (int i = startLine; i < endLine; i++) {
            String line = lines.get(i);
            int y = padding + ((i - startLine) * lineHeight) + fm.getAscent();
            g.drawString(line, padding, y);
        }

        // Cursor
        if (focused && cursorVisible) {
            CursorLinePosition pos = getCursorLinePosition();
            
            // Ajustar índice visual según las líneas mostradas
            if (pos.lineIndex >= startLine && pos.lineIndex < endLine) {
                String lineBeforeCursor = lines.get(pos.lineIndex).substring(0, pos.columnInLine);
                int x = padding + fm.stringWidth(lineBeforeCursor);
                int y = padding + ((pos.lineIndex - startLine) * lineHeight);
                
                g.setColor(cursorColor);
                g.fillRect(x, y, 2, lineHeight - 2);
            }
        }

        g.dispose();
        setImage(img);
    }

    // ==================== MÉTODOS PÚBLICOS ====================
    
    public String getText() {
        return text.toString();
    }

    public void setText(String newText) {
        text = new StringBuilder(newText != null ? newText : "");
        cursorPosition = text.length();
        updateLines();
        scrollToCursor();
        render();
    }

    public void clear() {
        text.setLength(0);
        cursorPosition = 0;
        lines.clear();
        lines.add("");
        scrollOffset = 0;
        render();
    }
    
    public void setFocused(boolean focused) {
        this.focused = focused;
        if (focused) {
            resetBlink();
        } else {
            cursorVisible = false;
        }
        render();
    }
    
    public boolean isFocused() {
        return focused;
    }
    
    public void setTextColor(Color color) {
        this.textColor = color;
        render();
    }
    
    public void setCursorColor(Color color) {
        this.cursorColor = color;
        render();
    }
    
    public void setBackgroundColor(Color color) {
        this.backgroundColor = color;
        render();
    }
    
    public Color getBackgroundColor() {
        return backgroundColor;
    }
    
    public void setFont(Font font) {
        this.font = font;
        updateLines();
        render();
    }

    @Override
    protected void onEnter() {}

    @Override
    protected void onExit() {}

    @Override
    public void onClick() {}

    @Override
    public void offFocus() {
        focused = false;
        cursorVisible = false;
        render();
    }
}