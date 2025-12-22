/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.dungeon_game.core.net;

import com.dungeon_game.core.net.GameTransport;
import java.util.function.Consumer;

/**
 *
 * @author USUARIO
 */
public interface TransportFactory {
    GameTransport createOnline(String host, int port);
    GameTransport createOffline();
}
