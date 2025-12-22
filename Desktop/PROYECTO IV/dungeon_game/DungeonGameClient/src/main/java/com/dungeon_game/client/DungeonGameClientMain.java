/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.dungeon_game.client;

import com.dungeon_game.client.local.LocalTransport;
import com.dungeon_game.client.net.NetworkClient;
import com.dungeon_game.core.net.GameTransport;
import java.util.Scanner;

public class DungeonGameClientMain {

    public static void main(String[] args) {
        // 0. Leer host y puerto desde argumentos
        String host = "localhost";
        int port = 5000;

        if (args.length >= 1) {
            host = args[0].trim();
        }

        if (args.length >= 2) {
            try {
                port = Integer.parseInt(args[1].trim());
            } catch (NumberFormatException e) {
                System.out.println("Puerto inválido, usando 5000 por defecto.");
                port = 5000;
            }
        }

        System.out.println("Conectando a " + host + ":" + port + "...");

        Scanner sc = new Scanner(System.in);

        GameTransport client;

        // 1️⃣ Intentar ONLINE primero (como siempre)
        NetworkClient onlineClient = new NetworkClient(host, port, msg -> {
            System.out.println("[SERVER] " + msg);
        });

        if (onlineClient.connect()) {
            System.out.println("Conectado al servidor remoto.");
            client = onlineClient;
        } else {
            System.out.println("No se pudo conectar al servidor. Iniciando MODO OFFLINE...");
            // 2️⃣ Fallback: modo OFFLINE
            LocalTransport offlineClient = new LocalTransport(msg -> {
                System.out.println("[OFFLINE] " + msg);
            });
            offlineClient.connect();
            client = offlineClient;
        }

        System.out.print("Ingresa tu nombre: ");
        String nombre = sc.nextLine().trim();

        // 3. En vez de escribir HELLO en telnet, lo manda el cliente (online u offline)
        client.sendCommand("HELLO " + nombre);

        System.out.println("Usa: HELLO, INVITE, ACCEPT, DECLINE, PARTY, PARTY_LEAVE, START_DUNGEON, END_DUNGEON, MOVE <idSala>, WHERE, WHO, QUIT");

        boolean seguir = true;
        while (seguir) {
            System.out.print("> ");
            String line = sc.nextLine();
            if (line.equalsIgnoreCase("QUIT")) {
                client.sendCommand("QUIT");
                seguir = false;
            } else {
                client.sendCommand(line);
            }
        }

        client.close();
        System.out.println("Cliente cerrado.");
    }
}
