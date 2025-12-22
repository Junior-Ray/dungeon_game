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
        // Guardamos solo una fila por amistad, ordenando códigos para evitar duplicar lógica
        String a = cod1.compareTo(cod2) < 0 ? cod1 : cod2;
        String b = cod1.compareTo(cod2) < 0 ? cod2 : cod1;

        String sql = "INSERT OR IGNORE INTO amigos (usuario_codigo, amigo_codigo) VALUES (?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, a);
            ps.setString(2, b);
            ps.executeUpdate();
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
            SELECT u.codigo, u.username, u.contrasena, u.avatar_path
            FROM amigos a
            JOIN usuarios_chat u ON u.codigo = a.amigo_codigo
            WHERE a.usuario_codigo = ?
            
            UNION
            
            SELECT u2.codigo, u2.username, u2.contrasena, u2.avatar_path
            FROM amigos a2
            JOIN usuarios_chat u2 ON u2.codigo = a2.usuario_codigo
            WHERE a2.amigo_codigo = ?
              AND u2.codigo <> ?
            """;

        List<Usuario> lista = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, codigoUsuario);
            ps.setString(2, codigoUsuario);
            ps.setString(3, codigoUsuario);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Usuario amigo = new Usuario(
                            rs.getString("codigo"),
                            rs.getString("username"),
                            rs.getString("contrasena"),
                            rs.getString("avatar_path")
                    );
                    lista.add(amigo);
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
