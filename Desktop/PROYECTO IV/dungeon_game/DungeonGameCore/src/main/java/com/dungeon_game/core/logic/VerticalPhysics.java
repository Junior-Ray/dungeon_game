/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.dungeon_game.core.logic;

/**
 *
 * @author GABRIEL SALGADO
 */
public class VerticalPhysics {

    // gravedad por tick (no por segundo)
    private final double gravity;

    // velocidad máxima de caída
    private final double maxFallSpeed;

    public VerticalPhysics(double gravity, double maxFallSpeed) {
        this.gravity = gravity;
        this.maxFallSpeed = maxFallSpeed;
    }

    /**
     * Aplica un paso de física vertical (t = 1)
     */
    public double apply(double currentVelocity) {

        double newVelocity = currentVelocity + gravity;

        // clamp de caída
        if (newVelocity > maxFallSpeed) {
            newVelocity = maxFallSpeed;
        }

        return newVelocity;
    }
}
