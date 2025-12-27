/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.dungeon_game.core.components;

import java.awt.Point;

/**
 *
 * @author USUARIO
 */
public class AddFriendButton extends UIButton {

    private final Runnable onHoverEnter;
    private final Runnable onHoverExit;

    public AddFriendButton(int x, int y, int w, int h, int layer,
                           String visualId, Point[] vertices, Point dir,
                           String text,
                           Runnable onHoverEnter,
                           Runnable onHoverExit) {
        super(x, y, w, h, layer, visualId, vertices, dir, text);
        this.onHoverEnter = onHoverEnter;
        this.onHoverExit = onHoverExit;
    }

    @Override
    protected void onEnter() {
        if (onHoverEnter != null) onHoverEnter.run();
    }

    @Override
    protected void onExit() {
        if (onHoverExit != null) onHoverExit.run();
    }
}
