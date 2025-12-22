package com.dungeon_game.server.service;

import com.dungeon_game.core.model.Usuario_y_Chat.Usuario;
import dao.AuthDAO;
import dao.UsuarioDAO;
import java.sql.SQLException;
import java.util.UUID;

public class AuthLogic {

    // Instancias de los DAOs
    private final UsuarioDAO usuarioDAO;
    private final AuthDAO authDAO; // <--- Necesario para los tokens

    public AuthLogic() {
        this.usuarioDAO = new UsuarioDAO();
        this.authDAO = new AuthDAO(); // <--- Inicializamos AuthDAO
    }

    // Constructor para inyección (Testing)
    public AuthLogic(UsuarioDAO uDao, AuthDAO aDao) {
        this.usuarioDAO = uDao;
        this.authDAO = aDao;
    }

    // ---------------------------------------------------------
    // MÉTODOS DE USUARIO (Registro y Login)
    // ---------------------------------------------------------

    public String registrarUsuario(String username, String email, String pass, String confirmPass) {
        try {
            if (this.usuarioDAO.existeUsername(username)) {
                return "El nombre de usuario ya está en uso.";
            }
            // Generamos código único
            String codigoUnico = "#" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            
            Usuario nuevoUsuario = new Usuario(codigoUnico, username, pass, "default_avatar.png");
            
            usuarioDAO.insertarUsuario(nuevoUsuario);
            return null; // Null = Éxito

        } catch (SQLException e) {
            e.printStackTrace();
            return "Error de base de datos.";
        }
    }

    public Usuario login(String username, String password) {
        // Usamos el método nuevo que compara contraseñas
        return usuarioDAO.buscarPorCredenciales(username, password);
    }

    // ---------------------------------------------------------
    // MÉTODOS DE SESIÓN (LOS QUE TE FALTABAN)
    // ---------------------------------------------------------

    /**
     * Crea un token en la base de datos para el usuario dado.
     * Este es el método que 'ClientAuthService' no encontraba.
     */
    public String generarTokenSesion(Usuario u) {
        try {
            // Llama al DAO para insertar en la tabla 'sesiones'
            return authDAO.crearToken(u.getCodigo());
        } catch (SQLException e) {
            System.err.println("Error al generar token SQL: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Verifica si un token es válido y devuelve el usuario dueño.
     */
    public Usuario obtenerUsuarioPorToken(String token) {
        try {
            // 1. Obtener ID del usuario dueño del token
            String codigoUser = authDAO.obtenerUsuarioPorToken(token);
            
            if (codigoUser != null) {
                // 2. Buscar los datos completos del usuario
                return usuarioDAO.buscarPorCodigo(codigoUser);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Borra el token de la base de datos (Logout).
     */
    public void invalidarToken(String token) {
        try {
            if (token != null) {
                authDAO.eliminarToken(token);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}