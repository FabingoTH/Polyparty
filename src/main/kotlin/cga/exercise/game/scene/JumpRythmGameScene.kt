package cga.exercise.game.scene

import cga.exercise.game.GameType
import cga.framework.GameWindow

class JumpRythmGameScene(override val window: GameWindow) : AScene() {
    override fun render(dt: Float, t: Float) {}
    override fun update(dt: Float, t: Float) {}
    override fun onKey(key: Int, scancode: Int, action: Int, mode: Int) {}
    override fun onMouseMove(xpos: Double, ypos: Double) {}
    override fun onMouseScroll(xoffset: Double, yoffset: Double) {}
    override fun changeScene(newScene: GameType) {}
}