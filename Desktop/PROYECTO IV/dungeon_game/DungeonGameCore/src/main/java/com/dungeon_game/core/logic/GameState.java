/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.dungeon_game.core.logic;

import com.dungeon_game.core.api.RenderProcessor;
import com.dungeon_game.core.api.Updater;
import com.dungeon_game.core.auth.ITokenStorage; // <--- NUEVO: Importar la interfaz
import com.dungeon_game.core.data.ChangeSala;
import com.dungeon_game.core.model.Loader;
import com.dungeon_game.core.model.Lobby;
import com.dungeon_game.core.model.Sala;
import com.dungeon_game.core.net.GameTransport;
import com.dungeon_game.core.net.TransportFactory;
import com.dungeon_game.core.structures.DungeonGraph;
import com.dungeon_game.core.structures.NodoSala;
import java.util.ArrayList;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 * @author USUARIO
 */
public class GameState {
    
    // --- NUEVO: Variable para la interfaz de almacenamiento ---
    private ITokenStorage tokenStorage;
    // ----------------------------------------------------------

    private final List<Updater> updaters = new ArrayList<>();
    
    private final List<Updater> toAdd = new ArrayList<>();
    private final List<Updater> toRemove = new ArrayList<>();

    public void registerUpdater(Updater u) {
        if (u == null) return;
        synchronized (toAdd) { //Se estaba bueguando la animacion
            toAdd.add(u);
        }
    }
    public void unregisterUpdater(Updater u) {
        if (u == null) return;
        synchronized (toRemove) {
            toRemove.add(u);
        }
    }
    private  DungeonGraph mapa;
    private NodoSala salaActual;
    private Sala actual;
    private ListaSala lista;
    private static GameState instance;
    private boolean cambioSala = true;
    /*No sé como funciona, pero implementare Singleton, cambia de logica
    public GameState(DungeonGraph mapa, NodoSala salaInicial) {
        if (mapa == null) {
            throw new IllegalArgumentException("El mapa no puede ser nulo");
        }
        if (salaInicial == null) {
            throw new IllegalArgumentException("La sala inicial no puede ser nula");
        }
        this.mapa = mapa;
        this.salaActual = salaInicial;
        this.salaActual.marcarComoVisitada();
    }
    */
    public GameState(){};
    
    public static GameState getInstance(){
        if(instance==null){
            instance= new GameState();
            instance.lista= new ListaSala();
            instance.setActual(Loader.getInstance());   
            instance.lista.agregar(Loader.getInstance());
            instance.lista.agregar(Lobby.getInstance());
//            Loader.getInstance().cargarIniciales(); 
            
            ChangeSala.getInstance().startAnimation(1280,720);
        }
        return instance;
    }
    

    // --- NUEVO: Métodos para manejar el almacenamiento ---
    public void setTokenStorage(ITokenStorage tokenStorage) {
        this.tokenStorage = tokenStorage;
    }

    public ITokenStorage getTokenStorage() {
        return tokenStorage;
    }
    
    // --- NUEVO: Llamar a esto desde el Main al finalizar la configuración ---
    public void iniciarGraficos() {
        Loader.getInstance().cargarIniciales();
    }
    // --------------------------------------------------------
    
    public NodoSala getSalaActual() {
        return salaActual;
    }
    
    public void update() {
        actual.update();

        // aplicar altas/bajas pendientes (seguro)
        synchronized (toAdd) {
            for (Updater u : toAdd) {
                if (!updaters.contains(u)) updaters.add(u);
            }
            toAdd.clear();
        }
        synchronized (toRemove) {
            updaters.removeAll(toRemove);
            toRemove.clear();
        }

        // ahora sí, iterar sin que reviente
        for (int i = 0; i < updaters.size(); i++) {
            updaters.get(i).update();
        }

        if (cambioSala) {
            ChangeSala.getInstance().update();
        }
    }
    /** Salas vecinas a la sala actual (las que se pueden alcanzar en un movimiento). */
    public List<NodoSala> getSalasVecinas() {
        return mapa.getVecinos(salaActual.getIdSala());
    }
    
    /**
     * Intenta mover al jugador a la sala indicada por id.
     * Devuelve true si el movimiento es válido (hay conexión y no está bloqueada).
     */
    public boolean moverA(String idDestino) {
        if (idDestino == null || idDestino.isBlank()) {
            return false;
        }

        String idOrigen = salaActual.getIdSala();

        // Usamos tu método del grafo para validar el movimiento
        if (!mapa.esMovimientoDirectoValido(idOrigen, idDestino)) {
            return false;
        }

        NodoSala destino = mapa.getSala(idDestino);
        if (destino == null) {
            return false;
        }

        this.salaActual = destino;
        destino.marcarComoVisitada();
        return true;
    }
    public void forzarMoverA(NodoSala sala) {
        this.salaActual = sala;
    }

    public void setActual(Sala actual) {
        this.actual = actual;
    }
    public void siguienteSala(){
        RenderProcessor.getInstance().eliminarTodo();
        actual.eliminarSala();
        actual =lista.getCola().getInfo();
        actual.cargarIniciales();
        ChangeSala.getInstance().startAnimation(1280, 720);
        cambioSala=true;
    }

    public boolean isCambioSala() {
        return cambioSala;
    }

    public void setCambioSala(boolean cambioSala) {
        this.cambioSala = cambioSala;
    }
    //*******************************************************************************
    
    private GameTransport transport;
    private String playerId;

    // “buzón” thread-safe para mensajes que llegan del server
    private final ConcurrentLinkedQueue<String> inbox = new ConcurrentLinkedQueue<>();

    public GameTransport getTransport() { return transport; }
    public String getPlayerId() { return playerId; }

    public ConcurrentLinkedQueue<String> getInbox() { return inbox; }

    public boolean hasTransport() { return transport != null; }
    
    public void bindTransport(GameTransport transport, String playerId) {
        this.transport = transport;
        this.playerId = playerId;
    }
    private TransportFactory transportFactory;

    public void setTransportFactory(TransportFactory factory) {
        this.transportFactory = factory;
    }
    
    // Método para permitir re-inyectar el transporte (para auto-login)
    public void setTransport(GameTransport transport) {
        this.transport = transport;
    }

    // método que se llama cuando ya tienes el playerId (login ok)
    public void connectAfterLogin(String host, int port, String playerId, boolean online) {
        if (transport != null) return; // ya conectado
        if (transportFactory == null) throw new IllegalStateException("TransportFactory no configurado");

        this.playerId = playerId;

        if (online) {
            transport = transportFactory.createOnline(host, port);
            if (!transport.connect()) {
                // fallback
                transport = transportFactory.createOffline();
                transport.connect();
            }
        } else {
            transport = transportFactory.createOffline();
            transport.connect();
        }

        transport.sendCommand("HELLO " + playerId);
    }
    
}