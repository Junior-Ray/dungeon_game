/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.dungeon_game.core.model.Usuario_y_Chat;
import java.util.Objects;
/**
 *
 * @author USUARIO
 */


public class Usuario implements Identificable, Comparable<Usuario>{

    private String codigo;  //Tipo valorant como #4353KGF45
    private String username;
    private String contrasena;
    private String email;
    private String avatarPath; 
    public Usuario(String codigo, String username, String contrasena, String avatarPath){
        this.codigo = codigo;
        this.username = username;
        this.contrasena = contrasena;
        this.avatarPath = avatarPath;
    }
    
    public Usuario(String codigo, String username, String contrasena) {
        this(codigo, username, contrasena, null);
    }
    public Usuario(){

    }
    @Override
    public String getCodigo(){
        return codigo;
    }
    public void setCodigo(String codigo){
        this.codigo = codigo;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username){ //Aca deberia ir una validacion, aca no, en la interfaz que se 
                                                //implemente para buscsar en la base de datos si hay o no :)
        this.username = username;
    }
    public String getContrasena(){
        return contrasena;
    }
    public void setContrasena(String contrasena){
        this.contrasena = contrasena;
    }
    public String getAvatarPath() {
        return avatarPath;
    }
    public void setAvatarPath(String avatarPath) {
        this.avatarPath = avatarPath;
    }
    @Override
    public int compareTo(Usuario u){
        int cmp = this.username.compareToIgnoreCase(u.getUsername());
        if (cmp == 0 ){
            return this.codigo.compareTo(u.getCodigo());
        }
        return cmp;
    }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public int hashCode(){
        return Objects.hash(codigo);
    }
    @Override
    public String toString(){
        return username + "(" + codigo + ")";
    }
}
