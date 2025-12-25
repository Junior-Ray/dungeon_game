/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.dungeon_game.server.chat;

import com.dungeon_game.core.model.Usuario_y_Chat.SolicitudAmistad;
import com.dungeon_game.core.model.Usuario_y_Chat.Usuario;
import dao.AmigoDAO;
import dao.SolicitudAmistadDAO;
import dao.UsuarioDAO;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author USUARIO
 */
public class ChatService {

    private UsuarioDAO usuarioDAO;
    private AmigoDAO amigoDAO;
    private SolicitudAmistadDAO solicitudAmistadDAO;
    
    public ChatService(UsuarioDAO usuarioDAO, AmigoDAO amigoDAO,
                        SolicitudAmistadDAO solcitudAmistadDAO){
        this.usuarioDAO  = usuarioDAO;
        this.amigoDAO = amigoDAO;

    }

    public Usuario login(String username, String passwordPlano) throws SQLException {
        Usuario u = usuarioDAO.buscarPorCodigo(username);
        if (u == null) {
            return null;
        }
        // validar password (por ahora simple)
        if (!u.getContrasena().equals(passwordPlano)) {
            return null;
        }
        return u;
    }
    public List<Usuario> obtenerAmigosDe(String codigoUsuario) throws SQLException {
        // delegado a AmigoDAO (es lo que ya hiciste)
        return amigoDAO.obtenerAmigosDe(codigoUsuario);
    }

    public void agregarAmigoDirecto(String cod1, String cod2) throws SQLException {
        if (!amigoDAO.existeAmistad(cod1, cod2)) {
            amigoDAO.agregarAmistad(cod1, cod2);
        }
    }

    // ===================== SOLICITUDES =====================

    public boolean enviarSolicitudAmistad(String emisorCodigo, String receptorCodigo) throws SQLException {
        // evitar duplicados
        if (solicitudAmistadDAO.existePendienteEntre(emisorCodigo, receptorCodigo)) {
            return false;
        }
        solicitudAmistadDAO.crearSolicitud(emisorCodigo, receptorCodigo);
        return true;
    }
    public List<SolicitudAmistad> obtenerSolicitudesPendientesDe(String receptorCodigo) throws SQLException {
        return solicitudAmistadDAO.obtenerPendientesRecibidas(receptorCodigo); 
                
             
    }

    public void aceptarSolicitud(int idSolicitud) throws SQLException {
        solicitudAmistadDAO.aceptarSolicitud(idSolicitud);
    }

    public void rechazarSolicitud(int idSolicitud) throws SQLException {
        solicitudAmistadDAO.rechazarSolicitud(idSolicitud);
    }
}
