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
    private val staticShader: ShaderProgram = ShaderProgram("assets/shaders/tron_vert.glsl", "assets/shaders/tron_frag.glsl")

    private val groundMaterial : Material
    private val ground : Renderable
    private val groundColor: Vector3f
    init {

        val groundDiff = Texture2D("assets/textures/stone_floor/tiles.png", true)
        groundDiff.setTexParams(GL11.GL_REPEAT, GL11.GL_REPEAT, GL11.GL_LINEAR_MIPMAP_LINEAR, GL11.GL_LINEAR)
        val groundSpecular = Texture2D("assets/textures/stone_floor/tiles.png", true)
        groundSpecular.setTexParams(GL11.GL_REPEAT, GL11.GL_REPEAT, GL11.GL_LINEAR_MIPMAP_LINEAR, GL11.GL_LINEAR)
        val groundEmit = Texture2D("assets/textures/stone_floor/tiles.png", true)
        groundEmit.setTexParams(GL11.GL_REPEAT, GL11.GL_REPEAT, GL11.GL_LINEAR_MIPMAP_LINEAR, GL11.GL_LINEAR)
        groundMaterial = Material(groundDiff, groundEmit, groundSpecular, 60f, Vector2f(64.0f, 64.0f))
        groundColor = Vector3f(0.8f)

        //load an object and create a mesh
        val gres = OBJLoader.loadOBJ("assets/models/ground.obj")
        //Create the mesh
        val stride = 8 * 4
        val atr1 = VertexAttribute(3, GL11.GL_FLOAT, stride, 0)     //position attribute
        val atr2 = VertexAttribute(2, GL11.GL_FLOAT, stride, 3 * 4) //texture coordinate attribute
        val atr3 = VertexAttribute(3, GL11.GL_FLOAT, stride, 5 * 4) //normal attribute
        val vertexAttributes = arrayOf(atr1, atr2, atr3)
        //Create renderable
        ground = Renderable()
        for (m in gres.objects[0].meshes) {
            val mesh = Mesh(m.vertexData, m.indexData, vertexAttributes, groundMaterial)
            ground.meshes.add(mesh)
        }
    }

    abstract val window: GameWindow
    open fun render(dt: Float, t: Float) {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)
        staticShader.use()
        staticShader.setUniform("shadingColor", groundColor)
        ground.render(staticShader)
    }
    abstract fun update(dt: Float, t: Float)
    abstract fun onKey(key: Int, scancode: Int, action: Int, mode: Int)
    abstract fun onMouseMove(xpos: Double, ypos: Double)
    abstract fun onMouseScroll(xoffset: Double, yoffset: Double)
    abstract fun changeScene(newScene: GameType)
}