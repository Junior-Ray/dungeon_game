/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.dungeon_game.server.session;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author USUARIO
 */
public class PlayerSession {

 private final String playerId;   // username o código único
 private String userCode;

    // -------------------------------
    // Estado global
    // -------------------------------
    private PlayerMode mode;         // LOBBY o DUNGEON

    // -------------------------------
    // Estado de LOBBY / PARTY
    // -------------------------------
    private String partyId;          // null si no pertenece a ninguna party
    private boolean partyLeader;     // true si es el líder de la party

    /**
     * Invitaciones pendientes que ESTE jugador ha recibido.
     * Contiene los IDs de los jugadores que lo invitan (ej. "Juan", "Josue").
     */
    private final Set<String> pendingInvitations = new HashSet<>();

    // -------------------------------
    // Estado de DUNGEON / PARTIDA
    // -------------------------------
    private String dungeonId;        // null si aún no está en una partida
    private String salaActual;       // id de la sala dentro del dungeon (S1, S2, etc.)

    // (Opcional) stats básicos para el futuro
    private int vida = 100;

    public PlayerSession(String playerId) {
        this.playerId = Objects.requireNonNull(playerId, "playerId no puede ser null");
        this.mode = PlayerMode.LOBBY;  // Por defecto, entra al LOBBY
    }

    // =====================================
    // Getters básicos
    // =====================================

    public String getPlayerId() {
        return playerId;
    }

    public PlayerMode getMode() {
        return mode;
    }

    public String getPartyId() {
        return partyId;
    }

    public boolean isPartyLeader() {
        return partyLeader;
    }

    public String getDungeonId() {
        return dungeonId;
    }

    public String getSalaActual() {
        return salaActual;
    }

    public int getVida() {
        return vida;
    }
    public String getUserCode(){
        return userCode;
    }
    public void setUserCode(String userCode){
        this.userCode = userCode;
    }

    public Set<String> getPendingInvitations() {
        // Vista inmodificable hacia afuera
        return Collections.unmodifiableSet(pendingInvitations);
    }

    // =====================================
    // Métodos de cambio de estado global
    // =====================================

    /**
     * Pone al jugador en modo LOBBY.
     * Ideal cuando termina una partida o recién se conecta.
     */
    public void enterLobby() {
        this.mode = PlayerMode.LOBBY;
        this.dungeonId = null;
        this.salaActual = null;
    }

    /**
     * Pone al jugador en modo DUNGEON, asociándolo a un dungeon concreto
     * y a una sala inicial.
     */
    public void enterDungeon(String dungeonId, String salaInicial) {
        this.mode = PlayerMode.DUNGEON;
        this.dungeonId = Objects.requireNonNull(dungeonId, "dungeonId no puede ser null");
        this.salaActual = Objects.requireNonNull(salaInicial, "salaInicial no puede ser null");
    }

    // =====================================
    // Métodos de PARTY / LOBBY
    // =====================================

    public void joinParty(String partyId, boolean asLeader) {
        this.partyId = partyId;
        this.partyLeader = asLeader;
    }

    public void leaveParty() {
        this.partyId = null;
        this.partyLeader = false;
    }

    public void setPartyLeader(boolean partyLeader) {
        this.partyLeader = partyLeader;
    }

    public void addInvitation(String fromPlayerId) {
        pendingInvitations.add(fromPlayerId);
    }

    public void removeInvitation(String fromPlayerId) {
        pendingInvitations.remove(fromPlayerId);
    }

    public void clearInvitations() {
        pendingInvitations.clear();
    }

    // =====================================
    // Métodos de DUNGEON
    // =====================================

    /**
     * Cambia la sala actual del jugador dentro de su dungeon.
     */
    public void moveToSala(String nuevaSalaId) {
        this.salaActual = Objects.requireNonNull(nuevaSalaId, "nuevaSalaId no puede ser null");
    }

    public void setVida(int vida) {
        this.vida = vida;
    }

    @Override
    public String toString() {
        return "PlayerSession{" +
               "playerId='" + playerId + '\'' +
               ", mode=" + mode +
               ", partyId='" + partyId + '\'' +
               ", partyLeader=" + partyLeader +
               ", dungeonId='" + dungeonId + '\'' +
               ", salaActual='" + salaActual + '\'' +
               ", vida=" + vida +
               '}';
    }
}
