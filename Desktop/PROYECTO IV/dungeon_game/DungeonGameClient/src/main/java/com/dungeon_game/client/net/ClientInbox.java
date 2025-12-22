/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.dungeon_game.client.net;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 * @author USUARIO
 */
public final class ClientInbox {
    private ClientInbox() {}

    public static final ConcurrentLinkedQueue<String> SERVER_MESSAGES =
            new ConcurrentLinkedQueue<>();
}
