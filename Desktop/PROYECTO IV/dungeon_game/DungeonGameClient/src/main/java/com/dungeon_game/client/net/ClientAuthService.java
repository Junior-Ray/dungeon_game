package com.dungeon_game.client.net;

import com.dungeon_game.core.auth.IAuthService;
import com.dungeon_game.core.model.Usuario_y_Chat.Usuario;
import com.dungeon_game.server.service.AuthLogic;

public class ClientAuthService implements IAuthService {

    private final AuthLogic authLogic;

    public ClientAuthService() {
        // Inicializamos la lógica (que ya contiene los DAOs internamente)
        this.authLogic = new AuthLogic();
    }

    @Override
    public String registrar(String username, String email, String password, String confirmPassword) {
        String error = this.authLogic.registrarUsuario(username, email, password, confirmPassword);
        
        // Auto-Login si el registro fue exitoso
        if (error == null) {
            login(username, password);
        }
        return error;
    }

    @Override
    public Usuario login(String username, String password) {
        // 1. Validar credenciales (Lógica Server)
        Usuario u = this.authLogic.login(username, password);

        // 2. Si existe, gestionar Token
        if (u != null) {
            // A. Pedir al Server que genere y guarde el token en BD
            String token = this.authLogic.generarTokenSesion(u);

            if (token != null) {
                // B. Guardar ese token en el Disco Local (Lógica Cliente)
                SessionStorage.guardarToken(token);
                System.out.println("[ClientAuth] Token sincronizado: " + token);
            }
        }
        return u;
    }

    @Override
    public Usuario loginConToken(String token) {
        if (token == null) return null;

        // 1. Preguntar al Server de quién es este token
        Usuario u = this.authLogic.obtenerUsuarioPorToken(token);

        if (u != null) {
            System.out.println("[ClientAuth] Token válido para: " + u.getUsername());
            return u;
        } else {
            // 2. Si el server dice que no vale, lo borramos del disco local
            System.out.println("[ClientAuth] Token rechazado por el servidor.");
            SessionStorage.borrarToken();
            return null;
        }
    }

    @Override
    public void logout(String token) {
        // 1. Avisar al Server que borre de BD
        this.authLogic.invalidarToken(token);
        
        // 2. Borrar del disco local
        SessionStorage.borrarToken();
    }
}