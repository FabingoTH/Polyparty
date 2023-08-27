package cga.exercise.game.scene

import cga.exercise.components.shader.ShaderProgram
import cga.exercise.game.GameType
import cga.framework.GameWindow
import org.lwjgl.opengl.GL11

abstract class AScene {
    val staticShader: ShaderProgram = ShaderProgram("assets/shaders/cel_vert.glsl", "assets/shaders/cel_frag.glsl")

    init {

    }

    abstract val window: GameWindow
    open fun render(dt: Float, t: Float) {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)
    }

    abstract fun update(dt: Float, t: Float)
    abstract fun onKey(key: Int, scancode: Int, action: Int, mode: Int)
    abstract fun onMouseMove(xpos: Double, ypos: Double)
    abstract fun onMouseScroll(xoffset: Double, yoffset: Double)
    abstract fun changeScene(newScene: GameType)
}