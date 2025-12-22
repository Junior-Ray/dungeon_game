/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.dungeon_game.core.auth;

import com.dungeon_game.core.model.Usuario_y_Chat.Usuario;

public class AuthManager {
    
    private static IAuthService service;

    // Método para inyectar la implementación desde el Cliente
    public static void setService(IAuthService s) {
        service = s;
    }

    public static String registrar(String u, String e, String p, String cp) {
        if(service == null) return "Error crítico: Servicio Auth no iniciado.";
        return service.registrar(u, e, p, cp);
    }

    public static Usuario login(String u, String p) {
        if(service == null){
            System.out.println("LA INTERFAZ IAUTHSERVICE ES NULL");
            return null;
        }
        return service.login(u, p);
    }
    
    public static Usuario validarSesion(String token) {
         if(service == null) return null;
         return service.loginConToken(token);
    }
    
    public static void cerrarSesion(String token) {
        if(service != null) service.logout(token);
    }
    
    public static Usuario loginConToken(String token) {
        // service es tu instancia de IAuthService (ClientAuthService)
        if (service == null) return null; 
        return service.loginConToken(token);
    }
    
}