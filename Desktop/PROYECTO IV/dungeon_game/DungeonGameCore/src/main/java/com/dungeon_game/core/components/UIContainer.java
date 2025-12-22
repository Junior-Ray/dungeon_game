/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.dungeon_game.core.components;

/**
 *
 * @author USUARIO
 */


import com.dungeon_game.core.api.VisualRenderable;
import com.dungeon_game.core.data.RenderableVisual;
import com.dungeon_game.core.data.VisualRender;
import java.util.ArrayList;
import java.util.List;

public class UIContainer extends RenderableVisual {

    private final List<VisualRenderable> children = new ArrayList<>();

    public UIContainer(int x, int y, int w, int h, int layer) {
        super(
            null,
            null,     // no imagen propia
            x, y,
            w, h,
            layer,
            255
        );
    }

    public void add(VisualRenderable child) {
        children.add(child);
    }

    public List<VisualRenderable> getChildren() {
        return children;
    }

    @Override
    public void setPosition(int x, int y) {
        int dx = x - this.renderX;
        int dy = y - this.renderY;

        super.setPosition(x, y);

        // mover hijos RELATIVAMENTE
        for (VisualRenderable child : children) {
            if (child instanceof RenderableVisual rv) {
                rv.setPosition(
                    rv.getRenderX() + dx,
                    rv.getRenderY() + dy
                );
            }
            if (child instanceof VisualRender vr) {
                vr.getObjeto().setPosition(
                    vr.getObjeto().getRenderX() + dx,
                    vr.getObjeto().getRenderY() + dy
                );
            }
        }
    }
}
