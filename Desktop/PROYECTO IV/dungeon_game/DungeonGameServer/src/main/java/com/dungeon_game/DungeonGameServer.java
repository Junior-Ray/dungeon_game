/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.dungeon_game;

import com.dungeon_game.server.ServerContext;
import com.dungeon_game.server.core.GameLoop;
import com.dungeon_game.server.net.ConnectionManager;

/**
 *
 * @author Vidal Minchon Juan 
 */

public class DungeonGameServer {

    public static void main(String[] args) {
        int port = 5000;

        // Creamos el contexto compartido
        ServerContext context = new ServerContext();

        // 1️⃣ Motor (desde el contexto)
        var engine = context.getGameEngine();

        // 2️⃣ GameLoop como siempre
        GameLoop loop = new GameLoop(engine, 20);
        Thread loopThread = new Thread(loop, "GameLoop");
        loopThread.start();

        // 3️⃣ ConnectionManager ahora recibe el contexto, no solo engine
        ConnectionManager cm = new ConnectionManager(port, context);
        cm.start();
    }
}