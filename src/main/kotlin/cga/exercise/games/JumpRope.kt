package cga.exercise.games

import cga.exercise.components.geometry.Renderable
import cga.exercise.game.Player
import cga.framework.ModelLoader.loadModel
import org.joml.Math
import org.joml.Vector3f

class JumpRope {
    private var isGameRunning = false

    private var ropeRotation = Math.toRadians(360f)
    private var ropeSpeed = 3.5f

    private var playerOneScore = 0
    private var playerTwoScore = 0

    /**
     * Lineal als "Seil"
     * Model: Ruler by Poly by Google [CC-BY] via Poly Pizza
     */
    private val rope: Renderable

    /**
     * Spielraum
     * Model: Jason's Isometric Room by Jason Toff [CC-BY] via Poly Pizza
     */

    private val room: Renderable

    init {
        rope = loadModel("assets/Lineal/ruler.obj", Math.toRadians(-90f), Math.toRadians(90f), 0f)
            ?: throw IllegalArgumentException("Could not load the ruler")
        rope.preTranslate(Vector3f(0f, 0.15f, 15f))
        rope.rotate(0f, Math.toRadians(90f), Math.toRadians(90f))
        rope.scale(Vector3f(0.5f))

        room = loadModel("assets/JumpRope_Raum/model.obj", Math.toRadians(-90f), Math.toRadians(90f), Math.toRadians(-90f))
            ?: throw IllegalArgumentException("Could not load the jump rope room")
        room.preTranslate(Vector3f(0f, 3f, 0f))
    }

    fun getObjects(): Array<Renderable> {
        return arrayOf(rope, room)
    }

    fun startGame() {
        isGameRunning = true
    }

    fun update(dt: Float, players: Array<Player>) {
        if (!isGameRunning) return

        rope.rotateAroundPoint(dt * ropeSpeed, 0f, 0f, Vector3f(0f, 2.5f, 15f))
        ropeRotation -= dt * ropeSpeed
        if (Math.toDegrees(ropeRotation.toDouble()) <= 2) {
            if (players[0].getHeight() <= 0.5f) {
                System.out.println("You Lost! Final Score: " + playerOneScore)
                isGameRunning = false
                playerOneScore = 0
                playerTwoScore = 0
                resetRope()
            } else {
                playerOneScore++
            }
            ropeSpeed = Math.min(9.5f, 3.5f + (playerOneScore * 0.2f))
            ropeRotation = Math.toRadians(360f)
        }
    }

    private fun resetRope() {
        rope.setRotation(0f, Math.toRadians(90f), Math.toRadians(90f))
        rope.scale(Vector3f(0.5f))
    }
}