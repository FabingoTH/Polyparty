package cga.exercise.game.scene

import cga.exercise.components.camera.Aspectratio
import cga.exercise.components.camera.Camera
import cga.exercise.components.camera.OrbitCamera
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


    private val playerPOV: Renderable


    private val flowerSetup: Renderable
    private val originalFlower: Renderable

    private val rakeSetup: Renderable
    private val originalRake: Renderable

    private val lilySetup: Renderable
    private val originalLily: Renderable


    private val objList = mutableListOf<Renderable>()
    private val camera: Camera
    private val devCamera: OrbitCamera

    private var memoStage: MemoStage
    private var pointsP1: Int = 0
    private var pointsP2: Int = 0

    private var oldMouseX = 0.0
    private var oldMouseY = 0.0
    private var firstMouseMove = true

    init {

        // for easier development: target for orbit cam
        playerPOV = loadModel("assets/project_models/Eichhoernchen/squirrel.obj", 0f, 0f, 0f)
            ?: throw IllegalArgumentException("Could not load the player")
        objList.add(playerPOV)

        /**
         * object scenes for guessing
         */

        flowerSetup = loadModel("assets/project_models/MemorizeGameScene/FLOWERSCENE.obj", 0f, 0f, 0f)
            ?: throw IllegalArgumentException("Could not load the flower scene")
        objList.add(flowerSetup)

        originalFlower = loadModel("assets/project_models/MemorizeGameScene/Blumen/orig/flower.obj", 0f, 0f, 0f)
            ?: throw IllegalArgumentException("Could not load the flower")
        objList.add(this.originalFlower)
        originalFlower.preTranslate(Vector3f(-2.08f, 0.78f, 0f))
        originalFlower.scale(Vector3f(0.18f))


        rakeSetup = loadModel("assets/project_models/MemorizeGameScene/RAKESCENE.obj", 0f, 0f, 0f)
            ?: throw IllegalArgumentException("Could not load the rake scene")
        objList.add(rakeSetup)
        rakeSetup.translate(Vector3f(3f, 0f, 0f))

        originalRake = loadModel("assets/project_models/MemorizeGameScene/Hake/orig/rake.obj", 0f, 0f, 0f)
            ?: throw IllegalArgumentException("Could not load the rake")
        objList.add(this.originalRake)


        lilySetup = loadModel("assets/project_models/MemorizeGameScene/LILYSCENE.obj", 0f, 0f, 0f)
            ?: throw IllegalArgumentException("Could not load the lily scene")
        objList.add(this.lilySetup)
        lilySetup.translate(Vector3f(6f, 0f, 0f))


        originalLily = loadModel("assets/project_models/MemorizeGameScene/Wasserlilie/orig/lily.obj", 0f, 0f, 0f)
            ?: throw IllegalArgumentException("Could not load the lily")
        objList.add(this.originalLily)

        /**
         * setup static camera
         */

        camera = Camera(
            Aspectratio.custom(window.framebufferWidth, window.framebufferHeight),
            Math.toRadians(90.0f),
            0.1f,
            100.0f
        )

        // game cam settings
        camera.lookAt(Vector3f(1f, 0f, 0f))
        camera.fov = 0.12f
        camera.translate(Vector3f(-0.05f, 0.7f, 8f))

        /**
         * setup developer Camera (for easier object placement)
         */

        devCamera = OrbitCamera(playerPOV)
        memoStage = MemoStage.FLOWER // initial stage (beim rendern/spawnen)

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

        devCamera.bind(staticShader)
        devCamera.updateCameraPosition()

        for (obj in objList) {
            obj.render(staticShader)
        }


    }

    override fun update(dt: Float, t: Float) {

        val moveMul = 3.0f
        val rotateMul = 1f * Math.PI.toFloat()

        /**
         * (Steuerung analog zu quadratischem Layout des Games)
         * Steuerung Player 1: QWAS
         * Steuerung Player 2: UIJK
         * SPACE ends game
         */

        if (window.getKeyState(GLFW.GLFW_KEY_SPACE)) {
            window.changeScene(GameType.LOBBY)
        }

        // STAGE 1
        // initial position: flower
        // minuspunkt bei falscher Antwort (um spammen zu punishen)
        // pluspunkt bei eichtiger antwort + teleportation zum nächsten spiel
        if (memoStage == MemoStage.FLOWER) {
            if (window.getKeyState(GLFW.GLFW_KEY_W) || window.getKeyState(GLFW.GLFW_KEY_S) || window.getKeyState(GLFW.GLFW_KEY_A)) {
                pointsP1--
            }
            if (window.getKeyState(GLFW.GLFW_KEY_U) || window.getKeyState(GLFW.GLFW_KEY_J) || window.getKeyState(GLFW.GLFW_KEY_K)) {
                pointsP2--
            }
            if (window.getKeyState(GLFW.GLFW_KEY_Q)) {
                pointsP1++
                camera.preTranslate(Vector3f(3f, 0f, 0f))
                memoStage = MemoStage.RAKE
            }
            if (window.getKeyState(GLFW.GLFW_KEY_I)) {
                pointsP2++
                camera.preTranslate(Vector3f(3f, 0f, 0f))
                memoStage = MemoStage.RAKE
            }
        }

        //STAGE 2
        if (memoStage == MemoStage.RAKE) {
            if (window.getKeyState(GLFW.GLFW_KEY_Q) || window.getKeyState(GLFW.GLFW_KEY_W) || window.getKeyState(GLFW.GLFW_KEY_S)) {
                pointsP1--
            }
            if (window.getKeyState(GLFW.GLFW_KEY_I) || window.getKeyState(GLFW.GLFW_KEY_J) || window.getKeyState(GLFW.GLFW_KEY_K)) {
                pointsP2--
            }
            if (window.getKeyState(GLFW.GLFW_KEY_A)) {
                pointsP1++
                camera.preTranslate(Vector3f(3f, 0f, 0f))
                memoStage = MemoStage.LILY
            }
            if (window.getKeyState(GLFW.GLFW_KEY_U)) {
                pointsP2++
                camera.preTranslate(Vector3f(3f, 0f, 0f))
                memoStage = MemoStage.LILY
            }
        }

        // STAGE 3
        if (memoStage == MemoStage.LILY) {
            if (window.getKeyState(GLFW.GLFW_KEY_Q) || window.getKeyState(GLFW.GLFW_KEY_W) || window.getKeyState(GLFW.GLFW_KEY_A)) {
                pointsP1--
            }
            if (window.getKeyState(GLFW.GLFW_KEY_U) || window.getKeyState(GLFW.GLFW_KEY_I) || window.getKeyState(GLFW.GLFW_KEY_K)) {
                pointsP2--
            }
            if (window.getKeyState(GLFW.GLFW_KEY_S)) {
                pointsP1++
                camera.preTranslate(Vector3f(3f, 0f, 0f))
                memoStage = MemoStage.WIN
            }
            if (window.getKeyState(GLFW.GLFW_KEY_J)) {
                pointsP2++
                camera.preTranslate(Vector3f(3f, 0f, 0f))
                memoStage = MemoStage.WIN
            }
        }

        if (memoStage == MemoStage.WIN) {

            if (pointsP1 > pointsP2) // translate to win screen player 1
            else 0 // translate to winscreen player 2


            //  can go back to lobby with space as instructed
        }


        // dev cam steuerung

        if (window.getKeyState(GLFW.GLFW_KEY_W)) {
            playerPOV.translate(Vector3f(0.0f, 0.0f, -dt * moveMul))


        }
        if (window.getKeyState(GLFW.GLFW_KEY_S)) {
            playerPOV.translate(Vector3f(0.0f, 0.0f, dt * moveMul))
        }

        if (window.getKeyState(GLFW.GLFW_KEY_A)) {
            playerPOV.rotate(0.0f, dt * rotateMul, 0.0f)
        }

        if (window.getKeyState(GLFW.GLFW_KEY_D)) {
            playerPOV.rotate(0.0f, -dt * rotateMul, 0.0f)
        }

    }

    override fun onKey(key: Int, scancode: Int, action: Int, mode: Int) {}
    override fun onMouseMove(xpos: Double, ypos: Double) {

        // dev cam
        val azimuthRate: Float = 0.1f
        val elevationRate: Float = 0.025f

        if (firstMouseMove) {
            val yawAngle = (xpos - oldMouseX).toFloat() * azimuthRate
            val pitchAngle = (ypos - oldMouseY).toFloat() * elevationRate

            // Ändere die elevation und azimuth Winkel der OrbitCamera
            devCamera.azimuth -= yawAngle

            // Begrenze die elevation, um nicht unter -45 Grad zu gehen
            val newElevation = devCamera.elevation - pitchAngle
            devCamera.elevation = newElevation.coerceIn(10.0f, 70.0f)

            // Speichere die Mausposition für den nächsten Aufruf
            oldMouseX = xpos
            oldMouseY = ypos
        }

    }

    override fun onMouseScroll(xoffset: Double, yoffset: Double) {
        val zoom = devCamera.distance + Math.toRadians(yoffset.toFloat()) * -10.0f
        devCamera.distance = zoom.coerceAtMost(7.0f) // Max Zoom Out
    }

    override fun changeScene(newScene: GameType) {}
}