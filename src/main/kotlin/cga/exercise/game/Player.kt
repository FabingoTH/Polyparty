package cga.exercise.game

import cga.exercise.components.geometry.Renderable
import cga.framework.GameWindow
import org.joml.Vector3f
import org.lwjgl.glfw.GLFW.*

data class ControlSchema(val forward: Int, val left: Int, val right: Int, val back: Int, val jump: Int)

fun controlSchemeByPlayerNumber(playerNumber: Int): ControlSchema {
    return when (playerNumber) {
        1 -> ControlSchema(GLFW_KEY_W, GLFW_KEY_A, GLFW_KEY_D, GLFW_KEY_S, GLFW_KEY_SPACE)
        2 -> ControlSchema(GLFW_KEY_I, GLFW_KEY_J, GLFW_KEY_L, GLFW_KEY_K, GLFW_KEY_RIGHT_SHIFT)
        else -> throw Error("Invalid Player Number - Only 1 or 2 is allowed")
    }
}

class Player(private val playerNumber: Int, val obj: Renderable) {
    private val controls = controlSchemeByPlayerNumber(playerNumber)

    private val movementMul = 15f
    private val rotateMul = 1.5f * Math.PI.toFloat()

    // Jump Animation Variabeln
    private val jumpHeight = 0.05f
    private val jumpFrequency = 25.0f // Anzahl der Hüpfbewegungen pro Sekunde
    private var jumpPhase = 0.0f // Aktuelle Phase der Hüpfanimation

    private var jumpVelocity = 0f
    private var isJumping = false
    private var currentHeight = 0f

    private val gravityMul = 0.6f

    fun update(deltaTime: Float, window: GameWindow, activeGame: GameType) {

        /**
         * Wenn kein Minispiel aktiv ist:
         * Steuerung Charakter 1: WASD - Jump: Space
         * Steuerung Charakter 2: IJKL(?) - Jump: Right Shift
         *
         * Wenn Jump Rope aktiv ist:
         * Steuerung Charakter 1: Space: Jump
         * Steuerung Charakter 2: Right Shift: Jump
         *
         * TODO: coolere Laufanimation.
         */

        when (activeGame) {
            GameType.LOBBY -> useDefaultControls(deltaTime, window)
            GameType.JUMP_ROPE -> useJumpRopeControls(deltaTime, window)
            GameType.MEMORIZE -> TODO()
            GameType.RACING -> TODO()
        }



        calculateJump(deltaTime)
    }

    private fun useDefaultControls(deltaTime: Float, window: GameWindow) {
        if (window.getKeyState(controls.forward)) {
            obj.translate(Vector3f(0.0f, 0.0f, -deltaTime * movementMul))

            if (!isJumping) {
                // Hüpfanimation
                jumpPhase += deltaTime * jumpFrequency
                val verticalOffset = jumpHeight * org.joml.Math.sin(jumpPhase)
                obj.translate(Vector3f(0.0f, verticalOffset, 0.0f))
            }
        }
        if (window.getKeyState(controls.back)) {
            obj.translate(Vector3f(0.0f, 0.0f, deltaTime * movementMul))

            if (!isJumping) {
                // Hüpfanimation
                jumpPhase += deltaTime * jumpFrequency
                val verticalOffset = jumpHeight * org.joml.Math.sin(jumpPhase)
                obj.translate(Vector3f(0.0f, verticalOffset, 0.0f))
            }
        }

        // Setzt den Character wieder direkt auf den Boden
        if (!window.getKeyState(controls.forward) && !window.getKeyState(controls.back) && !isJumping) {
            val currentPosition = obj.getWorldPosition()
            obj.translate(Vector3f(0.0f, -currentPosition.y, 0.0f))
            jumpPhase = 0.0f
        }
        if (window.getKeyState(controls.left)) {
            obj.rotate(0.0f, deltaTime * rotateMul, 0.0f)
        }
        if (window.getKeyState(controls.right)) {
            obj.rotate(0.0f, -deltaTime * rotateMul, 0.0f)
        }
        if (window.getKeyState(controls.jump)) {
            if (!isJumping) {
                jumpVelocity = 15f
                isJumping = true
            }
        }
    }

    private fun useJumpRopeControls(deltaTime: Float, window: GameWindow) {
        if (window.getKeyState(controls.jump)) {
            if (!isJumping) {
                jumpVelocity = 15f
                isJumping = true
            }
        }
    }

    private fun calculateJump(deltaTime: Float) {
        if (!isJumping) return
        val newHeight = currentHeight + deltaTime * jumpVelocity
        obj.translate(Vector3f(0f, deltaTime * jumpVelocity, 0f))
        jumpVelocity -= gravityMul
        currentHeight = org.joml.Math.max(0f, newHeight)
        if (currentHeight == 0f) {
            jumpVelocity = 0f
            isJumping = false
        }
    }

    fun getHeight(): Float {
        return currentHeight
    }

    fun resetJump() {
        jumpVelocity = 0f
        currentHeight = 0f
        isJumping = false
    }
}