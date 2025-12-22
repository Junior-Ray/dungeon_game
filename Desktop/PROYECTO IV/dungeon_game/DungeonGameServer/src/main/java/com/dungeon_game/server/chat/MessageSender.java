/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.dungeon_game.server.chat;

/**
 *
 * @author USUARIO
 */
public interface MessageSender {

/**
     * Envía un mensaje de texto al jugador especificado.
     *
     * @param playerId  ID del jugador (coincide con PlayerSession.getPlayerId()).
     * @param message   Mensaje de texto ya formateado.
     */
    void sendToPlayer(String playerId, String message);
}
