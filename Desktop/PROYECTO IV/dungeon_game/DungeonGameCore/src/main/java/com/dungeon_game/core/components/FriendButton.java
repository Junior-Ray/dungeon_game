/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.dungeon_game.core.components;

import com.dungeon_game.core.api.DriverRender;
import com.dungeon_game.core.api.RenderProcessor;
import com.dungeon_game.core.data.SpatialGrid;
import com.dungeon_game.core.logic.InterpreterEvent;
import com.dungeon_game.core.model.Imagen;
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
public class FriendButton extends UIButton {

    private final String friendName;
    private final Imagen menuCanvas;
    
    
    private static FriendButton activeOwner = null;

    // Menú interno
    private final List<UIButton> menuOptions = new ArrayList<>();
    private final UIButton btnInvitar;
    private final UIButton btnSusurrar;
    private boolean menuOpen = false;

    // Parámetros visuales del menú
    private static final int MENU_WIDTH = 160;
    private static final int MENU_ITEM_HEIGHT = 26;
    private static final int MENU_MARGIN_Y = 4;

    // OJO: capa del menú (debe ser > capa del FriendButton y <= 9)
    private static final int MENU_BG_LAYER = 8;
     private static final int MENU_BTN_LAYER = 9; 
     
     
     //Referencia global al botón que tiene menú abierto ===
    private static FriendButton activeWithMenu = null;

    public FriendButton(
            int x, int y,
            int width, int height,
            int layer,            // capa del botón del amigo (ej. 7)
            Point[] vertices,
            Point dir,
            String friendName
    ) {
        super(x, y, width, height, layer, null, vertices, dir, friendName);
        
        this.friendName = friendName;
        menuCanvas = new Imagen(0, 0, 1280, 720, MENU_BG_LAYER, null, null, 210);

        // ==== forma base de botones del menú ====
        Point[] vBtnInv = new Point[4];
        vBtnInv[0] = new Point(0, 0);
        vBtnInv[1] = new Point(0, 3);
        vBtnInv[2] = new Point(6, 3);
        vBtnInv[3] = new Point(6, 0);

        // === Botón "Invitar" ===
        btnInvitar = new UIButton(
                0, 0,                        // X/Y se fijan en openMenu()
                MENU_WIDTH, MENU_ITEM_HEIGHT,
                MENU_BTN_LAYER,
                null,
                vBtnInv,
                new Point(0, 0),
                "Invitar"
        );

        btnInvitar.setOnClickAction(() -> {
            if (!menuOpen) return;
            System.out.println("INVITAR a: " + friendName);
            // TODO: lógica real (enviar invitación)
            closeMenu();
        });

        // === Botón "Susurrar" ===
        btnSusurrar = new UIButton(
                0, 0,
                MENU_WIDTH, MENU_ITEM_HEIGHT,
                MENU_BTN_LAYER,
                null,
                vBtnInv,
                new Point(0, 0),
                "Susurrar"
        );

        btnSusurrar.setOnClickAction(() -> {
            if (!menuOpen) return;
            System.out.println("SUSURRAR a: " + friendName);
            // TODO: abrir ventanita de chat
            closeMenu();
        });

        menuOptions.add(btnInvitar);
        menuOptions.add(btnSusurrar);
    }
    public String getFriendName() {
        return friendName;
    }
    public void setSize(Point[] p){
        Point[] vertices = p;
    }
    public void setSizeNull(){
        Point[] p = new Point[4];
        p[0] = new Point(0,0);
        p[1] = new Point(0,0);
        p[2] = new Point(0,0);
        p[3] = new Point(0,0);
        setSize(p);
    }

    @Override
    public void onClick() {
        // Esto lo llama AbstractUIComponent.executeClickAction()
        // antes/además de la lambda de setOnClickAction (si la hubiera)
        if (menuOpen) {
            closeMenu();
        } else {
            if (activeOwner != null && activeOwner != this) {
                activeOwner.closeMenu();
            }
            System.out.println("CLICK en amigo: " + friendName);
            openMenu();

        }

    }


    /** Cierra el menú contextual y elimina sus botones */
    public void closeMenu() {
        if (!menuOpen) return;
        menuOpen = false;
        
         if (activeWithMenu == this) {
            activeWithMenu = null;
        }
        if (activeOwner == this) {
            activeOwner = null;
        }

        // volvemos a permitir click desde la capa 1
        InterpreterEvent.getInstance().setMinActiveLayer(1);

        DriverRender dr = DriverRender.getInstance();
        SpatialGrid grid = SpatialGrid.getInstance();

        dr.eliminarNodo(menuCanvas);
        

        
        for (UIButton btn : menuOptions) {
            dr.eliminarNodo(btn);
            grid.limpiar(btn);
        }

        DriverRender.getInstance().string();
    }


    private void openMenu() {
        if (menuOpen) return;
        menuOpen = true;
        activeOwner = this;
        
        if (activeWithMenu != null && activeWithMenu != this) {
            activeWithMenu.closeMenu();
        }

        activeWithMenu = this;
        menuOpen = true;

        // ====== calcular posición del panel ======
        int panelX = getRenderX() + getWidth() + 10;
        int panelY = getRenderY() - 4;
        int panelW = MENU_WIDTH + 20;
        int panelH = MENU_ITEM_HEIGHT * menuOptions.size()
                   + MENU_MARGIN_Y * (menuOptions.size() + 1);

        // seguridad para no salir de pantalla (opcional)
        if (panelX + panelW > 1280) {
            panelX = getRenderX() - panelW - 10;
        }

        // ====== dibujar el panel en un BufferedImage ======
        BufferedImage img = new BufferedImage(1280, 720, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();

        // Fondo ligeramente transparente sólo en la zona del panel
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.0f));
        g2d.setColor(new Color(0, 0, 0, 0));
        g2d.fillRect(0, 0, 1280, 720);

        g2d.setComposite(AlphaComposite.SrcOver);
        // Panel
        g2d.setColor(new Color(40, 40, 70, 230));
        g2d.fillRoundRect(panelX, panelY, panelW, panelH, 10, 10);
        g2d.setColor(new Color(180, 180, 220));
        g2d.drawRoundRect(panelX, panelY, panelW, panelH, 10, 10);

        // Título opcional arriba del menú
        g2d.setColor(Color.WHITE);
        g2d.drawString("Opciones", panelX + 8, panelY + 14);

        g2d.dispose();
        menuCanvas.setImage(img);

        // ====== posicionar botones dentro del panel ======
        int btnX = panelX + 10;
        int btnY = panelY + 18;

        for (UIButton btn : menuOptions) {
            btn.moveTo(btnX, btnY);
            btn.render(); // genera su imagen interna
            btnY += MENU_ITEM_HEIGHT + MENU_MARGIN_Y;
        }

        // ====== registrar en RenderProcessor ======
        RenderProcessor rp = RenderProcessor.getInstance();
        rp.setElement(menuCanvas);
        for (UIButton btn : menuOptions) {
            rp.setElement(btn);
        }

        // mientras el menú esté abierto, podemos limitar el input a las capas altas
        InterpreterEvent.getInstance().setMinActiveLayer(9);
        DriverRender.getInstance().string();
    }
    public void dispose() {
        closeMenu();
        // nada más por ahora: LobbyFriendsOverlay se encarga de sacarlo del RenderProcessor
    }
    public static void closeAnyOpenMenu() {
        if (activeWithMenu != null) {
            activeWithMenu.closeMenu();
        }
    }
}