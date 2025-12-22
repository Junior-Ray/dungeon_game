/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.dungeon_game.client.net;

import com.dungeon_game.core.auth.ITokenStorage;
import com.google.gson.Gson;
import java.io.*;

public class SessionStorage implements ITokenStorage {
    private static final String FILE_PATH = "session.json";
    private static final Gson gson = new Gson();

    // Clase interna para estructura del JSON
    private static class TokenJson {
        String token;
        TokenJson(String t) { this.token = t; }
    }

    public static void guardarToken(String token) {
        try (FileWriter writer = new FileWriter(FILE_PATH)) {
            gson.toJson(new TokenJson(token), writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String cargarToken() {
        File f = new File(FILE_PATH);
        if(!f.exists()) return null;
        
        try (FileReader reader = new FileReader(FILE_PATH)) {
            TokenJson data = gson.fromJson(reader, TokenJson.class);
            return (data != null) ? data.token : null;
        } catch (IOException e) {
            return null;
        }
    }

    public static void borrarToken() {
        new File(FILE_PATH).delete();
    }
    
    @Override
    public void guardar(String token) {
        guardarToken(token); // Reutiliza tu código existente
    }

    @Override
    public String cargar() {
        return cargarToken(); // Reutiliza tu código existente
    }

    @Override
    public void borrar() {
        borrarToken(); // Reutiliza tu código existente
    }
}
