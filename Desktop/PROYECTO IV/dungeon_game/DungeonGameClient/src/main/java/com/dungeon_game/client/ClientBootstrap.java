/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.dungeon_game.client;

import com.dungeon_game.core.logic.GameState;


/**
 *
 * @author USUARIO
 */
public class ClientBootstrap {

    public static void configure() {
        GameState.getInstance().setTransportFactory(new DefaultTransportFactory());
    }
}
