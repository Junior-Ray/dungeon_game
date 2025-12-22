/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.dungeon_game.core.components;

import com.dungeon_game.core.api.DriverRender;
import com.dungeon_game.core.api.RenderProcessor;
import com.dungeon_game.core.data.SpatialGrid;
import com.dungeon_game.core.logic.GameState;
import com.dungeon_game.core.logic.InterpreterEvent;
import com.dungeon_game.core.model.Imagen;
import com.dungeon_game.core.net.GameTransport;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;

/**
 *
 * @author USUARIO
 */
public class InviteOverlay implements com.dungeon_game.core.api.Updater {

    // modal overlay en toda la pantalla
    private final Imagen canvas;

    private final UIButton btnInvite;
    private final UIButton btnClose;

    private final InputText inputName;

    private boolean isOpen = false;

    // para repintar cuando cambia el input
    private String lastText = "";
    private boolean lastEmpty = true;

    public InviteOverlay() {

        canvas = new Imagen(0, 0, 1280, 720, 6, null, null, 220);

        // Base de botones
        Point[] vBtn = new Point[4];
        vBtn[0] = new Point(0, 0);
        vBtn[1] = new Point(0, 4);
        vBtn[2] = new Point(12, 4);
        vBtn[3] = new Point(12, 0);

        // Botón INVITAR (hitbox)
        btnInvite = new UIButton(
                540, 380, 120, 40,
                7, null, vBtn,
                new Point(54, 38),
                "INVITAR"
        );

        // Botón CERRAR (hitbox)
        btnClose = new UIButton(
                680, 380, 120, 40,
                7, null, vBtn,
                new Point(68, 38),
                "CERRAR"
        );

        // InputText (hitbox)
        // Ajusta vertices/dir igual a como lo vienes creando en ChatOverlay
        Point[] ver = new Point[4];
        ver[0] = new Point(0, 0);
        ver[1] = new Point(24, 0);
        ver[2] = new Point(24, 3);
        ver[3] = new Point(0, 3);

        // zona donde se escribe (dentro del panel)
        Imagen inputImg = new Imagen(
                470, 345,   // x,y (puedes ajustar)
                340, 28,    // w,h
                7,
                null, null, 255
        );

        inputName = new InputText(ver, new Point(47, 35), inputImg);
        inputName.setAllowEnter(false);

        // handlers
        btnClose.setOnClickAction(this::close);
        btnInvite.setOnClickAction(this::sendInvite);
    }

    public void open() {
        if (isOpen) return;
        isOpen = true;

        // Bloquea clicks por debajo
        InterpreterEvent.getInstance().setMinActiveLayer(6);
        
        canvas.setImage(buildImage());

        // Pintar
        redraw();

        RenderProcessor.getInstance().setElement(canvas);
        RenderProcessor.getInstance().setElement(inputName);
        RenderProcessor.getInstance().setElement(btnInvite);
        RenderProcessor.getInstance().setElement(btnClose);

        inputName.setEnabled(true);
        btnInvite.setEnabled(true);
        btnClose.setEnabled(true);

        // IMPORTANTE: registrarlo al update loop igual que ChatOverlay
        GameState.getInstance().registerUpdater(this);

        DriverRender.getInstance().string();
    }

    public void close() {
        if (!isOpen) return;
        isOpen = false;

        InterpreterEvent.getInstance().setMinActiveLayer(1);

        DriverRender dr = DriverRender.getInstance();
        SpatialGrid grid = SpatialGrid.getInstance();

        dr.eliminarNodo(canvas);
        dr.eliminarNodo(inputName);
        dr.eliminarNodo(btnInvite);
        dr.eliminarNodo(btnClose);

        
        grid.limpiar(inputName);
        grid.limpiar(btnInvite);
        grid.limpiar(btnClose);

        inputName.setEnabled(false);
        btnInvite.setEnabled(false);
        btnClose.setEnabled(false);

        inputName.clear();
        inputName.render();

        GameState.getInstance().unregisterUpdater(this);

        DriverRender.getInstance().string();
    }

    @Override
    public void update() {
        if (!isOpen) return;

        // Actualiza cursor blink etc
        inputName.update();

        // Si cambia texto/estado vacío => repintar el canvas (placeholder + caja)
        String now = inputName.getText();
        boolean empty = now.trim().isEmpty();

        if (!now.equals(lastText) || empty != lastEmpty) {
            lastText = now;
            lastEmpty = empty;
            redraw();
        }
    }

    private void sendInvite() {
        String target = inputName.getText().trim();
        if (target.isEmpty()) return;

        GameTransport t = GameState.getInstance().getTransport();
        if (t == null) {
            // si quieres, podrías mandar un mensaje al chat overlay o al inbox
            System.out.println("[InviteOverlay] No hay transport conectado.");
            return;
        }

        t.sendCommand("INVITE " + target);

        // opcional: limpiar y cerrar
        inputName.clear();
        inputName.render();
        close();
    }

    private void redraw() {
        canvas.setImage(buildImage());
        DriverRender.getInstance().string();
    }

    private BufferedImage buildImage() {
        BufferedImage img = new BufferedImage(1280, 720, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();

        // oscurecer fondo
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.95f));
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, 1280, 720);
        g2d.setComposite(AlphaComposite.SrcOver);

        // panel
        g2d.setColor(new Color(120, 120, 200, 220));
        g2d.fillRoundRect(440, 260, 400, 220, 30, 30);

        g2d.setColor(new Color(40, 40, 80, 230));
        g2d.fillRoundRect(450, 270, 380, 200, 30, 30);

        // textos
        g2d.setColor(Color.WHITE);
        g2d.drawString("INVITAR AMIGO", 540, 295);

        g2d.setColor(Color.LIGHT_GRAY);
        g2d.drawString("Ingresa el ID o nombre del amigo", 470, 330);

        // caja input (visual)
        g2d.setColor(new Color(25, 25, 50, 255));
        g2d.fillRoundRect(468, 340, 344, 34, 12, 12);
        g2d.setColor(new Color(90, 90, 140));
        g2d.drawRoundRect(468, 340, 344, 34, 12, 12);

        // placeholder (lo que tú querías)
        if (inputName.getText().trim().isEmpty()) {
            g2d.setColor(new Color(180, 180, 220));
            g2d.drawString("Ej: pepe", 480, 362);
        }

        // botón INVITAR (visual)
        g2d.setColor(new Color(230, 210, 90));
        g2d.fillRoundRect(540, 390, 120, 40, 12, 12);
        g2d.setColor(Color.DARK_GRAY);
        g2d.drawRoundRect(540, 390, 120, 40, 12, 12);
        g2d.setColor(Color.BLACK);
        g2d.drawString("INVITAR", 565, 415);

        // botón CERRAR (visual)
        g2d.setColor(new Color(220, 90, 90));
        g2d.fillRoundRect(680, 390, 120, 40, 12, 12);
        g2d.setColor(Color.DARK_GRAY);
        g2d.drawRoundRect(680, 390, 120, 40, 12, 12);
        g2d.setColor(Color.BLACK);
        g2d.drawString("CERRAR", 705, 415);

        g2d.dispose();
        return img;
    }
}