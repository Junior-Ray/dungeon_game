/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.dungeon_game.client.net;

import com.dungeon_game.core.auth.IAuthService;
import com.dungeon_game.core.logic.GameState;
import com.dungeon_game.core.model.Usuario_y_Chat.Usuario;

/**
 *
 * @author USUARIO
 */

public class NetworkAuthService implements IAuthService {

    private static final String HOST = "52.14.160.71";
    private static final int PORT = 5000;

    private boolean ensureConnected() {
        return GameState.getInstance().connectOnly(HOST, PORT, true);
    }

    @Override
    public String registrar(String username, String email, String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) return "Las contraseñas no coinciden.";
        if (!ensureConnected()) return "No se pudo conectar al servidor.";

        GameState.getInstance().getTransport().sendCommand("REGISTER " + username + " " + email + " " + password);
        return null; // el resultado real llega por REGISTER_OK / REGISTER_FAIL en Loader.onAuthLine
    }

    @Override
    public Usuario login(String username, String password) {
        if (!ensureConnected()) return null;

        GameState.getInstance().getTransport().sendCommand("LOGIN " + username + " " + password);
        return null; // el resultado real llega por LOGIN_OK / LOGIN_FAIL en Loader.onAuthLine
    }

    @Override
    public Usuario loginConToken(String token) {
        if (token == null || token.isBlank()) return null;
        if (!ensureConnected()) return null;

        GameState.getInstance().getTransport().sendCommand("AUTH_TOKEN " + token);
        return null; // Loader.onAuthLine maneja AUTH_OK / AUTH_FAIL
    }

    @Override
    public void logout(String token) {
        // si tu server tiene comando LOGOUT:
        // if (ensureConnected() && token != null) GameState.getInstance().getTransport().sendCommand("LOGOUT " + token);
        SessionStorage.borrarToken();
    }
}
