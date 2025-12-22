/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.dungeon_game.core.components;

import com.dungeon_game.core.api.DriverRender;
import com.dungeon_game.core.api.RenderProcessor;
import com.dungeon_game.core.api.Updater;
import com.dungeon_game.core.logic.ClientMessageBus;
import com.dungeon_game.core.logic.GameState;
import com.dungeon_game.core.model.Imagen;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author USUARIO
 */
public class LobbyPartyView implements Updater {

    // Posiciones de los 2 slots animados y sus nombres p :v
    private final Point slotPos1;
    private final Point slotPos2;
    private NameLabel slot1Name;
    private NameLabel slot2Name;


    // Sprites por defecto (puedes cambiar luego por skin real)
    private final List<String> idleFrames;
    private final long frameMs;

    // Estado actual
    private final List<String> currentOthers  = new ArrayList<>(2);

    // Renderables
    private Imagen slot1Img;
    private Imagen slot2Img;

    // Animadores
    private SpriteImageAnimator slot1Anim;
    private SpriteImageAnimator slot2Anim;

    private boolean enabled = false;
    private boolean subscribed = false;

    public LobbyPartyView(Point slotPos1, Point slotPos2, List<String> idleFrames, long frameMs) {
        this.slotPos1 = slotPos1;
        this.slotPos2 = slotPos2;
        this.idleFrames = idleFrames;
        this.frameMs = frameMs;
    }
    
    private void onBusLine(String line) {
        if (!enabled) return; // clave: si no estás en lobby, ignora
        handleLine(line);
    }

    /** Llamar cuando entras al lobby */
    public void enable() {
        if (enabled) return;
        enabled = true;
        ClientMessageBus.getInstance().start();

        // Nos suscribimos UNA sola vez (porque el bus no tiene unsubscribe)
        if (!subscribed) {
            subscribed = true;
            ClientMessageBus.getInstance().subscribe(
                line -> line != null && line.startsWith("PARTY_MEMBERS"),
                this::onBusLine
            );
        }
    }

    /** Llamar cuando sales del lobby */
    public void disable() {
        if (!enabled) return;
        enabled = false;
        clearSlots();
    }

    @Override
    public void update() {
        if (!enabled) return;


    }

    public void handleLine(String line) {
        if (line == null) return;
        line = line.trim();
        if (!line.startsWith("PARTY_MEMBERS")) return;

        String[] parts = line.split("\\s+", 4);
        if (parts.length < 4) return;

        String membersCsv = parts[3].trim();
        String me = GameState.getInstance().getPlayerId();

        List<String> others = new ArrayList<>();
        if (!membersCsv.isEmpty()) {
            for (String s : membersCsv.split(",")) {
                String id = s.trim();
                if (id.isEmpty()) continue;
                if (me != null && id.equalsIgnoreCase(me)) continue;
                others.add(id);
            }
        }

        if (others.size() > 2) others = others.subList(0, 2);
        if (sameOthers(others)) return;

        currentOthers.clear();
        currentOthers.addAll(others);
        applyMembers(currentOthers);
    }

    private boolean sameOthers(List<String> others) {
    if (others.size() != currentOthers.size()) return false;
    for (int i = 0; i < others.size(); i++) {
        if (!others.get(i).equals(currentOthers.get(i))) return false;
    }
    return true;
}

    private void applyMembers(List<String> members) {
        clearSlots();

        if (members.size() >= 1) {
            slot1Img = createSprite(slotPos1);
            slot1Anim = new SpriteImageAnimator(slot1Img, idleFrames, frameMs, true);
            GameState.getInstance().registerUpdater(slot1Anim);
            slot1Name = createNameLabel(members.get(0), slotPos1.x + 80, slotPos1.y - 28);
        }

        if (members.size() >= 2) {
            slot2Img = createSprite(slotPos2);
            slot2Anim = new SpriteImageAnimator(slot2Img, idleFrames, frameMs, true);
            GameState.getInstance().registerUpdater(slot2Anim);
            slot2Name = createNameLabel( members.get(1), slotPos2.x + 80, slotPos2.y - 28);
        }

        DriverRender.getInstance().string();
    }

    private Imagen createSprite(Point pos) {
        Imagen img = new Imagen(pos.x, pos.y, 320, 300, 2, idleFrames.get(0), null, 255);
        RenderProcessor.getInstance().setElement(img);
        return img;
    }

    private void clearSlots() {
        // 1) detener animadores
        if (slot1Anim != null) {
            GameState.getInstance().unregisterUpdater(slot1Anim);
            slot1Anim = null;
        }
        if (slot2Anim != null) {
            GameState.getInstance().unregisterUpdater(slot2Anim);
            slot2Anim = null;
        }

        // 2) eliminar imagen del render + grid
        if (slot1Img != null) {
            DriverRender.getInstance().eliminarNodo(slot1Img);
        
            slot1Img = null;
        }
        if (slot2Img != null) {
            DriverRender.getInstance().eliminarNodo(slot2Img);
       
            slot2Img = null;
        }
        if (slot1Name != null) {
            RenderProcessor.getInstance().eliminarElemento(slot1Name);
            slot1Name = null;
        }
        if (slot2Name != null) {
            RenderProcessor.getInstance().eliminarElemento(slot2Name);
            slot2Name = null;
        }

        DriverRender.getInstance().string();
    }
    public void forceClearOthers() {
        currentOthers.clear();   // si ya cambiaste members->others
        clearSlots();            // borra derecha/izquierda
    }
    private NameLabel createNameLabel(String name, int x, int y) {
        NameLabel label = new NameLabel(name, x, y, 3); // capa superior al sprite
        RenderProcessor.getInstance().setElement(label);
        return label;
    }
}