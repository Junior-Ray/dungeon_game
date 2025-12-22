/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.dungeon_game.core.components;

import com.dungeon_game.core.api.DriverRender;
import com.dungeon_game.core.api.RenderProcessor;
import com.dungeon_game.core.data.SpatialGrid;
import com.dungeon_game.core.logic.InterpreterEvent;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import com.dungeon_game.core.model.Imagen;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



public class LobbyFriendsOverlay {
        
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
    private final List<String> conectados = Arrays.asList(
            "Gabriel (En línea)",
            "Richard (En línea)"
    );

    private final List<String> desconectados = Arrays.asList(
            "Jugador_003",
            "Ninja_404"
    );

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

    public LobbyFriendsOverlay() {
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
            System.out.println("AGREGAR AMIGO (TODO abrir popup)");
        });

        btnBuscarAmigo.setOnClickAction(() -> {
            System.out.println("BUSCAR AMIGO (TODO abrir buscador)");
        });

        btnCerrarAmigos.setOnClickAction(this::close);
    }

    // ================== PÚBLICOS ==================

    /** Abre el panel de amigos */
    public void open() {
        InterpreterEvent.getInstance().setMinActiveLayer(7);
        semiFondoAmigos.setImage(buildFriendsImage());

        RenderProcessor.getInstance().setElement(semiFondoAmigos);
        RenderProcessor.getInstance().setElement(btnAgregarAmigo);
        RenderProcessor.getInstance().setElement(btnBuscarAmigo);
        RenderProcessor.getInstance().setElement(btnCerrarAmigos);
        RenderProcessor.getInstance().setElement(btnHeaderConectados);
        RenderProcessor.getInstance().setElement(btnHeaderDesconectados);

        //InterpreterEvent.getInstance().setCapa(9);
        redraw();
    }

    /** Cierra el panel de amigos y limpia nodos */
    public void close() {
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
        

        SpatialGrid.getInstance().limpiar(btnAgregarAmigo);
        SpatialGrid.getInstance().limpiar(btnBuscarAmigo);
        SpatialGrid.getInstance().limpiar(btnCerrarAmigos);
        SpatialGrid.getInstance().limpiar(btnHeaderConectados);
        SpatialGrid.getInstance().limpiar(btnHeaderDesconectados);

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

            for (String c : conectados) {
                FriendContextMenu friendMenu = new FriendContextMenu();
                if (yContenidoConectados + lineHeight > yLimiteConectados) break;

                // Pintas el texto como antes
                g2d.drawString("• " + c, PX + 30, yContenidoConectados);

                // Y creas un botón invisible (o con imagen muy simple)
                FriendButton btnFriend = new FriendButton(
                        PX + 20,                   // X del botón del amigo
                        yContenidoConectados - 16, // Y un poco por encima de la línea base
                        260,                       // ancho clickeable
                        lineHeight,                // alto
                        7,                         // capa del amigo (por encima de lobby base, por debajo del menú=8)
                        vBtn,
                        new Point((PX + 20) / 10, (yContenidoConectados - 16) / 10),
                        c  // texto interno no te importa si sigues dibujando con g2d
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
            for (String d : desconectados) {
                g2d.drawString("• " + d, PX + 30, yContenidoDesconect);
                yContenidoDesconect += lineHeight;
            }
        }

        // ===== BOTONES INFERIORES (no dependen del layout dinámico) =====
        g2d.setColor(new Color(230, 210, 90));
        g2d.fillRoundRect(870, 540, 160, 36, 12, 12);
        g2d.setColor(Color.DARK_GRAY);
        g2d.drawRoundRect(870, 540, 160, 36, 12, 12);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Agregar amigo", 885, 562);
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

}
