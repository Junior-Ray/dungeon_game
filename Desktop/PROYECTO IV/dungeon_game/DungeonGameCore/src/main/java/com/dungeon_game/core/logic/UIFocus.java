/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.dungeon_game.core.logic;

import com.dungeon_game.core.data.VisualRender;



/**
 *
 * @author GABRIEL SALGADO
 */
public class UIFocus {
    private static VisualRender focused;

    public static void setFocus(VisualRender comp) {
        if(focused!= comp&&focused!=null) focused.offFocus();
        focused = comp;
    }

    public static VisualRender getFocus() {
        return focused;
    }
}
