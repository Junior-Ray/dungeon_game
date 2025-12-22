/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.dungeon_game.core.logic;

import com.dungeon_game.core.api.Updater;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 *
 * @author USUARIO
 */
public class ClientMessageBus implements Updater {

    public interface Listener {
        void onMessage(String line);
    }

    private static ClientMessageBus instance;

    public static ClientMessageBus getInstance() {
        if (instance == null) instance = new ClientMessageBus();
        return instance;
    }

    private static class Sub {
        Predicate<String> filter;
        Listener listener;
        Sub(Predicate<String> f, Listener l) {
            filter = f;
            listener = l;
        }
    }

    private final List<Sub> subs = new ArrayList<>();
    private boolean running = false;

    public void start() {
        if (running) return;
        running = true;
        GameState.getInstance().registerUpdater(this);
    }

    public void subscribe(Predicate<String> filter, Listener listener) {
        subs.add(new Sub(filter, listener));
    }

    @Override
    public void update() {
        var inbox = GameState.getInstance().getInbox();
        String line;

        while ((line = inbox.poll()) != null) {
            dispatch(line);
        }
    }

    private void dispatch(String line) {
        for (Sub s : subs) {
            if (s.filter.test(line)) {
                s.listener.onMessage(line);
            }
        }
    }
}