/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.dungeon_game.core.components;

import com.dungeon_game.core.api.DriverRender;
import com.dungeon_game.core.api.RenderProcessor;
import com.dungeon_game.core.api.Updater;
import com.dungeon_game.core.data.SlideVertical;
import com.dungeon_game.core.data.SpatialGrid;
import com.dungeon_game.core.data.VisualRender;
import com.dungeon_game.core.logic.ClientMessageBus;
import com.dungeon_game.core.logic.GameState;
import com.dungeon_game.core.logic.InterpreterEvent;
import com.dungeon_game.core.model.Imagen;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author USUARIO
 */
public class LobbyFriendsOverlay2 implements Updater{
        
    private final List<FriendButton> friendButtonsConectados = new ArrayList<>();
    private final List<FriendButton> friendButtonsDesconectados = new ArrayList<>();

    // Panel lateral (coordenadas base)
    private static final int PX = 780;
    private static final int PY = 110;
    private static final int PW = 420;
    private static final int PH = 480;

    // “Altura lógica” reservada para conectados cuando está expandido
    private static final int CONECTADOS_AREA_EXPANDED = PH / 2; // aprox mitad del panel
    private static final int CONECTADOS_AREA_COLLAPSED = 30;    // sólo header

    // Estado de los desplegables
    private boolean showConectados = true;
    private boolean showDesconectados = true;

    // Listas de amigos (para futuro dinámico)
    private final List<FriendInfo> conectados = Arrays.asList();

    private final List<FriendInfo> desconectados = Arrays.asList();

    // Posiciones dinámicas reales calculadas en cada dibujado
    private int lastYHeaderConectados;
    private int lastYHeaderDesconectados;

    // Fondo con todo dibujado
    private final Imagen semiFondoAmigos;

    // Botones inferiores y X
    private final UIButton btnAgregarAmigo;
    private final UIButton btnBuscarAmigo;
    private final UIButton btnCerrarAmigos;

    // Zonas clickeables de encabezados
    private final UIButton btnHeaderConectados;
    private final UIButton btnHeaderDesconectados;
    
    private boolean addExpanded = false;
    private SlideVertical addAnimBtn;
    private SlideVertical addAnimInput;

    private InputText addFriendInput;

    private String toastText = null;
    private long toastUntilMs = 0;
    private boolean changed = false;
    private boolean subscribed = false;


    public LobbyFriendsOverlay2() {
        
        
        // Imagen base
        semiFondoAmigos = new Imagen(0, 0, 1280, 720, 6, null, null, 210);
        

        // Forma base de botones
        Point[] vBtn = new Point[4];
        vBtn[0] = new Point(0, 0);
        vBtn[1] = new Point(0, 3);
        vBtn[2] = new Point(6, 3);
        vBtn[3] = new Point(6, 0);
        
        


        // ================== BOTONES INFERIORES ==================
        btnAgregarAmigo = new UIButton(
                870, 540,
                160, 36,
                9,
                null,
                vBtn,
                new Point(87, 54),
                "Agregar amigo"
        );

        btnBuscarAmigo = new UIButton(
                870, 586,
                160, 36,
                9,
                null,
                vBtn,
                new Point(87, 58),
                "Buscar amigo"
        );

        btnCerrarAmigos = new UIButton(
                1150, 130,
                24, 24,
                9,
                null,
                vBtn,
                new Point(115, 13),
                "X"
        );

        // ================== HEADERS CLICKEABLES ==================
        // IMPORTANTE: les damos tamaño (hitbox) y un dir inicial
        btnHeaderConectados = new UIButton(
                PX + 15, PY + 35,
                390, 30,      // alto 30 para buen margen de click
                7,
                null,
                vBtn,
                new Point((PX + 15) / 10, (PY + 25) / 10),
                ""
        );

        btnHeaderDesconectados = new UIButton(
                PX + 15, PY + 255,
                390, 30,
                7,
                null,
                vBtn,
                new Point((PX + 30) / 10, (PY+290) / 10), //255
                ""
        );

        // ==== HANDLERS ====

        // Toggle de conectados
        btnHeaderConectados.setOnClickAction(() -> {
            System.out.println("HEADERCONECTADO");
            showConectados = !showConectados;
            redraw();
        });

        // Toggle de desconectados
        btnHeaderDesconectados.setOnClickAction(() -> {
            System.out.println("HEADER DESCONECTADO");
            showDesconectados = !showDesconectados;
            redraw();
        });

        btnAgregarAmigo.setOnClickAction(() -> {
            if (!addExpanded) {
                expandAddFriend();
            } else {
                submitAddFriend();
            }
            System.out.println("AGREGAR AMIGO (TODO abrir popup)");
        });

        btnBuscarAmigo.setOnClickAction(() -> {
            System.out.println("BUSCAR AMIGO (TODO abrir buscador)");
        });

        btnCerrarAmigos.setOnClickAction(this::close);
        
        int inputW = 240;
        int inputH = 26;
        int inputX = 870;     // alineado con el botón
        int inputY = 540 + 42; // debajo del botón base (cuando está cerrado)

        Point[] vInput = new Point[4];
        vInput[0] = new Point(0, 0);
        vInput[1] = new Point(24, 0);
        vInput[2] = new Point(24, 3);
        vInput[3] = new Point(0, 3);

        Imagen inputImg = new Imagen(inputX, inputY, inputW, inputH, 9, null, null, 255);
        addFriendInput = new InputText(vInput, new Point(inputX / 10, inputY / 10), inputImg);
        addFriendInput.setAllowEnter(false);
        addFriendInput.setEnabled(false);
        addFriendInput.render();
        
    }

    // ================== PÚBLICOS ==================

    /** Abre el panel de amigos */
    public void open() {
        if (!subscribed) {
            subscribed = true;
            ClientMessageBus.getInstance().subscribe(
                line -> line.startsWith("FRIEND_REQUEST_SENT") || line.startsWith("FRIEND_FAIL"),
                line -> {
                    if (line.startsWith("FRIEND_FAIL")) {
                        showToast(line.replace("FRIEND_FAIL", "").trim());
                        return;
                    }
                    if (line.startsWith("FRIEND_REQUEST_SENT")) {
                        showToast("Solicitud enviada");
                        addFriendInput.clear();
                        addFriendInput.render();
                        collapseAddFriend(); // solo en éxito ✅
                    }
                }
            );
        }

        GameState.getInstance().registerUpdater(this); 
        InterpreterEvent.getInstance().setMinActiveLayer(7);
        semiFondoAmigos.setImage(buildFriendsImage());

        RenderProcessor.getInstance().setElement(semiFondoAmigos);
        RenderProcessor.getInstance().setElement(btnAgregarAmigo);
        RenderProcessor.getInstance().setElement(btnBuscarAmigo);
        RenderProcessor.getInstance().setElement(btnCerrarAmigos);
        RenderProcessor.getInstance().setElement(btnHeaderConectados);
        RenderProcessor.getInstance().setElement(btnHeaderDesconectados);
        RenderProcessor.getInstance().setElement(addFriendInput);
        addFriendInput.setEnabled(false);

        //InterpreterEvent.getInstance().setCapa(9);
        redraw();
    }

    /** Cierra el panel de amigos y limpia nodos */
    public void close() {
        GameState.getInstance().unregisterUpdater(this);
        InterpreterEvent.getInstance().setMinActiveLayer(1);
        

        clearFriendButtons();

        DriverRender.getInstance().eliminarNodo(semiFondoAmigos);
        DriverRender.getInstance().eliminarNodo(btnAgregarAmigo);
        btnAgregarAmigo.setEnabled(false);
        DriverRender.getInstance().eliminarNodo(btnBuscarAmigo);
        btnBuscarAmigo.setEnabled(false);
        DriverRender.getInstance().eliminarNodo(btnCerrarAmigos);
        DriverRender.getInstance().eliminarNodo(btnHeaderConectados);
        DriverRender.getInstance().eliminarNodo(btnHeaderDesconectados);
        DriverRender.getInstance().eliminarNodo(addFriendInput);
        addFriendInput.setEnabled(false);
        addFriendInput.offFocus();

        SpatialGrid.getInstance().limpiar(btnAgregarAmigo);
        SpatialGrid.getInstance().limpiar(btnBuscarAmigo);
        SpatialGrid.getInstance().limpiar(btnCerrarAmigos);
        SpatialGrid.getInstance().limpiar(btnHeaderConectados);
        SpatialGrid.getInstance().limpiar(btnHeaderDesconectados);
        SpatialGrid.getInstance().limpiar(addFriendInput);

        DriverRender.getInstance().string();
    }

    /** Redibuja sólo la imagen del overlay según el estado actual */
    private void redraw() {
        semiFondoAmigos.setImage(buildFriendsImage());

        //btnHeaderConectados.moveTo( PX + 15, lastYHeaderConectados - 18 );

        //btnHeaderDesconectados.moveTo( PX + 15, lastYHeaderDesconectados - 18 );

        // 👇 ACTUALIZAR SpatialGrid DESPUÉS DE MOVER


        DriverRender.getInstance().string();
    }

    // ================== LÓGICA DE LAYOUT ==================



    /** Dibuja el panel con los estados de Conectados/Desconectados */
    private BufferedImage buildFriendsImage() {
        clearFriendButtons();
        Point[] vBtn = new Point[4];
        vBtn[0] = new Point(0, 0);
        vBtn[1] = new Point(0, 3);
        vBtn[2] = new Point(6, 3);
        vBtn[3] = new Point(6, 0);

        int width = 1280, height = 720;
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();

        // Fondo oscuro (bien sólido para que se lea bien)
        g2d.setColor(Color.BLACK);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.90f));
        g2d.fillRect(0, 0, width, height);
        g2d.setComposite(AlphaComposite.SrcOver);

        // Panel lateral
        g2d.setColor(new Color(40, 40, 70, 255));
        g2d.fillRoundRect(PX, PY, PW, PH, 30, 30);
        g2d.setColor(new Color(20, 20, 45, 255));
        g2d.fillRoundRect(PX + 5, PY + 5, PW - 10, PH - 10, 30, 30);

        // Título
        g2d.setColor(Color.WHITE);
        g2d.drawString("AMIGOS", PX + 20, PY + 25);
        
        if (toastText != null) {
            g2d.setColor(new Color(0, 0, 0, 160));
            g2d.fillRoundRect(PX + 20, PY + 30, 360, 24, 12, 12);
            g2d.setColor(Color.WHITE);
            g2d.drawString(toastText, PX + 30, PY + 47);
        }

        // ===== LAYOUT DINÁMICO =====
        int y = PY + 55;

        // ---------- CONECTADOS ----------
        int yHeaderConectados = y;
        g2d.setColor(new Color(180, 255, 180));
        g2d.drawString("Conectados", PX + 20, yHeaderConectados);

        // Guardamos para la hitbox
        lastYHeaderConectados = yHeaderConectados;

        // Altura reservada para el bloque de conectados
        int conectadosAreaHeight = showConectados
                ? CONECTADOS_AREA_EXPANDED
                : CONECTADOS_AREA_COLLAPSED;

        int yContenidoConectados = yHeaderConectados + 20;
        int yLimiteConectados = yHeaderConectados + conectadosAreaHeight;
        
        friendButtonsConectados.clear();

        if (showConectados) {
            g2d.setColor(Color.WHITE);
            int lineHeight = 20;

            for (FriendInfo c : conectados) {
                FriendContextMenu friendMenu = new FriendContextMenu();
                if (yContenidoConectados + lineHeight > yLimiteConectados) break;

                // Pintas el texto como antes
                g2d.drawString("• " + c.getUsername(), PX + 30, yContenidoConectados);

                // Y creas un botón invisible (o con imagen muy simple)
                FriendButton btnFriend = new FriendButton(
                        PX + 20,                   // X del botón del amigo
                        yContenidoConectados - 16, // Y un poco por encima de la línea base
                        260,                       // ancho clickeable
                        lineHeight,                // alto
                        7,                         // capa del amigo (por encima de lobby base, por debajo del menú=8)
                        vBtn,
                        new Point((PX + 20) / 10, (yContenidoConectados - 16) / 10),
                        c.getUsername()  // texto interno no te importa si sigues dibujando con g2d
                );
                

                // Registrar el botón (para clicks)
                RenderProcessor.getInstance().setElement(btnFriend);
                friendButtonsConectados.add(btnFriend);

                yContenidoConectados += lineHeight;
            }
        }

        // Separador fijado al final del área reservada
        int ySeparador = yHeaderConectados + conectadosAreaHeight;
        g2d.setColor(new Color(180, 180, 220));
        g2d.drawLine(PX + 15, ySeparador, PX + PW - 30, ySeparador);

        // ---------- DESCONECTADOS ----------
        int yHeaderDesconectados = ySeparador + 22;
        g2d.setColor(new Color(220, 200, 200));
        g2d.drawString("Desconectados", PX + 20, yHeaderDesconectados);

        // Guardamos para la hitbox
        lastYHeaderDesconectados = yHeaderDesconectados;

        int yContenidoDesconect = yHeaderDesconectados + 20;

        if (showDesconectados) {
            g2d.setColor(new Color(200, 200, 200));
            int lineHeight = 20;
            for (FriendInfo d : desconectados) {
                g2d.drawString("• " + d.getUsername(), PX + 30, yContenidoDesconect);
                yContenidoDesconect += lineHeight;
            }
        }

        // ===== BOTONES INFERIORES (no dependen del layout dinámico) =====
        
        int addX = btnAgregarAmigo.getRenderX();
        int addY = btnAgregarAmigo.getRenderY();
    
        g2d.setColor(new Color(230, 210, 90));
        g2d.fillRoundRect(addX, addY, 160, 36, 12, 12);
        g2d.setColor(Color.DARK_GRAY);
        g2d.drawRoundRect(addX, addY, 160, 36, 12, 12);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Agregar amigo", addX + 15, addY + 22);
        g2d.drawOval(1010, 546, 10, 10);
        g2d.drawLine(1018, 554, 1024, 560);

        g2d.setColor(new Color(200, 160, 240));
        g2d.fillRoundRect(870, 586, 160, 36, 12, 12);
        g2d.setColor(Color.DARK_GRAY);
        g2d.drawRoundRect(870, 586, 160, 36, 12, 12);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Buscar amigo", 890, 608);
        g2d.drawOval(1010, 592, 10, 10);
        g2d.drawLine(1018, 600, 1024, 606);

        // Botón X
        g2d.setColor(new Color(220, 90, 90));
        g2d.fillOval(1150, 130, 24, 24);
        g2d.setColor(Color.BLACK);
        g2d.drawString("X", 1158, 146);
        if (addExpanded) {
            int ix = addFriendInput.getRenderX();
            int iy = addFriendInput.getRenderY();
            int iw = addFriendInput.getWidth();
            int ih = addFriendInput.getHeight();

            g2d.setColor(new Color(25, 25, 50, 255));
            g2d.fillRoundRect(ix, iy, iw, ih, 12, 12);
            g2d.setColor(new Color(90, 90, 140));
            g2d.drawRoundRect(ix, iy, iw, ih, 12, 12);

            if (addFriendInput.getText().isEmpty()) {
                g2d.setColor(new Color(180, 180, 220));
                g2d.drawString("username...", ix + 8, iy + 18);
            }
        }


        g2d.dispose();

        return img;
    }
    private void clearFriendButtons() {
        RenderProcessor rp = RenderProcessor.getInstance();

        for (FriendButton b : friendButtonsConectados) {
            b.dispose();             // cierra su menú si estaba abierto
            b.setSizeNull();
            rp.eliminarElemento(b);
        }
        friendButtonsConectados.clear();

        for (FriendButton b : friendButtonsDesconectados) {
            b.dispose();
            b.setSizeNull();
            rp.eliminarElemento(b);
        }
        friendButtonsDesconectados.clear();
    }
    private void expandAddFriend() {
        if (addExpanded) return;
        addExpanded = true;

        int btnEndY = 580; // donde quieres que baje
        int inputEndY = btnEndY + 42;

        addFriendInput.setEnabled(true);
        addFriendInput.render();

        addAnimBtn = new SlideVertical(btnAgregarAmigo, btnEndY, 8);
        addAnimBtn.setListener(() -> addAnimBtn = null);

        addAnimInput = new SlideVertical(addFriendInput, inputEndY, 8);
        addAnimInput.setListener(() -> addAnimInput = null);

        GameState.getInstance().registerUpdater(addAnimBtn);
        GameState.getInstance().registerUpdater(addAnimInput);
    }
    private void collapseAddFriend() {
        if (!addExpanded) return;
        addExpanded = false;

        addFriendInput.offFocus();
        addFriendInput.setEnabled(false);

        int btnStartY = 540;
        int inputStartY = btnStartY + 42;
        addAnimBtn = new SlideVertical(btnAgregarAmigo, btnStartY, 8);
        addAnimBtn.setListener(() -> addAnimBtn = null);

        addAnimInput = new SlideVertical(addFriendInput, inputStartY, 8);
        addAnimInput.setListener(() -> addAnimInput = null);


        GameState.getInstance().registerUpdater(addAnimBtn);
        GameState.getInstance().registerUpdater(addAnimInput);
    }
    private void submitAddFriend() {
        String target = addFriendInput.getText().trim();
        if (target.isEmpty()) {
            showToast("Escribe un username primero");

            changed = true;
            return;
        }

        if (!GameState.getInstance().hasTransport()) {
            showToast("Sin conexión");
     
            changed = true;
            return;
        }

        GameState.getInstance().getTransport().sendCommand("FRIEND_REQUEST " + target);
        showToast("Enviando solicitud a " + target);
        changed = true;
    }
    @Override
    public void update() {
        // Para cursor blink
        if (addFriendInput != null && addFriendInput.isFocused()) {
            addFriendInput.update();
            changed = true;
        }
        
        boolean animating = (addAnimBtn != null) || (addAnimInput != null);
        
        if (addExpanded || animating ) {
            refreshGridFor(btnAgregarAmigo);
            refreshGridFor(addFriendInput);
            changed = true;
        } else {
            // opcional: también si está colapsado para asegurar que vuelva bien
            refreshGridFor(btnAgregarAmigo);
            refreshGridFor(addFriendInput);
        }
        long now = System.currentTimeMillis();
        if (toastText != null && now > toastUntilMs) {
            toastText = null;
            changed =true;
        }

        if (changed) {
            changed = false;
            redraw();
        }
    }
    private void showToast(String text) {
        toastText = text;
        toastUntilMs = System.currentTimeMillis() + 2000;
        changed = true;
    }
    public void onFriendRequestSent() {
        addFriendInput.clear();
        addFriendInput.render();
        collapseAddFriend();      
        changed = true;
    }
    private void refreshGridFor(VisualRender vr) {
        SpatialGrid grid = SpatialGrid.getInstance();
        grid.limpiar(vr);
        grid.setElement(vr);
    }
    public void setFriends(List<FriendInfo> list) {
        for(FriendInfo f: list){
            if(f.online){
                conectados.add(f);
            }
            else{
                desconectados.add(f);
            }
        }
        
        changed = true; // para que redraw ocurra en update()
    }




}
