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

    public boolean existePendienteEntre(String emisor, String receptor) throws SQLException {
        String sql = """
            SELECT 1
            FROM solicitudes_amistad
            WHERE emisor_codigo = ? AND receptor_codigo = ?
              AND estado = 'PENDIENTE'
            """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, emisor);
            ps.setString(2, receptor);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
    public SolicitudAmistad crearSolicitud(String emisorCodigo, String receptorCodigo) throws SQLException {
        String sql = """
            INSERT INTO solicitudes_amistad (emisor_codigo, receptor_codigo, estado, fecha_envio)
            VALUES (?, ?, 'PENDIENTE', CURRENT_TIMESTAMP)
            """;

        SolicitudAmistad solicitud = new SolicitudAmistad();
        solicitud.setEmisorCodigo(emisorCodigo);
        solicitud.setReceptorCodigo(receptorCodigo);
        solicitud.setEstado(EstadoSolicitud.PENDIENTE);
        solicitud.setFechaEnvio(LocalDateTime.now());

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, emisorCodigo);
            ps.setString(2, receptorCodigo);
            ps.executeUpdate();

            // Obtener el ID AUTOINCREMENT generado
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    solicitud.setId(rs.getInt(1));
                }
            }
        }

        return solicitud;
    }
    public List<SolicitudAmistad> obtenerSolicitudesPendientesDe(int idReceptor) throws SQLException {
        String sql = """
            SELECT id, emisor_id, receptor_id, estado, fecha_envio, fecha_respuesta
            FROM solicitudes_amistad
            WHERE receptor_id = ?
              AND estado = 'PENDIENTE'
            """;

        List<SolicitudAmistad> solicitudes = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idReceptor);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    solicitudes.add(mapRowToSolicitud(rs));
                }
            }
        }
        return solicitudes;
    }

    // Opcional para más adelante:
    public void actualizarEstado(int idSolicitud, EstadoSolicitud nuevoEstado) throws SQLException {
        String sql = """
            UPDATE solicitudes_amistad
            SET estado = ?, fecha_respuesta = NOW()
            WHERE id = ?
            """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nuevoEstado.name()); // "PENDIENTE", "ACEPTADA", ...
            ps.setInt(2, idSolicitud);
            ps.executeUpdate();
        }
    }
    public void aceptarSolicitud(int idSolicitud) throws SQLException {
        actualizarEstado(idSolicitud, EstadoSolicitud.ACEPTADA);
    }

    public void rechazarSolicitud(int idSolicitud) throws SQLException {
        actualizarEstado(idSolicitud, EstadoSolicitud.RECHAZADA);
    }

    // ===================== MAPEO RESULTSET -> OBJETO =====================

    private SolicitudAmistad mapRowToSolicitud(ResultSet rs) throws SQLException {
        SolicitudAmistad s = new SolicitudAmistad();

        s.setId(rs.getInt("id"));  
        s.setEmisorCodigo(rs.getString("emisor_codigo"));
        s.setReceptorCodigo(rs.getString("receptor_codigo"));

        String estadoStr = rs.getString("estado");
        s.setEstado(EstadoSolicitud.valueOf(estadoStr));  // "PENDIENTE", "ACEPTADA", etc.

        Timestamp tsEnvio = rs.getTimestamp("fecha_envio");
        if (tsEnvio != null) {
            s.setFechaEnvio(tsEnvio.toLocalDateTime());
        }

        Timestamp tsResp = rs.getTimestamp("fecha_respuesta");
        if (tsResp != null) {
            s.setFechaRespuesta(tsResp.toLocalDateTime());
        }

        return s;
    }

    
}
