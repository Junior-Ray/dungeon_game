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
import java.awt.Point;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author USUARIO
 */
public class LobbyPartyControls {

    private final UIButton btnLeave;
    private boolean visible = false;

    private final LobbyPartyView partyView; // para limpiar sprites cuando sales

    public LobbyPartyControls(LobbyPartyView partyView) {
        this.partyView = partyView;

        // hitbox base
        Point[] vBtn = new Point[] {
            new Point(0,0), new Point(0,3), new Point(10,3), new Point(10,0)
        };

        // Botón (ajusta posición a tu UI)
        btnLeave = new UIButton(
            940, 520, 220, 50,
            2, null, vBtn,
            new Point(94, 52),
            "SALIR PARTY"
        );

        btnLeave.setOnClickAction(this::leaveParty);
        btnLeave.setEnabled(false); // arranca oculto/desactivado

        // Suscribimos al bus una sola vez
        ClientMessageBus.getInstance().subscribe(
            line ->
                line.startsWith("PARTY_MEMBERS") ||
                line.startsWith("PARTY_INFO") ||
                line.startsWith("PARTY_LEAVE_OK") ||
                line.startsWith("[PARTY]") ||
                line.startsWith("ERROR"),
            this::onServerLine
        );
    }

    /** Llamar cuando entras al lobby */
    public void enable() {
        ClientMessageBus.getInstance().start();
        RenderProcessor.getInstance().setElement(btnLeave);
        btnLeave.render();
        updateVisibilityFromLocalState(false); // lo dejamos apagado hasta que llegue info
        var t = GameState.getInstance().getTransport();
        if (t != null) t.sendCommand("PARTY");
        DriverRender.getInstance().string();
    }

    /** Llamar cuando sales del lobby */
    public void disable() {
        // Si tienes forma de eliminar el nodo, hazlo aquí si quieres
        btnLeave.setEnabled(false);
        visible = false;
        RenderProcessor.getInstance().eliminarElemento(btnLeave); 
        DriverRender.getInstance().string();
    }

    private void leaveParty() {
        var t = GameState.getInstance().getTransport();
        if (t == null) return;
        t.sendCommand("PARTY_LEAVE");
        // opcional: deshabilitar para evitar spam
        btnLeave.setEnabled(false);
        DriverRender.getInstance().string();
    }

    private void onServerLine(String line) {
        line = line.trim();

        if (line.startsWith("PARTY_LEAVE_OK")) {
            // ✅ ya saliste, limpia sprites extra y oculta botón
            partyView.forceClearOthers();

            btnLeave.setEnabled(false);
            visible = false;

            DriverRender.getInstance().eliminarNodo(btnLeave);
            SpatialGrid.getInstance().limpiar(btnLeave);

            DriverRender.getInstance().string();
            return;

        }

        if (line.startsWith("PARTY_MEMBERS")) {
            // PARTY_MEMBERS <partyId> <leaderId> <a,b,c>
            String[] parts = line.split("\\s+", 4);
            if (parts.length < 4 || parts[3].trim().isEmpty()) {
                partyView.forceClearOthers();
                updateVisibilityFromLocalState(false);
                return;
            }

            String me = GameState.getInstance().getPlayerId();
            List<String> members = Arrays.stream(parts[3].split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();

            boolean inParty = (me != null && members.stream().anyMatch(m -> m.equalsIgnoreCase(me)));
            boolean hasOther = (me != null && members.stream().anyMatch(m -> !m.equalsIgnoreCase(me)));

            // Si estás en party y hay al menos otro miembro, mostramos botón
            updateVisibilityFromLocalState(inParty && hasOther);
            return;
        }

        // PARTY_INFO también puede servir si lo usas:
        // PARTY_INFO PartyId=..., Leader=..., Members=...
        if (line.startsWith("PARTY_INFO")) {

            // Caso: "PARTY_INFO No perteneces a ninguna party"
            if (line.contains("No perteneces")) {
                partyView.forceClearOthers();
                updateVisibilityFromLocalState(false);

                return;
            }

            // Caso: "PARTY_INFO PartyId=..., Leader=..., Members=a, b, c"
            String me = GameState.getInstance().getPlayerId();
            int idx = line.indexOf("Members=");
            if (idx < 0 || me == null) return;

            String membersPart = line.substring(idx + "Members=".length()).trim(); // "Juan, Pepe"
            List<String> members = Arrays.stream(membersPart.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();

            boolean inParty = members.stream().anyMatch(m -> m.equalsIgnoreCase(me));
            boolean hasOther = members.stream().anyMatch(m -> !m.equalsIgnoreCase(me));

            updateVisibilityFromLocalState(inParty && hasOther);
            DriverRender.getInstance().string();
            return;
        }

        if (line.startsWith("ERROR")) {
            // si falló salir, re-habilita si estaba visible
            if (visible) btnLeave.setEnabled(true);
            DriverRender.getInstance().string();
        }
    }

    private void updateVisibilityFromLocalState(boolean show) {
        visible = show;
        btnLeave.setEnabled(show);
        if(show){
            showButton();
        }
        else{
            hideButton();
        }
       
        // Si quieres “ocultar visualmente” cuando show=false, puedes renderizarlo transparente
        // o moverlo fuera de pantalla. Por ahora solo lo deshabilitamos.
    }
    private void showButton() {
        RenderProcessor.getInstance().setElement(btnLeave);
        btnLeave.setEnabled(true);
        btnLeave.render();
        DriverRender.getInstance().string();
    }
    private void hideButton() {
        btnLeave.setEnabled(false);
        DriverRender.getInstance().eliminarNodo(btnLeave);
        SpatialGrid.getInstance().limpiar(btnLeave);
        DriverRender.getInstance().string();
    }
}