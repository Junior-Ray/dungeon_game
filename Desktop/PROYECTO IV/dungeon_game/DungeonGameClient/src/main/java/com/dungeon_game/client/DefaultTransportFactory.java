/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.dungeon_game.client;

import com.dungeon_game.client.local.LocalTransport;
import com.dungeon_game.client.net.NetworkClient;
import com.dungeon_game.core.logic.GameState;
import com.dungeon_game.core.net.GameTransport;
import com.dungeon_game.core.net.TransportFactory;

/**
 *
 * @author USUARIO
 */
public class DefaultTransportFactory implements TransportFactory {

    @Override
    public GameTransport createOnline(String host, int port) {
        return new NetworkClient(host, port, GameState.getInstance().getInbox()::add);
    }

    @Override
    public GameTransport createOffline() {
        return new LocalTransport(GameState.getInstance().getInbox()::add);
    }
}