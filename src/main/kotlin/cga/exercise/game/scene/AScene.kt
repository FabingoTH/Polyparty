package cga.exercise.game.scene

import cga.exercise.components.geometry.Material
import cga.exercise.components.geometry.Mesh
import cga.exercise.components.geometry.Renderable
import cga.exercise.components.geometry.VertexAttribute
import cga.exercise.components.shader.ShaderProgram
import cga.exercise.components.texture.Texture2D
import cga.exercise.game.GameType
import cga.framework.GameWindow
import cga.framework.OBJLoader
import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.opengl.GL11

abstract class AScene {
    val staticShader: ShaderProgram = ShaderProgram("assets/shaders/tron_vert.glsl", "assets/shaders/tron_frag.glsl")

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