package com.dungeon_game.core.model.character;

import com.dungeon_game.core.api.InputKeyboard;
import com.dungeon_game.core.api.RenderProcessor;
import com.dungeon_game.core.components.SpriteAnimatorWithHitbox;
import com.dungeon_game.core.data.ClickOn;
import com.dungeon_game.core.data.RenderableVisual;
import com.dungeon_game.core.data.SpatialGrid;
import com.dungeon_game.core.data.VisualRender;
import com.dungeon_game.core.logic.VerticalPhysics;
import com.dungeon_game.core.model.Entity;
import com.dungeon_game.core.model.Entity;
import com.dungeon_game.core.model.Entity;
import com.dungeon_game.core.model.Imagen;
import com.dungeon_game.core.model.Imagen;
import com.dungeon_game.core.model.Imagen;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.util.List;

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
    private static double MAX_SPEED = 1.0;
    private static final int ANIM_TICKS = 6;
    private static final int ACCEL_TICKS = 18;
    private static final double JUMP_INITIAL_SPEED = -3.2;
    private static final int DASH_DURATION = 12;

    // ---------------- STATE ----------------
    private State state = State.IDLE;
    private SpriteAnimatorWithHitbox animator;

    private double posX;
    private double posY;
    private double velY = 0;
    private boolean facingRight = true;

    private final VerticalPhysics physics = new VerticalPhysics(0.25, 6.0);

    private int moveTicks = 0;
    private int animTickCounter = 0;
    private boolean jumpRequested = false;
    private boolean onGround = false;
    
    private int dashTickCounter = 0;

    private int cachedWidth = 0;
    private int cachedHeight = 0;
    
    // ⭐ SISTEMA DE ATAQUE
    private AttackSystem attackSystem;

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
    
    private static final int IDLE_SPRITE_WIDTH = 19;
    private static final int RUN_SPRITE_WIDTH = 19;
    private static final int JUMP_SPRITE_WIDTH = 20;
    private static final int ATTACK1_SPRITE_WIDTH = 31;
    private static final int ATTACK2_SPRITE_WIDTH = 31;
    private static final int ATTACK_DASH_SPRITE_WIDTH = 35;

    // ---------------- FRAMES (animaciones básicas) ----------------
    private final List<String> idleFrames = List.of(
            "jp"+(char)92+"idle_01","jp"+(char)92+"idle_02", "jp"+(char)92+"idle_03"
    );
    
    private final List<Point[]> idleHitboxes = List.of(
            new Point[]{new Point(8, 3), new Point(10, 1), new Point(12, 3), 
                new Point(15, 10), new Point(14,18), new Point(12, 18), 
                new Point(12, 16), new Point(10, 16), new Point(10, 18), new Point(8,18)},
            new Point[]{new Point(8, 3), new Point(12, 2), new Point(15, 13), 
                new Point(14,18), new Point(12, 18), new Point(12, 16), 
                new Point(10, 16), new Point(10, 18), new Point(8,18)},
            new Point[]{new Point(8, 3), new Point(11, 2), new Point(15, 11), 
                new Point(14,18), new Point(12, 18), new Point(12, 16), 
                new Point(10, 16), new Point(10, 18), new Point(8,18)}
    );

    private final List<String> runFrames = List.of(
            "jp"+(char)92+"run_01", "jp"+(char)92+"run_02", "jp"+(char)92+"run_03",
            "jp"+(char)92+"run_04", "jp"+(char)92+"run_05", "jp"+(char)92+"run_06"
    );
    
    private final List<Point[]> runHitboxes = List.of(
            new Point[]{new Point(2, 2), new Point(7, 1), new Point(10, 9), 
                new Point(13, 14), new Point(7,15), new Point(6, 16), new Point(4,16)},
            new Point[]{new Point(3, 3), new Point(6, 3), new Point(10, 10), 
                new Point(10, 15), new Point(9,17), new Point(6, 17), 
                new Point(7, 15), new Point(3,13)},
            new Point[]{new Point(3, 2), new Point(7, 2), new Point(10, 9), 
                new Point(11, 13), new Point(13,15), new Point(11, 17), 
                new Point(10, 15), new Point(6,16), new Point(4,15)},
            new Point[]{new Point(3, 2), new Point(7, 2), new Point(10, 8), 
                new Point(11, 13), new Point(13,14), new Point(12, 16), 
                new Point(10, 14), new Point(7,15), new Point(6,16), new Point(4,16)},
            new Point[]{new Point(3, 4), new Point(6, 3), new Point(10, 10), 
                new Point(10, 14), new Point(10,16), new Point(6, 17), new Point(3, 13)},
            new Point[]{new Point(3, 2), new Point(6, 1), new Point(11, 9), 
                new Point(11, 13), new Point(13,15), new Point(12, 17), 
                new Point(10, 15), new Point(7,15), new Point(6,16), 
                new Point(4,16), new Point(5,14), new Point(3,13)}
    );

    private final List<String> jumpFrames = List.of(
            "jp"+(char)92+"jump_01", "jp"+(char)92+"jump_02", "jp"+(char)92+"jump_03"
    );
    
    private final List<Point[]> jumpHitboxes = List.of(
            new Point[]{new Point(9, 2), new Point(12, 1), new Point(17, 12), 
                new Point(14,16), new Point(14, 18), new Point(13, 19), 
                new Point(12, 16), new Point(11, 15), new Point(10,16), 
                new Point(9, 17), new Point(7, 13), new Point(9,9)},
            new Point[]{new Point(8, 3), new Point(11, 1), new Point(18, 11), 
                new Point(15,14), new Point(15, 17), new Point(13, 17), 
                new Point(12, 15), new Point(11, 17), new Point(8,17), new Point(9,9)},
            new Point[]{new Point(8, 4), new Point(10, 2), new Point(18, 9), 
                new Point(17,11), new Point(15, 8), new Point(13, 10), 
                new Point(14, 16), new Point(13, 17), new Point(11,14), 
                new Point(11,18), new Point(9,19)}
    );

    // ---------------- CONSTRUCTOR ----------------
    public Character(Point[] vertices, Point dir) {
        super(vertices, dir, cargarIniciales());
        posX = dir.x;
        posY = dir.y;
        attackSystem = new AttackSystem(this);
        setAnimator(idleFrames, idleHitboxes, 200);
    }

    public static RenderableVisual cargarIniciales() {
        return new Imagen(640, 300, 189, 195, 4, "idle_01", null, 255);
    }

    // ---------------- UPDATE ----------------
    @Override
    public void update() {
        if (attackSystem.isAttacking()) {
            attackSystem.update();
            updateAttack();
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

        // ⭐ DETECTAR DASH con tecla Q
        if (input.isKeyDown(KeyEvent.VK_Q)) {
            attackSystem.attemptDash(facingRight);
            return;
        }

        // Si está atacando o en dash, no procesar otros inputs de movimiento
        if (attackSystem.isAttacking() || attackSystem.isDashing()) {
            return;
        }

        // ⭐ VERIFICAR A Y D SIMULTÁNEOS
        boolean aPressed = input.isKeyDown(KeyEvent.VK_A);
        boolean dPressed = input.isKeyDown(KeyEvent.VK_D);

        // Si ambos están presionados, quedarse quieto
        if (aPressed && dPressed) {
            changeState(State.IDLE);
            return;
        }

        if (aPressed) {
            changeState(State.RUN_LEFT);
            return;
        }

        if (dPressed) {
            changeState(State.RUN_RIGHT);
            return;
        }

        changeState(State.IDLE);
    }
    
    public void onLeftClick() {
        attackSystem.onLeftClick();
    }

    // ---------------- MOVIMIENTO ----------------
    private void applyMovement() {
        InputKeyboard input = InputKeyboard.getInstance();

        // ⭐ SI EL DASH SE MUEVE POR FRAMES, NO APLICAR MOVIMIENTO AQUÍ
        if (attackSystem.isDashing() && attackSystem.isDashMovingByFrames()) {
            // El movimiento lo maneja AttackSystem frame por frame
            return;
        }

        if (input.isKeyDown(KeyEvent.VK_W) && onGround && !jumpRequested) {
            MAX_SPEED = 3;
            velY = JUMP_INITIAL_SPEED;
            jumpRequested = true;
            onGround = false;
            changeState(State.JUMP);
        }

        int maxX = 0;
        for (Point v : vertices) {
            if (v.x > maxX) maxX = v.x;
        }

        switch (state) {
            case RUN_LEFT -> {
                facingRight = false;
                moveTicks = Math.min(moveTicks + 1, ACCEL_TICKS);
                double speed = speedFromTicks(moveTicks);
                posX -= speed;
                if (posX < 0) posX = 0;
            }

            case RUN_RIGHT -> {
                facingRight = true;
                moveTicks = Math.min(moveTicks + 1, ACCEL_TICKS);
                double speed = speedFromTicks(moveTicks);
                posX += speed;
                int limiteX = 127 - maxX;
                if (posX > limiteX) posX = limiteX;
            }

            default -> moveTicks = 0;
        }
    }
    
    /**
    * Aplica movimiento del dash basado en frames
    * Llamado por AttackSystem cuando cambia el frame
    */
    public void applyDashMovement(double displacement) {
        posX += displacement;

        // Calcular límites
        int maxX = 0;
        for (Point v : vertices) {
            if (v.x > maxX) maxX = v.x;
        }

        // Limitar movimiento
        if (posX < 0) {
            posX = 0;
        }
        int limiteX = 127 - maxX;
        if (posX > limiteX) {
            posX = limiteX;
        }

        System.out.println("🏃 Posición actualizada: " + posX);
    }

    private double speedFromTicks(int x) {
        if (x >= ACCEL_TICKS) return MAX_SPEED;
        return MAX_SPEED - MAX_SPEED * Math.exp(-(x / 12.0));
    }

    // ---------------- GRAVEDAD ----------------
    private void applyGravity() {
        velY = physics.apply(velY);
        double nextPosY = posY + velY;

        int maxY = 0;
        for (Point v : vertices) {
            if (v.y > maxY) maxY = v.y;
        }

        if (nextPosY < 0) {
            nextPosY = 0;
            velY = 0;
        }

        double safeMaxY = 71.0 - maxY;

        if (nextPosY > safeMaxY) {
            nextPosY = safeMaxY;
            posY = nextPosY;
            velY = 0;
            onGround = true;
            jumpRequested = false;
            return;
        }

        double footY = nextPosY + maxY;
        Integer sueloY = null;
        if (footY >= 0 && footY < 72) {
            sueloY = getSueloYEn(footY);
        }

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

        if (!attackSystem.isAttacking()) {
            changeState(State.JUMP);
        }
    }

    private Integer getSueloYEn(double worldY) {
        int y = (int) Math.floor(worldY);
        if (y < 0 || y >= 72) return null;

        Point dir = getDireccion();
        SpatialGrid grid = SpatialGrid.getInstance();

        if (dir.x < 0 || dir.x >= 128) return null;

        try {
            VisualRender a = grid.getElement(new Point(dir.x, y), 2);
            if (a != null) return a.getDireccion().y;

            int rightX = dir.x + vertices[2].x - 1;
            if (rightX >= 0 && rightX < 128) {
                VisualRender b = grid.getElement(new Point(rightX, y), 2);
                if (b != null) return b.getDireccion().y;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
        }

        return null;
    }

    @Override
    public void setVertices(Point[] newVertices) {
        if (newVertices != null && newVertices.length > 0) {
            this.vertices = new Point[newVertices.length];
            for (int i = 0; i < newVertices.length; i++) {
                this.vertices[i] = new Point(newVertices[i].x, newVertices[i].y);
            }
            validatePositionAfterVertexChange();
        }
    }

    @Override
    public Point[] getVertices() {
        return vertices;
    }

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
        if (posX > safeMaxX) posX = safeMaxX;

        if (posY < 0) posY = 0;
        if (posX < 0) posX = 0;
    }

    private void syncTransform() {
        Point dir = getDireccion();

        int maxX = 0, maxY = 0;
        for (Point v : vertices) {
            if (v.x > maxX) maxX = v.x;
            if (v.y > maxY) maxY = v.y;
        }

        double safeMaxY = 71.0 - maxY;
        if (posY > safeMaxY) {
            posY = safeMaxY;
            velY = 0;
            onGround = true;
        }

        double safeMaxX = 127.0 - maxX;
        if (posX < 0) posX = 0;
        if (posX > safeMaxX) posX = safeMaxX;

        if (posY < 0) posY = 0;

        dir.x = (int) Math.round(posX);
        dir.y = (int) Math.round(posY);

        for (Point v : vertices) {
            int finalY = dir.y + v.y;
            int finalX = dir.x + v.x;

            if (finalY >= 72 || finalX >= 128 || finalY < 0 || finalX < 0) {
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
            RenderProcessor.getInstance().updateHitbox(this, render, dir);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("❌ FATAL: updateHitbox failed");
            posX = 30;
            posY = 10;
            dir.x = 30;
            dir.y = 10;
            velY = 0;
            onGround = false;
        }
    }

    private void updateAnimation() {
        if (attackSystem.isAttacking()) return;
        
        animTickCounter++;
        if (animTickCounter >= ANIM_TICKS) {
            animator.update();
            animTickCounter = 0;
        }
    }

    private void changeState(State newState) {
        if (state == newState) return;

        state = newState;
        animTickCounter = 0;

        switch (state) {
            case IDLE -> setAnimator(idleFrames, idleHitboxes, 200);      // 200ms - Idle pausado
            case RUN_LEFT, RUN_RIGHT -> setAnimator(runFrames, runHitboxes, 100); // 100ms - Run fluido
            case JUMP -> setAnimator(jumpFrames, jumpHitboxes, 150);      // 150ms - JUMP visible ✅
            case ATTACK -> setAttackAnimator();
            case ATTACK_DASH -> setAttackAnimator();
        }
    }

    // ⭐ NUEVO: Método para refrescar el animator sin cambiar de estado
    private void refreshAttackAnimator() {
        animTickCounter = 0;
        setAttackAnimator();
    }
    
    private void setAttackAnimator() {
        if (animator != null) animator.stop();

        int type = attackSystem.getCurrentAttackType();
        List<String> frames = attackSystem.getFramesForType(type);
        List<Point[]> bodyHitboxes = attackSystem.getBodyHitboxesForType(type);

        // ⭐ OBTENER ANCHO CORRECTO SEGÚN EL TIPO DE ATAQUE
        final int SPRITE_WIDTH = switch (type) {
            case 2 -> ATTACK2_SPRITE_WIDTH;
            case 3 -> ATTACK_DASH_SPRITE_WIDTH;
            default -> ATTACK1_SPRITE_WIDTH;
        };

        System.out.println("🖼️ Attack Sprite width: " + SPRITE_WIDTH + "px, Type: " + type + 
                          ", Facing: " + (facingRight ? "RIGHT →" : "LEFT ←"));

        // ⭐ APLICAR FLIP A LAS HITBOXES SI MIRA A LA DERECHA
        List<Point[]> processedHitboxes = bodyHitboxes;
        if (facingRight) {
            processedHitboxes = bodyHitboxes.stream()
                .map(hitbox -> flipHitboxHorizontally(hitbox, SPRITE_WIDTH))
                .toList();
            System.out.println("   ↪ Attack hitboxes volteadas para RIGHT");
        }

        long frameMs = switch (type) {
            case 2 -> 85L;
            case 3 -> 90L;
            default -> 90L;
        };

        animator = new SpriteAnimatorWithHitbox(
                (Imagen) getObjeto(),
                this,
                frames,
                processedHitboxes,
                frameMs,
                false
        );

        // ⭐ APLICAR FLIP AL SPRITE
        ((Imagen) getObjeto()).setFlippedHorizontally(facingRight);

        final int totalFrames = frames.size();
        animator.setOnFrameChangeListener(frameIndex -> {
            attackSystem.onFrameChange(frameIndex, totalFrames);
        });

        animator.play();
        System.out.println("🎬 Attack Animator: " + totalFrames + " frames configurado");
    }

    private void setAnimator(List<String> frames, List<Point[]> hitboxes, long frameMs) {
        if (animator != null) animator.stop();

        // ⭐ OBTENER ANCHO CORRECTO SEGÚN EL ESTADO ACTUAL
        final int SPRITE_WIDTH = getCurrentSpriteWidth();

        System.out.println("🖼️ Sprite width: " + SPRITE_WIDTH + "px, Estado: " + state + 
                          ", Facing: " + (facingRight ? "RIGHT →" : "LEFT ←"));

        // ⭐ APLICAR FLIP A LAS HITBOXES SI MIRA A LA DERECHA
        List<Point[]> processedHitboxes = hitboxes;
        if (facingRight) {
            processedHitboxes = hitboxes.stream()
                .map(hitbox -> flipHitboxHorizontally(hitbox, SPRITE_WIDTH))
                .toList();
            System.out.println("   ↪ Hitboxes volteadas para RIGHT");
        }

        animator = new SpriteAnimatorWithHitbox(
                (Imagen) getObjeto(),
                this,
                frames,
                processedHitboxes,
                frameMs,
                true
        );

        // ⭐ APLICAR FLIP AL SPRITE
        ((Imagen) getObjeto()).setFlippedHorizontally(facingRight);

        animator.play();
    }
    
    private void updateAttack() {
        animTickCounter++;
        if (animTickCounter >= ANIM_TICKS) {
            animator.update();
            animTickCounter = 0;
        }
    }

    public void onAttackStarted() {
        if (state == State.ATTACK) {
            // Ya estamos en ATTACK, solo refrescar animator
            refreshAttackAnimator();
        } else {
            changeState(State.ATTACK);
        }
    }

    public void onDashStarted() {
        changeState(State.ATTACK_DASH);
    }

    public void onAttackEnded() {
        changeState(State.IDLE);
    }
    
    /**
    * Refleja las hitboxes horizontalmente basándose en la dirección
    * @param originalHitbox Hitbox original (mirando a la izquierda)
    * @param spriteWidth Ancho del sprite
    * @return Hitbox reflejada
    */
    private Point[] flipHitboxHorizontally(Point[] originalHitbox, int spriteWidth) {
        Point[] flipped = new Point[originalHitbox.length];

        for (int i = 0; i < originalHitbox.length; i++) {
            Point original = originalHitbox[i];
            // Fórmula para reflejar: new_x = spriteWidth - 1 - original_x
            flipped[i] = new Point(spriteWidth - 1 - original.x, original.y);
        }

        return flipped;
    }

    /**
     * Refleja una lista de hitboxes de arma
     */
    private List<Point> flipWeaponHitboxHorizontally(List<Point> originalHitbox, int spriteWidth) {
        if (originalHitbox == null) return null;

        return originalHitbox.stream()
            .map(p -> new Point(spriteWidth - p.x, p.y))
            .toList();
    }
    

    @Override
    public void executeClickAction() {
        // Solo ataque normal con click
        onLeftClick();

        // Ejecutar acción de click personalizada si existe
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

    // ---------------- GETTERS ----------------
    public String getCurrentStateName() {
        return state.name();
    }

    public int getCurrentFrameIndex() {
        return animator != null ? animator.getCurrentFrameIndex() : 0;
    }

    public boolean isOnGround() {
        return onGround;
    }

    public double getVelocityY() {
        return velY;
    }

    public double getPosX() {
        return posX;
    }

    public double getPosY() {
        return posY;
    }

    public boolean isAttacking() {
        return attackSystem.isAttacking();
    }
    
    public boolean isFacingRight() {
        return facingRight;
    }

    public String getCurrentSpriteName() {
        try {
            return ((Imagen) getObjeto()).getVisualId();
        } catch (Exception e) {
            return "unknown";
        }
    }
    
    public long getDashCooldownRemaining() {
        return attackSystem.getDashCooldownRemaining();
    }

    public boolean isDashAvailable() {
        return getDashCooldownRemaining() == 0;
    }

    public int getCurrentAttackType() {
        return attackSystem.getCurrentAttackType();
    }

    public boolean isDashing() {
        return attackSystem.isDashing();
    }

    public boolean isComboQueued() {
        return attackSystem.isComboQueued();
    }
    
    public List<Point> getCurrentAttackHitbox() {
        return attackSystem.getCurrentAttackHitbox();
    }
    
    public boolean isDashMovingByFrames() {
        return attackSystem.isDashMovingByFrames();
    }
    
    private int getCurrentSpriteWidth() {
        switch (state) {
            case IDLE:
                return IDLE_SPRITE_WIDTH;
            case RUN_LEFT:
            case RUN_RIGHT:
                return RUN_SPRITE_WIDTH;
            case JUMP:
                return JUMP_SPRITE_WIDTH;
            case ATTACK:
                return switch (attackSystem.getCurrentAttackType()) {
                    case 2 -> ATTACK2_SPRITE_WIDTH;
                    default -> ATTACK1_SPRITE_WIDTH;
                };
            case ATTACK_DASH:
                return ATTACK_DASH_SPRITE_WIDTH;
            default:
                return 24; // Fallback seguro
        }
    }
    
    public int getTotalFrames() {
        switch (state) {
            case IDLE: return idleFrames.size();
            case RUN_LEFT:
            case RUN_RIGHT: return runFrames.size();
            case JUMP: return jumpFrames.size();
            case ATTACK: 
                return attackSystem.getFramesForType(attackSystem.getCurrentAttackType()).size();
            case ATTACK_DASH: 
                return attackSystem.getFramesForType(3).size();
            default: return 0;
        }
    }
}