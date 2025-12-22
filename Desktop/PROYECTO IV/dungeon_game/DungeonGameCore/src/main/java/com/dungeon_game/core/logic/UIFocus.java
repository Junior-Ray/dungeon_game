/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.dungeon_game.core.logic;

import com.dungeon_game.core.components.AbstractUIComponent;

/**
 *
 * @author GABRIEL SALGADO
 */
public class UIFocus {
    private static AbstractUIComponent focused;

    public static void setFocus(AbstractUIComponent comp) {
        if(focused!= comp&&focused!=null) focused.offFocus();
        focused = comp;
    }

    public static AbstractUIComponent getFocus() {
        return focused;
    }
}
