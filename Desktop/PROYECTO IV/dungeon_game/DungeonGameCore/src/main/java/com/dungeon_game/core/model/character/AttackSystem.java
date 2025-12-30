package com.dungeon_game.core.model.character;

import com.dungeon_game.core.model.character.Character;
import com.dungeon_game.core.audio.AudioManager;
import com.dungeon_game.core.audio.SoundEffect;
import com.dungeon_game.core.data.SpatialGrid;
import com.dungeon_game.core.data.VisualRender;
import java.awt.Point;
import java.util.Arrays;
import java.util.List;

/**
 * Sistema de combate del personaje
 * Maneja ataques, combos y dash
 * 
 * @author GABRIEL SALGADO
 */
public class AttackSystem {
    
    // Referencias
    private final Character character;
    
    // Estado del ataque
    private boolean isAttacking = false;
    private int currentAttackType = 1; // 1=attack1, 2=attack2, 3=dash
    private boolean attackClickedDuringCombo = false;
    private List<Point> currentAttackHitbox = null;
    private boolean attackHitRegistered = false;
    
    // Dash
    private boolean isDashing = false;
    private double dashVelocityX = 0;
    private long lastDashTime = 0;
    private int lastDashFrame = 0;
    
    // Constantes
    private static final double DASH_SPEED = 4.5;
    private static final long DASH_COOLDOWN_MS = 2000;
    
    private static final int ATTACK1_SPRITE_WIDTH = 31;
    private static final int ATTACK2_SPRITE_WIDTH = 31;
    private static final int ATTACK_DASH_SPRITE_WIDTH = 35;
    
    // ============ ATTACK 1 FRAMES ============
    private final List<String> attack1Frames = List.of(
            "jp"+(char)92+"attack1_01", 
            "jp"+(char)92+"attack1_02", 
            "jp"+(char)92+"attack1_03", 
            "jp"+(char)92+"attack1_04",
            "jp"+(char)92+"attack1_05", 
            "jp"+(char)92+"attack1_06"
    );
    
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
            // Frame 3: GOLPE2
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
    
    // ============ ATTACK 2 FRAMES ============
    private final List<String> attack2Frames = List.of(
            "jp"+(char)92+"attack2_01", 
            "jp"+(char)92+"attack2_02", 
            "jp"+(char)92+"attack2_03", 
            "jp"+(char)92+"attack2_04",
            "jp"+(char)92+"attack2_05", 
            "jp"+(char)92+"attack2_06"
    );
    
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
            // Frame 3: GOLPE2
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
    
    // ============ DASH ATTACK FRAMES ============
    private final List<String> attackDashFrames = List.of(
            "jp"+(char)92+"attack_dash_01", 
            "jp"+(char)92+"attack_dash_02", 
            "jp"+(char)92+"attack_dash_03", 
            "jp"+(char)92+"attack_dash_04",
            "jp"+(char)92+"attack_dash_05", 
            "jp"+(char)92+"attack_dash_06",
            "jp"+(char)92+"attack_dash_07"
    );
    
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
            // Frame 4: GOLPE2
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
    
    // ⭐ DASH: Desplazamiento por frame (ajusta según tu animación)
    // Estos valores definen cuánto se mueve en cada frame
    private final double[] dashDisplacementPerFrame = {
        2.5,   // Frame 1: inicio (25px)
        5.5,   // Frame 2: acelerando rápido (55px)
        7.0,   // Frame 3: PICO DE VELOCIDAD (70px)
        5.5,   // Frame 4: mantiene (55px)
        4.0,   // Frame 5: reduciendo (40px)
        2.5,   // Frame 6: frenando (25px)
        1.0    // Frame 7: parado (10px)
    };
    
    // ============ CONSTRUCTOR ============
    public AttackSystem(Character character) {
        this.character = character;
    }
    
    // ============ MÉTODOS PÚBLICOS ============
    
    /**
     * Procesar click izquierdo para ataques
     */
    public void onLeftClick() {
        if (isDashing) {
            return;
        }
        
        if (!isAttacking) {
            startAttack(1);
        } else if (currentAttackType == 1 && !attackClickedDuringCombo) {
            attackClickedDuringCombo = true;
            //System.out.println("✅ Combo encolado! Attack2 se activará automáticamente");
        }
    }
    
    /**
     * Intentar dash
     */
    public void attemptDash(boolean facingRight) {
        long currentTime = System.currentTimeMillis();
        long timeSinceLastDash = currentTime - lastDashTime;
        
        if (timeSinceLastDash < DASH_COOLDOWN_MS) {
            long remainingMs = DASH_COOLDOWN_MS - timeSinceLastDash;
            //System.out.println("⏱️ Dash en cooldown. Espera " + (remainingMs / 1000.0) + "s");
            return;
        }
        
        if (!isDashing && !isAttacking && character.isOnGround()) {
            startDash(facingRight);
        }
    }
    
    /**
     * Actualizar el sistema de ataque (llamar en update())
     */
    public void update() {
        // Por ahora vacío, el dash se maneja en Character.applyMovement()
    }
    
    /**
    * Callback cuando cambia el frame de animación
    */
    public void onFrameChange(int frameIndex, int totalFrames) {
//        System.out.println("📽️ Frame " + (frameIndex + 1) + "/" + totalFrames + " (Attack Type " + currentAttackType + ")");

        // ⭐ MOVIMIENTO ESPECIAL PARA DASH
        if (currentAttackType == 3 && isDashing) {
            handleDashMovement(frameIndex);
        }

        // Actualizar weapon hitbox
        updateWeaponHitbox(frameIndex);

        // ⭐ CRÍTICO: Terminar en último frame
        if (frameIndex >= totalFrames - 1) {
            //System.out.println("✅ Último frame alcanzado, terminando ataque tipo " + currentAttackType);
            endAttack();
        }
    }
    
    // ============ GETTERS ============
    
    public boolean isAttacking() { 
        return isAttacking; 
    }
    
    public boolean isDashing() { 
        return isDashing; 
    }
    
    public int getCurrentAttackType() { 
        return currentAttackType; 
    }
    
    public boolean isComboQueued() { 
        return attackClickedDuringCombo; 
    }
    
    public List<Point> getCurrentAttackHitbox() { 
        return currentAttackHitbox; 
    }
    
    public double getDashVelocityX() { 
        return dashVelocityX; 
    }
    
    public long getDashCooldownRemaining() {
        long elapsed = System.currentTimeMillis() - lastDashTime;
        return elapsed >= DASH_COOLDOWN_MS ? 0 : DASH_COOLDOWN_MS - elapsed;
    }
    
    public List<String> getFramesForType(int type) {
        return switch (type) {
            case 2 -> attack2Frames;
            case 3 -> attackDashFrames;
            default -> attack1Frames;
        };
    }
    
    public List<Point[]> getBodyHitboxesForType(int type) {
        return switch (type) {
            case 2 -> attack2BodyHitboxes;
            case 3 -> attackDashBodyHitboxes;
            default -> attack1BodyHitboxes;
        };
    }
    
    public List<List<Point>> getWeaponHitboxesForType(int type) {
        return switch (type) {
            case 2 -> attack2WeaponHitboxes;
            case 3 -> attackDashWeaponHitboxes;
            default -> attack1WeaponHitboxes;
        };
    }
    
    // ============ MÉTODOS PRIVADOS ============
    
    private void startAttack(int type) {
        isAttacking = true;
        currentAttackType = type;
        attackHitRegistered = false;
        
        switch (type) {
            case 1 -> {
                AudioManager.getInstance().playSound(SoundEffect.ATTACK_LIGHT);
//                System.out.println("⚔️ Attack 1 iniciado");
            }
            case 2 -> {
                AudioManager.getInstance().playSound(SoundEffect.ATTACK_HEAVY);
//                System.out.println("⚔️⚔️ Attack 2 (COMBO) iniciado");
            }
        }
        
        // Notificar al Character que cambie el estado
        character.onAttackStarted();
    }
    
    private void startDash(boolean facingRight) {
        isDashing = true;
        isAttacking = true;
        currentAttackType = 3;
        lastDashTime = System.currentTimeMillis();
        dashVelocityX = facingRight ? DASH_SPEED : -DASH_SPEED;
        attackClickedDuringCombo = false;
        lastDashFrame = 0; // ⭐ RESETEAR

        AudioManager.getInstance().playSound(SoundEffect.ATTACK_DASH);
//        System.out.println("🏃 Dash iniciado! Dirección: " + (facingRight ? "DERECHA →" : "IZQUIERDA ←"));

        character.onDashStarted();
    }
    
    // En AttackSystem.java, reemplaza el método endAttack() con esta versión corregida:

    private void endAttack() {
//        System.out.println("🔚 endAttack() llamado - Type: " + currentAttackType + 
//                          ", ComboQueued: " + attackClickedDuringCombo);

        // ⭐ VERIFICAR SI HAY COMBO ENCOLADO
        if (currentAttackType == 1 && attackClickedDuringCombo) {
            attackClickedDuringCombo = false;
            startAttack(2);
//            System.out.println("🔥 COMBO ACTIVADO! Attack 2 iniciado automáticamente");
            return;
        }

        // ⭐ SI ERA UN DASH, LIMPIARLO ANTES
        if (currentAttackType == 3) {
            endDash();
        }

        // Terminar normalmente
        isAttacking = false;
        currentAttackType = 1;
        currentAttackHitbox = null;
        attackHitRegistered = false;
        attackClickedDuringCombo = false;

//        System.out.println("✅ Ataque terminado, volviendo a IDLE");
        character.onAttackEnded();
    }

    // Y también actualiza endDash() para que NO repita la lógica:
    public void endDash() {
        isDashing = false;
        dashVelocityX = 0;
        lastDashFrame = 0;
//        System.out.println("🏃 Dash limpiado");
        // NO llamar a endAttack() aquí para evitar recursión
    }
    
    private void updateWeaponHitbox(int frameIndex) {
        // ⭐ OBTENER ANCHO CORRECTO SEGÚN EL TIPO DE ATAQUE
        final int SPRITE_WIDTH = getCurrentSpriteWidth();
        boolean facingRight = character.isFacingRight();

        // ⭐ OBTENER HITBOXES CON FLIP APLICADO
        List<List<Point>> weaponHitboxes = getWeaponHitboxesForType(
            currentAttackType, 
            facingRight, 
            SPRITE_WIDTH
        );

        if (frameIndex < 0 || frameIndex >= weaponHitboxes.size()) {
            currentAttackHitbox = null;
            return;
        }

        List<Point> weaponBox = weaponHitboxes.get(frameIndex);
        if (weaponBox == null || weaponBox.isEmpty()) {
            currentAttackHitbox = null;
            return;
        }

        // Convertir a coordenadas del mundo
        Point dir = character.getDireccion();
        currentAttackHitbox = weaponBox.stream()
            .map(p -> new Point(dir.x + p.x, dir.y + p.y))
            .toList();

        // Debug
        if (currentAttackHitbox != null && !currentAttackHitbox.isEmpty()) {
            System.out.println("⚔️ Weapon hitbox Frame " + frameIndex + 
                              ": " + currentAttackHitbox.size() + " puntos" +
                              " | Facing: " + (facingRight ? "RIGHT" : "LEFT"));
        }

        if (!attackHitRegistered) {
            checkAttackHit();
        }
    }
    
    private void checkAttackHit() {
        if (currentAttackHitbox == null || currentAttackHitbox.isEmpty()) return;

        SpatialGrid grid = SpatialGrid.getInstance();

        // Recorrer todos los puntos de la hitbox del arma
        for (Point p : currentAttackHitbox) {
            VisualRender entity = grid.getElement(p, 2);

            // Aquí detectarías colisiones con enemigos
            // if (entity instanceof Enemy enemy) {
            //     enemy.takeDamage(10);
            //     attackHitRegistered = true;
            //     System.out.println("¡Golpe conectado! Daño: 10");
            //     return;
            // }
        }
    }
    
    /**
    * Obtiene las weapon hitboxes, aplicando flip si es necesario
    * @param type Tipo de ataque (1, 2, 3)
    * @param facingRight Si el personaje mira a la derecha
    * @param spriteWidth Ancho del sprite para calcular el flip
    * @return Lista de hitboxes de arma (puede contener nulls)
    */
    public List<List<Point>> getWeaponHitboxesForType(int type, boolean facingRight, int spriteWidth) {
        // Obtener hitboxes originales (mirando a la izquierda)
        List<List<Point>> original = switch (type) {
            case 2 -> attack2WeaponHitboxes;
            case 3 -> attackDashWeaponHitboxes;
            default -> attack1WeaponHitboxes;
        };

        // Si mira a la izquierda, retornar las originales sin modificar
        if (!facingRight) {
            return original;
        }

        // ⭐ Si mira a la derecha, reflejar todas las hitboxes
        return original.stream()
            .map(frameHitbox -> {
                // Si este frame no tiene hitbox (null), mantenerlo null
                if (frameHitbox == null || frameHitbox.isEmpty()) {
                    return null;
                }

                // Reflejar cada punto de la hitbox
                // Fórmula: new_x = spriteWidth - 1 - original_x
                return frameHitbox.stream()
                    .map(p -> new Point(spriteWidth - 1 - p.x, p.y))
                    .toList();
            })
            .toList();
    }
    
    public List<List<Point>> getWeaponHitboxesRaw(int type) {
        return switch (type) {
            case 2 -> attack2WeaponHitboxes;
            case 3 -> attackDashWeaponHitboxes;
            default -> attack1WeaponHitboxes;
        };
    }
    
    /**
    * Obtiene el ancho real del sprite actual del personaje
    */
    private int getCurrentSpriteWidth() {
        return switch (currentAttackType) {
            case 2 -> ATTACK2_SPRITE_WIDTH;
            case 3 -> ATTACK_DASH_SPRITE_WIDTH;
            default -> ATTACK1_SPRITE_WIDTH;
        };
    }
    
    /**
    * Maneja el movimiento del dash sincronizado con los frames
    */
    private void handleDashMovement(int currentFrame) {
        if (currentFrame >= dashDisplacementPerFrame.length) {
            return;
        }

        // Obtener desplazamiento para este frame
        double displacement = dashDisplacementPerFrame[currentFrame];

        // Aplicar dirección
        double actualDisplacement = (dashVelocityX > 0) ? displacement : -displacement;

        // ⭐ NOTIFICAR AL CHARACTER QUE SE MUEVA
        character.applyDashMovement(actualDisplacement);

        //System.out.println("🏃 Dash Frame " + currentFrame + ": movimiento = " + actualDisplacement);

        lastDashFrame = currentFrame;
    }
    public boolean isDashMovingByFrames() {
        return currentAttackType == 3 && isDashing;
    }
}