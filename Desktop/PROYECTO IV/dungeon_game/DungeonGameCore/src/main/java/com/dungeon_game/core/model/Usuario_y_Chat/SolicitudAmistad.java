/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.dungeon_game.core.model.Usuario_y_Chat;

import java.time.LocalDateTime;

/**
 *
 * @author USUARIO
 */
public class SolicitudAmistad {

    private int id;                 // PK en la tabla solicitudes_amistad
    private String emisorCodigo;    // emisor_codigo en la BD
    private String receptorCodigo;  // receptor_codigo en la BD
    private EstadoSolicitud estado;
    private LocalDateTime fechaEnvio;
    private LocalDateTime fechaRespuesta;

    public SolicitudAmistad() {
    }

    public SolicitudAmistad(int id,
                            String emisorCodigo,
                            String receptorCodigo,
                            EstadoSolicitud estado,
                            LocalDateTime fechaEnvio,
                            LocalDateTime fechaRespuesta) {
        this.id = id;
        this.emisorCodigo = emisorCodigo;
        this.receptorCodigo = receptorCodigo;
        this.estado = estado;
        this.fechaEnvio = fechaEnvio;
        this.fechaRespuesta = fechaRespuesta;
    }

    // ===== GETTERS / SETTERS =====

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmisorCodigo() {
        return emisorCodigo;
    }

    public void setEmisorCodigo(String emisorCodigo) {
        this.emisorCodigo = emisorCodigo;
    }

    public String getReceptorCodigo() {
        return receptorCodigo;
    }

    public void setReceptorCodigo(String receptorCodigo) {
        this.receptorCodigo = receptorCodigo;
    }

    public EstadoSolicitud getEstado() {
        return estado;
    }

    public void setEstado(EstadoSolicitud estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(LocalDateTime fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }

    public LocalDateTime getFechaRespuesta() {
        return fechaRespuesta;
    }

    public void setFechaRespuesta(LocalDateTime fechaRespuesta) {
        this.fechaRespuesta = fechaRespuesta;
    }

    @Override
    public String toString() {
        return "SolicitudAmistad{" +
                "id=" + id +
                ", emisorCodigo='" + emisorCodigo + '\'' +
                ", receptorCodigo='" + receptorCodigo + '\'' +
                ", estado=" + estado +
                ", fechaEnvio=" + fechaEnvio +
                ", fechaRespuesta=" + fechaRespuesta +
                '}';
    }
}