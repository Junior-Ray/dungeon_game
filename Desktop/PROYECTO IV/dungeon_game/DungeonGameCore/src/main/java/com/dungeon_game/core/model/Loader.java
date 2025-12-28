/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.dungeon_game.core.model;


import com.dungeon_game.core.api.CroppedImage;
import com.dungeon_game.core.api.RenderProcessor;
import com.dungeon_game.core.api.Updater;
import com.dungeon_game.core.audio.AudioManager;
import com.dungeon_game.core.audio.MusicTrack;
import com.dungeon_game.core.components.AbstractUIComponent;
import com.dungeon_game.core.components.InputText;
import com.dungeon_game.core.components.UIButton;
import com.dungeon_game.core.data.AntFromLeft;
import com.dungeon_game.core.data.RenderableVisual;
import com.dungeon_game.core.logic.ClientMessageBus;
import com.dungeon_game.core.logic.GameState;

import com.dungeon_game.core.util.ValidadorDatos;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

/**
 *
 * @author GABRIEL SALGADO
 */
public class Loader extends Sala {

    private enum PanelActual {
        NINGUNO,
        INICIO,
        REGISTRO
    }
    private static Loader instance;
    private static List<Updater> list;
    private static List<AbstractUIComponent> listaInicio = null;
    private static List<AbstractUIComponent> listaRegistro = null;
    private AntFromLeft ant_inicio;    // animación inicio
    private AntFromLeft ant_registro;  // animación registro
    private UIButton registro;
    private UIButton inicio;
    private Imagen kenji;
    private CroppedImage ani_bg;
    private Imagen s_i;
    private Imagen s_r;
    private UIButton cerrar;
    private InputText nombre;
    private InputText correo;
    private InputText confirmar;
    private InputText contraseña;
    private UIButton recuperar;
    private UIButton iniciar;
    private UIButton registrarse;
    private static boolean authSubscribed = false;
    private PanelActual panel = PanelActual.NINGUNO;

    private Loader() {
    }

    ;
    public static Loader getInstance() {
        if (instance == null) {
            instance = new Loader();
        }
        return instance;
    }

    @Override
    public void cargarIniciales() {
        listaInicio = null;
        listaRegistro = null;
        ant_inicio = null;
        ant_registro = null;
        panel = PanelActual.NINGUNO;
        if (list == null) list = new ArrayList<>();

        panel = PanelActual.NINGUNO;
        if (!authSubscribed) {
            authSubscribed = true;
        ClientMessageBus.getInstance().start();
        ClientMessageBus.getInstance().subscribe(
            line -> line != null && (
                line.startsWith("LOGIN_OK") ||
                line.startsWith("LOGIN_FAIL") ||
                line.startsWith("AUTH_OK") ||
                line.startsWith("AUTH_FAIL") ||
                line.startsWith("REGISTER_OK") ||
                line.startsWith("REGISTER_FAIL")
            ),
            this::onAuthLine
        );
     
    }
        // ====================================================================
        // 1. INICIO AUTO-LOGIN
        // ====================================================================
        
        // Pedimos la herramienta de almacenamiento al GameState (que el Main inyectó)
        System.out.println("--- [DEBUG] INICIANDO CARGA DE LOADER ---");

        // 1. Verificar si tenemos Storage
        var storage = GameState.getInstance().getTokenStorage();
        if (storage == null) {
            System.err.println("❌ [DEBUG] Error: TokenStorage es NULL. Revisa tu Main.");
        } else {
            // 2. Intentar leer token
            String token = storage.cargar();
            System.out.println("👀 [DEBUG] Token leído del archivo: " + token);

            if (token != null) {
                boolean ok = GameState.getInstance().connectOnly("52.14.160.71", 5000, true);
                if (ok) {
                    GameState.getInstance().getTransport().sendCommand("AUTH_TOKEN " + token);
                    System.out.println("🔄 [DEBUG] Enviando AUTH_TOKEN al servidor...");
                    // IMPORTANTE: aquí NO haces siguienteSala()
                    // el cambio a lobby lo hace onAuthLine cuando llegue AUTH_OK
                    
                } else {
                    System.err.println("❌ No se pudo conectar al servidor para validar token.");
                }
            } else {
                System.out.println("📂 [DEBUG] No hay token guardado (es la primera vez o cerraste sesión).");
            }
        }
        
        System.out.println("🎨 [DEBUG] Cargando interfaz de Login manual...");
        // ====================================================================
        // FIN AUTO-LOGIN (Si falló, sigue cargando la interfaz normal abajo)
        // ====================================================================
        
        
        list = new ArrayList<>();

        //BG : PREPARACION DE ANIMACION
        RenderableVisual bg = new Imagen(0, 0, 1280, 720, 0, "bg_load", null, 255);
        CroppedImage.Scrap scrap_bg = new CroppedImage.Scrap(0, 0, 1280, 720);
        ani_bg = new CroppedImage(bg, scrap_bg);

        //KENJI
        kenji = new Imagen(0, 0, 1280, 720, 0, "kenji", null, 255);

        //Registrarse
        Point[] ver = new Point[4];
        ver[0] = new Point(0, 0);
        ver[1] = new Point(0, 8);
        ver[2] = new Point(26, 8);
        ver[3] = new Point(26, 0);
        registro = new UIButton(930, 590, 264, 84, 1, "btn_registro", ver, new Point(93, 59), null);
        //Iniciar
        ver = new Point[4];
        ver[0] = new Point(0, 0);
        ver[1] = new Point(0, 8);
        ver[2] = new Point(26, 8);
        ver[3] = new Point(26, 0);
        inicio = new UIButton(930, 480, 264, 84, 1, "btn_sesion", ver, new Point(93, 48), null);

        RenderProcessor.getInstance().setElement(ani_bg);
        list.add(ani_bg);
        RenderProcessor.getInstance().setElement(kenji);
        list.add(kenji);
        RenderProcessor.getInstance().setElement(registro);
        list.add(registro);
        RenderProcessor.getInstance().setElement(inicio);
        list.add(inicio);
        //DINAMICOS:

        s_i = new Imagen(824, 0, 456, 720, 0, "sesion_iniciar", null, 255);

        s_r = new Imagen(824, 0, 456, 720, 0, "sesion_registro", null, 255);

        ani_bg.setOnUpdateAction(() -> {
            int x = ani_bg.getScrap().getX();
            if (x > 1200) {
                x = 0;
            } else {
                x += 2;
            }
            ani_bg.getScrap().setX(x);
        });

        inicio.setOnClickAction(() -> {

            // CASO 1: Panel cerrado → abrir panel INICIO
            if (panel == PanelActual.NINGUNO) {

                ant_inicio = new AntFromLeft(s_i);

                Loader.getInstance().moverBotonesIzquierda();

                RenderProcessor.getInstance().setElement(ant_inicio.getCropped());
                list.add(ant_inicio);
                Loader.getInstance().botonesInicio();
                panel = PanelActual.INICIO;
                System.out.println("HOLA");
                return;
            }

            // CASO 2: Panel INICIO abierto → cerrar
            if (panel == PanelActual.INICIO) {

                Loader.getInstance().moverBotonesDerecha();

                RenderProcessor.getInstance().eliminarElemento(ant_inicio.getTarget());
                list.remove(ant_inicio);
                Loader.getInstance().eliminarBotonesInicio();
                ant_inicio = null;

                panel = PanelActual.NINGUNO;
                return;
            }

            // CASO 3: Panel REGISTRO abierto → cambiar a INICIO
            if (panel == PanelActual.REGISTRO) {

                // cerrar registro
                RenderProcessor.getInstance().eliminarElemento(ant_registro.getTarget());
                list.remove(ant_registro);
                Loader.getInstance().eliminarBotonesRegistro();
                ant_registro = null;

                // abrir inicio
                ant_inicio = new AntFromLeft(s_i);

                Loader.getInstance().moverBotonesIzquierda();

                RenderProcessor.getInstance().setElement(ant_inicio.getCropped());
                list.add(ant_inicio);
                Loader.getInstance().botonesInicio();

                panel = PanelActual.INICIO;
            }
        });

        registro.setOnClickAction(() -> {

            // CASO 1: Panel cerrado → abrir REGISTRO
            if (panel == PanelActual.NINGUNO) {

                ant_registro = new AntFromLeft(s_r);

                Loader.getInstance().moverBotonesIzquierda();

                RenderProcessor.getInstance().setElement(ant_registro.getCropped());
                list.add(ant_registro);
                Loader.getInstance().botonesRegistro();
                panel = PanelActual.REGISTRO;
                return;
            }

            // CASO 2: Panel REGISTRO abierto → cerrar
            if (panel == PanelActual.REGISTRO) {

                Loader.getInstance().moverBotonesDerecha();

                RenderProcessor.getInstance().eliminarElemento(ant_registro.getTarget());
                list.remove(ant_registro);
                Loader.getInstance().eliminarBotonesRegistro();
                ant_registro = null;

                panel = PanelActual.NINGUNO;
                return;
            }

            // CASO 3: Panel INICIO abierto → cambiar a REGISTRO
            if (panel == PanelActual.INICIO) {

                // cerrar inicio
                RenderProcessor.getInstance().eliminarElemento(ant_inicio.getTarget());
                list.remove(ant_inicio);

                Loader.getInstance().eliminarBotonesInicio();

                ant_inicio = null;

                // abrir registro
                ant_registro = new AntFromLeft(s_r);

                Loader.getInstance().moverBotonesIzquierda();

                RenderProcessor.getInstance().setElement(ant_registro.getCropped());
                list.add(ant_registro);
                botonesRegistro();
                panel = PanelActual.REGISTRO;
            }
        });
        AudioManager.getInstance().playMusic(MusicTrack.INTRO,true);
    }

    @Override
    public void commonUpdate() {
        if (list != null) {
            for (Updater u : list) u.update();
        }
        if (listaInicio != null) {
            for (AbstractUIComponent updaters : listaInicio) {

                updaters.update();
            }
        }
        if (listaRegistro != null) {
            for (AbstractUIComponent updaters : listaRegistro) {
                updaters.update();
            }
        }
    }

    private void moverBotonesIzquierda() {
        RenderProcessor.getInstance().updateHitbox(inicio, new Point(80, 480), new Point(8, 48));
        RenderProcessor.getInstance().updateHitbox(registro, new Point(80, 590), new Point(8, 59));
    }

    private void moverBotonesDerecha() {
        RenderProcessor.getInstance().updateHitbox(inicio, new Point(930, 480), new Point(93, 48));
        RenderProcessor.getInstance().updateHitbox(registro, new Point(930, 590), new Point(93, 59));
    }

    private void botonesInicio() {
        listaInicio = new ArrayList<>();
        Point[] ver = new Point[4];
        ver[0] = new Point(0, 0);
        ver[1] = new Point(3, 0);
        ver[2] = new Point(3, 3);
        ver[3] = new Point(0, 3);
        Imagen obj = new Imagen(880, 10, 30, 30, 1, null, null, 255);
        cerrar = new UIButton(null, ver, new Point(88, 1), obj);
        cerrar.setOnClickAction(() -> {
            moverBotonesDerecha();

            RenderProcessor.getInstance().eliminarElemento(ant_inicio.getTarget());
            list.remove(ant_inicio);
            eliminarBotonesInicio();
            ant_inicio = null;

            panel = PanelActual.NINGUNO;
        });
        ver = new Point[4];
        ver[0] = new Point(0, 0);
        ver[1] = new Point(24, 0);
        ver[2] = new Point(24, 3);
        ver[3] = new Point(0, 3);
        obj = new Imagen(950, 330, 240, 30, 1, null, null, 255);
        nombre = new InputText(ver, new Point(95, 33), obj);

        ver = new Point[4];
        ver[0] = new Point(0, 0);
        ver[1] = new Point(24, 0);
        ver[2] = new Point(24, 3);
        ver[3] = new Point(0, 3);
        obj = new Imagen(950, 390, 240, 30, 1, null, null, 255);
        contraseña = new InputText(ver, new Point(95, 39), obj);
        contraseña.setMode(InputText.InputMode.PASSWORD);
        ver = new Point[4];
        ver[0] = new Point(0, 0);
        ver[1] = new Point(13, 0);
        ver[2] = new Point(13, 1);
        ver[3] = new Point(0, 1);
        obj = new Imagen(1060, 430, 130, 10, 1, null, null, 255);
        recuperar = new UIButton(null, ver, new Point(106, 43), obj);
        ver = new Point[4];
        ver[0] = new Point(0, 0);
        ver[1] = new Point(24, 0);
        ver[2] = new Point(24, 4);
        ver[3] = new Point(0, 4);
        obj = new Imagen(950, 460, 240, 40, 1, null, null, 255); //244x56
        iniciar = new UIButton(null, ver, new Point(95, 46), obj);
        ver = new Point[4];
        ver[0] = new Point(0, 0);
        ver[1] = new Point(7, 0);
        ver[2] = new Point(7, 1);
        ver[3] = new Point(0, 1);
        obj = new Imagen(1110, 510, 70, 10, 1, null, null, 255); //244x56
        registrarse = new UIButton(null, ver, new Point(111, 51), obj);

        RenderProcessor.getInstance().setElement(cerrar);
        listaInicio.add(cerrar);
        nombre.render();
        RenderProcessor.getInstance().setElement(nombre);
        listaInicio.add(nombre);
        contraseña.render();
        RenderProcessor.getInstance().setElement(contraseña);
        listaInicio.add(contraseña);
        RenderProcessor.getInstance().setElement(recuperar);
        listaInicio.add(recuperar);
        RenderProcessor.getInstance().setElement(iniciar);
        listaInicio.add(iniciar);
        RenderProcessor.getInstance().setElement(registrarse);
        listaInicio.add(registrarse);
        iniciar.setOnClickAction(() -> {
            // 1. Obtener datos de los campos
            String user = nombre.getText().trim();
            String pass = contraseña.getText().trim();

            // 2. Validación Local (Campos vacíos)
            if (user.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Por favor, ingresa usuario y contraseña.");
                return;
            }

            // 3. Llamada al Backend (AuthManager)
            // AuthManager.login devuelve un objeto Usuario si es correcto, o null si falló.
            boolean ok = GameState.getInstance().connectOnly("52.14.160.71", 5000, true);
            if (!ok) {
                JOptionPane.showMessageDialog(null, "No se pudo conectar al servidor.");
                return;
            }

            GameState.getInstance().getTransport().sendCommand("LOGIN " + user + " " + pass);
            System.out.println("[Loader] Enviando LOGIN al servidor...");
            // No hagas siguienteSala aquí.
            // onAuthLine hará el cambio cuando llegue LOGIN_OK.
        });
        registrarse.setOnClickAction(() -> {
            // cerrar inicio
            eliminarBotonesInicio();
            RenderProcessor.getInstance().eliminarElemento(ant_inicio.getTarget());
            list.remove(ant_inicio);
            ant_inicio = null;

            // abrir registro
            ant_registro = new AntFromLeft(s_r);

            moverBotonesIzquierda();

            RenderProcessor.getInstance().setElement(ant_registro.getCropped());
            list.add(ant_registro);
            botonesRegistro();
            panel = PanelActual.REGISTRO;
        });
    }

    /*
    *BOTONES REGISTRO
    *
     */
    private void botonesRegistro() {
        listaRegistro = new ArrayList<>();
        Point[] ver = new Point[4];
        ver[0] = new Point(0, 0);
        ver[1] = new Point(3, 0);
        ver[2] = new Point(3, 3);
        ver[3] = new Point(0, 3);
        Imagen obj = new Imagen(880, 10, 30, 30, 1, null, null, 255);
        cerrar = new UIButton(null, ver, new Point(88, 1), obj);
        cerrar.setOnClickAction(() -> {
            Loader.getInstance().moverBotonesDerecha();

            RenderProcessor.getInstance().eliminarElemento(ant_registro.getTarget());
            list.remove(ant_registro);
            Loader.getInstance().eliminarBotonesRegistro();
            ant_registro = null;

            panel = PanelActual.NINGUNO;
        });
        ver = new Point[4];
        ver[0] = new Point(0, 0);
        ver[1] = new Point(24, 0);
        ver[2] = new Point(24, 2);
        ver[3] = new Point(0, 2);
        obj = new Imagen(950, 320, 240, 20, 1, null, null, 255);
        nombre = new InputText(ver, new Point(95, 32), obj);

        ver = new Point[4];
        ver[0] = new Point(0, 0);
        ver[1] = new Point(24, 0);
        ver[2] = new Point(24, 2);
        ver[3] = new Point(0, 2);
        obj = new Imagen(950, 380, 240, 20, 1, null, null, 255);
        correo = new InputText(ver, new Point(95, 38), obj);
        correo.setMode(InputText.InputMode.EMAIL);

        ver = new Point[4];
        ver[0] = new Point(0, 0);
        ver[1] = new Point(24, 0);
        ver[2] = new Point(24, 2);
        ver[3] = new Point(0, 2);
        obj = new Imagen(950, 440, 240, 30, 1, null, null, 255);
        contraseña = new InputText(ver, new Point(95, 44), obj);
        contraseña.setMode(InputText.InputMode.PASSWORD);

        ver = new Point[4];
        ver[0] = new Point(0, 0);
        ver[1] = new Point(24, 0);
        ver[2] = new Point(24, 2);
        ver[3] = new Point(0, 2);
        obj = new Imagen(950, 500, 240, 30, 1, null, null, 255);
        confirmar = new InputText(ver, new Point(95, 50), obj);

        ver = new Point[4];
        ver[0] = new Point(0, 0);
        ver[1] = new Point(24, 0);
        ver[2] = new Point(24, 4);
        ver[3] = new Point(0, 4);
        obj = new Imagen(950, 540, 240, 40, 1, null, null, 255); //244x56
        registrarse = new UIButton(null, ver, new Point(95, 54), obj);

        registrarse.setOnClickAction(()->{
            String u = nombre.getText().trim();
            String e = correo.getText().trim();
            String p1 = contraseña.getText().trim();
            String p2 = confirmar.getText().trim();

            // 1) validación local
            String mensaje = ValidadorDatos.registrarUsuario(u, e, p1, p2);
            if (mensaje != null) {
                JOptionPane.showMessageDialog(null, mensaje);
                return;
            }

            // 2) conectar si no está conectado
            boolean ok = GameState.getInstance().connectOnly("52.14.160.71", 5000, true);
            if (!ok) {
                JOptionPane.showMessageDialog(null, "No se pudo conectar al servidor.");
                return;
            }

            // 3) mandar comando al server (¡ojo espacios!)
            GameState.getInstance().getTransport().sendCommand("REGISTER " + u + " " + e + " " + p1);
            System.out.println("[Loader] Enviando REGISTER al servidor...");
        });
        
        ver = new Point[4];
        ver[0] = new Point(0, 0);
        ver[1] = new Point(7, 0);
        ver[2] = new Point(7, 1);
        ver[3] = new Point(0, 1);
        obj = new Imagen(1110, 590, 70, 10, 1, null, null, 255); //244x56
        iniciar = new UIButton(null, ver, new Point(111, 59), obj);
        iniciar.setOnClickAction(() -> {
            // cerrar registro
            RenderProcessor.getInstance().eliminarElemento(ant_registro.getTarget());
            list.remove(ant_registro);
            Loader.getInstance().eliminarBotonesRegistro();
            ant_registro = null;

            // abrir inicio
            ant_inicio = new AntFromLeft(s_i);

            Loader.getInstance().moverBotonesIzquierda();

            RenderProcessor.getInstance().setElement(ant_inicio.getCropped());
            list.add(ant_inicio);
            Loader.getInstance().botonesInicio();

            panel = PanelActual.INICIO;
        });
        RenderProcessor.getInstance().setElement(cerrar);
        listaRegistro.add(cerrar);
        RenderProcessor.getInstance().setElement(nombre);
        listaRegistro.add(nombre);
        RenderProcessor.getInstance().setElement(correo);
        listaRegistro.add(correo);
        RenderProcessor.getInstance().setElement(contraseña);
        listaRegistro.add(contraseña);
        RenderProcessor.getInstance().setElement(confirmar);
        listaRegistro.add(confirmar);
        RenderProcessor.getInstance().setElement(registrarse);
        listaRegistro.add(registrarse);
        RenderProcessor.getInstance().setElement(iniciar);
        listaRegistro.add(iniciar);
    }

    private void eliminarBotonesInicio() {
        // System.out.println("Lista de Inicio: " + ((listaInicio!=null)? listaInicio.size(): "no tiene elementos"));
        if (listaInicio != null) {
            for (AbstractUIComponent obj : listaInicio) {
                RenderProcessor.getInstance().eliminarElemento(obj);
            }
        }
        listaInicio = null;
    }

    private void eliminarBotonesRegistro() {
        if (listaRegistro != null) {
            for (AbstractUIComponent obj : listaRegistro) {
                RenderProcessor.getInstance().eliminarElemento(obj);
            }
        }
        listaRegistro = null;
    }

    @Override
    public void eliminarSala() {
        instance = null;
    }
    private void onAuthLine(String line) {
        System.out.println("[Loader] AuthLine: " + line);
            
        // LOGIN_OK <token> <username> <userCode>
        if (line.startsWith("LOGIN_OK")) {
            String[] p = line.split("\\s+");
            if (p.length >= 4) {
                String token = p[1];
                String username = p[2];
                String userCode = p[3];
                
                var storage = GameState.getInstance().getTokenStorage();
                if (storage != null) storage.guardar(token);

                GameState.getInstance().setPlayerId(username);
                GameState.getInstance().setUserCode(userCode);

                AudioManager.getInstance().playMusic(MusicTrack.MAIN_MENU,true);
                GameState.getInstance().siguienteSala();

                GameState.getInstance().setAuthToken(token);
                storage.guardar(token);
            }
            return;
        }

        // AUTH_OK <username> <userCode>
        if (line.startsWith("AUTH_OK")) {
            String[] p = line.split("\\s+");
            if (p.length >= 3) {
                String username = p[1];
                String userCode = p[2];
                GameState.getInstance().setPlayerId(username);
                GameState.getInstance().setUserCode(userCode);

                AudioManager.getInstance().playMusic(MusicTrack.MAIN_MENU,true);
                GameState.getInstance().siguienteSala();
            }
            return;
        }

        if (line.startsWith("AUTH_FAIL")) {
            var storage = GameState.getInstance().getTokenStorage();
            if (storage != null) storage.borrar();
            System.out.println("[Loader] Token inválido, mostrando login manual.");
            // aquí NO cambias sala, solo dejas que cargue la UI normal
            return;
        }

        if (line.startsWith("LOGIN_FAIL")) {
            javax.swing.JOptionPane.showMessageDialog(null, "Usuario o contraseña incorrectos.");
        }
        if (line.startsWith("REGISTER_OK")) {
            // ejemplo: REGISTER_OK <token> <username> <userCode>
            String[] p = line.split("\\s+");
            if (p.length >= 4) {
                String token = p[1];
                String username = p[2];
                String userCode = p[3];

                var storage = GameState.getInstance().getTokenStorage();
                if (storage != null) storage.guardar(token);

                GameState.getInstance().setPlayerId(username);
                GameState.getInstance().setUserCode(userCode);

                AudioManager.getInstance().playMusic(MusicTrack.MAIN_MENU, true);
                GameState.getInstance().siguienteSala();
                GameState.getInstance().setAuthToken(token);
                storage.guardar(token);
            }
            return;
        }
        
        if (line.startsWith("REGISTER_FAIL")) {
            // ejemplo: REGISTER_FAIL <razon...>
            String msg = line.length() > "REGISTER_FAIL".length()
                ? line.substring("REGISTER_FAIL".length()).trim()
                : "No se pudo registrar.";
            JOptionPane.showMessageDialog(null, msg);
            return;
        }
    }
    public static void resetForReentry() {
        authSubscribed = false;
        list = null;
        listaInicio = null;
        listaRegistro = null;
        if (instance != null) {
            instance.panel = PanelActual.NINGUNO;
        }
    }


}
