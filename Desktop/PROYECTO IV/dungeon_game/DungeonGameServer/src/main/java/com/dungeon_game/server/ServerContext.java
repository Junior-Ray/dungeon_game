/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.dungeon_game.server;

import com.dungeon_game.server.core.GameEngine;
import com.dungeon_game.server.session.SessionManager;

/**
 *
 * @author USUARIO
 */
public class ServerContext {

    private final SessionManager sessionManager = new SessionManager();
    private final GameEngine gameEngine = new GameEngine();

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public GameEngine getGameEngine() {
        return gameEngine;
    }
}