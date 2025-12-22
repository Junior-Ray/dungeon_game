/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.dungeon_game.core.components;


import com.dungeon_game.core.api.DriverRender;
import com.dungeon_game.core.api.RenderProcessor;
import com.dungeon_game.core.api.Updater;
import com.dungeon_game.core.data.SpatialGrid;
import com.dungeon_game.core.logic.ClientMessageBus;
import com.dungeon_game.core.logic.GameState;
import com.dungeon_game.core.logic.InterpreterEvent;
import com.dungeon_game.core.model.Imagen;
import com.dungeon_game.core.net.GameTransport;

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

public class ChatOverlay implements Updater{

    private InputText chatInput;
    private volatile boolean changed = false;
    // Panel de chat (lado derecho)
    private static final int CX = 780;
    private static final int CY = 110;
    private static final int CW = 420;
    private static final int CH = 480;

    // Canvas del chat
    private final Imagen chatCanvas;

    // Tabs / botones superiores
    private final UIButton tabGeneral;
    private final UIButton tabSala;
    private final UIButton tabAmigo;
    
    private final UIButton btnCerrarChat;
    private final UIButton btnSend;
    

    
    //Estado
    private boolean isOpen = false;

    
    
    private static final int INPUT_X = CX + 20;
    private static final int INPUT_Y = CY + CH - 60;
    private static final int INPUT_W = CW - 140;
    private static final int INPUT_H = 32;
    
    // Estado de pestaña activa: 0 = General, 1 = Sala, 2 = Amigo
    private int activeTab = 0;

    // Mensajes de prueba (luego los cambiarás por tu sistema real)
    private final List<String> generalMessages = new ArrayList<>(Arrays.asList(
        "[Sistema] Bienvenido al chat general.",
        "Gabriel: Hola bro 😎",
        "Richard: Probando el chat..."
    ));

    private final List<String> salaMessages = new ArrayList<>(Arrays.asList(
        "[Sistema] Esta es la sala de la partida.",
        "Player1: Listo para jugar.",
        "Player2: Un momento, configurando build."
    ));

    private final List<String> amigoMessages = new ArrayList<>(Arrays.asList(
        "[Privado] Gabriel: Oe, ¿jugamos ranked luego?",
        "Richard: Cuando quieras."
    ));

    public ChatOverlay() {
      
        // Lienzo del chat (no oscurezco toda la pantalla, solo panel)
        chatCanvas = new Imagen(0, 0, 1280, 720, 6, null, null, 220);

        // Forma base de botones
        Point[] vBtn = new Point[4];
        vBtn[0] = new Point(0, 0);
        vBtn[1] = new Point(0, 3);
        vBtn[2] = new Point(6, 3);
        vBtn[3] = new Point(6, 0);

        // ====== TABS SUPERIORES ======
        int tabY = CY + 15;
        tabGeneral = new UIButton(
                CX + 20, tabY,
                110, 28,
                7,
                null,
                vBtn,
                new Point((CX + 20) / 10, tabY / 10),
                "General"
        );

        tabSala = new UIButton(
                CX + 140, tabY,
                110, 28,
                7,
                null,
                vBtn,
                new Point((CX + 140) / 10, tabY / 10),
                "Sala"
        );

        tabAmigo = new UIButton(
                CX + 260, tabY,
                110, 28,
                7,
                null,
                vBtn,
                new Point((CX + 260) / 10, tabY / 10),
                "Amigo"
        );

        // Botón X para cerrar el panel de chat
        btnCerrarChat = new UIButton(
                CX + CW - 30, CY + 10,
                20, 20,
                7,
                null,
                vBtn,
                new Point((CX + CW - 30) / 10, (CY + 10) / 10),
                "X"
        );
        btnSend = new UIButton(
                INPUT_X + INPUT_W + 10, INPUT_Y,
                80, INPUT_H,
                7,
                null,
                vBtn,
                new Point((INPUT_X + INPUT_W + 10) / 10, INPUT_Y / 10),
                "Enviar"
        );
        Point[] ver = new Point[4];
        ver[0] = new Point(0, 0);
        ver[1] = new Point(24, 0);
        ver[2] = new Point(24, 3);
        ver[3] = new Point(0, 3);

        Imagen inputImg = new Imagen(
                INPUT_X,
                INPUT_Y+11,
                INPUT_W,
                INPUT_H,
                7,
                null,
                null,
                255
        );
        chatInput = new InputText(ver, new Point(INPUT_X / 10, INPUT_Y / 10), inputImg);
        chatInput.setAllowEnter(false);

        // ==== HANDLERS DE TABS ====
        tabGeneral.setOnClickAction(() -> {
            activeTab = 0;
            System.out.println("TAB CHAT: GENERAL");
            redraw();
        });

        tabSala.setOnClickAction(() -> {
            activeTab = 1;
            System.out.println("TAB CHAT: SALA");
            redraw();
        });

        tabAmigo.setOnClickAction(() -> {
            activeTab = 2;
            System.out.println("TAB CHAT: AMIGO");
            redraw();
        });

        // Cerrar chat
        btnCerrarChat.setOnClickAction(this::close);
        

        // Click en SEND
        btnSend.setOnClickAction(this::enviarMensaje);
        
    }

    // ================== PÚBLICO ==================

    public void open() {
        GameState.getInstance().registerUpdater(this);
        redraw();
       
        isOpen = true;
        InterpreterEvent.getInstance().setMinActiveLayer(6);
                
        chatCanvas.setImage(buildChatImage());

        RenderProcessor.getInstance().setElement(chatCanvas);
        RenderProcessor.getInstance().setElement(tabGeneral);
        RenderProcessor.getInstance().setElement(tabSala);
        RenderProcessor.getInstance().setElement(tabAmigo);
        RenderProcessor.getInstance().setElement(btnCerrarChat);
        RenderProcessor.getInstance().setElement(chatInput);
        RenderProcessor.getInstance().setElement(btnSend);
        chatInput.setEnabled(true);
        ClientMessageBus.getInstance().start();
        ClientMessageBus.getInstance().subscribe(
            line -> line != null && (
                line.startsWith("[GLOBAL]") ||
                line.startsWith("[PARTY]")  ||
                line.startsWith("[PM]")     ||
                line.startsWith("[SERVER]")
            ),
            this::onBusLine
        );

        // Si quieres que los clicks del juego no molesten mientras escribes, puedes subir la capa mínima
        //InterpreterEvent.getInstance().setMinActiveLayer(6);

        DriverRender.getInstance().string();
    }

    public void close() {
        
        isOpen = false; 
 
        InterpreterEvent.getInstance().setMinActiveLayer(1);
        
        DriverRender dr = DriverRender.getInstance();
        SpatialGrid grid = SpatialGrid.getInstance();

        dr.eliminarNodo(chatCanvas);
        dr.eliminarNodo(tabGeneral);
        dr.eliminarNodo(tabSala);
        dr.eliminarNodo(tabAmigo);
        dr.eliminarNodo(btnCerrarChat);

        dr.eliminarNodo(btnSend);
        dr.eliminarNodo(chatInput);

        grid.limpiar(tabGeneral);
        grid.limpiar(tabSala);
        grid.limpiar(tabAmigo);
        grid.limpiar(btnCerrarChat);
        grid.limpiar(btnSend);
        grid.limpiar(chatInput);

        // Opcional: desactivar hitbox
        tabGeneral.setEnabled(false); 
        chatInput.setEnabled(false);
        btnSend.setEnabled(false);
    
        tabSala.setEnabled(false); 
        tabAmigo.setEnabled(false); 
        btnCerrarChat.setEnabled(false); 

        DriverRender.getInstance().string();
    }

    private void redraw() {
        chatCanvas.setImage(buildChatImage());
        DriverRender.getInstance().string();
    }

    // ================== DIBUJO DEL CHAT ==================

    private BufferedImage buildChatImage() {
        int width = 1280, height = 720;
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();

        // Fondo transparente total
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.0f));
        g2d.setColor(new Color(0, 0, 0, 0));
        g2d.fillRect(0, 0, width, height);

        g2d.setComposite(AlphaComposite.SrcOver);

        // Panel de chat
        g2d.setColor(new Color(40, 40, 70, 230));
        g2d.fillRoundRect(CX, CY, CW, CH, 22, 22);
        g2d.setColor(new Color(20, 20, 40, 255));
        g2d.fillRoundRect(CX + 4, CY + 4, CW - 8, CH - 8, 22, 22);

        // Título
        g2d.setColor(Color.WHITE);
        g2d.drawString("CHAT", CX + 20, CY + 12);

        // ====== PESTAÑAS (pintado visual, independiente de los UIButton) ======
        int tabY = CY + 25;
        int tabH = 24;

        drawTab(g2d, 0, "General", CX + 20, tabY, 110, tabH);
        drawTab(g2d, 1, "Sala",    CX + 140, tabY, 110, tabH);
        drawTab(g2d, 2, "Amigo",   CX + 260, tabY, 110, tabH);

        // ====== Área de mensajes ======
        int msgAreaX = CX + 16;
        int msgAreaY = CY + 60;
        int msgAreaW = CW - 32;
        int msgAreaH = CH - 120;

        g2d.setColor(new Color(15, 15, 30, 255));
        g2d.fillRoundRect(msgAreaX, msgAreaY, msgAreaW, msgAreaH, 16, 16);

        g2d.setColor(new Color(80, 80, 130));
        g2d.drawRoundRect(msgAreaX, msgAreaY, msgAreaW, msgAreaH, 16, 16);

        // Seleccionamos lista de mensajes según pestaña
        List<String> mensajes;
        switch (activeTab) {
            case 1 -> mensajes = salaMessages;
            case 2 -> mensajes = amigoMessages;
            default -> mensajes = generalMessages;
        }

        // Pintar mensajes
        g2d.setColor(Color.WHITE);
        int lineY = msgAreaY + 20;
        int lineStep = 18;
        for (String m : mensajes) {
            if (lineY > msgAreaY + msgAreaH - 10) break;
            g2d.drawString(m, msgAreaX + 10, lineY);
            lineY += lineStep;
        }

        // ====== Caja de escribir ======
        int inputY = CY + CH - 40;
        g2d.setColor(new Color(25, 25, 50, 255));
        g2d.fillRoundRect(CX + 16, inputY, CW - 120, 24, 12, 12);
        g2d.setColor(new Color(90, 90, 140));
        g2d.drawRoundRect(CX + 16, inputY, CW - 120, 24, 12, 12);

   
        g2d.setColor(new Color(180, 180, 220));
        g2d.drawString("", CX + 24, inputY + 16);
       
            
   
        // Botón enviar (solo visual por ahora)
        int sendX = CX + CW - 90;
        g2d.setColor(new Color(90, 200, 120));
        g2d.fillRoundRect(sendX, inputY, 70, 24, 12, 12);
        g2d.setColor(Color.DARK_GRAY);
        g2d.drawRoundRect(sendX, inputY, 70, 24, 12, 12);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Enviar", sendX + 18, inputY + 16);

        // X (visual)
        g2d.setColor(new Color(220, 90, 90));
        g2d.fillOval(CX + CW - 30, CY + 10, 18, 18);
        g2d.setColor(Color.BLACK);
        g2d.drawString("X", CX + CW - 25, CY + 23);

        g2d.dispose();
        return img;
    }

    private void drawTab(Graphics2D g2d, int idx, String text,
                         int x, int y, int w, int h) {
        boolean active = (activeTab == idx);

        if (active) {
            g2d.setColor(new Color(230, 210, 90));
        } else {
            g2d.setColor(new Color(80, 80, 130));
        }
        g2d.fillRoundRect(x, y, w, h, 10, 10);

        g2d.setColor(Color.DARK_GRAY);
        g2d.drawRoundRect(x, y, w, h, 10, 10);

        g2d.setColor(Color.BLACK);
        g2d.drawString(text, x + 10, y + 16);
        
    }
    
    @Override
    public void update() {
        if (!isOpen) return;

        chatInput.update();

      
        if (changed) {
            changed = false;
            redraw();
        }
    }

    private void enviarMensaje() {
        String msg = chatInput.getText().trim();
        
        if (msg.isEmpty()) return;
        chatInput.clear();
        chatInput.render();
   
        
         GameTransport transport = GameState.getInstance().getTransport();
        if (transport == null) {
            System.err.println("[ChatOverlay] Transport no inicializado");
            return;
        }

        switch (activeTab) {
            case 0 -> transport.sendCommand("CHAT " + msg);
            case 1 -> transport.sendCommand("PARTY_CHAT " + msg);
            case 2 -> transport.sendCommand("WHISPER Richard " + msg);
        }
        chatInput.clear();
        redraw();
    }

    public Imagen getCanvas() {
        return chatCanvas;
    }
    private void routeIncoming(String line) {
        if (line.startsWith("[GLOBAL]")) {
            generalMessages.add(line.replace("[GLOBAL] ", ""));
            return;
        }
        if (line.startsWith("[PARTY]")) {
            salaMessages.add(line.replace("[PARTY] ", ""));
            return;
        }
        if (line.startsWith("[PM]")) {
            amigoMessages.add(line.replace("[PM] ", ""));
            return;
        }
        if (line.startsWith("[SERVER]")) {
            // tú decides dónde mostrar server notices
            generalMessages.add(line);
            return;
        }

        generalMessages.add(line);
    }
    
    private void onBusLine(String line) {
        routeIncoming(line);
        changed = true;
    }

    


}
