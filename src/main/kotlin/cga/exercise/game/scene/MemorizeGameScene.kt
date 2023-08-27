package cga.exercise.game.scene

import cga.exercise.components.camera.Aspectratio
import cga.exercise.components.camera.Camera
import cga.exercise.components.camera.OrbitCamera
import cga.exercise.components.geometry.*
import cga.exercise.components.texture.Texture2D
import cga.exercise.game.GameType
import cga.framework.GLError
import cga.framework.GameWindow
import cga.framework.ModelLoader.loadModel
import cga.framework.OBJLoader
import org.joml.Math
import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL11

class MemorizeGameScene(override val window: GameWindow) : AScene() {


    private val groundMaterial: Material
    private val ground: Renderable
    private val groundColor: Vector3f

    private val playerPOV: Renderable


    private val flowerSetup: Renderable
    private val originalFlower: Renderable

    private val rakeSetup: Renderable
    private val originalRake: Renderable

    private val lilySetup: Renderable
    private val originalLily: Renderable


    private val win1setup: Renderable
    private val win2setup: Renderable

    private val objList = mutableListOf<Renderable>()
    private var camera: Camera
    private val devCamera: OrbitCamera

    private var memoStage: MemoStage
    private var pointsP1: Int = 0
    private var pointsP2: Int = 0

    private var oldMouseX = 0.0
    private var oldMouseY = 0.0
    private var firstMouseMove = true

    init {

        // ground init
        val groundDiff = Texture2D("assets/textures/stone_floor/Stone_Tiles_002_COLOR.jpg", true)
        groundDiff.setTexParams(GL11.GL_REPEAT, GL11.GL_REPEAT, GL11.GL_LINEAR_MIPMAP_LINEAR, GL11.GL_LINEAR)
        val groundSpecular = Texture2D("assets/textures/stone_floor/Stone_Tiles_002_DISP.png", true)
        groundSpecular.setTexParams(GL11.GL_REPEAT, GL11.GL_REPEAT, GL11.GL_LINEAR_MIPMAP_LINEAR, GL11.GL_LINEAR)
        val groundEmit = Texture2D("assets/textures/stone_floor/Stone_Tiles_002_OCC.jpg", true)
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

        // for easier development: target for orbit cam
        playerPOV = loadModel("assets/project_models/Eichhoernchen/squirrel.obj", 0f, 0f, 0f)
            ?: throw IllegalArgumentException("Could not load the player")
        objList.add(playerPOV)
        playerPOV.translate(Vector3f(-8f, 0f, 5f))

        /**
         * object scenes for guessing
         */

        flowerSetup = loadModel("assets/project_models/MemorizeGameScene/FLOWERSCENE.obj", 0f, 0f, 0f)
            ?: throw IllegalArgumentException("Could not load the flower scene")
        objList.add(flowerSetup)
        flowerSetup.translate(Vector3f(-15f, 0f, 0f))

        originalFlower = loadModel("assets/project_models/MemorizeGameScene/Blumen/orig/flower.obj", 0f, 0f, 0f)
            ?: throw IllegalArgumentException("Could not load the flower")
        objList.add(originalFlower)
        originalFlower.preTranslate(Vector3f(-17.08f, 0.78f, 0f))
        originalFlower.scale(Vector3f(0.18f))


        rakeSetup = loadModel("assets/project_models/MemorizeGameScene/RAKESCENE.obj", 0f, 0f, 0f)
            ?: throw IllegalArgumentException("Could not load the rake scene")
        objList.add(rakeSetup)
        rakeSetup.translate(Vector3f(-5f, 0f, 0f))

        originalRake = loadModel("assets/project_models/MemorizeGameScene/Hake/orig/rake.obj", 0f, 0f, 0f)
            ?: throw IllegalArgumentException("Could not load the rake")
        objList.add(originalRake)
        originalRake.rotate(0f, Math.toRadians(-92.0f), 0f)
        originalRake.scale(Vector3f(0.05f))
        originalRake.preTranslate(Vector3f(-7.35f, 0.85f, -0.053f))


        lilySetup = loadModel("assets/project_models/MemorizeGameScene/LILYSCENE.obj", 0f, 0f, 0f)
            ?: throw IllegalArgumentException("Could not load the lily scene")
        objList.add(lilySetup)
        lilySetup.translate(Vector3f(5f, 0f, 0f))

        originalLily = loadModel("assets/project_models/MemorizeGameScene/Wasserlilie/orig/lily.obj", 0f, 0f, 0f)
            ?: throw IllegalArgumentException("Could not load the lily")
        objList.add(originalLily)
        originalLily.scale(Vector3f(0.1f))
        originalLily.preTranslate(Vector3f(2.85f, 0.85f, -0.05f))


        /**
         * setup win screens
         */

        win1setup = loadModel("assets/project_models/MemorizeGameScene/WIN1SCENE.obj", 0f, 0f, 0f)
            ?: throw IllegalArgumentException("Could not load the win screen")
        objList.add(win1setup)
        win1setup.preTranslate(Vector3f(15f, 0f, 0f))


        win2setup = loadModel("assets/project_models/MemorizeGameScene/WIN2SCENE.obj", 0f, 0f, 0f)
            ?: throw IllegalArgumentException("Could not load the win screen")
        objList.add(win2setup)
        win2setup.preTranslate(Vector3f(25f, 0f, 0f))


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
        camera.lookAt(Vector3f(1f, 0f, 0f)) // anders
        camera.fov = 0.12f
        //camera.translate(Vector3f(-0.05f, 0.7f, 8f))
        //camera.preTranslate(Vector3f(-24f, 0.7f, -0.05f))
        camera.setPosition(Vector3f(-24f, 0.7f, -0.05f))
        //camera.lookAt(Vector3f(0f, 0.7f, 1f)) // +x vorwärts, +y geht hoch, +z nach rechts

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

        staticShader.use()
        staticShader.setUniform("shadingColor", groundColor)
        ground.render(staticShader)

        devCamera.bind(staticShader)
        devCamera.updateCameraPosition()
        //camera.bind(staticShader)


        for (obj in objList) {
            obj.render(staticShader)
        }


    }

    override fun update(dt: Float, t: Float) {

        val moveMul = 15.0f
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
        // pluspunkt bei richtiger antwort + teleportation zum nächsten spiel
        // die else ifs verhindern ein punkte increasen/kamera verschieben wenn taste länger als
        // 1 framelänge gedrückt wird :/

        if (memoStage == MemoStage.FLOWER) {

            if (window.getKeyState(GLFW.GLFW_KEY_Q)) {
                pointsP1 = 1
                camera.setPosition(Vector3f(10f, 0f, 0f))
                memoStage = MemoStage.RAKE
            }
            if (window.getKeyState(GLFW.GLFW_KEY_I)) {
                pointsP2 = 1
                camera.setPosition(Vector3f(10f, 0f, 0f))
                memoStage = MemoStage.RAKE
            }
        }

        //STAGE 2
        if (memoStage == MemoStage.RAKE) {

            if (window.getKeyState(GLFW.GLFW_KEY_A)) {
                pointsP1 = if (pointsP1 == 0) 1 else 2
                camera.setPosition(Vector3f(10f, 0f, 0f))
                memoStage = MemoStage.LILY
            }
            if (window.getKeyState(GLFW.GLFW_KEY_U)) {
                pointsP2 = if (pointsP2 == 0) 1 else 2
                camera.setPosition(Vector3f(10f, 0f, 0f))
                memoStage = MemoStage.LILY
            }
        }

        // STAGE 3
        if (memoStage == MemoStage.LILY) {

            if (window.getKeyState(GLFW.GLFW_KEY_S)) {
                pointsP1 = if (pointsP1 == 0) 1 else if (pointsP1 == 1) 2 else 3
                if (pointsP1 > pointsP2) {
                    camera.setPosition(Vector3f(10f, 0f, 0f))

                } else if (pointsP1 < pointsP2) {
                    camera.setPosition(Vector3f(10f, 0f, 0f))

                }
                memoStage = MemoStage.WIN
            }
            if (window.getKeyState(GLFW.GLFW_KEY_J)) {
                pointsP2 = if (pointsP2 == 0) 1 else if (pointsP2 == 1) 2 else 3
                if (pointsP1 > pointsP2) {
                    camera.setPosition(Vector3f(10f, 0f, 0f))

                } else if (pointsP1 < pointsP2) {
                    camera.setPosition(Vector3f(10f, 0f, 0f))
                }
                memoStage = MemoStage.WIN
            }

        }

        if (memoStage == MemoStage.WIN) {
            val string =
                if (pointsP1 > pointsP2) "Player 1 won with $pointsP1 points!" else "Player 2 won with $pointsP2 points!"
            println(string)
            memoStage = MemoStage.DONE
        }


        // Memo stage done:
        // do nothing. stand still. let players end game with SPACE as instructed


        // dev cam steuerung

        if (window.getKeyState(GLFW.GLFW_KEY_W)) {
            playerPOV.translate(Vector3f(0.0f, 0.0f, -dt * moveMul))
        }
        if (window.getKeyState(GLFW.GLFW_KEY_A)) {
            playerPOV.rotate(0.0f, dt * rotateMul, 0.0f)
        }
        if (window.getKeyState(GLFW.GLFW_KEY_S)) {
            playerPOV.translate(Vector3f(0.0f, 0.0f, dt * moveMul))
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