/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.dungeon_game.core.structures;

import com.dungeon_game.core.model.Enemigo;
import com.dungeon_game.core.model.Item;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author USUARIO
 */


public class NodoSala {

    private String idSala; //Para la base de datos p
    
    private TipoSala tipoSala;
    
    private int nivel;
    
    private int coordX;//Se supone que est tanto X como Y seran iguales, pero para futuras modificaciones
    private int coordY; 
      
    private boolean bloqueada; //Si la sala esta bloqueada, puede ser que la puerta este cerrada o requiera llave
    
    private boolean visitada;  //Si es que ya se visito, el mapa ya no seria descubierto, logros, etc
                                // O incluso despues de eliminar todo sus mobs hostiles se quede limpio
    
    private final List<Enemigo> enemigos;
    private final List<Item> items;
    
    public NodoSala(String idSala, TipoSala tipoSala){
        this(idSala,tipoSala, 0, 0, 0, false, false);
    }
    public NodoSala(String idSala, TipoSala tipoSala, int nivel, int coordX, 
                        int coordY, boolean bloqueada, boolean visitada){
    if (idSala == null || idSala.isBlank()) {
            throw new IllegalArgumentException("idSala no puede ser nulo ni vacío");
        }
        if (tipoSala == null) {
            throw new IllegalArgumentException("tipoSala no puede ser nulo");
        }

        this.idSala = idSala;
        this.tipoSala = tipoSala;
        this.nivel = nivel;
        this.coordX = coordX;
        this.coordY = coordY;
        this.bloqueada = bloqueada;
        this.visitada = visitada;

        this.enemigos = new ArrayList<>();
        this.items = new ArrayList<>();
    }
    public String getIdSala() {
        return idSala;
    }

    public TipoSala getTipoSala() {
        return tipoSala;
    }

    public void setTipoSala(TipoSala tipoSala) {
        if (tipoSala == null) {
            throw new IllegalArgumentException("tipoSala no puede ser nulo");
        }
        this.tipoSala = tipoSala;
    }

    public int getNivel() {
        return nivel;
    }

    public void setNivel(int nivel) {
        this.nivel = nivel;
    }

    public int getCoordX() {
        return coordX;
    }

    public void setCoordX(int coordX) {
        this.coordX = coordX;
    }

    public int getCoordY() {
        return coordY;
    }

    public void setCoordY(int coordY) {
        this.coordY = coordY;
    }

    public boolean isBloqueada() {
        return bloqueada;
    }

    public void bloquear() {
        this.bloqueada = true;
    }

    public void desbloquear() {
        this.bloqueada = false;
    }

    public boolean isVisitada() {
        return visitada;
    }

    public void marcarComoVisitada() {
        this.visitada = true;
    }

    public void marcarComoNoVisitada() {
        this.visitada = false;
    }
    public List<Enemigo> getEnemigos() {
        // devolvemos lista inmodificable para proteger la estructura interna
        return Collections.unmodifiableList(enemigos);
    }

    public void agregarEnemigo(Enemigo enemigo) {
        if (enemigo == null) return;
        enemigos.add(enemigo);
    }

    public void eliminarEnemigo(Enemigo enemigo) {
        if (enemigo == null) return;
        enemigos.remove(enemigo);
    }

    public void limpiarEnemigos() {
        enemigos.clear();
    }

    public List<Item> getItems() {
        return Collections.unmodifiableList(items);
    }

    public void agregarItem(Item item) {
        if (item == null) return;
        items.add(item);
    }

    public void eliminarItem(Item item) {
        if (item == null) return;
        items.remove(item);
    }

    public void limpiarItems() {
        items.clear();
    }
    @Override
    public String toString() {
        return "NodoSala{" +
                "idSala='" + idSala + '\'' +
                ", tipoSala=" + tipoSala +
                ", nivel=" + nivel +
                ", coordX=" + coordX +
                ", coordY=" + coordY +
                ", bloqueada=" + bloqueada +
                ", visitada=" + visitada +
                ", enemigos=" + enemigos.size() +
                ", items=" + items.size() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NodoSala)) return false;
        NodoSala other = (NodoSala) o;
        return idSala.equals(other.idSala);
    }

    @Override
    public int hashCode() {
        return idSala.hashCode();
    }
}
