/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.dungeon_game.server.net;


import com.dungeon_game.core.model.Usuario_y_Chat.EstadoSolicitud;
import com.dungeon_game.core.model.Usuario_y_Chat.Usuario;
import com.dungeon_game.server.ServerContext;
import com.dungeon_game.server.session.PlayerMode;
import com.dungeon_game.server.session.PlayerSession;
import dao.AmigoDAO;
import dao.AuthDAO;
import dao.SolicitudAmistadDAO;
import dao.UsuarioDAO;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

/**
 *
 * @author USUARIO
 */
public class PlayerThread implements Runnable {
    
    private final Socket socket;
    private final ServerContext context;
    private final ConnectionManager manager;
    
    private PlayerSession session;
    private BufferedReader in;
    private PrintWriter out;
    private volatile boolean running = true;
    private String boundPlayerId;
    /**
     * @param args the command line arguments
     */
    public PlayerThread(Socket socket, ServerContext context, ConnectionManager manager) {
        this.socket = socket;
        this.context = context;
        this.manager = manager;
    }
    public void requestStop() {
        running = false;
        try { socket.close(); } catch (Exception ignored) {}
    }
    public String getPlayerId() {
        return (session != null) ? session.getPlayerId() : null;
    }
    @Override
    public void run() {
        try {
            in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

            sendLine("WELCOME DungeonGameServer v0.2");
            sendLine("Usa: HELLO, INVITE, ACCEPT, DECLINE, PARTY, PARTY_LEAVE, START_DUNGEON, END_DUNGEON, MOVE <idSala>, WHERE, WHO, QUIT");


            String line;
            while (running && (line = in.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                System.out.println("[Server] Recibido: " + line);

                String[] parts = line.split("\\s+", 2);
                String cmd = parts[0].toUpperCase();

                switch (cmd) {
                    case "HELLO" -> handleHello(parts.length > 1 ? parts[1] : null);
                    case "MOVE"  -> handleMove(parts.length > 1 ? parts[1] : null);
                    case "WHERE" -> handleWhere();
                    case "PARTY" -> handlePartyInfo();
                    case "PLAYERS" -> handlePlayers();  
                    case "WHO" -> handleWho();
                    case "INVITE" -> handleInvite(parts.length > 1 ? parts[1] : null);
                    case "ACCEPT" -> handleAccept(parts.length > 1 ? parts[1] : null);
                    case "DECLINE" -> handleDecline(parts.length > 1 ? parts[1] : null);
                    case "PARTY_LEAVE" -> handlePartyLeave();
                    case "START_DUNGEON" -> handleStartDungeon();
                    case "END_DUNGEON" -> handleEndDungeon();
                    
                    
                    case "CHAT" -> handleChat(parts.length > 1 ? parts[1] : null);
                    case "PARTY_CHAT" -> handlePartyChat(parts.length > 1 ? parts[1] : null);
                    case "WHISPER" -> handleWhisper(parts.length > 1 ? parts[1] : null);
                    
                    //Cosas donde se usa la base de datos del servidor
                    case "LOGIN" -> handleLogin(parts.length > 1 ? parts[1] : null);
                    case "AUTH_TOKEN" -> handleAuthToken(parts.length > 1 ? parts[1] : null);
                    case "REGISTER" -> handleRegister(parts.length > 1 ? parts[1] : null);
                    
                    case "FRIEND_LIST" -> handleFriendList();
                    case "FRIEND_REQUEST" -> handleFriendRequest(parts.length > 1 ? parts[1] : null);
                    case "FRIEND_PENDING" -> handleFriendPending();
                    case "FRIEND_ACCEPT" -> handleFriendAccept(parts.length > 1 ? parts[1] : null);
                    case "FRIEND_DECLINE" -> handleFriendDecline(parts.length > 1 ? parts[1] : null);

                    case "LOGOUT" -> handleLogout(parts.length > 1 ? parts[1] : null);
                    case "QUIT"  -> {
                        sendLine("BYE");
                        running = false;
                    }
                    default -> sendLine("ERROR Comando no reconocido");
                }
            }

        } catch (IOException e) {
            System.err.println("[Server] Error en PlayerThread: " + e.getMessage());
        } finally {
            try { socket.close(); } catch (IOException ignored) {}

            String pid = (session != null) ? session.getPlayerId() : null;
            String partyId = (session != null) ? session.getPartyId() : null;
            boolean wasLeader = (session != null) && session.isPartyLeader();

            // 1) Si estaba en party, límpialo como si hubiera hecho PARTY_LEAVE
            if (session != null && partyId != null) {
                // Obtener miembros actuales ANTES de borrar la sesión
                var miembros = context.getSessionManager().getSessionsByPartyId(partyId);

                // Quitar al que se va
                session.leaveParty();

                // Avisar al resto
                for (PlayerSession ps : miembros) {
                    String otherId = ps.getPlayerId();
                    if (pid != null && !otherId.equals(pid)) {
                        manager.sendTo(otherId, "[PARTY] " + pid + " se ha desconectado y salió de la party " + partyId);
                    }
                }

                // Reasignar líder si era líder
                if (wasLeader) {
                    PlayerSession nuevoLider = miembros.stream()
                            .filter(s -> pid == null || !s.getPlayerId().equals(pid))
                            .findFirst()
                            .orElse(null);

                    if (nuevoLider != null) {
                        nuevoLider.setPartyLeader(true);
                        manager.sendTo(nuevoLider.getPlayerId(),
                                "[PARTY] Ahora eres el líder de la party " + partyId);
                    }
                }

                // Broadcast actualizado (si queda gente)
                // OJO: getSessionsByPartyId ahora debe devolver sin el que salió
                // porque ya hicimos session.leaveParty()
                broadcastPartyMembers(partyId);
            }

            // 2) Sacarlo del SessionManager
            if (pid != null) {
                context.getSessionManager().removeSession(pid);
            }

            // 3) Sacarlo del ConnectionManager (threads activos :V)
            if (boundPlayerId != null) {
                manager.unregisterPlayer(boundPlayerId, this);
            }

            System.out.println("[Server] Conexión cerrada con " + pid);
            System.out.println("[Server][finally] pid=" + pid + " bound=" + boundPlayerId + " partyId=" + partyId);
        }
    }
    private void handleHello(String nombre) {
        if (session != null && boundPlayerId != null) {
            sendLine("OK HELLO " + boundPlayerId);
            return;
        }
        if (nombre == null || nombre.isBlank()) {
            sendLine("ERROR Debes enviar HELLO <nombre>");
            return;
        }

        String id = nombre.trim();

        // Crear la sesión lógica del jugador y ponerlo en LOBBY (por ahora)
        this.session = context.getSessionManager().getOrCreateSession(id);
        session.enterLobby();  // opcional, ya viene en LOBBY por defecto

        // Si tu GameEngine registra jugadores, sigue usando el id de la sesión:
        context.getGameEngine().registrarJugador(id);

        sendLine("OK HELLO " + id);
        this.boundPlayerId = id;
        manager.registerPlayer(id, this);
        System.out.println("[Server][HELLO] register " + id + " thread=" + Thread.currentThread().getName());
    }

    private void handleMove(String destino) {
        if (session == null) {
            sendLine("ERROR Primero usa HELLO <nombre>");
            return;
        }
        if (destino == null || destino.isBlank()) {
            sendLine("ERROR Uso: MOVE <idSala>");
            return;
        }

        String idDestino = destino.trim();
        String playerId = session.getPlayerId();  // 👉 obtenemos el id desde la sesión

        boolean ok = context.getGameEngine().moverJugador(playerId, idDestino);
        if (ok) {
            // opcional: actualizar salaActual en la sesión, si quieres
            session.moveToSala(idDestino);
            sendLine("MOVE_OK " + idDestino);
        } else {
            sendLine("MOVE_FAIL " + idDestino);
        }
    }

    private void handleWhere() {
        if (session == null) {
            sendLine("ERROR Primero usa HELLO <nombre>");
            return;
        }

        String playerId  = session.getPlayerId();
        PlayerMode mode  = session.getMode();
        String dungeonId = session.getDungeonId();
        String salaSesion = session.getSalaActual();

        System.out.println("[WHERE] player=" + playerId +
                " mode=" + mode +
                " dungeonId=" + dungeonId +
                " salaSesion=" + salaSesion);

        // 1) Si está en LOBBY, la respuesta es simple y estable
        if (mode == PlayerMode.LOBBY) {
            sendLine("WHERE LOBBY");
            return;
        }

        // 2) Si está en DUNGEON, usamos sesión como fuente de verdad
        if (mode == PlayerMode.DUNGEON) {
            // Intento de sincronización con el engine (solo para detectar inconsistencias)
            String salaEngine = null;
            try {
                salaEngine = context.getGameEngine().getSalaActualDe(playerId);
            } catch (Exception e) {
                System.err.println("[WHERE] Error consultando GameEngine para " + playerId + ": " + e.getMessage());
            }

            // --- CASO A: ambas fuentes tienen valor ---
            if (salaSesion != null && salaEngine != null) {
                if (!salaSesion.equals(salaEngine)) {
                    // Inconsistencia detectada → forzamos al engine a alinearse con la sesión
                    System.err.println("[WHERE][WARN] Inconsistencia sala. session=" + salaSesion +
                            " engine=" + salaEngine + " → Forzando engine a " + salaSesion);
                    try {
                        context.getGameEngine().moverJugador(playerId, salaSesion);
                    } catch (Exception e) {
                        System.err.println("[WHERE][ERROR] No se pudo forzar moverJugador(" +
                                playerId + ", " + salaSesion + "): " + e.getMessage());
                    }
                }
                sendLine("WHERE " + salaSesion);
                return;
            }

            // --- CASO B: sesión tiene sala, engine no ---
            if (salaSesion != null && salaEngine == null) {
                System.err.println("[WHERE][WARN] Engine no tiene sala para " + playerId +
                        " pero la sesión sí (" + salaSesion + "). Re-sincronizando engine.");
                try {
                    context.getGameEngine().moverJugador(playerId, salaSesion);
                } catch (Exception e) {
                    System.err.println("[WHERE][ERROR] No se pudo mover en engine: " + e.getMessage());
                }
                sendLine("WHERE " + salaSesion);
                return;
            }

            // --- CASO C: engine tiene sala, sesión no ---
            if (salaSesion == null && salaEngine != null) {
                System.err.println("[WHERE][WARN] Sesión sin sala, pero engine tiene " +
                        salaEngine + " para " + playerId + ". Actualizando sesión.");
                session.moveToSala(salaEngine);
                sendLine("WHERE " + salaEngine);
                return;
            }

            // --- CASO D: ninguna fuente tiene sala ---
            System.err.println("[WHERE][WARN] Ni sesión ni engine tienen sala para " +
                    playerId + " estando en DUNGEON. Respondiendo (sin_sala).");
            sendLine("WHERE (sin_sala)");
            return;
        }

        // 3) Si algún día agregas más modos, al menos no revienta:
        System.err.println("[WHERE][WARN] Estado desconocido para " + playerId +
                " mode=" + mode + ". Devolviendo (unknown_state).");
        sendLine("WHERE (unknown_state)");
    }

    public void sendLine(String msg) {
        if (out != null) {
            out.println(msg);
        }
    }
    private void handlePlayers() {
        if (session == null) {
            sendLine("ERROR Primero usa HELLO <nombre>");
            return;
        }

        switch (session.getMode()) {

            case LOBBY -> {
                var todas = context.getSessionManager().getAllSessions();

                List<String> lobbyNames = todas.stream()
                        .filter(s -> s.getMode() == PlayerMode.LOBBY)
                        .map(PlayerSession::getPlayerId)
                        .toList();

                sendLine("PLAYERS Lobby: " + String.join(", ", lobbyNames));
            }

            case DUNGEON -> {
                String salaActual = session.getSalaActual();
                if (salaActual == null) {
                    sendLine("PLAYERS Dungeon vacío");
                    return;
                }

                var todas = context.getSessionManager().getAllSessions();

                List<String> mismos = todas.stream()
                        .filter(s -> s.getMode() == PlayerMode.DUNGEON)
                        .filter(s -> salaActual.equals(s.getSalaActual()))
                        .map(PlayerSession::getPlayerId)
                        .toList();

                sendLine("PLAYERS " + salaActual + ": " + String.join(", ", mismos));
            }
        }
    }
    private void handleWho() {
        var lista = context.getSessionManager().getResumenSesiones();

        if (lista.isEmpty()) {
            sendLine("WHO (vacío)");
            return;
        }

        String joined = String.join(", ", lista);
        sendLine("WHO " + joined);
    }
    private void handleInvite(String targetName) {
        if (session == null) {
            sendLine("ERROR Primero usa HELLO <nombre>");
            return;
        }
        if (session.getMode() != PlayerMode.LOBBY) {
            sendLine("ERROR Solo puedes invitar desde el LOBBY");
            return;
        }
        if (targetName == null || targetName.isBlank()) {
            sendLine("ERROR Uso: INVITE <nombreJugador>");
            return;
        }

        String fromId = session.getPlayerId();
        String toId = targetName.trim();

        if (fromId.equals(toId)) {
            sendLine("ERROR No puedes invitarte a ti mismo");
            return;
        }

        var targetSession = context.getSessionManager().getSession(toId);
        if (targetSession == null) {
            sendLine("ERROR El jugador " + toId + " no está conectado");
            return;
        }
        if (targetSession.getMode() != PlayerMode.LOBBY) {
            sendLine("ERROR El jugador " + toId + " no está en el LOBBY");
            return;
        }

        // Registrar invitación en el receptor
        targetSession.addInvitation(fromId);

        // Avisar al emisor
        sendLine("INVITE_OK Enviada a " + toId);

        // Avisar al receptor (si tiene conexión activa)
        manager.sendTo(toId, "[INVITE] " + fromId + " te ha invitado a su party. Usa: ACCEPT " + fromId + " o DECLINE " + fromId);
    }
    private void handleAccept(String fromName) {
        if (session == null) {
            sendLine("ERROR Primero usa HELLO <nombre>");
            return;
        }
        if (session.getMode() != PlayerMode.LOBBY) {
            sendLine("ERROR Solo puedes aceptar invitaciones en el LOBBY");
            return;
        }
        if (fromName == null || fromName.isBlank()) {
            sendLine("ERROR Uso: ACCEPT <nombreJugadorQueInvitó>");
            return;
        }

        String myId = session.getPlayerId();
        String inviterId = fromName.trim();

        if (!session.getPendingInvitations().contains(inviterId)) {
            sendLine("ERROR No tienes invitación pendiente de " + inviterId);
            return;
        }

        // Obtener la sesión del que invitó
        var inviterSession = context.getSessionManager().getSession(inviterId);
        if (inviterSession == null) {
            sendLine("ERROR El jugador " + inviterId + " ya no está conectado");
            session.removeInvitation(inviterId);
            return;
        }

        // Quitar la invitación pendiente
        session.removeInvitation(inviterId);

        // Determinar partyId a usar:
        String partyId = inviterSession.getPartyId();
        if (partyId == null) {
            // El que invita no tiene party todavía → se crea una
            partyId = "party-" + inviterId;
            inviterSession.joinParty(partyId, true);  // él es líder
        }

        // Ahora el que acepta se une a esa party
        session.joinParty(partyId, false);

        sendLine("ACCEPT_OK Te has unido a la party de " + inviterId + " (" + partyId + ")");
        manager.sendTo(inviterId, "[PARTY] " + myId + " ha aceptado tu invitación. PartyId=" + partyId);
        broadcastPartyMembers(partyId);
    }
    private void handleDecline(String fromName) {
        if (session == null) {
            sendLine("ERROR Primero usa HELLO <nombre>");
            return;
        }
        if (session.getMode() != PlayerMode.LOBBY) {
            sendLine("ERROR Solo puedes rechazar invitaciones en el LOBBY");
            return;
        }
        if (fromName == null || fromName.isBlank()) {
            sendLine("ERROR Uso: DECLINE <nombreJugadorQueInvitó>");
            return;
        }

        String myId = session.getPlayerId();
        String inviterId = fromName.trim();

        if (!session.getPendingInvitations().contains(inviterId)) {
            sendLine("ERROR No tienes invitación pendiente de " + inviterId);
            return;
        }

        // Quitar invitación
        session.removeInvitation(inviterId);
        sendLine("DECLINE_OK Has rechazado la invitación de " + inviterId);

        // Avisar (opcional) al que invitó
        manager.sendTo(inviterId, "[INVITE] " + myId + " ha rechazado tu invitación.");
    }
    private void handlePartyInfo() {
        if (session == null) {
            sendLine("ERROR Primero usa HELLO <nombre>");
            return;
        }

        String partyId = session.getPartyId();
        if (partyId == null) {
            sendLine("PARTY_INFO No perteneces a ninguna party");
            return;
        }

        var todos = context.getSessionManager().getAllSessions();

        List<String> miembros = todos.stream()
                .filter(s -> partyId.equals(s.getPartyId()))
                .map(PlayerSession::getPlayerId)
                .toList();

        String leader = miembros.stream()
                .filter(id -> {
                    PlayerSession s = context.getSessionManager().getSession(id);
                    return s != null && s.isPartyLeader();
                })
                .findFirst()
                .orElse("(sin líder)");

        sendLine("PARTY_INFO " +
                 "PartyId=" + partyId +
                 ", Leader=" + leader +
                 ", Members=" + String.join(", ", miembros));
        broadcastPartyMembers(partyId);
    }

    private void handlePartyLeave() {
        if (session == null) {
            sendLine("ERROR Primero usa HELLO <nombre>");
            return;
        }

        if (session.getMode() != PlayerMode.LOBBY) {
            sendLine("ERROR Solo puedes abandonar la party desde el LOBBY");
            return;
        }

        String partyId = session.getPartyId();
        if (partyId == null) {
            sendLine("PARTY_LEAVE No perteneces a ninguna party");
            return;
        }

        String myId = session.getPlayerId();

        // Obtener todos los miembros de la party
        var todas = context.getSessionManager().getAllSessions();

        // Miembros actuales
        var miembros = todas.stream()
                .filter(s -> partyId.equals(s.getPartyId()))
                .toList();

        // Caso 1: solo está él en la party -> se disuelve
        if (miembros.size() <= 1) {
            session.leaveParty();
            sendLine("PARTY_LEAVE_OK Has abandonado la party. La party se ha disuelto.");

            // IMPORTANTE: forzar limpieza en UI del que se fue
            sendLine("PARTY_MEMBERS (none) (sin_lider) "); // lista vacía

            return;
        }

        // Caso 2: hay más miembros
        boolean yoSoyLider = session.isPartyLeader();

        if (yoSoyLider) {
            // Buscar un nuevo líder (cualquiera que no sea yo)
            PlayerSession nuevoLider = miembros.stream()
                    .filter(s -> !s.getPlayerId().equals(myId))
                    .findFirst()
                    .orElse(null);

            if (nuevoLider != null) {
                nuevoLider.setPartyLeader(true);
                manager.sendTo(nuevoLider.getPlayerId(),
                        "[PARTY] Ahora eres el líder de la party " + partyId);
            }
        }

        // El que llama abandona la party
        session.leaveParty();
        sendLine("PARTY_LEAVE_OK Has abandonado la party " + partyId);

        // Avisar al resto de miembros
    
        for (PlayerSession ps : miembros) {
        
            String otherId = ps.getPlayerId();
            if (!otherId.equals(myId)) {
                manager.sendTo(otherId, "[PARTY] " + myId + " ha abandonado la party " + partyId);
            }
        }
       
        String partyIdAfter = partyId; // (mismo id)
        broadcastPartyMembers(partyIdAfter);
    }
    private void handleStartDungeon() {
        if (session == null) {
            sendLine("ERROR Primero usa HELLO <nombre>");
            return;
        }

        if (session.getMode() != PlayerMode.LOBBY) {
            sendLine("ERROR Ya estás en un dungeon o no estás en el LOBBY");
            return;
        }

        String myId = session.getPlayerId();
        String partyId = session.getPartyId();

        // ========================================
        // 1) Determinar quiénes van a entrar
        // ========================================
        List<PlayerSession> participantes;

        if (partyId == null) {
            // Jugador solo, sin party
            participantes = List.of(session);
        } else {
            // Está en una party: solo el líder puede iniciar
            if (!session.isPartyLeader()) {
                sendLine("ERROR Solo el líder de la party puede iniciar el dungeon");
                return;
            }

            var todas = context.getSessionManager().getAllSessions();
            participantes = todas.stream()
                    .filter(s -> partyId.equals(s.getPartyId()))
                    .toList();

            if (participantes.isEmpty()) {
                sendLine("ERROR La party está vacía (?)");
                return;
            }
        }

        // ========================================
        // 2) Validar que todos estén en LOBBY
        // ========================================
        boolean todosEnLobby = participantes.stream()
                .allMatch(s -> s.getMode() == PlayerMode.LOBBY);

        if (!todosEnLobby) {
            sendLine("ERROR Todos los miembros de la party deben estar en el LOBBY");
            return;
        }

        // ========================================
        // 3) Construir dungeonId lógico
        // ========================================
        String dungeonId;
        if (partyId == null) {
            dungeonId = "dungeon-" + myId;
        } else {
            dungeonId = "dungeon-" + partyId;
        }

        String salaInicial = "S1"; // de momento, siempre S1

        // ========================================
        // 4) Meter a todos en el dungeon
        // ========================================
        var engine = context.getGameEngine();

        StringBuilder sbMiembros = new StringBuilder();

        for (PlayerSession ps : participantes) {
            String pid = ps.getPlayerId();

            // Estado lógico
            ps.enterDungeon(dungeonId, salaInicial);

            // Estado en el engine (por si acaso, los mandamos a S1)
            engine.moverJugador(pid, salaInicial);

            // Listado para feedback
            if (!pid.equals(myId)) {
                if (!sbMiembros.isEmpty()) sbMiembros.append(", ");
                sbMiembros.append(pid);
            }

            // Aviso a cada uno
            if (pid.equals(myId)) {
                // yo mismo
                sendLine("DUNGEON_START " + dungeonId + " " + salaInicial);
            } else {
                manager.sendTo(pid, "DUNGEON_START " + dungeonId + " " + salaInicial);
            }
        }

        // ----------------------------------------
        // Mensaje resumen para el líder
        // ----------------------------------------
        if (partyId == null) {
            sendLine("START_DUNGEON_OK " + dungeonId + " " + salaInicial + " (solo)");
        } else {
            sendLine("START_DUNGEON_OK " + dungeonId + " " + salaInicial +
                     " Miembros: " + myId +
                     (sbMiembros.isEmpty() ? "" : ", " + sbMiembros));
        }
    }
    private void handleEndDungeon() {
        if (session == null) {
            sendLine("ERROR Primero usa HELLO <nombre>");
            return;
        }

        if (session.getMode() != PlayerMode.DUNGEON) {
            sendLine("ERROR No estás dentro de un dungeon");
            return;
        }

        String myId = session.getPlayerId();
        String partyId = session.getPartyId();
        String dungeonId = session.getDungeonId(); // asumiendo que tienes este getter

        // =======================================================
        // 1) Determinar quiénes van a salir del dungeon
        // =======================================================
        List<PlayerSession> participantes;

        if (partyId == null) {
            // Jugador solo, sin party
            participantes = List.of(session);
        } else {
            // Está en party: solo el líder puede terminar el dungeon
            if (!session.isPartyLeader()) {
                sendLine("ERROR Solo el líder de la party puede terminar el dungeon");
                return;
            }

            var todas = context.getSessionManager().getAllSessions();
            participantes = todas.stream()
                    .filter(s -> partyId.equals(s.getPartyId()))
                    .filter(s -> s.getMode() == PlayerMode.DUNGEON)
                    .toList();
        }

        if (participantes.isEmpty()) {
            sendLine("ERROR No hay participantes activos en el dungeon");
            return;
        }

        // =======================================================
        // 2) Sacar a todos del dungeon → LOBBY
        // =======================================================
        for (PlayerSession ps : participantes) {
            String pid = ps.getPlayerId();

            // Estado lógico: volver a lobby
            ps.enterLobby();  // aquí internamente debería limpiar dungeonId, salaActual, etc.

            try {
                context.getGameEngine().resetearPosicion(pid);
            } catch (Exception e) {
                System.err.println("[END_DUNGEON][WARN] No se pudo resetear posición de " +
                        pid + " en GameEngine: " + e.getMessage());
            }

            // Avisar a cada uno
            if (pid.equals(myId)) {
                sendLine("DUNGEON_END lobby");
            } else {
                manager.sendTo(pid, "DUNGEON_END lobby");
            }
        }

        // =======================================================
        // 3) Mensaje de confirmación para el que llamó
        // =======================================================
        if (partyId == null) {
            sendLine("END_DUNGEON_OK " + (dungeonId != null ? dungeonId : "(dungeon-solo)"));
        } else {
            StringBuilder sbMiembros = new StringBuilder();
            for (PlayerSession ps : participantes) {
                if (!ps.getPlayerId().equals(myId)) {
                    if (!sbMiembros.isEmpty()) sbMiembros.append(", ");
                    sbMiembros.append(ps.getPlayerId());
                }
            }
            sendLine("END_DUNGEON_OK " +
                     (dungeonId != null ? dungeonId : "(dungeon-" + partyId + ")") +
                     " Miembros enviados al lobby: " + myId +
                     (sbMiembros.isEmpty() ? "" : ", " + sbMiembros));
        }
    }
    private void handleChat(String text) {
        if (session == null) {
            sendLine("ERROR Primero usa HELLO <nombre>");
            return;
        }
        if (text == null || text.isBlank()) {
            sendLine("ERROR Uso: CHAT <mensaje>");
            return;
        }

        manager.getChatService().sendGlobal(session, text);
    }
    private void handlePartyChat(String text) {
        if (session == null) {
            sendLine("ERROR Primero usa HELLO <nombre>");
            return;
        }
        if (text == null || text.isBlank()) {
            sendLine("ERROR Uso: PARTY_CHAT <mensaje>");
            return;
        }

        manager.getChatService().sendParty(session, text);
    }
    private void handleWhisper(String args) {
        if (session == null) {
            sendLine("ERROR Primero usa HELLO <nombre>");
            return;
        }
        if (args == null || args.isBlank()) {
            sendLine("ERROR Uso: WHISPER <nombreJugador> <mensaje>");
            return;
        }

        String[] parts = args.split("\\s+", 2);
        if (parts.length < 2) {
            sendLine("ERROR Uso: WHISPER <nombreJugador> <mensaje>");
            return;
        }

        String targetName = parts[0];
        String msg = parts[1];

        manager.getChatService().sendWhisper(session, targetName, msg);
    }
    private void broadcastPartyMembers(String partyId) {
        
        
        var todas = context.getSessionManager().getAllSessions();

        List<PlayerSession> miembros = todas.stream()
                .filter(s -> partyId.equals(s.getPartyId()))
                .toList();

        String leader = miembros.stream()
                .filter(PlayerSession::isPartyLeader)
                .map(PlayerSession::getPlayerId)
                .findFirst()
                .orElse("(sin_lider)");

        String list = miembros.stream()
                .map(PlayerSession::getPlayerId)
                .reduce((a,b) -> a + "," + b)
                .orElse("");

        String msg = "PARTY_MEMBERS " + partyId + " " + leader + " " + list;
        
        for (PlayerSession ps : miembros) {
            manager.sendTo(ps.getPlayerId(), msg);
            
        }
        System.out.println("[Server] broadcastPartyMembers -> " + msg);
        System.out.println("[Server][broadcastPartyMembers] partyId=" + partyId + " msg=" + msg);
        for (PlayerSession ps : miembros) {
            System.out.println("[Server][broadcastPartyMembers] -> sendTo=" + ps.getPlayerId());
            manager.sendTo(ps.getPlayerId(), msg);
        }

    }
    private void handleLogin(String args) {
        if (args == null) {
            sendLine("LOGIN_FAIL Datos incompletos");
            return;
        }

        String[] p = args.split("\\s+", 2);
        if (p.length < 2) {
            sendLine("LOGIN_FAIL Datos incompletos");
            return;
        }

        String username = p[0];
        String password = p[1];

        try {
            UsuarioDAO usuarioDAO = new UsuarioDAO();
            Usuario u = usuarioDAO.buscarPorCredenciales(username, password);

            if (u == null) {
                sendLine("LOGIN_FAIL Credenciales inválidas");
                return;
            }

            // Crear token
            String token = java.util.UUID.randomUUID().toString();
            long expira = System.currentTimeMillis() + (1000L * 60 * 60 * 24); // 24h

            AuthDAO authDAO = new AuthDAO();
            authDAO.guardarToken(token, u.getCodigo(), expira);

            // Crear sesión lógica
            this.session = context.getSessionManager()
                    .getOrCreateSession(u.getUsername());
            session.setUserCode(u.getCodigo());
            session.enterLobby();

            this.boundPlayerId = u.getUsername();
            manager.registerPlayer(boundPlayerId, this);

            sendLine("LOGIN_OK " + token + " " + u.getUsername() + " " + u.getCodigo());

            System.out.println("[LOGIN_OK] " + u.getUsername());

        } catch (Exception e) {
            e.printStackTrace();
            sendLine("LOGIN_FAIL Error interno");
        }
    }
    private void handleAuthToken(String token) {
        try {
            AuthDAO authDAO = new AuthDAO();
            String userCode = authDAO.obtenerUsuarioPorToken(token);

            if (userCode == null) {
                sendLine("AUTH_FAIL");
                return;
            }

            UsuarioDAO usuarioDAO = new UsuarioDAO();
            Usuario u = usuarioDAO.buscarPorCodigo(userCode);

            if (u == null) {
                sendLine("AUTH_FAIL");
                return;
            }

            session = context.getSessionManager()
                    .getOrCreateSession(u.getUsername());
            session.setUserCode(userCode);
            session.enterLobby();

            boundPlayerId = u.getUsername();
            manager.registerPlayer(boundPlayerId, this);

            sendLine("AUTH_OK " + u.getUsername() + " " + userCode);

        } catch (Exception e) {
            e.printStackTrace();
            sendLine("AUTH_FAIL");
        }
    }
    private void handleRegister(String args) {
        if (args == null || args.isBlank()) {
            sendLine("REGISTER_FAIL Datos incompletos");
            return;
        }

        // REGISTER user email pass  (pass puede tener espacios? normalmente no)
        String[] p = args.split("\\s+", 3);
        if (p.length < 3) {
            sendLine("REGISTER_FAIL Uso: REGISTER <username> <email> <password>");
            return;
        }

        String username = p[0].trim();
        String email    = p[1].trim();
        String password = p[2].trim();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            sendLine("REGISTER_FAIL Datos incompletos");
            return;
        }

        try {
            UsuarioDAO usuarioDAO = new UsuarioDAO();

            // Validaciones mínimas en server (además de las del cliente)
            if (usuarioDAO.existeUsername(username)) {
                sendLine("REGISTER_FAIL Username ya existe");
                return;
            }
            if (usuarioDAO.existeEmail(email)) {
                sendLine("REGISTER_FAIL Email ya existe");
                return;
            }

            // Crear usuario
            Usuario u = usuarioDAO.crearUsuario(username, email, password, "default_avatar.png");

            // Crear token (igual que login)
            String token = java.util.UUID.randomUUID().toString();
            long expira = System.currentTimeMillis() + (1000L * 60 * 60 * 24); // 24h

            AuthDAO authDAO = new AuthDAO();
            authDAO.guardarToken(token, u.getCodigo(), expira);

            // Crear sesión lógica (queda logueado automáticamente)
            this.session = context.getSessionManager().getOrCreateSession(u.getUsername());
            session.setUserCode(u.getCodigo());
            session.enterLobby();

            this.boundPlayerId = u.getUsername();
            manager.registerPlayer(boundPlayerId, this);

            sendLine("REGISTER_OK " + token + " " + u.getUsername() + " " + u.getCodigo());
            System.out.println("[REGISTER_OK] " + u.getUsername());

        } catch (Exception e) {
            e.printStackTrace();
            sendLine("REGISTER_FAIL Error interno");
        }
    }
    private void handleLogout(String token) {
        try {
            if (token != null && !token.isBlank()) {
                AuthDAO.eliminarToken(token.trim()); // o authDAO.eliminarToken(...)
            }

            // Limpieza de session igual que en finally:
            if (session != null) {
                String pid = session.getPlayerId();
                context.getSessionManager().removeSession(pid);
            }

            sendLine("LOGOUT_OK");
        } catch (Exception e) {
            e.printStackTrace();
            sendLine("LOGOUT_FAIL");
        } finally {
            running = false;
            try { socket.close(); } catch (Exception ignored) {}
        }
    }
    private void handleFriendList() {
        if (session == null || session.getUserCode() == null) {
            sendLine("FRIEND_FAIL No autenticado");
            return;
        }

        String myCode = session.getUserCode();

        try {
            AmigoDAO amigoDAO = new AmigoDAO();
            List<com.dungeon_game.core.model.Usuario_y_Chat.Usuario> amigos = amigoDAO.obtenerAmigosDe(myCode);

            // Armamos respuesta con online/offline
            // Necesitamos username para checar online, tú guardas conexiones por playerId (=username)
            // amigos trae username y codigo
            // JSON: [{ "username":"", "code":"", "online":true }]
            StringBuilder sb = new StringBuilder();
            sb.append("[");

            for (int i = 0; i < amigos.size(); i++) {
                var u = amigos.get(i);
                boolean online = manager.isOnline(u.getUsername());

                sb.append("{")
                  .append("\"username\":\"").append(escape(u.getUsername())).append("\",")
                  .append("\"code\":\"").append(escape(u.getCodigo())).append("\",")
                  .append("\"online\":").append(online)
                  .append("}");

                if (i < amigos.size() - 1) sb.append(",");
            }

            sb.append("]");
            sendLine("FRIEND_LIST " + sb.toString());

        } catch (Exception e) {
            e.printStackTrace();
            sendLine("FRIEND_FAIL Error interno");
        }
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
    private void handleFriendRequest(String args) {
        if (session == null || session.getUserCode() == null) {
            sendLine("FRIEND_FAIL No autenticado");
            return;
        }
        if (args == null || args.isBlank()) {
            sendLine("FRIEND_FAIL Uso: FRIEND_REQUEST <username>");
            return;
        }

        String targetUsername = args.trim();
        String myCode = session.getUserCode();

        try {
            UsuarioDAO usuarioDAO = new UsuarioDAO();
            String targetCode = usuarioDAO.obtenerCodigoPorUsername(targetUsername);

            if (targetCode == null) {
                sendLine("FRIEND_FAIL Usuario no existe");
                return;
            }
            if (targetCode.equals(myCode)) {
                sendLine("FRIEND_FAIL No puedes agregarte a ti mismo");
                return;
            }

            AmigoDAO amigoDAO = new AmigoDAO();
            if (amigoDAO.existeAmistad(myCode, targetCode)) {
                sendLine("FRIEND_FAIL Ya son amigos");
                return;
            }

            SolicitudAmistadDAO solDAO = new SolicitudAmistadDAO();
            if (solDAO.existePendienteEntre(myCode, targetCode)) {
                sendLine("FRIEND_FAIL Ya hay una solicitud pendiente");
                return;
            }

            var sol = solDAO.crearSolicitud(myCode, targetCode);
            sendLine("FRIEND_REQUEST_SENT " + targetUsername);

            // Push en tiempo real al receptor si está online:
            // Necesitamos el username del emisor para mostrarlo.
            String fromUsername = session.getPlayerId(); // en tu server playerId = username
            if (fromUsername == null) fromUsername = "Unknown";

            if (manager.isOnline(targetUsername)) {
                manager.sendTo(targetUsername,
                    "FRIEND_REQUEST_RECEIVED " + sol.getId() + " " + fromUsername
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendLine("FRIEND_FAIL Error interno");
        }
    }
    private void handleFriendPending() {
        if (session == null || session.getUserCode() == null) {
            sendLine("FRIEND_FAIL No autenticado");
            return;
        }

        String myCode = session.getUserCode();

        try {
            SolicitudAmistadDAO solDAO = new SolicitudAmistadDAO();
            UsuarioDAO usuarioDAO = new UsuarioDAO();

            var pendientes = solDAO.obtenerPendientesRecibidas(myCode);

            // JSON: [{ "id":1, "fromCode":"...", "fromUsername":"..." }]
            StringBuilder sb = new StringBuilder();
            sb.append("[");

            for (int i = 0; i < pendientes.size(); i++) {
                var s = pendientes.get(i);

                var fromUser = usuarioDAO.buscarPorCodigo(s.getEmisorCodigo());
                String fromUsername = (fromUser != null) ? fromUser.getUsername() : "Unknown";

                sb.append("{")
                  .append("\"id\":").append(s.getId()).append(",")
                  .append("\"fromCode\":\"").append(escape(s.getEmisorCodigo())).append("\",")
                  .append("\"fromUsername\":\"").append(escape(fromUsername)).append("\"")
                  .append("}");

                if (i < pendientes.size() - 1) sb.append(",");
            }

            sb.append("]");
            sendLine("FRIEND_PENDING " + sb);

        } catch (Exception e) {
            e.printStackTrace();
            sendLine("FRIEND_FAIL Error interno");
        }
    }
    private void handleFriendAccept(String args) {
        if (session == null || session.getUserCode() == null) {
            sendLine("FRIEND_FAIL No autenticado");
            return;
        }
        if (args == null || args.isBlank()) {
            sendLine("FRIEND_FAIL Uso: FRIEND_ACCEPT <solicitudId>");
            return;
        }

        int id;
        try { id = Integer.parseInt(args.trim()); }
        catch (Exception e) { sendLine("FRIEND_FAIL solicitudId inválido"); return; }

        String myCode = session.getUserCode();

        try {
            SolicitudAmistadDAO solDAO = new SolicitudAmistadDAO();
            var sol = solDAO.obtenerPorId(id);

            if (sol == null || sol.getEstado() != com.dungeon_game.core.model.Usuario_y_Chat.EstadoSolicitud.PENDIENTE) {
                sendLine("FRIEND_FAIL Solicitud no válida");
                return;
            }

            // Validar que yo soy el receptor
            if (!myCode.equals(sol.getReceptorCodigo())) {
                sendLine("FRIEND_FAIL No es tu solicitud");
                return;
            }

            // Aceptar + crear amistad
            solDAO.aceptarSolicitud(id);

            AmigoDAO amigoDAO = new AmigoDAO();
            amigoDAO.agregarAmistad(sol.getEmisorCodigo(), sol.getReceptorCodigo());

            // Responder al que aceptó: quién es el amigo
            UsuarioDAO usuarioDAO = new UsuarioDAO();
            var friendUser = usuarioDAO.buscarPorCodigo(sol.getEmisorCodigo());
            String friendUsername = (friendUser != null) ? friendUser.getUsername() : "Unknown";

            sendLine("FRIEND_ACCEPTED " + friendUsername + " " + sol.getEmisorCodigo());

            // Push al emisor si está online
            if (friendUser != null && manager.isOnline(friendUser.getUsername())) {
                manager.sendTo(friendUser.getUsername(),
                    "FRIEND_ACCEPTED " + session.getPlayerId() + " " + myCode
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendLine("FRIEND_FAIL Error interno");
        }
    }
    private void handleFriendDecline(String args) {
        if (session == null || session.getUserCode() == null) {
            sendLine("FRIEND_FAIL No autenticado");
            return;
        }
        if (args == null || args.isBlank()) {
            sendLine("FRIEND_FAIL Uso: FRIEND_DECLINE <solicitudId>");
            return;
        }

        int id;
        try { id = Integer.parseInt(args.trim()); }
        catch (Exception e) { sendLine("FRIEND_FAIL solicitudId inválido"); return; }

        String myCode = session.getUserCode();

        try {
            SolicitudAmistadDAO solDAO = new SolicitudAmistadDAO();
            var sol = solDAO.obtenerPorId(id);

            if (sol == null || sol.getEstado() != EstadoSolicitud.PENDIENTE) {
                sendLine("FRIEND_FAIL Solicitud no válida");
                return;
            }

            if (!myCode.equals(sol.getReceptorCodigo())) {
                sendLine("FRIEND_FAIL No es tu solicitud");
                return;
            }

            solDAO.rechazarSolicitud(id);
            sendLine("FRIEND_DECLINED " + id);

        } catch (Exception e) {
            e.printStackTrace();
            sendLine("FRIEND_FAIL Error interno");
        }
    }







}
