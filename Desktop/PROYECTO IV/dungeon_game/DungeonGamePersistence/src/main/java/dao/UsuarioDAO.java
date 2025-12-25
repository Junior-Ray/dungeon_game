/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package dao;


import com.dungeon_game.core.model.Usuario_y_Chat.Usuario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author USUARIO
 */
public class UsuarioDAO {
    
    // En UsuarioDAO.java
    

    public void crearTablasSiNoExisten() throws SQLException {
        
        String sqlUsuarios = "CREATE TABLE IF NOT EXISTS usuarios_chat (" +
                "codigo TEXT PRIMARY KEY," +
                "username TEXT NOT NULL," +
                "email TEXT," +                 // <--- FALTABA ESTO
                "contrasena TEXT NOT NULL," +
                "avatar_path TEXT" +            // <--- FALTABA ESTO
                ")";

        String sqlAmigos = "CREATE TABLE IF NOT EXISTS amigos (" +
                "usuario_codigo TEXT NOT NULL," +
                "amigo_codigo TEXT NOT NULL," +
                "PRIMARY KEY (usuario_codigo, amigo_codigo)" +
                ")";

        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement()) {
            st.execute(sqlUsuarios);
            st.execute(sqlAmigos);
        }
    }
    public Usuario crearUsuario(String username, String email, String password, String avatarPath) throws SQLException {
        String codigo = java.util.UUID.randomUUID().toString();

        if (avatarPath == null || avatarPath.isBlank()) {
            avatarPath = "default_avatar.png";
        }

        String sql = """
            INSERT INTO usuarios_chat (codigo, username, email, contrasena, avatar_path)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, codigo);
            ps.setString(2, username);
            ps.setString(3, email);
            ps.setString(4, password);
            ps.setString(5, avatarPath);

            ps.executeUpdate();
        }

        // Ajusta el constructor según tu clase Usuario real
        Usuario u = new Usuario(codigo, username, password, avatarPath);
        u.setEmail(email); // si tienes setter; si no, crea constructor con email

        return u;
    }

    public void insertarUsuario(Usuario u) throws SQLException {
        String sql = "INSERT INTO usuarios_chat (codigo, username, contrasena, avatar_path) VALUES (?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.getCodigo());
            ps.setString(2, u.getUsername());
            ps.setString(3, u.getContrasena());
            ps.setString(4, u.getAvatarPath());
            ps.executeUpdate();
        }
    }
    
    public Usuario buscarPorCodigo(String codigo) throws SQLException {
        String sql = "SELECT codigo, username, contrasena, avatar_path FROM usuarios_chat WHERE codigo = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Usuario(
                            rs.getString("codigo"),
                            rs.getString("username"),
                            rs.getString("contrasena")
                    );
                }
            }
        }
        return null;
    }
    
    public List<Usuario> listarTodos() throws SQLException {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT codigo, username, contrasena, avatar_path FROM usuarios_chat";
        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Usuario u = new Usuario(
                        rs.getString("codigo"),
                        rs.getString("username"),
                        rs.getString("contrasena")
                );
                lista.add(u);
            }
        }
        return lista;
    }
    public Usuario buscarPorUsernameYCodigo(String username, String codigo) throws SQLException {
        String sql = "SELECT codigo, username, contrasena, avatar_path " +
                     "FROM usuarios_chat WHERE username = ? AND codigo = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, codigo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Usuario(
                            rs.getString("codigo"),
                            rs.getString("username"),
                            rs.getString("contrasena"),
                            rs.getString("avatar_path")
                    );
                }
            }
        }
        return null;
    }
    
    /**
     * Busca un usuario que coincida con el nombre y la contraseña.
     * Se usa para el Login normal.
     */
    public Usuario buscarPorCredenciales(String username, String password) {
        String sql = "SELECT codigo, username, contrasena, avatar_path FROM usuarios_chat WHERE username = ? AND contrasena = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Si encuentra coincidencia, crea y devuelve el objeto Usuario
                    return new Usuario(
                        rs.getString("codigo"),
                        rs.getString("username"),
                        rs.getString("contrasena"),
                        rs.getString("avatar_path")
                    );
                }
            }
        } catch (SQLException e) {
            // Aquí capturamos cualquier error de la base de datos
            System.err.println("Error al intentar loguear usuario: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Retorna null si no lo encontró o si hubo un error
        return null; 
    }
    
    public boolean existeUsername(String username) throws SQLException {
        String sql = "SELECT 1 FROM usuarios_chat WHERE username = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); // Retorna true si encontró algo
            }
        }
    }
    
    public boolean existeEmail(String email) throws SQLException {
        String sql = "SELECT 1 FROM usuarios_chat WHERE email = ?"; 
        // Asegúrate de haber agregado la columna email a tu tabla en la BD
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
    public String obtenerCodigoPorUsername(String username) throws SQLException {
    String sql = "SELECT codigo FROM usuarios_chat WHERE username = ? LIMIT 1";
    try (Connection c = Database.getConnection();
         PreparedStatement ps = c.prepareStatement(sql)) {
        ps.setString(1, username);
        try (ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getString("codigo") : null;
        }
    }
}

}
