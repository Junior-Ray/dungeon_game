/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.dungeon_game.core.model;

import com.dungeon_game.core.api.InputKeyboard;
import com.dungeon_game.core.api.RenderProcessor;
import com.dungeon_game.core.audio.AudioManager;
import com.dungeon_game.core.audio.SoundEffect;
import com.dungeon_game.core.components.SpriteAnimatorWithHitbox;
import com.dungeon_game.core.data.ClickOn;
import com.dungeon_game.core.data.RenderableVisual;
import com.dungeon_game.core.data.SpatialGrid;
import com.dungeon_game.core.data.VisualRender;
import com.dungeon_game.core.logic.VerticalPhysics;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Arrays;

/**
 * Personaje jugable del juego
 * @author GABRIEL SALGADO
 */
public class Character extends Entity implements ClickOn {
    
    //----------------- Click ----------------
    public boolean isPressed = false;
    public ClickAction clickAction = null;
    
    // ---------------- CONFIG ----------------
    private static final double WORLD_SCALE = 10;
    private static double MAX_SPEED = 1.8;
    private static final int ANIM_TICKS = 6;
    private static final int ACCEL_TICKS = 18;
    private static final double JUMP_INITIAL_SPEED = -3.2;

    // ---------------- STATE ----------------
    private State state = State.IDLE;
    private SpriteAnimatorWithHitbox animator;

    private double posX;
    private double posY;
    private double velY = 0;

    private final VerticalPhysics physics = new VerticalPhysics(0.25, 6.0);

    private int moveTicks = 0;
    private int animTickCounter = 0;
    private boolean jumpRequested = false;
    private boolean onGround = false;

    private boolean isAttacking = false;
    private int attackFrameIndex = 0;
    private List<Point> currentAttackHitbox = null;
    private boolean attackHitRegistered = false;

    // ⭐ VARIABLES PARA COMBO Y DASH
    private int currentAttackType = 1;           // 1=attack1, 2=attack2, 3=dash
    private boolean attackClickedDuringCombo = false; // Si el usuario clickeó durante attack1
    private boolean isDashing = false;           // Si está ejecutando dash
    private double dashVelocityX = 0;            // Velocidad horizontal del dash
    private static final double DASH_SPEED = 4.5;     // Velocidad del dash
    private static final int DASH_DURATION = 12; // Duración del dash en ticks
    private int dashTickCounter = 0;             // Contador para dash

    // ⭐ COOLDOWN DEL DASH
    private long lastDashTime = 0;               // Timestamp del último dash
    private static final long DASH_COOLDOWN_MS = 2000; // 2 segundos de cooldown

    private int cachedWidth = 0;
    private int cachedHeight = 0;

    @Override
    public void interactuable(Entity other) {
    }

    // ---------------- ENUM ----------------
    private enum State {
        IDLE,
        RUN_LEFT,
        RUN_RIGHT,
        JUMP,
        ATTACK,
        ATTACK_DASH
    }

    // ---------------- FRAMES ----------------
    private final List<String> idleFrames = List.of(
            "jp"+(char)92+"idle_01","jp"+(char)92+"idle_02", "jp"+(char)92+"idle_03"
    );
    
    private final List<Point[]> idleHitboxes = List.of(
            //IDDLE 01
            new Point[]{new Point(8, 3),
                new Point(10, 1),
                new Point(12, 3),
                new Point(15, 10),
                new Point(14,18),
                new Point(12, 18),
                new Point(12, 16),
                new Point(10, 16),
                new Point(10, 18),
                new Point(8,18)
            },
            // IDDLE 02
            new Point[]{
                new Point(8, 3),
                new Point(12, 2),
                new Point(15, 13),
                new Point(14,18),
                new Point(12, 18),
                new Point(12, 16),
                new Point(10, 16),
                new Point(10, 18),
                new Point(8,18)
            },
            // IDDLE 03
            new Point[]{
                new Point(8, 3),
                new Point(11, 2),
                new Point(15, 11),
                new Point(14,18),
                new Point(12, 18),
                new Point(12, 16),
                new Point(10, 16),
                new Point(10, 18),
                new Point(8,18)
            }
    );

    private final List<String> runFrames = List.of(
            "jp"+(char)92+"run_01", "jp"+(char)92+"run_02", "jp"+(char)92+"run_03",
            "jp"+(char)92+"run_04", "jp"+(char)92+"run_05", "jp"+(char)92+"run_06"
    );
    
    private final List<Point[]> runHitboxes = List.of(
            new Point[]{
                new Point(2, 2),
                new Point(7, 1),
                new Point(10, 9),
                new Point(13, 14),
                new Point(7,15),
                new Point(6, 16),
                new Point(4,16)
            },
            new Point[]{
                new Point(3, 3),
                new Point(6, 3),
                new Point(10, 10),
                new Point(10, 15),
                new Point(9,17),
                new Point(6, 17),
                new Point(7, 15),
                new Point(3,13)
            },
            new Point[]{
                new Point(3, 2),
                new Point(7, 2),
                new Point(10, 9),
                new Point(11, 13),
                new Point(13,15),
                new Point(11, 17),
                new Point(10, 15),
                new Point(6,16),
                new Point(4,15)
            },
            new Point[]{
                new Point(3, 2),
                new Point(7, 2),
                new Point(10, 8),
                new Point(11, 13),
                new Point(13,14),
                new Point(12, 16),
                new Point(10, 14),
                new Point(7,15),
                new Point(6,16),
                new Point(4,16)
            },
            new Point[]{
                new Point(3, 4),
                new Point(6, 3),
                new Point(10, 10),
                new Point(10, 14),
                new Point(10,16),
                new Point(6, 17),
                new Point(3, 13)
            },
            new Point[]{
                new Point(3, 2),
                new Point(6, 1),
                new Point(11, 9),
                new Point(11, 13),
                new Point(13,15),
                new Point(12, 17),
                new Point(10, 15),
                new Point(7,15),
                new Point(6,16),
                new Point(4,16),
                new Point(5,14),
                new Point(3,13)
            }
    );

    private final List<String> jumpFrames = List.of(
            "jp"+(char)92+"jump_01", "jp"+(char)92+"jump_02", "jp"+(char)92+"jump_03"
    );
    
    private final List<Point[]> jumpHitboxes = List.of(
            new Point[]{
                new Point(9, 2),
                new Point(12, 1),
                new Point(17, 12),
                new Point(14,16),
                new Point(14, 18),
                new Point(13, 19),
                new Point(12, 16),
                new Point(11, 15),
                new Point(10,16),
                new Point(9, 17),
                new Point(7, 13),
                new Point(9,9)
            },
            new Point[]{
                new Point(8, 3),
                new Point(11, 1),
                new Point(18, 11),
                new Point(15,14),
                new Point(15, 17),
                new Point(13, 17),
                new Point(12, 15),
                new Point(11, 17),
                new Point(8,17),
                new Point(9,9)
            },
            new Point[]{
                new Point(8, 4),
                new Point(10, 2),
                new Point(18, 9),
                new Point(17,11),
                new Point(15, 8),
                new Point(13, 10),
                new Point(14, 16),
                new Point(13, 17),
                new Point(11,14),
                new Point(11,18),
                new Point(9,19)
            }
    );
    
    // ---------------- ATTACK FRAMES ----------------
    
    private final List<String> attack1Frames = List.of(
            "jp"+(char)92+"attack1_01", 
            "jp"+(char)92+"attack1_02", 
            "jp"+(char)92+"attack1_03", 
            "jp"+(char)92+"attack1_04",
            "jp"+(char)92+"attack1_05", 
            "jp"+(char)92+"attack1_06"
    );
    
    // Hitboxes del CUERPO durante el ataque
    private final List<Point[]> attack1BodyHitboxes = List.of(
            // Frame 1: preparando
            new Point[]{
                new Point(21, 3),
                new Point(24, 2),
                new Point(27, 6),
                new Point(28,9),
                new Point(27, 18),
                new Point(25, 19),
                new Point(24, 17),
                new Point(22, 17),
                new Point(21,18),
                new Point(19, 18),
                new Point(20, 13),
                new Point(18,11),
                new Point(21,8)
            },
            // Frame 2: golpeando
            new Point[]{
                new Point(13, 4),
                new Point(21, 5),
                new Point(22, 8),
                new Point(26, 9),
                new Point(28, 13),
                new Point(27, 18),
                new Point(25, 18),
                new Point(22, 17),
                new Point(20, 17),
                new Point(20, 18),
                new Point(17, 18),
                new Point(17, 16),
                new Point(20,13)
            },
            // Frame 3
            new Point[]{
                new Point(17, 5),
                new Point(21, 4),
                new Point(22, 9),
                new Point(26, 8),
                new Point(28, 18),
                new Point(25, 18),
                new Point(24, 17),
                new Point(20, 17),
                new Point(20, 18),
                new Point(18, 18),
                new Point(19, 13)
            },
            // Frame 4
            new Point[]{
                new Point(17, 5),
                new Point(21, 4),
                new Point(22, 9),
                new Point(26, 8),
                new Point(28, 18),
                new Point(25, 18),
                new Point(24, 17),
                new Point(20, 17),
                new Point(20, 18),
                new Point(18, 18),
                new Point(19, 13)
            },
            // Frame 5
            new Point[]{
                new Point(17, 5),
                new Point(21, 4),
                new Point(22, 9),
                new Point(26, 8),
                new Point(28, 18),
                new Point(25, 18),
                new Point(24, 17),
                new Point(20, 17),
                new Point(20, 18),
                new Point(18, 18),
                new Point(19, 13)
            },
            // Frame 6
            new Point[]{
                new Point(17, 5),
                new Point(21, 4),
                new Point(22, 9),
                new Point(26, 8),
                new Point(28, 18),
                new Point(25, 18),
                new Point(24, 17),
                new Point(20, 17),
                new Point(20, 18),
                new Point(18, 18),
                new Point(19, 13)
            }
    );
    
    // Hitboxes del ARMA (espada/ataque)
    // null = no hay hitbox de ataque en ese frame
    private final List<List<Point>> attack1WeaponHitboxes = Arrays.asList(
            null,  // Frame 1: no hay hitbox aún
            // Frame 2: GOLPE
            List.of(
                new Point(0, 1),
                new Point(1, 11),
                new Point(6, 17),
                new Point(11, 19),
                new Point(17, 19),
                new Point(25, 15),
                new Point(15, 14),
                new Point(10, 11),
                new Point(9, 5),
                new Point(12, 6),
                new Point(12, 4)
            ),
            // Frame 3: GOLPE2!
            List.of(
                new Point(0, 1),
                new Point(0, 8),
                new Point(7, 15),
                new Point(14, 15),
                new Point(9, 6),
                new Point(13, 6),
                new Point(13, 5)
            ),
            // Frame 4
            List.of(
                new Point(0, 1),
                new Point(13, 5),
                new Point(13, 7),
                new Point(2, 4)
            ),
            List.of(
                new Point(0, 1),
                new Point(13, 5),
                new Point(13, 7),
                new Point(2, 4)
            ),
            List.of(
                new Point(0, 1),
                new Point(13, 5),
                new Point(13, 7),
                new Point(2, 4)
            )
    );
    
    private final List<String> attack2Frames = List.of(
            "jp"+(char)92+"attack2_01", 
            "jp"+(char)92+"attack2_02", 
            "jp"+(char)92+"attack2_03", 
            "jp"+(char)92+"attack2_04",
            "jp"+(char)92+"attack2_05", 
            "jp"+(char)92+"attack2_06"
    );
    
    // Hitboxes del CUERPO durante el ataque
    private final List<Point[]> attack2BodyHitboxes = List.of(
            // Frame 1: preparando
            new Point[]{
                new Point(18, 3),
                new Point(22, 2),
                new Point(23, 6),
                new Point(28, 11),
                new Point(27, 17),
                new Point(25, 17),
                new Point(24, 16),
                new Point(21, 16),
                new Point(20, 17),
                new Point(18, 17),
                new Point(20, 12)
            },
            // Frame 2: golpeando
            new Point[]{
                new Point(20, 5),
                new Point(25, 3),
                new Point(27, 6),
                new Point(28, 9),
                new Point(28, 17),
                new Point(26, 17),
                new Point(25, 16),
                new Point(22, 16),
                new Point(21, 18),
                new Point(18, 18),
                new Point(20, 13)
            },
            // Frame 3
            new Point[]{
                new Point(20, 5),
                new Point(25, 3),
                new Point(27, 6),
                new Point(28, 9),
                new Point(28, 17),
                new Point(26, 17),
                new Point(25, 16),
                new Point(22, 16),
                new Point(21, 18),
                new Point(18, 18),
                new Point(20, 13)
            },
            // Frame 4
            new Point[]{
                new Point(20, 5),
                new Point(25, 3),
                new Point(27, 6),
                new Point(28, 9),
                new Point(28, 17),
                new Point(26, 17),
                new Point(25, 16),
                new Point(22, 16),
                new Point(21, 18),
                new Point(18, 18),
                new Point(20, 13)
            },
            // Frame 5
            new Point[]{
                new Point(20, 5),
                new Point(25, 3),
                new Point(27, 6),
                new Point(28, 9),
                new Point(28, 17),
                new Point(26, 17),
                new Point(25, 16),
                new Point(22, 16),
                new Point(21, 18),
                new Point(18, 18),
                new Point(20, 13)
            },
            // Frame 6
            new Point[]{
                new Point(20, 5),
                new Point(25, 3),
                new Point(27, 6),
                new Point(28, 9),
                new Point(28, 17),
                new Point(26, 17),
                new Point(25, 16),
                new Point(22, 16),
                new Point(21, 18),
                new Point(18, 18),
                new Point(20, 13)
            }
    );
    
    // Hitboxes del ARMA (espada/ataque)
    // null = no hay hitbox de ataque en ese frame
    private final List<List<Point>> attack2WeaponHitboxes = Arrays.asList(
            null,  // Frame 1: no hay hitbox aún
            // Frame 2: GOLPE
            List.of(
                new Point(2, 0),
                new Point(0, 7),
                new Point(1, 14),
                new Point(8, 20),
                new Point(20, 16),
                new Point(12, 10),
                new Point(11, 4)
            ),
            // Frame 3: GOLPE2!
            List.of(
                new Point(4, 5),
                new Point(1, 9),
                new Point(2, 13),
                new Point(7, 19),
                new Point(21, 15),
                new Point(12, 11),
                new Point(9, 6)
            ),
            // Frame 4
            List.of(
                new Point(20, 14),
                new Point(10, 16),
                new Point(7, 19),
                new Point(21, 16)
            ),
            List.of(
                new Point(20, 14),
                new Point(10, 16),
                new Point(7, 19),
                new Point(21, 16)
            ),
            List.of(
                new Point(20, 14),
                new Point(10, 16),
                new Point(7, 19),
                new Point(21, 16)
            )
    );
    
    private final List<String> attackDash = List.of(
            "jp"+(char)92+"attack_dash_01", 
            "jp"+(char)92+"attack_dash_02", 
            "jp"+(char)92+"attack_dash_03", 
            "jp"+(char)92+"attack_dash_04",
            "jp"+(char)92+"attack_dash_05", 
            "jp"+(char)92+"attack_dash_06"
    );
    
    // Hitboxes del CUERPO durante el ataque
    private final List<Point[]> attackDashBodyHitboxes = List.of(
            // Frame 1: preparando
            new Point[]{
                new Point(19, 4),
                new Point(22, 3),
                new Point(25, 6),
                new Point(28, 7),
                new Point(30, 10),
                new Point(28, 12),
                new Point(30, 16),
                new Point(28, 16),
                new Point(27, 14),
                new Point(23, 15),
                new Point(23, 16),
                new Point(20, 16)
            },
            // Frame 2: golpeando
            new Point[]{
                new Point(20, 5),
                new Point(22, 3),
                new Point(25, 6),
                new Point(28, 7),
                new Point(30, 10),
                new Point(28, 12),
                new Point(30, 16),
                new Point(28, 16),
                new Point(27, 14),
                new Point(23, 15),
                new Point(23, 16),
                new Point(20, 16)
            },
            // Frame 3
            new Point[]{
                new Point(12, 3),
                new Point(15, 3),
                new Point(17, 6),
                new Point(23,6),
                new Point(25, 9),
                new Point(23,12),
                new Point(25, 14),
                new Point(25, 15),
                new Point(21, 15),
                new Point(13, 9),
                new Point(7,10),
                new Point(5, 8),
                new Point(11, 7)
            },
            // Frame 4
            new Point[]{
                new Point(12, 3),
                new Point(15, 3),
                new Point(17, 6),
                new Point(23,6),
                new Point(25, 9),
                new Point(23,12),
                new Point(25, 14),
                new Point(25, 15),
                new Point(21, 15),
                new Point(13, 9),
                new Point(7,9),
                new Point(5, 7),
                new Point(11, 7)
            },
            // Frame 5
            new Point[]{
                new Point(12, 3),
                new Point(15, 3),
                new Point(17, 6),
                new Point(23,6),
                new Point(25, 9),
                new Point(23,12),
                new Point(25, 14),
                new Point(24, 15),
                new Point(21, 15),
                new Point(13, 9),
                new Point(8,11),
                new Point(5, 8),
                new Point(11, 7)
            },
            // Frame 6
            new Point[]{
                new Point(10, 3),
                new Point(13, 2),
                new Point(22, 15),
                new Point(21,16),
                new Point(19, 16),
                new Point(18, 15),
                new Point(14, 14),
                new Point(14, 16),
                new Point(11,16),
                new Point(10, 11),
                new Point(12, 9)
            },
            // Frame 7
            new Point[]{
                new Point(11, 5),
                new Point(13, 2),
                new Point(22, 15),
                new Point(21,16),
                new Point(19, 16),
                new Point(18, 15),
                new Point(14, 14),
                new Point(14, 16),
                new Point(11,16),
                new Point(11, 14),
                new Point(13, 9)
            }
    );
    
    // Hitboxes del ARMA (espada/ataque)
    // null = no hay hitbox de ataque en ese frame
    private final List<List<Point>> attackDashWeaponHitboxes = Arrays.asList(
            null,  // Frame 1: no hay hitbox aún
            null,  // Frame 2: Preparando
            // Frame 3: GOLPE
            List.of(
                new Point(4, 8),
                new Point(1, 10),
                new Point(1, 13),
                new Point(4,16),
                new Point(9, 17),
                new Point(13,18),
                new Point(6, 15),
                new Point(4, 12),
                new Point(6, 10)
            ),
            // Frame 4: GOLPE2!
            List.of(
                new Point(4, 7),
                new Point(0, 10),
                new Point(0, 13),
                new Point(2,16),
                new Point(8, 18),
                new Point(3,14),
                new Point(3, 11),
                new Point(6, 9)
            ),
            // Frame 5
            List.of(
                new Point(5, 8),
                new Point(0, 8),
                new Point(0, 12),
                new Point(2,14),
                new Point(2, 11),
                new Point(5,10)
            ),
            // Frame 6
            List.of(
                new Point(11, 9),
                new Point(9, 10),
                new Point(10, 12),
                new Point(12,10)
            ),
            // Frame 7
            null //Fin del ataque
    );

    

    // ---------------- CONSTRUCTOR ----------------
    public Character(Point[] vertices, Point dir) {
        super(vertices, dir, cargarIniciales());

        posX = dir.x;
        posY = dir.y;

        setAnimator(idleFrames, idleHitboxes, 200);
    }

    public static RenderableVisual cargarIniciales() {
        return new Imagen(640, 300, 189, 195, 4, "idle_01", null, 255);
    }

    // ---------------- UPDATE ----------------
    @Override
    public void update() {
        if (isAttacking) {
        updateAttack();  // ← AÑADIR ESTO
        } else {
            readInput();
            applyMovement();
        }

        applyGravity();
        syncTransform();
        updateAnimation();
    }

    // ---------------- INPUT ----------------
    private void readInput() {
        InputKeyboard input = InputKeyboard.getInstance();
    
        // ⭐ DETECTAR DASH (Shift + Click o K)
        if (input.isKeyDown(KeyEvent.VK_SHIFT)) {
            // El dash con click se maneja en onLeftClick con shift presionado
            // Aquí solo manejamos la tecla K directa
            if (input.isKeyDown(KeyEvent.VK_K)) {
                attemptDash();
                return;
            }
        }

        // Si está atacando o en dash, no procesar otros inputs de movimiento
        if (isAttacking || isDashing) {
            return;
        }

        if (input.isKeyDown(KeyEvent.VK_A)) {
            changeState(State.RUN_LEFT);
            return;
        }

        if (input.isKeyDown(KeyEvent.VK_D)) {
            changeState(State.RUN_RIGHT);
            return;
        }

        changeState(State.IDLE);
    }
    
    /**
    * Método que debe ser llamado cuando se detecta un click izquierdo
    * Integrar con tu sistema de clicks existente
    */
    public void onLeftClick() {
        if (isDashing) {
            return; // No permitir clicks durante dash
        }

        if (!isAttacking) {
            // Iniciar ataque 1 si no está atacando
            startAttack(1);
        } else if (isAttacking && currentAttackType == 1 && !attackClickedDuringCombo) {
            // ⭐ MARCAR QUE SE CLICKEÓ DURANTE ATTACK1
            // El combo se activará automáticamente al terminar attack1
            attackClickedDuringCombo = true;
            System.out.println("✅ Combo encolado! Attack2 se activará automáticamente");
        }
        // Si ya está en attack2, ignorar clicks adicionales
    }

    // ---------------- MOVIMIENTO ----------------
    private void applyMovement() {
        InputKeyboard input = InputKeyboard.getInstance();

        // ⭐ MOVIMIENTO DEL DASH
        if (isDashing) {
            dashTickCounter++;

            // Aplicar velocidad horizontal del dash
            posX += dashVelocityX;

            // Calcular límites
            int maxX = 0;
            for (Point v : vertices) {
                if (v.x > maxX) maxX = v.x;
            }

            // Limitar movimiento
            if (posX < 0) posX = 0;
            int limiteX = 127 - maxX;
            if (posX > limiteX) posX = limiteX;

            // Terminar dash después de la duración
            if (dashTickCounter >= DASH_DURATION) {
                endDash();
            }

            return; // No procesar otros movimientos durante dash
        }

        // -------- SALTO -------- (código original)
        if (input.isKeyDown(KeyEvent.VK_W) && onGround && !jumpRequested) {
            MAX_SPEED = 3;
            velY = JUMP_INITIAL_SPEED;
            jumpRequested = true;
            onGround = false;
            changeState(State.JUMP);
        }

        // Calcular el ancho máximo del personaje una sola vez
        int maxX = 0;
        for (Point v : vertices) {
            if (v.x > maxX) maxX = v.x;
        }

        // -------- MOVIMIENTO HORIZONTAL -------- (código original)
        switch (state) {
            case RUN_LEFT -> {
                moveTicks = Math.min(moveTicks + 1, ACCEL_TICKS);
                double speed = speedFromTicks(moveTicks);
                posX -= speed;

                if (posX < 0) {
                    posX = 0;
                }
            }

            case RUN_RIGHT -> {
                moveTicks = Math.min(moveTicks + 1, ACCEL_TICKS);
                double speed = speedFromTicks(moveTicks);
                posX += speed;

                int limiteX = 127 - maxX;
                if (posX > limiteX) {
                    posX = limiteX;
                }
            }

            default -> moveTicks = 0;
        }
    }

    private double speedFromTicks(int x) {
        if (x >= ACCEL_TICKS) {
            return MAX_SPEED;
        }
        return MAX_SPEED - MAX_SPEED * Math.exp(-(x / 12.0));
    }

    // ---------------- GRAVEDAD ----------------
    private void applyGravity() {
        velY = physics.apply(velY);
        double nextPosY = posY + velY;

        // Calcular maxY ACTUAL (puede cambiar con animaciones)
        int maxY = 0;
        for (Point v : vertices) {
            if (v.y > maxY) maxY = v.y;
        }

        // Validación: No permitir Y negativo
        if (nextPosY < 0) {
            nextPosY = 0;
            velY = 0;
        }

        // Límite superior: el pie del personaje no puede superar Y=71
        double safeMaxY = 71.0 - maxY;

        if (nextPosY > safeMaxY) {
            nextPosY = safeMaxY;
            posY = nextPosY;
            velY = 0;
            onGround = true;
            jumpRequested = false;
            return;
        }

        // Buscar suelo en el grid
        double footY = nextPosY + maxY;
        Integer sueloY = null;
        if (footY >= 0 && footY < 72) {
            sueloY = getSueloYEn(footY);
        }

        // Si encontramos suelo en el grid y estamos cayendo
        if (sueloY != null && velY >= 0) {
            double landingY = sueloY - maxY;
            if (landingY >= 0 && landingY <= safeMaxY) {
                posY = landingY;
                MAX_SPEED = 1.5;
                velY = 0;
                onGround = true;
                jumpRequested = false;
                return;
            }
        }

        // Si no hay suelo detectado en el grid pero el pie alcanza Y=51 (suelo por defecto)
        // Esto es un fallback para el visualizer cuando no hay SpatialGrid inicializado
        if (sueloY == null && footY >= 51 && velY >= 0) {
            double groundLanding = 51.0 - maxY;
            if (groundLanding >= 0 && groundLanding <= safeMaxY) {
                posY = groundLanding;
                velY = 0;
                onGround = true;
                jumpRequested = false;
                MAX_SPEED = 1.5;
                return;
            }
        }

        onGround = false;
        posY = nextPosY;

        if (!isAttacking) {
            changeState(State.JUMP);
        }
    }

    // ---------------- SUELO ----------------
    private Integer getSueloYEn(double worldY) {
        int y = (int) Math.floor(worldY);

        // Validación: Asegurar que Y esté dentro de los límites del grid
        if (y < 0 || y >= 72) {
            return null;
        }

        Point dir = getDireccion();
        SpatialGrid grid = SpatialGrid.getInstance();

        // Validación: Asegurar que X también esté dentro de límites
        if (dir.x < 0 || dir.x >= 128) {
            return null;
        }

        try {
            // Pie izquierdo
            VisualRender a = grid.getElement(new Point(dir.x, y), 2);
            if (a != null) {
                return a.getDireccion().y;
            }

            // Pie derecho
            int rightX = dir.x + vertices[2].x - 1;
            if (rightX >= 0 && rightX < 128) {
                VisualRender b = grid.getElement(new Point(rightX, y), 2);
                if (b != null) {
                    return b.getDireccion().y;
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            // Si hay error, retornar null silenciosamente
        }

        return null;
    }

    @Override
    public void setVertices(Point[] newVertices) {
        if (newVertices != null && newVertices.length > 0) {
            // 🔧 CRÍTICO: SIEMPRE crear copias profundas
            this.vertices = new Point[newVertices.length];
            for (int i = 0; i < newVertices.length; i++) {
                this.vertices[i] = new Point(newVertices[i].x, newVertices[i].y);
            }

            validatePositionAfterVertexChange();
        }
    }

    @Override
    public Point[] getVertices() {
        // Retornar los vértices RELATIVOS originales
        // NO crear copias aquí porque el visualizer necesita leerlos frecuentemente
        return vertices;
    }

    /**
     * Valida y ajusta la posición después de cambiar los vértices.
     */
    private void validatePositionAfterVertexChange() {
        int maxY = 0;
        int maxX = 0;
        for (Point v : vertices) {
            if (v.y > maxY) maxY = v.y;
            if (v.x > maxX) maxX = v.x;
        }

        cachedWidth = maxX;
        cachedHeight = maxY;

        double safeMaxY = 71.0 - maxY;
        if (posY > safeMaxY) {
            posY = safeMaxY;
            velY = 0;
            onGround = true;
        }

        double safeMaxX = 127.0 - maxX;
        if (posX > safeMaxX) {
            posX = safeMaxX;
        }

        if (posY < 0) posY = 0;
        if (posX < 0) posX = 0;
    }

    // Asegúrate de tener este método también
    private void updateDimensionsCache() {
        cachedWidth = 0;
        cachedHeight = 0;
        for (Point v : vertices) {
            if (v.x > cachedWidth) cachedWidth = v.x;
            if (v.y > cachedHeight) cachedHeight = v.y;
        }
    }

    // ---------------- GRID ----------------
    private void syncTransform() {
        Point dir = getDireccion();

        // Calcular dimensiones REALES del personaje
        int maxX = 0, maxY = 0;
        for (Point v : vertices) {
            if (v.x > maxX) maxX = v.x;
            if (v.y > maxY) maxY = v.y;
        }

        // Ajustar posY basándose en el maxY ACTUAL
        double safeMaxY = 71.0 - maxY;
        if (posY > safeMaxY) {
            posY = safeMaxY;
            velY = 0;
            onGround = true;
        }

        // Limitar posX
        double safeMaxX = 127.0 - maxX;
        if (posX < 0) posX = 0;
        if (posX > safeMaxX) posX = safeMaxX;

        // Limitar posY
        if (posY < 0) posY = 0;

        // Redondear a enteros
        dir.x = (int) Math.round(posX);
        dir.y = (int) Math.round(posY);

        // Verificación final: comprobar cada vértice
        for (Point v : vertices) {
            int finalY = dir.y + v.y;
            int finalX = dir.x + v.x;

            if (finalY >= 72 || finalX >= 128 || finalY < 0 || finalX < 0) {
                // Ajustar inmediatamente
                if (finalY >= 72) {
                    dir.y = 71 - maxY;
                    posY = dir.y;
                    velY = 0;
                    onGround = true;
                }
                if (finalX >= 128) {
                    dir.x = 127 - maxX;
                    posX = dir.x;
                }
                if (finalY < 0) {
                    dir.y = 0;
                    posY = 0;
                }
                if (finalX < 0) {
                    dir.x = 0;
                    posX = 0;
                }
                break;
            }
        }

        Point render = new Point(
               (int) Math.round(posX * WORLD_SCALE),
               (int) Math.round(posY * WORLD_SCALE)
        );

        try {
            RenderProcessor.getInstance()
                    .updateHitbox(this, render, dir);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("❌ FATAL: updateHitbox failed at pos=(" + posX + "," + posY + 
                              ") dir=(" + dir.x + "," + dir.y + ") maxY=" + maxY);
            // Resetear a posición segura
            posX = 30;
            posY = 10;
            dir.x = 30;
            dir.y = 10;
            velY = 0;
            onGround = false;
        }
    }

    // ---------------- ANIMATION ----------------
    private void updateAnimation() {
        if (isAttacking) return; // ← AÑADIR ESTO (el ataque maneja su propia animación)
        
        animTickCounter++;

        if (animTickCounter >= ANIM_TICKS) {
            animator.update();
            animTickCounter = 0;
        }
    }

    // ---------------- STATE CHANGE ----------------
    private void changeState(State newState) {
        if (state == newState) return;

        state = newState;
        animTickCounter = 0;

        switch (state) {
            case IDLE -> setAnimator(idleFrames, idleHitboxes, 200);
            case RUN_LEFT, RUN_RIGHT -> setAnimator(runFrames, runHitboxes, 100);
            case JUMP -> setAnimator(jumpFrames, jumpHitboxes, 90);
            case ATTACK -> setAttackAnimator(); // Maneja attack1, attack2
            case ATTACK_DASH -> setAttackAnimator(); // Dash attack
        }
    }
    
    private void setAttackAnimator() {
        if (animator != null) {
            animator.stop();
        }

        // ⭐ SELECCIONAR FRAMES Y HITBOXES SEGÚN EL TIPO DE ATAQUE
        List<String> frames;
        List<Point[]> bodyHitboxes;
        List<List<Point>> weaponHitboxes;
        long frameMs;

        switch (currentAttackType) {
            case 1 -> {
                frames = attack1Frames;
                bodyHitboxes = attack1BodyHitboxes;
                weaponHitboxes = attack1WeaponHitboxes;
                frameMs = 80;
            }
            case 2 -> {
                frames = attack2Frames;
                bodyHitboxes = attack2BodyHitboxes;
                weaponHitboxes = attack2WeaponHitboxes;
                frameMs = 75; // Un poco más rápido
            }
            case 3 -> {
                // Dash attack
                frames = attackDash;
                bodyHitboxes = attackDashBodyHitboxes;
                weaponHitboxes = attackDashWeaponHitboxes;
                frameMs = 60; // Más rápido aún
            }
            default -> {
                frames = attack1Frames;
                bodyHitboxes = attack1BodyHitboxes;
                weaponHitboxes = attack1WeaponHitboxes;
                frameMs = 80;
            }
        }

        animator = new SpriteAnimatorWithHitbox(
                (Imagen) getObjeto(),
                this,
                frames,
                bodyHitboxes,
                frameMs,
                false  // NO loop
        );

        // 🎯 CONFIGURAR CALLBACK
        final List<List<Point>> finalWeaponHitboxes = weaponHitboxes;
        final int finalFrameCount = frames.size();

        animator.setOnFrameChangeListener(frameIndex -> {
            // 1. Actualizar hitbox del arma
            updateWeaponHitboxForFrame(frameIndex, finalWeaponHitboxes);

            // 2. Efectos por frame
            if (frameIndex == 1 || frameIndex == 2) {
                // Sonidos de ataque (ya se reproduce en startAttack)
            }

            // 3. Terminar ataque en último frame
            if (frameIndex >= finalFrameCount - 1) {
                endAttack();
            }
        });

        animator.play();
    }
    
    private void updateWeaponHitboxForFrame(int frameIndex, List<List<Point>> weaponHitboxes) {
        if (frameIndex >= 0 && frameIndex < weaponHitboxes.size()) {
            List<Point> weaponBox = weaponHitboxes.get(frameIndex);

            if (weaponBox != null && !weaponBox.isEmpty()) {
                Point dir = getDireccion();
                currentAttackHitbox = weaponBox.stream()
                    .map(p -> new Point(dir.x + p.x, dir.y + p.y))
                    .toList();

                if (!attackHitRegistered) {
                    checkAttackHit();
                }
            } else {
                currentAttackHitbox = null;
            }
        }
    }

    private void setAnimator(List<String> frames, List<Point[]> hitboxes, long frameMs) {
        if (animator != null) {
            animator.stop();
        }

        animator = new SpriteAnimatorWithHitbox(
                (Imagen) getObjeto(),
                this,
                frames,
                hitboxes,
                frameMs,
                state != State.ATTACK
        );

        // NUEVO: Si es ataque, configurar listener para hitbox de arma
        if (state == State.ATTACK) {
            animator.setOnFrameChangeListener(frameIndex -> {
                updateWeaponHitboxForFrame(frameIndex);

                // Terminar ataque en último frame
                if (frameIndex >= attack1Frames.size() - 1) {
                    endAttack();
                }
            });
        }

        animator.play();
    }
    
    // ---------------- ATTACK SYSTEM METHODS ----------------
    private void startAttack(int attackType) {
        if (!isAttacking && !isDashing) {
            isAttacking = true;
            attackFrameIndex = 0;
            attackHitRegistered = false;
            currentAttackType = attackType;
            changeState(State.ATTACK);

            // Sonido específico por tipo
            switch (attackType) {
                case 1 -> {
                    AudioManager.getInstance().playSound(SoundEffect.ATTACK_LIGHT);
                    System.out.println("⚔️ Attack 1 iniciado");
                }
                case 2 -> {
                    AudioManager.getInstance().playSound(SoundEffect.ATTACK_MEDIUM);
                    System.out.println("⚔️⚔️ Attack 2 (COMBO) iniciado");
                }
                case 3 -> {} // Dash se maneja en startDashAttack()
            }
        }
    }
    
    private void startDashAttack() {
        if (!isDashing && !isAttacking && onGround) {
            // Verificar cooldown una última vez
            long currentTime = System.currentTimeMillis();
            long timeSinceLastDash = currentTime - lastDashTime;

            if (timeSinceLastDash < DASH_COOLDOWN_MS) {
                return; // No permitir dash
            }

            isDashing = true;
            isAttacking = true;
            attackFrameIndex = 0;
            attackHitRegistered = false;
            currentAttackType = 3;
            dashTickCounter = 0;
            attackClickedDuringCombo = false;

            // ⭐ GUARDAR TIMESTAMP DEL DASH
            lastDashTime = currentTime;

            // Determinar dirección del dash
            InputKeyboard input = InputKeyboard.getInstance();
            if (input.isKeyDown(KeyEvent.VK_A)) {
                dashVelocityX = -DASH_SPEED;
            } else if (input.isKeyDown(KeyEvent.VK_D)) {
                dashVelocityX = DASH_SPEED;
            } else {
                // Si no hay input, usar la última dirección de movimiento
                dashVelocityX = (state == State.RUN_LEFT) ? -DASH_SPEED : DASH_SPEED;
            }

            changeState(State.ATTACK_DASH);
            AudioManager.getInstance().playSound(SoundEffect.DASH);
            System.out.println("🏃 Dash iniciado! Cooldown: 2s");
        }
    }

    private void endDash() {
        isDashing = false;
        dashTickCounter = 0;
        dashVelocityX = 0;
        System.out.println("🏃 Dash terminado");
        // La animación terminará y llamará a endAttack()
    }
    
    // NUEVO método
    private void updateWeaponHitboxForFrame(int frameIndex) {
        if (frameIndex >= 0 && frameIndex < attack1WeaponHitboxes.size()) {
            List<Point> weaponBox = attack1WeaponHitboxes.get(frameIndex);

            if (weaponBox != null && !weaponBox.isEmpty()) {
                Point dir = getDireccion();
                currentAttackHitbox = weaponBox.stream()
                    .map(p -> new Point(dir.x + p.x, dir.y + p.y))
                    .toList();

                if (!attackHitRegistered) {
                    checkAttackHit();
                }
            } else {
                currentAttackHitbox = null;
            }
        }
    }

    private void updateAttack() {
        // El animator ya maneja todo con el listener
        // Solo necesitas actualizar la animación
        animTickCounter++;
        if (animTickCounter >= ANIM_TICKS) {
            animator.update();
            animTickCounter = 0;
        }
    }
    
    public void setCurrentAttackHitbox(List<Point> hitbox) {
        this.currentAttackHitbox = hitbox;
    }

    private void checkAttackHit() {
        if (currentAttackHitbox == null || currentAttackHitbox.isEmpty()) return;

        SpatialGrid grid = SpatialGrid.getInstance();

        // Recorrer todos los puntos de la hitbox del arma
        for (Point p : currentAttackHitbox) {
            VisualRender entity = grid.getElement(p, 2);

//            if (entity instanceof Enemy enemy) {
//                // ¡Golpeamos un enemigo!
//                enemy.takeDamage(10);
//                attackHitRegistered = true;
//                System.out.println("¡Golpe conectado! Daño: 10");
//                return;  // Salir después del primer golpe
//            }
        }
    }

    private void endAttack() {
        isAttacking = false;
        attackFrameIndex = 0;
        currentAttackHitbox = null;
        attackHitRegistered = false;
        changeState(State.IDLE);
    }

    // Getter para debug/visualización
    public List<Point> getCurrentAttackHitbox() {
        return currentAttackHitbox;
    }
    
    /**
    * Intenta iniciar el dash verificando el cooldown
    */
    private void attemptDash() {
        long currentTime = System.currentTimeMillis();
        long timeSinceLastDash = currentTime - lastDashTime;

        if (timeSinceLastDash < DASH_COOLDOWN_MS) {
            // ⭐ AÚN EN COOLDOWN
            long remainingMs = DASH_COOLDOWN_MS - timeSinceLastDash;
            System.out.println("⏱️ Dash en cooldown. Espera " + (remainingMs / 1000.0) + "s");
            return;
        }

        if (!isDashing && onGround) {
            startDashAttack();
        }
    }
    
    /**
    * Versión pública para llamar desde clicks con shift
    */
    public void onShiftClick() {
        attemptDash();
    }
    

    //-------------------- Click ----------------------------
    public void executeClickAction() {
        onClick();
        if (this.clickAction != null) {
            this.clickAction.execute();
        }
    }

    public void onClick() {
    }

    @Override
    public void setOnClickAction(ClickAction action) {
        this.clickAction = action;
    }
    
    private int getCharacterWidth() {
        int maxX = 0;
        for (Point v : vertices) {
            if (v.x > maxX) maxX = v.x;
        }
        return maxX;
    }

    /**
     * Obtiene la altura máxima del personaje basada en sus vértices
     */
    private int getCharacterHeight() {
        int maxY = 0;
        for (Point v : vertices) {
            if (v.y > maxY) maxY = v.y;
        }
        return maxY;
    }
    
    /**
    * Obtiene el estado actual del personaje
    * @return String con el nombre del estado
    */
    public String getCurrentStateName() {
        return state.name();
    }

    /**
     * Obtiene el índice del frame actual
     * @return índice del frame
     */
    public int getCurrentFrameIndex() {
        if (animator != null) {
            return animator.getCurrentFrameIndex();
        }
        return 0;
    }

    /**
     * Obtiene el número total de frames de la animación actual
     * @return cantidad de frames
     */
    public int getTotalFrames() {
        switch (state) {
            case IDLE: return idleFrames.size();
            case RUN_LEFT:
            case RUN_RIGHT: return runFrames.size();
            case JUMP: return jumpFrames.size();
            case ATTACK: return attack1Frames.size();
            default: return 0;
        }
    }

    /**
     * Verifica si el personaje está en el suelo
     * @return true si está en el suelo
     */
    public boolean isOnGround() {
        return onGround;
    }

    /**
     * Obtiene la velocidad vertical actual
     * @return velocidad en Y
     */
    public double getVelocityY() {
        return velY;
    }

    /**
     * Obtiene la posición X en el mundo
     * @return posición X
     */
    public double getPosX() {
        return posX;
    }

    /**
     * Obtiene la posición Y en el mundo
     * @return posición Y
     */
    public double getPosY() {
        return posY;
    }

    /**
     * Verifica si está atacando actualmente
     * @return true si está en ataque
     */
    public boolean isAttacking() {
        return isAttacking;
    }

    /**
     * Obtiene el nombre del sprite actual
     * @return String con el ID del sprite
     */
    public String getCurrentSpriteName() {
        try {
            return ((Imagen) getObjeto()).getVisualId();
        } catch (Exception e) {
            return "unknown";
        }
    }
    
    /**
    * Obtiene el tiempo restante de cooldown del dash en milisegundos
    * @return milisegundos restantes, 0 si está disponible
    */
    public long getDashCooldownRemaining() {
        long currentTime = System.currentTimeMillis();
        long timeSinceLastDash = currentTime - lastDashTime;

        if (timeSinceLastDash >= DASH_COOLDOWN_MS) {
            return 0; // Disponible
        }

        return DASH_COOLDOWN_MS - timeSinceLastDash;
    }

    /**
     * Verifica si el dash está disponible (no en cooldown)
     * @return true si puede usar dash
     */
    public boolean isDashAvailable() {
        return getDashCooldownRemaining() == 0;
    }

    /**
     * Obtiene el tipo de ataque actual
     * @return 1=attack1, 2=attack2, 3=dash
     */
    public int getCurrentAttackType() {
        return currentAttackType;
    }

    /**
     * Verifica si está ejecutando dash
     * @return true si está en dash
     */
    public boolean isDashing() {
        return isDashing;
    }

    /**
     * Verifica si se encoló un combo
     * @return true si attack2 se activará al terminar attack1
     */
    public boolean isComboQueued() {
        return attackClickedDuringCombo;
    }
    
    public int getTotalFrames() {
        switch (state) {
            case IDLE: return idleFrames.size();
            case RUN_LEFT:
            case RUN_RIGHT: return runFrames.size();
            case JUMP: return jumpFrames.size();
            case ATTACK: 
                // Retornar según el tipo actual
                switch (currentAttackType) {
                    case 1: return attack1Frames.size();
                    case 2: return attack2Frames.size();
                    default: return attack1Frames.size();
                }
            case ATTACK_DASH: return attackDash.size();
            default: return 0;
        }
    }
    
}