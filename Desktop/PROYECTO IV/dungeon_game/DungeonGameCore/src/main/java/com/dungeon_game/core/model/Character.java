/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.dungeon_game.core.model;

import com.dungeon_game.core.api.InputKeyboard;
import com.dungeon_game.core.api.RenderProcessor;
import com.dungeon_game.core.components.SpriteImageAnimator;
import com.dungeon_game.core.data.ClickOn;
import com.dungeon_game.core.data.RenderableVisual;
import com.dungeon_game.core.data.SpatialGrid;
import com.dungeon_game.core.data.VisualRender;
import com.dungeon_game.core.logic.VerticalPhysics;
import java.awt.Point;

/**
 *
 * @author GABRIEL SALGADO
 */
import java.awt.event.KeyEvent;
import java.util.List;

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
    private SpriteImageAnimator animator;

    private double posX;
    private double posY;

    private double velY = 0;

    private final VerticalPhysics physics
            = new VerticalPhysics(0.25, 6.0);

    private int moveTicks = 0;
    private int animTickCounter = 0;
    private boolean jumpRequested = false;
    private boolean onGround = false;

    @Override
    public void interactuable(Entity other) {
    }

    // ---------------- ENUM ----------------
    private enum State {
        IDLE,
        RUN_LEFT,
        RUN_RIGHT,
        JUMP,
        DASH
    }

    // ---------------- FRAMES ----------------
    private final List<String> idleFrames = List.of(
            "jp"+(char)92+"idle_01","jp"+(char)92+"idle_02", "jp"+(char)92+"idle_03"
    );

    private final List<String> runFrames = List.of(
            "jp"+(char)92+"run_01", "jp"+(char)92+"run_02", "jp"+(char)92+"run_03",
            "jp"+(char)92+"run_04", "jp"+(char)92+"run_05", "jp"+(char)92+"run_06"
    );

    private final List<String> jumpFrames = List.of(
            "jp"+(char)92+"jump_01", "jp"+(char)92+"jump_02", "jp"+(char)92+"jump_03"
    );

    // ---------------- CONSTRUCTOR ----------------
    public Character(Point[] vertices, Point dir) {
        super(vertices, dir, cargarIniciales());

        posX = dir.x;
        posY = dir.y;

        setAnimator(idleFrames, 200);
    }

    public static RenderableVisual cargarIniciales() {
        return new Imagen(640, 300, 189, 195, 4, "idle_01", null, 255);
    }

    // ---------------- UPDATE ----------------
    @Override
    public void update() {

        readInput();          // estado horizontal
        applyMovement();     // TU movimiento exponencial
        applyGravity();      // NUEVO
        syncTransform();     // grid + render
        updateAnimation();   // animación por ticks

    }

    // ---------------- INPUT ----------------
    private void readInput() {

        InputKeyboard input = InputKeyboard.getInstance();

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

    private void applyMovement() {

        InputKeyboard input = InputKeyboard.getInstance();

        // -------- SALTO (EVENTO, NO CONTINUO) --------
        if (input.isKeyDown(KeyEvent.VK_W) && onGround && !jumpRequested) {
            MAX_SPEED=3;
            velY = JUMP_INITIAL_SPEED;
            jumpRequested = true;
            onGround = false;
            changeState(State.JUMP);
        }

        // -------- MOVIMIENTO HORIZONTAL (NO TOCADO) --------
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
                int lim =127-vertices[1].x;
                if (posX >lim) {
                    posX =lim;
                }
            }

            default ->
                moveTicks = 0;
        }
    }

    private double speedFromTicks(int x) {

        if (x >= ACCEL_TICKS) {
            return MAX_SPEED;
        }

        // v(x) = Vmax - Vmax * e^(-x/16)
        return MAX_SPEED - MAX_SPEED * Math.exp(-(x / 12.0));
    }

    // ---------------- GRAVEDAD ----------------
    private void applyGravity() {

        velY = physics.apply(velY);

        double nextPosY = posY + velY;
        double footY = nextPosY + vertices[3].y;

        Integer sueloY = getSueloYEn(footY);

        if (sueloY != null && velY >= 0) {

            // Ajuste PERFECTO al suelo
            posY = sueloY - vertices[3].y;
            MAX_SPEED=1.5;
            velY = 0;
            onGround = true;
            jumpRequested = false;
            return;
        }

        onGround = false;
        posY = nextPosY;
        changeState(State.JUMP);
    }

    // ---------------- GRID ----------------
    private void syncTransform() {

        Point dir = getDireccion();
        dir.x = (int) Math.round(posX);
        dir.y = (int) Math.round(posY);

        Point render = new Point(
               (int) Math.round(posX * WORLD_SCALE),
               (int) Math.round(posY * WORLD_SCALE)
        );

        RenderProcessor.getInstance()
                .updateHitbox(this, render, dir);
    }

    // ---------------- SUELO ----------------
    private Integer getSueloYEn(double worldY) {

        int y = (int) Math.floor(worldY);
        Point dir = getDireccion();

        SpatialGrid grid = SpatialGrid.getInstance();

        // Pie izquierdo
        VisualRender a = grid.getElement(
                new Point(dir.x, y), 2);

        if (a != null) {
            return a.getDireccion().y; // cara superior del bloque
        }

        // Pie derecho
        VisualRender b = grid.getElement(
                new Point(dir.x + vertices[2].x - 1, y), 2);

        if (b != null) {
            return b.getDireccion().y;
        }

        return null;
    }

    // ---------------- ANIMATION ----------------
    private void updateAnimation() {

        animTickCounter++;

        if (animTickCounter >= ANIM_TICKS) {
            animator.update();
            animTickCounter = 0;
        }
    }

    // ---------------- STATE CHANGE ----------------
    private void changeState(State newState) {

        if (state == newState) {
            return;
        }

        state = newState;
        animTickCounter = 0;

        switch (state) {
            case IDLE ->
                setAnimator(idleFrames, 200);
            case RUN_LEFT, RUN_RIGHT ->
                setAnimator(runFrames, 100);
            case JUMP ->
                setAnimator(jumpFrames, 90);
        }
    }

    private void setAnimator(List<String> frames, long frameMs) {

        if (animator != null) {
            animator.stop();
        }

        animator = new SpriteImageAnimator(
                (Imagen) getObjeto(),
                frames,
                frameMs,
                true
        );
        animator.play();
    }
    //--------------------Click----------------------------
    public void executeClickAction() {
        // 1. Ejecuta el método abstracto de acción visual/sonora
        onClick();

        // 2. Ejecuta la acción delegada (la función que el usuario programó)
        if (this.clickAction != null) {
            this.clickAction.execute();
        }
    }
    public void onClick(){
        
    }
    @Override
    public void setOnClickAction(ClickAction action) {
        this.clickAction = action;
    }
}
