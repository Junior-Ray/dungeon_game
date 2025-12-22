/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.dungeon_game.core.net;

/**
 *
 * @author USUARIO
 */
public interface GameTransport {
    boolean connect();
    void sendCommand(String cmd);
    void close();
}