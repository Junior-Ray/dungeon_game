/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.dungeon_game.core.util;

import java.util.regex.Pattern;

public class ValidadorDatos {

    // Regex simple para emails
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    );
    
    // Regex para username: Solo letras y números, de 3 a 15 caracteres
    private static final Pattern USERNAME_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9]{3,15}$"
    );

    // Regex contraseña: Mínimo 6 caracteres, al menos un número (opcional según tu gusto)
    private static final Pattern PASS_PATTERN = Pattern.compile(
        "^(?=.*[0-9])(?=.*[a-z]).{6,20}$"
    );

    public static boolean esEmailValido(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean esUsernameValido(String username) {
        return username != null && USERNAME_PATTERN.matcher(username).matches();
    }

    public static boolean esPasswordSegura(String password) {
        // Para este ejemplo, validamos longitud y contenido básico
        return password != null && PASS_PATTERN.matcher(password).matches();
    }
    
    public static String registrarUsuario(String username, String correo, String pass, String confirmPass){
        // 1. Validar campos vacíos
        if (username.isEmpty() || correo.isEmpty() || pass.isEmpty()) {
            return "Todos los campos son obligatorios.";
        }
        // 2. Validar coincidencia de contraseñas
        if (!pass.equals(confirmPass)) {
            return "Las contraseñas no coinciden.";
        }
        //3. Validar formatos
        if (!esUsernameValido(username)) {
            return "El usuario debe tener 3-15 caracteres alfanuméricos.";
        }
        if (!esEmailValido(correo)) {
            return "El formato del correo electrónico no es válido.";
        }
        if (!esPasswordSegura(pass)) {
            return "La contraseña debe tener mín 6 caracteres, letras y números.";
        }
        return null;
    }
}