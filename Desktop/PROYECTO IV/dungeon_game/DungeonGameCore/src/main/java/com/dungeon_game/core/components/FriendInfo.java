/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.dungeon_game.core.components;

/**
 *
 * @author USUARIO
 */
public class FriendInfo {

    private String username;
    private String userCode;
    boolean online;
    public FriendInfo(String username, String userCode, boolean online){
        this.username = username;
        this.userCode = userCode;
        this.online = online;
       
    }
    public String getUsername(){
        return username;
    }
    public String getUserCode(){
        return userCode;
    }
    public boolean isOnline(){
        return online;
    }
    public void setUsername(String username){
        this.username = username;
    }
    public void setUserCode(String userCode){
        this.userCode = userCode;
    }
    public void setOnline(boolean online){
        this.online = online;
    }
    
}
