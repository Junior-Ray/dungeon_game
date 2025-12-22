/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.dungeon_game.core.components;

import com.dungeon_game.core.api.DriverRender;
import com.dungeon_game.core.api.RenderProcessor;
import com.dungeon_game.core.data.SpatialGrid;
import com.dungeon_game.core.logic.ClientMessageBus;
import com.dungeon_game.core.logic.GameState;
import com.dungeon_game.core.logic.InterpreterEvent;
import com.dungeon_game.core.model.Imagen;
import com.dungeon_game.core.net.GameTransport;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author USUARIO
 */
public class InviteOverlay2 implements com.dungeon_game.core.api.Updater {

    // ===== Panel =====
    private static final int PX = 300;
    private static final int PY = 80;
    private static final int PW = 680;
    private static final int PH = 560;

    private final Imagen canvas;

    // ===== UI =====
    private final UIButton btnClose;

    // Input invitar por ID
    private final InputText inviteInput;
    private final UIButton btnInviteById;

    // Slots UI (fijos) para lista dinámica
    private static final int INVITE_ROWS = 4;  // invitaciones recibidas
    private static final int PLAYER_ROWS = 6;  // jugadores disponibles

    private final UIButton[] inviteAcceptBtns = new UIButton[INVITE_ROWS];
    private final UIButton[] inviteDeclineBtns = new UIButton[INVITE_ROWS];

    private final UIButton[] playerInviteBtns = new UIButton[PLAYER_ROWS];

    // ===== Estado =====
    private boolean isOpen = false;

    private final List<String> onlinePlayers = new ArrayList<>();
    private final List<String> pendingInvites = new ArrayList<>(); // lista de FROM ids

    // Mensaje tipo toast
    private String toastText = null;
    private long toastUntilMs = 0;

    // refresco players
    private long lastPlayersRequestMs = 0;
    private static final long PLAYERS_REFRESH_MS = 1000;
    
    private boolean subscribed = false;
    
    public InviteOverlay2() {
        canvas = new Imagen(0, 0, 1280, 720, 6, null, null, 220);

        // Forma base de botones (tu hitbox típica)
        Point[] vBtn = new Point[4];
        vBtn[0] = new Point(0, 0);
        vBtn[1] = new Point(0, 3);
        vBtn[2] = new Point(6, 3);
        vBtn[3] = new Point(6, 0);

        // Close
        btnClose = new UIButton(
                PX + PW - 34, PY + 10,
                24, 24,
                7, null, vBtn,
                new Point((PX + PW - 34) / 10, (PY + 10) / 10),
                "X"
        );
        btnClose.setOnClickAction(this::close);

        // ===== InputText INVITE BY ID =====
        int inputX = PX + 24;
        int inputY = PY + 60;
        int inputW = PW - 24 - 24 - 120 - 10;
        int inputH = 30;

        Point[] vInput = new Point[4];
        vInput[0] = new Point(0, 0);
        vInput[1] = new Point(24, 0);
        vInput[2] = new Point(24, 3);
        vInput[3] = new Point(0, 3);

        Imagen inputImg = new Imagen(inputX, inputY, inputW, inputH, 7, null, null, 255);
        inviteInput = new InputText(vInput, new Point(inputX / 10, inputY / 10), inputImg);
        inviteInput.setAllowEnter(false);

        // Botón "INVITAR" (por ID)
        btnInviteById = new UIButton(
                inputX + inputW + 10, inputY,
                120, inputH,
                7, null, vBtn,
                new Point((inputX + inputW + 10) / 10, inputY / 10),
                "INVITAR"
        );
        btnInviteById.setOnClickAction(this::inviteById);

        // ===== Slots: Invitaciones recibidas =====
        int invitesStartY = PY + 120;
        int rowH = 38;

        for (int i = 0; i < INVITE_ROWS; i++) {
            int y = invitesStartY + 28 + (i * rowH);

            // ACEPTAR
            inviteAcceptBtns[i] = new UIButton(
                    PX + PW - 24 - 140, y,
                    90, 28,
                    7, null, vBtn,
                    new Point((PX + PW - 24 - 140) / 10, y / 10),
                    "OK"
            );

            // RECHAZAR
            inviteDeclineBtns[i] = new UIButton(
                    PX + PW - 24 - 45, y,
                    35, 28,
                    7, null, vBtn,
                    new Point((PX + PW - 24 - 45) / 10, y / 10),
                    "X"
            );

            // Inicialmente desactivados
            inviteAcceptBtns[i].setEnabled(false);
            inviteDeclineBtns[i].setEnabled(false);
        }

        // ===== Slots: Players disponibles =====
        int playersStartY = invitesStartY + 28 + (INVITE_ROWS * rowH) + 60;

        for (int i = 0; i < PLAYER_ROWS; i++) {
            int y = playersStartY + 28 + (i * rowH);

            playerInviteBtns[i] = new UIButton(
                    PX + PW - 24 - 120, y,
                    110, 28,
                    7, null, vBtn,
                    new Point((PX + PW - 24 - 120) / 10, y / 10),
                    "INVITE"
            );
            playerInviteBtns[i].setEnabled(false);
        }
    }

    // ================== PUBLICO ==================

    public void open() {
        if (isOpen) return;
        
        GameState.getInstance().registerUpdater(this);
        isOpen = true;

        InterpreterEvent.getInstance().setMinActiveLayer(6);

        RenderProcessor.getInstance().setElement(canvas);
        RenderProcessor.getInstance().setElement(btnClose);

        RenderProcessor.getInstance().setElement(inviteInput);
        RenderProcessor.getInstance().setElement(btnInviteById);

        for (int i = 0; i < INVITE_ROWS; i++) {
            RenderProcessor.getInstance().setElement(inviteAcceptBtns[i]);
            RenderProcessor.getInstance().setElement(inviteDeclineBtns[i]);
        }
        for (int i = 0; i < PLAYER_ROWS; i++) {
            RenderProcessor.getInstance().setElement(playerInviteBtns[i]);
        }
        ClientMessageBus.getInstance().subscribe(
            line ->
                line.startsWith("PLAYERS") ||
                line.startsWith("[INVITE]") ||
                line.startsWith("INVITE_OK") ||
                line.startsWith("ACCEPT_OK") ||
                line.startsWith("DECLINE_OK") ||
                line.startsWith("ERROR"),
            line -> {
                if (handleServerMessage(line)) {
                    redraw();
                }
            }
        );

        inviteInput.setEnabled(true);
        btnInviteById.setEnabled(true);
        btnClose.setEnabled(true);

        // pedir lista inicial
        requestPlayers();

        redraw();
    }

    public void close() {
        if (!isOpen) return;
        isOpen = false;
        
        GameState.getInstance().unregisterUpdater(this);

        InterpreterEvent.getInstance().setMinActiveLayer(1);

        DriverRender dr = DriverRender.getInstance();
        SpatialGrid grid = SpatialGrid.getInstance();

        dr.eliminarNodo(canvas);
        dr.eliminarNodo(btnClose);

        dr.eliminarNodo(inviteInput);
        dr.eliminarNodo(btnInviteById);

        for (int i = 0; i < INVITE_ROWS; i++) {
            dr.eliminarNodo(inviteAcceptBtns[i]);
            dr.eliminarNodo(inviteDeclineBtns[i]);
        }
        for (int i = 0; i < PLAYER_ROWS; i++) {
            dr.eliminarNodo(playerInviteBtns[i]);
        }

        grid.limpiar(btnClose);
        grid.limpiar(inviteInput);
        grid.limpiar(btnInviteById);

        for (int i = 0; i < INVITE_ROWS; i++) {
            grid.limpiar(inviteAcceptBtns[i]);
            grid.limpiar(inviteDeclineBtns[i]);
        }
        for (int i = 0; i < PLAYER_ROWS; i++) {
            grid.limpiar(playerInviteBtns[i]);
        }

        btnClose.setEnabled(false);
        inviteInput.setEnabled(false);
        btnInviteById.setEnabled(false);

        for (int i = 0; i < INVITE_ROWS; i++) {
            inviteAcceptBtns[i].setEnabled(false);
            inviteDeclineBtns[i].setEnabled(false);
        }
        for (int i = 0; i < PLAYER_ROWS; i++) {
            playerInviteBtns[i].setEnabled(false);
        }

        DriverRender.getInstance().string();
    }

    public void update() {
        if (!isOpen) return;

        inviteInput.update();

        long now = System.currentTimeMillis();
        if (now - lastPlayersRequestMs >= PLAYERS_REFRESH_MS) {
            requestPlayers();
        }

        if (toastText != null && now > toastUntilMs) {
            toastText = null;
            redraw();
        }
    }

    // ================== LOGICA ==================

    private void inviteById() {
        String target = inviteInput.getText().trim();
        if (target.isEmpty()) {
            showToast("Escribe un ID primero");
            redraw();
            return;
        }

        GameTransport t = GameState.getInstance().getTransport();
        if (t == null) {
            showToast("Sin conexión");
            redraw();
            return;
        }

        t.sendCommand("INVITE " + target);
        inviteInput.clear();
        inviteInput.render();
        showToast("Invitación enviada a " + target);
        redraw();
    }

    private void acceptInvite(String from) {
        GameTransport t = GameState.getInstance().getTransport();
        if (t == null) {
            showToast("Sin conexión");
            return;
        }
        t.sendCommand("ACCEPT " + from);
        // optimista: lo quitamos de la lista local
        pendingInvites.remove(from);
        showToast("Aceptaste a " + from);
    }

    private void declineInvite(String from) {
        GameTransport t = GameState.getInstance().getTransport();
        if (t == null) {
            showToast("Sin conexión");
            return;
        }
        t.sendCommand("DECLINE " + from);
        pendingInvites.remove(from);
        showToast("Rechazaste a " + from);
    }

    private void invitePlayerFromList(String playerId) {
        GameTransport t = GameState.getInstance().getTransport();
        if (t == null) {
            showToast("Sin conexión");
            return;
        }
        t.sendCommand("INVITE " + playerId);
        showToast("Invitación enviada a " + playerId);
    }

    private void requestPlayers() {
        lastPlayersRequestMs = System.currentTimeMillis();
        GameTransport t = GameState.getInstance().getTransport();
        if (t != null) {
            t.sendCommand("PLAYERS");
        }
    }

    /**
     * Devuelve true si cambió estado UI.
     */
    private boolean handleServerMessage(String line) {
        if (line == null) return false;
        line = line.trim();
        if (line.isEmpty()) return false;

        // 1) PLAYERS ...
        // Ej: "PLAYERS Lobby: Juan, Pepe"
        if (line.startsWith("PLAYERS")) {
            parsePlayers(line);
            return true;
        }

        // 2) Invitación entrante (tu server manda algo como):
        // "[INVITE] Juan te ha invitado..."
        if (line.startsWith("[INVITE]")) {
            String from = extractInviteFrom(line);
            if (from != null && !from.isBlank() && !pendingInvites.contains(from)) {
                pendingInvites.add(from);
                showToast("Invitación recibida de " + from);
                return true;
            }
        }

        // 3) Mensajes informativos que puedes mostrar como toast
        if (line.startsWith("INVITE_OK")) {
            showToast("Invitación enviada");
            return true;
        }
        if (line.startsWith("ERROR")) {
            showToast(line);
            return true;
        }
        if (line.startsWith("ACCEPT_OK")) {
            showToast("Invitación aceptada");
            return true;
        }
        if (line.startsWith("DECLINE_OK")) {
            showToast("Invitación rechazada");
            return true;
        }

        return false;
    }

    private void parsePlayers(String line) {
        // "PLAYERS Lobby: A, B, C"
        int idx = line.indexOf(':');
        if (idx < 0) return;

        String listPart = line.substring(idx + 1).trim();
        onlinePlayers.clear();

        if (listPart.isEmpty() || listPart.equalsIgnoreCase("(vacío)") || listPart.equalsIgnoreCase("Dungeon vacío")) {
            return;
        }

        // split por coma
        String[] parts = listPart.split(",");
        for (String p : parts) {
            String name = p.trim();
            if (!name.isEmpty()) onlinePlayers.add(name);
        }

        // opcional: quitarte a ti mismo
        String me = GameState.getInstance().getPlayerId();
        if (me != null) {
            onlinePlayers.removeIf(n -> n.equalsIgnoreCase(me));
        }
    }

    private String extractInviteFrom(String line) {
        // Ej server: "[INVITE] Juan te ha invitado a su party..."
        // => FROM = primer token después de ] (o después de "[INVITE] ")
        String s = line.replace("[INVITE]", "").trim();
        if (s.isEmpty()) return null;
        String[] parts = s.split("\\s+");
        return (parts.length >= 1) ? parts[0].trim() : null;
    }

    private void showToast(String text) {
        toastText = text;
        toastUntilMs = System.currentTimeMillis() + 2000;
    }

    // ================== RENDER ==================

    private void redraw() {
        canvas.setImage(buildInviteImage());
        refreshSlotButtons(); // MUY importante: reconectar acciones según lista actual
        DriverRender.getInstance().string();
    }

    private void refreshSlotButtons() {
        // ===== Invitaciones recibidas =====
        for (int i = 0; i < INVITE_ROWS; i++) {
            if (i < pendingInvites.size()) {
                String from = pendingInvites.get(i);

                inviteAcceptBtns[i].setEnabled(true);
                inviteDeclineBtns[i].setEnabled(true);

                inviteAcceptBtns[i].setOnClickAction(() -> {
                    acceptInvite(from);
                    redraw();
                });

                inviteDeclineBtns[i].setOnClickAction(() -> {
                    declineInvite(from);
                    redraw();
                });
            } else {
                inviteAcceptBtns[i].setEnabled(false);
                inviteDeclineBtns[i].setEnabled(false);
                inviteAcceptBtns[i].setOnClickAction(null);
                inviteDeclineBtns[i].setOnClickAction(null);
            }
        }

        // ===== Players disponibles =====
        for (int i = 0; i < PLAYER_ROWS; i++) {
            if (i < onlinePlayers.size()) {
                String pid = onlinePlayers.get(i);

                playerInviteBtns[i].setEnabled(true);
                playerInviteBtns[i].setOnClickAction(() -> {
                    invitePlayerFromList(pid);
                    redraw();
                });
            } else {
                playerInviteBtns[i].setEnabled(false);
                playerInviteBtns[i].setOnClickAction(null);
            }
        }
    }

    private BufferedImage buildInviteImage() {
        BufferedImage img = new BufferedImage(1280, 720, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();

        // fondo transparente
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.0f));
        g2d.setColor(new Color(0, 0, 0, 0));
        g2d.fillRect(0, 0, 1280, 720);
        g2d.setComposite(AlphaComposite.SrcOver);

        // panel
        g2d.setColor(new Color(40, 40, 70, 230));
        g2d.fillRoundRect(PX, PY, PW, PH, 22, 22);
        g2d.setColor(new Color(20, 20, 40, 255));
        g2d.fillRoundRect(PX + 4, PY + 4, PW - 8, PH - 8, 22, 22);

        // header
        g2d.setColor(Color.WHITE);
        g2d.drawString("TEAM UP", PX + 20, PY + 22);

        // close (visual)
        g2d.setColor(new Color(220, 90, 90));
        g2d.fillOval(PX + PW - 34, PY + 10, 24, 24);
        g2d.setColor(Color.BLACK);
        g2d.drawString("X", PX + PW - 26, PY + 27);

        // ===== Top: invite by id =====
        int inputY = PY + 60;

        g2d.setColor(new Color(180, 180, 220));
        g2d.drawString("Invitar por ID", PX + 24, inputY - 10);

        // caja input (visual)
        g2d.setColor(new Color(25, 25, 50, 255));
        g2d.fillRoundRect(PX + 24, inputY, PW - 24 - 24 - 120 - 10, 30, 12, 12);
        g2d.setColor(new Color(90, 90, 140));
        g2d.drawRoundRect(PX + 24, inputY, PW - 24 - 24 - 120 - 10, 30, 12, 12);

        if (inviteInput.getText().isEmpty()) {
            g2d.setColor(new Color(180, 180, 220));
            g2d.drawString("ID o nombre...", PX + 34, inputY + 20);
        }

        // botón invitar (visual)
        int btnX = PX + 24 + (PW - 24 - 24 - 120 - 10) + 10;
        g2d.setColor(new Color(90, 200, 120));
        g2d.fillRoundRect(btnX, inputY, 120, 30, 12, 12);
        g2d.setColor(Color.DARK_GRAY);
        g2d.drawRoundRect(btnX, inputY, 120, 30, 12, 12);
        g2d.setColor(Color.BLACK);
        g2d.drawString("INVITAR", btnX + 34, inputY + 20);

        // ===== Section: invites =====
        int invitesTop = PY + 120;

        g2d.setColor(new Color(180, 180, 220));
        g2d.drawString("INVITACIONES", PX + 24, invitesTop + 10);

        // caja sección
        g2d.setColor(new Color(15, 15, 30, 255));
        g2d.fillRoundRect(PX + 16, invitesTop + 18, PW - 32, 28 + INVITE_ROWS * 38, 16, 16);
        g2d.setColor(new Color(80, 80, 130));
        g2d.drawRoundRect(PX + 16, invitesTop + 18, PW - 32, 28 + INVITE_ROWS * 38, 16, 16);

        // filas invites
        int rowY = invitesTop + 18 + 28;
        for (int i = 0; i < INVITE_ROWS; i++) {
            int y = rowY + i * 38;

            g2d.setColor(new Color(255, 255, 255));
            String text = (i < pendingInvites.size())
                    ? pendingInvites.get(i)
                    : "(vacío)";
            if (i >= pendingInvites.size()) {
                g2d.setColor(new Color(140, 140, 170));
            }
            g2d.drawString(text, PX + 30, y + 18);

            if (i < pendingInvites.size()) {
                // dibujar botones visuales
                int okX = PX + PW - 24 - 140;
                int xX  = PX + PW - 24 - 45;

                g2d.setColor(new Color(230, 210, 90));
                g2d.fillRoundRect(okX, y, 90, 28, 12, 12);
                g2d.setColor(Color.DARK_GRAY);
                g2d.drawRoundRect(okX, y, 90, 28, 12, 12);
                g2d.setColor(Color.BLACK);
                g2d.drawString("ACEPTAR", okX + 12, y + 18);

                g2d.setColor(new Color(220, 90, 90));
                g2d.fillRoundRect(xX, y, 35, 28, 12, 12);
                g2d.setColor(Color.DARK_GRAY);
                g2d.drawRoundRect(xX, y, 35, 28, 12, 12);
                g2d.setColor(Color.BLACK);
                g2d.drawString("X", xX + 14, y + 18);
            }
        }

        // ===== Section: players =====
        int playersTop = invitesTop + 18 + 28 + INVITE_ROWS * 38 + 40;

        g2d.setColor(new Color(180, 180, 220));
        g2d.drawString("JUGADORES DISPONIBLES", PX + 24, playersTop + 10);

        g2d.setColor(new Color(15, 15, 30, 255));
        g2d.fillRoundRect(PX + 16, playersTop + 18, PW - 32, 28 + PLAYER_ROWS * 38, 16, 16);
        g2d.setColor(new Color(80, 80, 130));
        g2d.drawRoundRect(PX + 16, playersTop + 18, PW - 32, 28 + PLAYER_ROWS * 38, 16, 16);

        int pRowY = playersTop + 18 + 28;
        for (int i = 0; i < PLAYER_ROWS; i++) {
            int y = pRowY + i * 38;

            if (i < onlinePlayers.size()) {
                String pid = onlinePlayers.get(i);
                g2d.setColor(Color.WHITE);
                g2d.drawString(pid, PX + 30, y + 18);

                int invX = PX + PW - 24 - 120;
                g2d.setColor(new Color(90, 200, 120));
                g2d.fillRoundRect(invX, y, 110, 28, 12, 12);
                g2d.setColor(Color.DARK_GRAY);
                g2d.drawRoundRect(invX, y, 110, 28, 12, 12);
                g2d.setColor(Color.BLACK);
                g2d.drawString("INVITAR", invX + 28, y + 18);
            } else {
                g2d.setColor(new Color(140, 140, 170));
                g2d.drawString("(vacío)", PX + 30, y + 18);
            }
        }

        // ===== Toast =====
        if (toastText != null) {
            g2d.setColor(new Color(0, 0, 0, 160));
            g2d.fillRoundRect(PX + 180, PY + 10, 320, 26, 12, 12);
            g2d.setColor(Color.WHITE);
            g2d.drawString(toastText, PX + 190, PY + 28);
        }

        g2d.dispose();
        return img;
    }

    // getters opcionales
    public boolean isOpen() { return isOpen; }
    public Imagen getCanvas() { return canvas; }
}