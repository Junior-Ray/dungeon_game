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
public class AmigoDAO {

    public void agregarAmistad(String cod1, String cod2) throws SQLException {
        String sql = "INSERT IGNORE INTO amigos (usuario_codigo, amigo_codigo) VALUES (?, ?)";
        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, cod1); ps.setString(2, cod2); ps.executeUpdate();
                ps.setString(1, cod2); ps.setString(2, cod1); ps.executeUpdate();
            }
            conn.commit();
        }
    }
    public List<String[]> listarAmistades() throws SQLException {
        List<String[]> lista = new ArrayList<>();
        String sql = "SELECT usuario_codigo, amigo_codigo FROM amigos";
        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String u1 = rs.getString("usuario_codigo");
                String u2 = rs.getString("amigo_codigo");
                lista.add(new String[]{u1, u2});
            }
        }
        return lista;
    }
    public List<Usuario> obtenerAmigosDe(String codigoUsuario) throws SQLException {
        String sql = """
            SELECT u.codigo, u.username, u.email, u.avatar_path
            FROM amigos a
            JOIN usuarios_chat u ON u.codigo = a.amigo_codigo
            WHERE a.usuario_codigo = ?
            """;

        List<Usuario> lista = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, codigoUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Usuario(
                            rs.getString("codigo"),
                            rs.getString("username"),
                            null, // NO mandes contrasena
                            rs.getString("avatar_path")
                    ));
                }
            }
        }
        return lista;
    }
    public boolean existeAmistad(String codigo1, String codigo2) throws SQLException {
        String sql = """
            SELECT 1
            FROM amigos
            WHERE (usuario_codigo = ? AND amigo_codigo = ?)
               OR (usuario_codigo = ? AND amigo_codigo = ?)
            """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, codigo1);
            ps.setString(2, codigo2);
            ps.setString(3, codigo2);
            ps.setString(4, codigo1);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }


}
