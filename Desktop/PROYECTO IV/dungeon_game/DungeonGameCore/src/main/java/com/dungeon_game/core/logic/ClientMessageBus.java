/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.dungeon_game.core.logic;

import com.dungeon_game.core.api.Updater;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
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

    // ✅ Thread-safe para iterar mientras se agregan/quitan subs
    private final CopyOnWriteArrayList<Sub> subs = new CopyOnWriteArrayList<>();
    private boolean running = false;

    public void start() {
        if (running) return;
        running = true;
        GameState.getInstance().registerUpdater(this);
    }

    public void subscribe(Predicate<String> filter, Listener listener) {
        if (filter == null || listener == null) return;
        subs.add(new Sub(filter, listener));
    }
    public void unsubscribe(Listener listener) {
        if (listener == null) return;
        subs.removeIf(s -> s.listener == listener);
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
        List<Sub> snapshot = new ArrayList<>(subs);
        for (Sub s : snapshot) {
            if (s.filter.test(line)) {
                s.listener.onMessage(line);
            }
        }
    }
    public void clearSubscriptions() {
        subs.clear();
    }
}