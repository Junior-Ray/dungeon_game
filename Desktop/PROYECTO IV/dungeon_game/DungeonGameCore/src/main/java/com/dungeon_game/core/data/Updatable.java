/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.dungeon_game.core.data;

import com.dungeon_game.core.api.Updater;

/**
 *
 * @author GABRIEL SALGADO
 */
public abstract class Updatable implements Updater {

    private Runnable extraUpdate;  // acción externa opcional

    @Override
    public final void update() {
        // Código común
        commonUpdate();

        // Código personalizado de la clase derivada
        onUpdate();

        // Código pasado desde el exterior
        if (extraUpdate != null) {
            extraUpdate.run();
        }
    }

    protected void commonUpdate() {
        // lógica que se ejecuta SIEMPRE
        // (ejemplo: física, estado, animaciones internas...)
    }

    protected void onUpdate() {
        // las subclases pueden sobrescribir este método
    }

    public void setOnUpdateAction(Runnable action) {
        this.extraUpdate = action;
    }
}
