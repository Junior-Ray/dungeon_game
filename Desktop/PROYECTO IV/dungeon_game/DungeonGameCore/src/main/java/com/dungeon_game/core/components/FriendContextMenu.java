/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.dungeon_game.core.components;

import com.dungeon_game.core.api.RenderProcessor;
import com.dungeon_game.core.data.SpatialGrid;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author USUARIO
 */
public class FriendContextMenu {

    private final List<UIButton> options = new ArrayList<>();
    private boolean visible = false;

    // info del amigo seleccionado (por ahora solo nombre)
    private String currentFriendName;

    private FriendButton currentAnchor;

    // botones
    private final UIButton btnInvitar;
    private final UIButton btnSusurrar;

    // offset relativo al "anchor" (la fila del amigo)
    private static final int MENU_WIDTH = 140;
    private static final int MENU_ITEM_HEIGHT = 24;
    private static final int MENU_MARGIN_Y = 2;

    public FriendContextMenu() {

        Point[] vBtn = new Point[4];
        vBtn[0] = new Point(0, 0);
        vBtn[1] = new Point(0, 3);
        vBtn[2] = new Point(6, 3);
        vBtn[3] = new Point(6, 0);

        // ⚠ Capas 0–9 → usamos 8 para estar por encima del overlay (7)
        int layer = 8;

        btnInvitar = new UIButton(
                0, 0,
                MENU_WIDTH, MENU_ITEM_HEIGHT,
                layer,
                null,              // visualId null → no busca PNG
                vBtn,
                new Point(4, 3),
                "Invitar"
        );

        btnSusurrar = new UIButton(
                0, 0,
                MENU_WIDTH, MENU_ITEM_HEIGHT,
                layer,
                null,
                vBtn,
                new Point(4, 3),
                "Susurrar"
        );

        options.add(btnInvitar);
        options.add(btnSusurrar);

        // ==== ACCIONES ====

        btnInvitar.setOnClickAction(() -> {
            if (!visible) return;
            System.out.println("INVITAR a: " + currentFriendName);
            // TODO: lógica real de invitar al amigo
            close();
        });

        btnSusurrar.setOnClickAction(() -> {
            if (!visible) return;
            System.out.println("SUSURRAR a: " + currentFriendName);
            // TODO: abrir ventanita de chat / susurro
            close();
        });
    }
    public void openForFriend(String friendName, int anchorX, int anchorY) {
        // Si ya hay menú abierto, primero lo cerramos
        close();

        this.currentFriendName = friendName;
        this.visible = true;

        int baseX = anchorX + 10;                   // un poco a la derecha del amigo
        int baseY = anchorY - MENU_ITEM_HEIGHT / 2; // centrado verticalmente

        int yCursor = baseY;
        RenderProcessor rp = RenderProcessor.getInstance();

        for (UIButton btn : options) {
            btn.moveTo(baseX, yCursor);
            yCursor += MENU_ITEM_HEIGHT + MENU_MARGIN_Y;
            rp.setElement(btn); // registra → SpatialGrid + DriverRender
        }
    }

    /**
     * Abre el menú para un amigo específico en una posición cercana
     * @param friendName nombre del amigo
     * @param anchorX    X de referencia (por ej. inicio de la fila del amigo)
     * @param anchorY    Y de referencia (la línea del texto del amigo)
     */


    /** Cierra el menú y quita sus botones de la escena */
    public void close() {
        if (!visible) return;

        System.out.println("CERRAR MENU CONTEXTUAL");
        visible = false;
        currentAnchor = null;

        RenderProcessor rp = RenderProcessor.getInstance();
        SpatialGrid grid = SpatialGrid.getInstance();
        
        for (UIButton btn : options) {
            rp.eliminarElemento(btn);  // los sacamos del render y del grid
            grid.limpiar(btn);
            
        }
    }

    /** Útil si haces click fuera del menú y quieres cerrarlo */
    public boolean isVisible() {
        return visible;
    }
    public void closeForAnchor(FriendButton anchor) {
        if (visible && currentAnchor == anchor) {
            close();
        }
    }
    public void dispose() {
        close();
    }
}