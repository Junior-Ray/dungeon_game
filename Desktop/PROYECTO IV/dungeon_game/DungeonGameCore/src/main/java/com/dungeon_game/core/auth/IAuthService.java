/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.dungeon_game.core.auth;

import com.dungeon_game.core.model.Usuario_y_Chat.Usuario;

public interface IAuthService {
    /**
     * Intenta registrar un usuario.
     * @return null si tuvo éxito, o un mensaje de error (String) si falló.
     */
    String registrar(String username, String email, String password, String confirmPassword);

    /**
     * Login manual con usuario y contraseña.
     * @return Objeto Usuario si éxito, null si falla.
     */
    Usuario login(String username, String password);

    /**
     * Auto-login usando un token guardado.
     * @return Objeto Usuario si el token es válido, null si no.
     */
    Usuario loginConToken(String token);
    
    /**
     * Cierra la sesión (elimina token).
     */
    void logout(String token);
}
