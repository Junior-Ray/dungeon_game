/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import java.sql.*;
import java.util.UUID;

public class AuthDAO {
    
    public static void crearTablaSesiones() throws SQLException {
        // Definimos fecha_expiracion como BIGINT para guardar el long de Java
        String sql = "CREATE TABLE IF NOT EXISTS sesiones (" +
                "token TEXT PRIMARY KEY," +
                "usuario_codigo TEXT NOT NULL," +
                "fecha_expiracion BIGINT NOT NULL," + 
                "FOREIGN KEY(usuario_codigo) REFERENCES usuarios_chat(codigo)" +
                ")";

        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement()) {
            st.execute(sql);
        }
    }
    
    // Crea un token válido por 7 días
    // En dao.AuthDAO

    public String crearToken(String codigoUsuario) throws SQLException {
        String token = UUID.randomUUID().toString();
        // 7 días de duración
        long expiracion = System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 7); 

        String sql = "INSERT INTO sesiones (token, usuario_codigo, fecha_expiracion) VALUES (?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, token);
            ps.setString(2, codigoUsuario); // <--- IMPORTANTE: Esto no puede ser null
            ps.setLong(3, expiracion);

            int filas = ps.executeUpdate(); // <--- ¿TIENES ESTA LÍNEA?

            if (filas > 0) {
                System.out.println("DAO: Insertada sesión en tabla SQL.");
            }
        }
        return token;
    }

    // Verifica token y retorna el ID del usuario si es válido
    public static String obtenerUsuarioPorToken(String token) throws SQLException {
        String sql = "SELECT usuario_codigo, fecha_expiracion FROM sesiones WHERE token = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, token);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long exp = rs.getLong("fecha_expiracion");
                    if (System.currentTimeMillis() > exp) {
                        eliminarToken(token); // Limpieza automática
                        return null; 
                    }
                    return rs.getString("usuario_codigo");
                }
            }
        }
        return null;
    }

    public static void eliminarToken(String token) throws SQLException {
        String sql = "DELETE FROM sesiones WHERE token = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, token);
            ps.executeUpdate();
        }
    }
}
