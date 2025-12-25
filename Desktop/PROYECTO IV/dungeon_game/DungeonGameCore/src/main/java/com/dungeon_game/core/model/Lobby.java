/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.dungeon_game.core.model;


import com.dungeon_game.core.api.DriverRender;
import com.dungeon_game.core.api.RenderProcessor;
import com.dungeon_game.core.api.Updater;
import com.dungeon_game.core.audio.AudioManager;
import com.dungeon_game.core.components.ChatOverlay;
import com.dungeon_game.core.components.InviteOverlay;
import com.dungeon_game.core.components.InviteOverlay2;
import com.dungeon_game.core.components.LobbyFriendsOverlay;
import com.dungeon_game.core.components.LobbyPartyControls;
import com.dungeon_game.core.components.LobbyPartyView;
import com.dungeon_game.core.components.SpriteImageAnimator;
import com.dungeon_game.core.components.UIButton;
import com.dungeon_game.core.components.UISlider;
import com.dungeon_game.core.data.AntFromLeft;
import com.dungeon_game.core.data.SpatialGrid;
import com.dungeon_game.core.logic.ClientMessageBus;
import com.dungeon_game.core.logic.GameState;
import com.dungeon_game.core.logic.InterpreterEvent;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author GABRIEL SALGADO
 */
public class Lobby extends Sala {
    
    private final InviteOverlay inviteOverlay = new InviteOverlay();
    private final InviteOverlay2 inv = new InviteOverlay2();
    
    private LobbyFriendsOverlay friendsOverlay;
    private static Lobby instance;
    private ChatOverlay chatOverlay;
    private boolean chatControlsShown = false;
    private AntFromLeft ant_chat = null;
    private static List<Updater> list;

    private LobbyPartyView partyView;
    private LobbyPartyControls partyControls;

    private Lobby() {
    }

    public static Lobby getInstance() {
        if (instance == null) {
            instance = new Lobby();
        }
        return instance;
    }

    @Override
    public void cargarIniciales() {
        ClientMessageBus.getInstance().start();
        list = new ArrayList<>(); //No se si realmente deba ir aca :)
        
        //ELEMENTOS ESTATICOS 
        Point[] vertices = new Point[4];
        vertices[0] = new Point(0, 0);
        vertices[1] = new Point(7, 0);
        vertices[3] = new Point(0, 7);
        vertices[2] = new Point(7, 7);
        UIButton conf = new UIButton(1190, 30, 64, 64, 1, "config", vertices, new Point(119, 3), null);

        vertices = new Point[4];
        vertices[0] = new Point(0, 0);
        vertices[1] = new Point(7, 0);
        vertices[3] = new Point(0, 7);
        vertices[2] = new Point(7, 7);
        UIButton aband = new UIButton(1190, 130, 64, 64, 1, "abandonar", vertices, new Point(119, 13), null);

        vertices = new Point[4];
        vertices[0] = new Point(0, 0);
        vertices[1] = new Point(7, 0);
        vertices[3] = new Point(0, 7);
        vertices[2] = new Point(7, 7);
        UIButton stat = new UIButton(1190, 230, 64, 64, 1, "statsi", vertices, new Point(119, 23), null);

        vertices = new Point[4];
        vertices[0] = new Point(0, 0);
        vertices[1] = new Point(7, 0);
        vertices[3] = new Point(0, 7);
        vertices[2] = new Point(7, 7);
        UIButton friends = new UIButton(1190, 330, 64, 64, 1, "friends", vertices, new Point(119, 33), null);

        vertices = new Point[4];
        vertices[0] = new Point(0, 0);
        vertices[1] = new Point(7, 0);
        vertices[3] = new Point(0, 7);
        vertices[2] = new Point(7, 7);
        UIButton chat = new UIButton(1190, 430, 64, 64, 1, "chat", vertices, new Point(119, 43), null);
        
        vertices = new Point[4];
        vertices[0] = new Point(0, 0);
        vertices[1] = new Point(7, 0);
        vertices[3] = new Point(0, 7);
        vertices[2] = new Point(7, 7);
        UIButton inv1 = new UIButton(427, 288, 64, 64, 1, "invite", vertices, new Point(43, 29), null);
        vertices = new Point[4];
        vertices[0] = new Point(0, 0);
        vertices[1] = new Point(7, 0);
        vertices[3] = new Point(0, 7);
        vertices[2] = new Point(7, 7);
        UIButton inv2 = new UIButton(853, 288, 64, 64, 1, "invite", vertices, new Point(85, 29), null);

        //logo
        vertices = new Point[4];
        vertices[0] = new Point(0, 0);
        vertices[1] = new Point(32, 0);
        vertices[3] = new Point(0, 13);
        vertices[2] = new Point(32, 13);
        UIButton logo = new UIButton(60, 30, 320, 128, 1, "logo", vertices, new Point(6, 3), null);

        vertices = new Point[4];
        vertices[0] = new Point(0, 0);
        vertices[1] = new Point(32, 0);
        vertices[3] = new Point(0, 13);
        vertices[2] = new Point(32, 13);
        UIButton btn_listo = new UIButton(940, 572, 320, 128, 1, "btn_listo", vertices, new Point(94, 57), null);
        
        /**/
        vertices = new Point[4];
        vertices[0] = new Point(0, 0);
        vertices[1] = new Point(32, 0);
        vertices[3] = new Point(0, 30);
        vertices[2] = new Point(32, 30);
        //UIButton kenji = new UIButton(480, 210, 320, 300, 2, "kenji_idle-01", vertices, new Point(48, 21), null);
        Imagen kenji = new Imagen(480, 210, 320, 300, 2, "kenji_idle-01", null, 255);
     

        // animación idle
        SpriteImageAnimator kenjiIdle = new SpriteImageAnimator(
            kenji,
            java.util.List.of("kenji_idle-01", "kenji_idle-02"),
            250,   // ms por frame
            true
        );

        GameState.getInstance().registerUpdater(kenjiIdle);

        Imagen listo = new Imagen(972, 588, 192, 64, 2, "listo",null, 1);
        Imagen sushi = new Imagen(940, 30, 64, 64, 1, "sushi",null, 1);

        RenderableBackground fondo = new RenderableBackground("bg_lobby");
        RenderProcessor.getInstance().setElement(kenji);

        RenderProcessor.getInstance().setElement(logo);
        RenderProcessor.getInstance().setElement(sushi);
        RenderProcessor.getInstance().setElement(btn_listo);
        RenderProcessor.getInstance().setElement(listo);
        RenderProcessor.getInstance().setElement(inv1);
        RenderProcessor.getInstance().setElement(inv2);
        RenderProcessor.getInstance().setElement(conf);
        RenderProcessor.getInstance().setElement(aband);
        RenderProcessor.getInstance().setElement(stat);
        RenderProcessor.getInstance().setElement(friends);
        RenderProcessor.getInstance().setElement(chat);
        RenderProcessor.getInstance().setElement(fondo);

        //ELEMENTOS DINAMICOS
        Point[] ver = new Point[4];
        ver[0] = new Point(0, 0);
        ver[1] = new Point(0, 3);
        ver[2] = new Point(6, 3);
        ver[3] = new Point(6, 0);
        UIButton salir = new UIButton(550, 345, 60, 30, 9, null, ver, new Point(55, 35), "Salir");

        ver = new Point[4];
        ver[0] = new Point(0, 0);
        ver[1] = new Point(0, 3);
        ver[2] = new Point(6, 3);
        ver[3] = new Point(6, 0);
        UIButton noSalir = new UIButton(670, 345, 60, 30, 9, null, ver, new Point(67, 35), "Volver :D");

        Imagen semiFondo = new Imagen(0, 0, 1280, 720, 8, null, null, 130);
        //EVENTOS
        aband.setOnClickAction(() -> {
            BufferedImage img = new BufferedImage(1280, 720, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = img.createGraphics();
            g2d.setColor(Color.BLACK);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.95f));
            g2d.fillRect(0, 0, 1280, 720);

            g2d.setComposite(AlphaComposite.SrcOver);

            g2d.setColor(Color.yellow);
            g2d.fillRect(440, 260, 400, 200);
            g2d.setColor(Color.blue);
            g2d.fillRect(450, 270, 380, 180);

            semiFondo.setImage(img);
            salir.render();
            noSalir.render();

            RenderProcessor.getInstance().setElement(semiFondo);
            RenderProcessor.getInstance().setElement(salir);
            RenderProcessor.getInstance().setElement(noSalir);
            InterpreterEvent.getInstance().setMinActiveLayer(9);
            DriverRender.getInstance().string();
        });
        salir.setOnClickAction(() -> {
            //GameState.getInstance().iniciarGraficos();
            //
            
            GameState.getInstance().requestLogout();
        });

        noSalir.setOnClickAction(() -> {
            InterpreterEvent.getInstance().setMinActiveLayer(1);
            DriverRender.getInstance().eliminarNodo(semiFondo);
            DriverRender.getInstance().eliminarNodo(noSalir);
            DriverRender.getInstance().eliminarNodo(salir);
            SpatialGrid.getInstance().limpiar(noSalir);
            SpatialGrid.getInstance().limpiar(salir);
            DriverRender.getInstance().string();
        });
        
        
        Point[] vFriends = new Point[4];
        vFriends[0] = new Point(0, 0);
        vFriends[1] = new Point(0, 3);
        vFriends[2] = new Point(10, 3);
        vFriends[3] = new Point(10, 0);

        // ==== Overlay de amigos ====
        friendsOverlay = new LobbyFriendsOverlay();



        friends.setOnClickAction(() -> {
            System.out.println("CLICK FRIENDS");
            friendsOverlay.open();
        });
        
        
        final int[] configSection = {0};

        // Fondo semitransparente para el menú de configuración
        Imagen semiFondoConfig = new Imagen(0, 0, 1280, 720, 8, null, null, 210);

        // Panel central (usaremos estos valores también en el dibujo)
        final int cfgPanelX = 360;
        final int cfgPanelY = 180;
        final int cfgPanelW = 560;
        final int cfgPanelH = 360;

        // Forma base de botones
        Point[] vCfgBtn = new Point[4];
        vCfgBtn[0] = new Point(0, 0);
        vCfgBtn[1] = new Point(0, 3);
        vCfgBtn[2] = new Point(6, 3);
        vCfgBtn[3] = new Point(6, 0);

        // Pestañas superiores (General, Audio, Video, Controles)
        UIButton btnCfgGeneral = new UIButton(
                cfgPanelX + 20, cfgPanelY + 20,
                110, 30,
                9,
                null,
                vCfgBtn,
                new Point( (cfgPanelX + 20) / 10, (cfgPanelY + 20) / 10),
                "General"
        );

        UIButton btnCfgAudio = new UIButton(
                cfgPanelX + 140, cfgPanelY + 20,
                110, 30,
                9,
                null,
                vCfgBtn,
                new Point( (cfgPanelX + 140) / 10, (cfgPanelY + 20) / 10),
                "Audio"
        );

        UIButton btnCfgVideo = new UIButton(
                cfgPanelX + 260, cfgPanelY + 20,
                110, 30,
                9,
                null,
                vCfgBtn,
                new Point( (cfgPanelX + 260) / 10, (cfgPanelY + 20) / 10),
                "Video"
        );

        UIButton btnCfgControles = new UIButton(
                cfgPanelX + 380, cfgPanelY + 20,
                120, 30,
                9,
                null,
                vCfgBtn,
                new Point( (cfgPanelX + 380) / 10, (cfgPanelY + 20) / 10),
                "Controles"
        );

        // Botón X roja en la parte inferior derecha del panel
        UIButton btnCerrarConfig = new UIButton(
                cfgPanelX + cfgPanelW - 50,  // x
                cfgPanelY + cfgPanelH - 50,  // y
                40, 40,
                9,
                null,
                vCfgBtn,
                new Point( (cfgPanelX + cfgPanelW - 50) / 10, (cfgPanelY + cfgPanelH - 50) / 10),
                "X"
        );
        
        // ============ SLIDERS DE AUDIO ============
        int nivelG = (int)(AudioManager.getInstance().getMasterVolume() * 10);
        int nivelM = (int)(AudioManager.getInstance().getMusicVolume() * 10);
        int nivelFX = (int)(AudioManager.getInstance().getSoundVolume() * 10);

        Point[] verSlider = new Point[4];
        verSlider[0] = new Point(0, 0);
        verSlider[1] = new Point(40, 0);
        verSlider[2] = new Point(40, 3);
        verSlider[3] = new Point(0, 3);

        UISlider sliderGeneral = new UISlider(400, 300, 420, 30, 9, null, verSlider, new Point(40, 30), nivelG);
        UISlider sliderMusic = new UISlider(400, 360, 420, 30, 9, null, verSlider, new Point(40, 36), nivelM);
        UISlider sliderFX = new UISlider(400, 420, 420, 30, 9, null, verSlider, new Point(40, 42), nivelFX);
        
        conf.setOnClickAction(() -> {
            configSection[0] = 0; // empezar en General

            BufferedImage img = buildConfigImage(
                    configSection[0],
                    cfgPanelX, cfgPanelY, cfgPanelW, cfgPanelH
            );
            semiFondoConfig.setImage(img);

            // Registramos fondo y botones
            RenderProcessor.getInstance().setElement(semiFondoConfig);
            RenderProcessor.getInstance().setElement(btnCfgGeneral);
            RenderProcessor.getInstance().setElement(btnCfgAudio);
            RenderProcessor.getInstance().setElement(btnCfgVideo);
            RenderProcessor.getInstance().setElement(btnCfgControles);
            RenderProcessor.getInstance().setElement(btnCerrarConfig);

            //InterpreterEvent.getInstance().setCapa(9);
            DriverRender.getInstance().string();
        });

        // Cambio de pestañas
        btnCfgGeneral.setOnClickAction(() -> {
            configSection[0] = 0;
            semiFondoConfig.setImage(buildConfigImage(
                    configSection[0],
                    cfgPanelX, cfgPanelY, cfgPanelW, cfgPanelH
            ));
            // Eliminar sliders si estaban visibles
            DriverRender.getInstance().eliminarNodo(sliderGeneral);
            DriverRender.getInstance().eliminarNodo(sliderMusic);
            DriverRender.getInstance().eliminarNodo(sliderFX);
            SpatialGrid.getInstance().limpiar(sliderGeneral);
            SpatialGrid.getInstance().limpiar(sliderMusic);
            SpatialGrid.getInstance().limpiar(sliderFX);
            
            DriverRender.getInstance().string();
        });

        btnCfgAudio.setOnClickAction(() -> {
            configSection[0] = 1;
            semiFondoConfig.setImage(buildConfigImage(
                    configSection[0],
                    cfgPanelX, cfgPanelY, cfgPanelW, cfgPanelH
            ));
            // Mostrar sliders en la sección de Audio
            sliderGeneral.render();
            sliderMusic.render();
            sliderFX.render();
            RenderProcessor.getInstance().setElement(sliderGeneral);
            RenderProcessor.getInstance().setElement(sliderMusic);
            RenderProcessor.getInstance().setElement(sliderFX);
            
            DriverRender.getInstance().string();
        });

        btnCfgVideo.setOnClickAction(() -> {
            configSection[0] = 2;
            semiFondoConfig.setImage(buildConfigImage(
                    configSection[0],
                    cfgPanelX, cfgPanelY, cfgPanelW, cfgPanelH
            ));
            // Eliminar sliders si estaban visibles
            DriverRender.getInstance().eliminarNodo(sliderGeneral);
            DriverRender.getInstance().eliminarNodo(sliderMusic);
            DriverRender.getInstance().eliminarNodo(sliderFX);
            SpatialGrid.getInstance().limpiar(sliderGeneral);
            SpatialGrid.getInstance().limpiar(sliderMusic);
            SpatialGrid.getInstance().limpiar(sliderFX);
            
            DriverRender.getInstance().string();
        });

        btnCfgControles.setOnClickAction(() -> {
            configSection[0] = 3;
            semiFondoConfig.setImage(buildConfigImage(
                    configSection[0],
                    cfgPanelX, cfgPanelY, cfgPanelW, cfgPanelH
            ));
            // Eliminar sliders si estaban visibles
            DriverRender.getInstance().eliminarNodo(sliderGeneral);
            DriverRender.getInstance().eliminarNodo(sliderMusic);
            DriverRender.getInstance().eliminarNodo(sliderFX);
            SpatialGrid.getInstance().limpiar(sliderGeneral);
            SpatialGrid.getInstance().limpiar(sliderMusic);
            SpatialGrid.getInstance().limpiar(sliderFX);
            
            DriverRender.getInstance().string();
        });

        // Cerrar menú de configuración (X roja)
        btnCerrarConfig.setOnClickAction(() -> {
            InterpreterEvent.getInstance().setMinActiveLayer(1);

            DriverRender.getInstance().eliminarNodo(semiFondoConfig);
            DriverRender.getInstance().eliminarNodo(btnCfgGeneral);
            DriverRender.getInstance().eliminarNodo(btnCfgAudio);
            DriverRender.getInstance().eliminarNodo(btnCfgVideo);
            DriverRender.getInstance().eliminarNodo(btnCfgControles);
            DriverRender.getInstance().eliminarNodo(btnCerrarConfig);
            // Eliminar sliders también
            DriverRender.getInstance().eliminarNodo(sliderGeneral);
            DriverRender.getInstance().eliminarNodo(sliderMusic);
            DriverRender.getInstance().eliminarNodo(sliderFX);
            
            
            SpatialGrid.getInstance().limpiar(btnCfgGeneral);
            SpatialGrid.getInstance().limpiar(btnCfgAudio);
            SpatialGrid.getInstance().limpiar(btnCfgVideo);
            SpatialGrid.getInstance().limpiar(btnCfgControles);
            SpatialGrid.getInstance().limpiar(btnCerrarConfig);
            SpatialGrid.getInstance().limpiar(sliderGeneral);
            SpatialGrid.getInstance().limpiar(sliderMusic);
            SpatialGrid.getInstance().limpiar(sliderFX);

            DriverRender.getInstance().string();
        });
        
        // Eventos de los sliders
        sliderGeneral.setOnClickAction(() -> {
            float vol = sliderGeneral.getLevel() / 10f;
            AudioManager.getInstance().setMasterVolume(vol);
        });

        sliderMusic.setOnClickAction(() -> {
            float vol = sliderMusic.getLevel() / 10f;
            AudioManager.getInstance().setMusicVolume(vol);
        });

        sliderFX.setOnClickAction(() -> {
            float vol = sliderFX.getLevel() / 10f;
            AudioManager.getInstance().setSoundVolume(vol);
        });
        
        // Hasta aca si guncionar:
        Imagen semiFondoInvitar = new Imagen(0, 0, 1280, 720, 8, null, null, 210);

        // Forma base de botones del popup de invitar
        Point[] vBtnInv = new Point[4];
        vBtnInv[0] = new Point(0, 0);
        vBtnInv[1] = new Point(0, 3);
        vBtnInv[2] = new Point(6, 3);
        vBtnInv[3] = new Point(6, 0);

        // Botón ENVIAR INVITACIÓN
        UIButton btnEnviarInvitacion = new UIButton(
                540, 380,     // x, y
                120, 40,      // width, height
                9,            // layer
                null,
                vBtnInv,
                new Point(54, 38),
                "Invitar"
        );

        // Botón CERRAR popup de invitar
        UIButton btnCerrarInvitar = new UIButton(
                680, 380,
                120, 40,
                9,
                null,
                vBtnInv,
                new Point(68, 38),
                "Cerrar"
        );
        inv1.setOnClickAction(inviteOverlay::open);

        inv2.setOnClickAction(() -> {
            inv.open();
        });

        btnCerrarInvitar.setOnClickAction(() -> {
            // Volvemos a la capa normal
            InterpreterEvent.getInstance().setMinActiveLayer(1);

            // Eliminamos del render
            DriverRender.getInstance().eliminarNodo(semiFondoInvitar);
            DriverRender.getInstance().eliminarNodo(btnEnviarInvitacion);
            DriverRender.getInstance().eliminarNodo(btnCerrarInvitar);

            // Limpiamos del SpatialGrid
            SpatialGrid.getInstance().limpiar(btnEnviarInvitacion);
            SpatialGrid.getInstance().limpiar(btnCerrarInvitar);

            DriverRender.getInstance().string();
        });
        

        btnEnviarInvitacion.setOnClickAction(() -> {
            System.out.println("Enviar invitación (TODO: lógica de red)");
            // Aquí luego llamarás a tu sistema de invitaciones
        });
        chatOverlay = new ChatOverlay();
        
        chat.setOnClickAction(() -> {
            System.out.println("HOLA A TODOS");
            chatOverlay.open();
        });
        
        if (partyView == null) {
            partyView = new LobbyPartyView(
                new Point(200, 210),
                new Point(820, 210),
                java.util.List.of("kenji_idle-01", "kenji_idle-02"),
                250
            );
        }
        partyView.enable();

        if (partyControls == null) {
            partyControls = new LobbyPartyControls(partyView);
        }
        partyControls.enable();

    }

        /**
     * Dibuja el panel de configuración según la sección.
     *
     * @param section 0 = General, 1 = Audio, 2 = Video, 3 = Controles
     */
    private BufferedImage buildConfigImage(int section,
                                       int panelX, int panelY,
                                       int panelW, int panelH) {

    BufferedImage img = new BufferedImage(1280, 720, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = img.createGraphics();

    // Fondo oscurecido
    g2d.setColor(Color.BLACK);
    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.99f));
    g2d.fillRect(0, 0, 1280, 720);

    g2d.setComposite(AlphaComposite.SrcOver);

    // Panel central
    g2d.setColor(new Color(120, 120, 200, 245)); // marco
    g2d.fillRoundRect(panelX, panelY, panelW, panelH, 30, 30);
    g2d.setColor(new Color(30, 30, 60, 255));     // interior
    g2d.fillRoundRect(panelX + 5, panelY + 5, panelW - 10, panelH - 10, 30, 30);

    // ===== PESTAÑAS SUPERIORES (General / Audio / Video / Controles) =====
    int tabY = panelY + 15;
    int tabH = 28;

    // Helper lambda para pintar una tab
    java.util.function.BiConsumer<Integer, String> drawTab = (idx, text) -> {
        int tabX;
        int tabW = (idx == 3) ? 120 : 110; // la de Controles es un poco más larga

        if (idx == 0) tabX = panelX + 20;
        else if (idx == 1) tabX = panelX + 140;
        else if (idx == 2) tabX = panelX + 260;
        else tabX = panelX + 380;

        boolean active = (section == idx);

        // fondo de la pestaña
        if (active) {
            g2d.setColor(new Color(230, 210, 90));   // activa (amarillita)
        } else {
            g2d.setColor(new Color(90, 90, 140));    // inactiva (oscura)
        }
        g2d.fillRoundRect(tabX, tabY, tabW, tabH, 10, 10);

        // borde
        g2d.setColor(Color.DARK_GRAY);
        g2d.drawRoundRect(tabX, tabY, tabW, tabH, 10, 10);

        // texto
        g2d.setColor(Color.BLACK);
        g2d.drawString(text, tabX + 10, tabY + 18);
    };

    drawTab.accept(0, "General");
    drawTab.accept(1, "Audio");
    drawTab.accept(2, "Video");
    drawTab.accept(3, "Controles");

    // Título general
    g2d.setColor(Color.WHITE);
    g2d.drawString("CONFIGURACIÓN", panelX + 20, panelY + 60);

    // Contenido según sección
    int baseY = panelY + 100;
    switch (section) {
        case 0 -> { // GENERAL
            g2d.drawString("General", panelX + 20, baseY);

            g2d.setColor(Color.LIGHT_GRAY);
            g2d.drawString("- Idioma: Español", panelX + 40, baseY + 30);
            g2d.drawString("- Mostrar FPS: [   ]", panelX + 40, baseY + 60);
        }
        case 1 -> { // AUDIO
            g2d.drawString("Audio", panelX + 20, baseY);

            g2d.setColor(Color.LIGHT_GRAY);
            g2d.drawString("- Volumen maestro:  80%", panelX + 40, baseY + 30);
            g2d.drawString("- Volumen música:   60%", panelX + 40, baseY + 60);
            g2d.drawString("- Silenciar todo:   [   ]", panelX + 40, baseY + 90);
        }
        case 2 -> { // VIDEO
            g2d.drawString("Video", panelX + 20, baseY);

            g2d.setColor(Color.LIGHT_GRAY);
            g2d.drawString("- Resolución: 1280 x 720 (fija)", panelX + 40, baseY + 30);
            g2d.drawString("- Brillo: [==========    ]", panelX + 40, baseY + 60);
        }
        case 3 -> { // CONTROLES
            g2d.drawString("Controles", panelX + 20, baseY);

            g2d.setColor(Color.LIGHT_GRAY);
            g2d.drawString("- Sensibilidad mouse:  50%", panelX + 40, baseY + 30);
            g2d.drawString("- Invertir eje Y:      [   ]", panelX + 40, baseY + 60);
            g2d.drawString("- Atajos:", panelX + 40, baseY + 100);
            g2d.drawString("    W A S D  - mover", panelX + 60, baseY + 125);
            g2d.drawString("    ESC      - cerrar menú", panelX + 60, baseY + 145);
        }
    }

    // X roja en la parte inferior derecha del panel
    int xBtn = panelX + panelW - 50;
    int yBtn = panelY + panelH - 50;
    g2d.setColor(new Color(220, 90, 90));
    g2d.fillOval(xBtn, yBtn, 32, 32);
    g2d.setColor(Color.BLACK);
    g2d.drawString("X", xBtn + 10, yBtn + 22);

    g2d.dispose();
    return img;
}
    

    
    
    @Override
    public void eliminarSala(){
        instance = null;
    }
    

    
    

}
