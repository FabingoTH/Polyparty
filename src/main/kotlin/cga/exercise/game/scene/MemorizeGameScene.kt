package cga.exercise.game.scene

import cga.exercise.components.camera.Aspectratio
import cga.exercise.components.camera.Camera
import cga.exercise.components.geometry.Renderable
import cga.exercise.game.GameType
import cga.framework.GLError
import cga.framework.GameWindow
import cga.framework.ModelLoader.loadModel
import org.joml.Math
import org.joml.Vector3f
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL11

class MemorizeGameScene(override val window: GameWindow) : AScene() {

    private val partGarden: Renderable
    private val bg: Renderable
    private val objList = mutableListOf<Renderable>()
    private val camera: Camera

    init {

        partGarden = loadModel("assets/project_models/MemorizeGameScene/garden.obj", 0f, 0f, 0f)
            ?: throw IllegalArgumentException("Could not load the garden")
        objList.add(partGarden)

        // braucht am besten noch hintergrund
        // bg nicht sichtbar??
        bg = loadModel("assets/project_models/MemorizeGameScene/plane.obj", 0f, 0f, 0f)
            ?: throw IllegalArgumentException("Could not load the garden")
        //bg.translate(Vector3f(0f, 5f, 0f))
        objList.add(bg)

        //setup static camera
        camera = Camera(
            Aspectratio.custom(window.framebufferWidth, window.framebufferHeight),
            Math.toRadians(90.0f),
            0.1f,
            100.0f
        )

        // game cam settings
        camera.lookAt(Vector3f(-3f, 0f, 0f))
        camera.fov = 0.12f
        camera.translate(Vector3f(0.04f, 0.57f, 5f))

        //initial opengl state
        GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f); GLError.checkThrow()
        GL11.glEnable(GL11.GL_CULL_FACE); GLError.checkThrow()
        GL11.glFrontFace(GL11.GL_CCW); GLError.checkThrow()
        GL11.glCullFace(GL11.GL_BACK); GLError.checkThrow()
        GL11.glEnable(GL11.GL_DEPTH_TEST); GLError.checkThrow()
        GL11.glDepthFunc(GL11.GL_LESS); GLError.checkThrow()
    }


    override fun render(dt: Float, t: Float) {

        super.render(dt, t)

        camera.bind(staticShader)

        for (obj in objList) {
            obj.render(staticShader)
        }


    }

    override fun update(dt: Float, t: Float) {

        /**
         * (Steuerung analog zu quadratischem Layout des Games)
         * Steuerung Player 1: QWAS
         * Steuerung Player 2: UIJK
         * SPACE ends game
         */

        if (window.getKeyState(GLFW.GLFW_KEY_SPACE)) {
            window.changeScene(GameType.LOBBY)
        }


    }

    override fun onKey(key: Int, scancode: Int, action: Int, mode: Int) {}
    override fun onMouseMove(xpos: Double, ypos: Double) {}
    override fun onMouseScroll(xoffset: Double, yoffset: Double) {}
    override fun changeScene(newScene: GameType) {}
}