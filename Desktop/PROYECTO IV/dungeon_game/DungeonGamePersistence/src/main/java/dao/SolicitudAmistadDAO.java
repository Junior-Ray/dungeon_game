/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package dao;

import com.dungeon_game.core.model.Usuario_y_Chat.EstadoSolicitud;
import com.dungeon_game.core.model.Usuario_y_Chat.SolicitudAmistad;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author USUARIO
 */
public class SolicitudAmistadDAO {

    public boolean existePendienteEntre(String a, String b) throws SQLException {
        String sql = """
            SELECT 1
            FROM solicitudes_amistad
            WHERE (
                  (emisor_codigo = ? AND receptor_codigo = ?)
               OR (emisor_codigo = ? AND receptor_codigo = ?)
            )
              AND estado = 'PENDIENTE'
            LIMIT 1
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, a);
            ps.setString(2, b);
            ps.setString(3, b);
            ps.setString(4, a);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
    public SolicitudAmistad crearSolicitud(String emisorCodigo, String receptorCodigo) throws SQLException {
        String sql = """
            INSERT INTO solicitudes_amistad (emisor_codigo, receptor_codigo, estado)
            VALUES (?, ?, 'PENDIENTE')
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, emisorCodigo);
            ps.setString(2, receptorCodigo);

            ps.executeUpdate();

            int idGenerado = -1;
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) idGenerado = rs.getInt(1);
            }

            SolicitudAmistad s = new SolicitudAmistad();
            s.setId(idGenerado);
            s.setEmisorCodigo(emisorCodigo);
            s.setReceptorCodigo(receptorCodigo);
            s.setEstado(EstadoSolicitud.PENDIENTE);
            s.setFechaEnvio(LocalDateTime.now());
            s.setFechaRespuesta(null);

            return s;
        }
    }
     public List<SolicitudAmistad> obtenerPendientesRecibidas(String receptorCodigo) throws SQLException {
        String sql = """
            SELECT id, emisor_codigo, receptor_codigo, estado, fecha_envio, fecha_respuesta
            FROM solicitudes_amistad
            WHERE receptor_codigo = ?
              AND estado = 'PENDIENTE'
            ORDER BY fecha_envio DESC
        """;

        List<SolicitudAmistad> list = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, receptorCodigo);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }
     public List<SolicitudAmistad> obtenerPendientesEnviadas(String emisorCodigo) throws SQLException {
        String sql = """
            SELECT id, emisor_codigo, receptor_codigo, estado, fecha_envio, fecha_respuesta
            FROM solicitudes_amistad
            WHERE emisor_codigo = ?
              AND estado = 'PENDIENTE'
            ORDER BY fecha_envio DESC
        """;

        List<SolicitudAmistad> list = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, emisorCodigo);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }


    // Opcional para más adelante:
    public boolean actualizarEstado(int idSolicitud, EstadoSolicitud nuevoEstado) throws SQLException {
        String sql = """
            UPDATE solicitudes_amistad
            SET estado = ?, fecha_respuesta = NOW()
            WHERE id = ?
            """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nuevoEstado.name()); // "PENDIENTE", "ACEPTADA", ...
            ps.setInt(2, idSolicitud);
            return ps.executeUpdate() > 0;
        }
    }
    public void aceptarSolicitud(int idSolicitud) throws SQLException {
        actualizarEstado(idSolicitud, EstadoSolicitud.ACEPTADA);
    }

    public void rechazarSolicitud(int idSolicitud) throws SQLException {
        actualizarEstado(idSolicitud, EstadoSolicitud.RECHAZADA);
    }

    // ===================== MAPEO RESULTSET -> OBJETO =====================

    public SolicitudAmistad obtenerPorId(int solicitudId) throws SQLException {
        String sql = """
            SELECT id, emisor_codigo, receptor_codigo, estado, fecha_envio, fecha_respuesta
            FROM solicitudes_amistad
            WHERE id = ?
            LIMIT 1
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, solicitudId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return mapRow(rs);
            }
        }
    }
    private SolicitudAmistad mapRow(ResultSet rs) throws SQLException {
        SolicitudAmistad s = new SolicitudAmistad();
        s.setId(rs.getInt("id"));
        s.setEmisorCodigo(rs.getString("emisor_codigo"));
        s.setReceptorCodigo(rs.getString("receptor_codigo"));

        String est = rs.getString("estado");
        s.setEstado(EstadoSolicitud.valueOf(est));

        Timestamp tsEnvio = rs.getTimestamp("fecha_envio");
        if (tsEnvio != null) s.setFechaEnvio(tsEnvio.toLocalDateTime());

        Timestamp tsResp = rs.getTimestamp("fecha_respuesta");
        if (tsResp != null) s.setFechaRespuesta(tsResp.toLocalDateTime());
        else s.setFechaRespuesta(null);

        return s;
    }

    
}
