package cga.exercise.game.scene

import cga.exercise.components.camera.Camera
import cga.exercise.components.geometry.Renderable
import cga.exercise.components.gui.GuiElement
import cga.exercise.components.light.PointLight
import cga.exercise.components.shader.GuiShader
import cga.exercise.components.texture.Texture2D
import cga.exercise.game.GameType
import cga.exercise.game.Player
import cga.framework.GLError
import cga.framework.GameWindow
import cga.framework.ModelLoader.loadModel
import org.joml.Math
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.glfw.GLFW.GLFW_KEY_T
import org.lwjgl.opengl.GL11.*

data class JumpRopePlayer(val player: Player, var score: Int = 0, var isAlive: Boolean = true)

class JumpRythmGameScene(override val window: GameWindow) : AScene() {
    private val guiShader = GuiShader()

    private val pointLightList = mutableListOf<PointLight>()

    // CAMERA
    private val camera: Camera

    private val objList: MutableList<Renderable> = mutableListOf()
    private val guiList: MutableList<GuiElement> = mutableListOf()

    private val mainChar: JumpRopePlayer
    private val secChar: JumpRopePlayer

    private val squirrel: Renderable
    private val snail: Renderable

    private val level: Renderable
    private val rope: Renderable
    private val ropeMatrix: Matrix4f

    private val skybox: Renderable
    private val skyColor: Vector3f

    // jump rope variables
    private var isGameRunning = false

    private var ropeRotation = Math.toRadians(360f)
    private var ropeSpeed = 3.5f

    //scene setup
    init {

        squirrel = loadModel(
            "assets/project_models/Eichhoernchen/squirrel.obj", 0f, Math.toRadians(-22f), 0f
        ) ?: throw IllegalArgumentException("Could not load the squirrel")
        squirrel.scale(Vector3f(0.7f))
        squirrel.translate(Vector3f(4f, 0f, 4.25f))
        squirrel.rotate(0f, Math.toRadians(-90f), 0f)

        snail = loadModel("assets/project_models/Schnecke/Mesh_Snail.obj", 0f, Math.toRadians(90f), 0f)
            ?: throw IllegalArgumentException("Could not load the snail")
        snail.scale(Vector3f(0.8f))
        snail.translate(Vector3f(1.5f, 0f, 3.9f))

        level = loadModel("assets/project_models/JumpRope_Level/level.obj", 0f, 0f, 0f)
            ?: throw IllegalArgumentException("Could not load the level")
        level.translate(Vector3f(0f, 0.5f, 0f))
        level.scale(Vector3f(8f))

        rope = loadModel("assets/project_models/Lineal/ruler.obj", Math.toRadians(-90f), Math.toRadians(90f), 0f)
            ?: throw IllegalArgumentException("Could not load the ruler")
        rope.translate(Vector3f(1f, 0.1f, 3f))
        rope.rotate(0f, Math.toRadians(90f), Math.toRadians(90f))
        rope.scale(Vector3f(0.33f))

        ropeMatrix = rope.getWorldModelMatrix()

        objList.add(squirrel)
        objList.add(snail)
        objList.add(level)
        objList.add(rope)

        //setup camera
        camera = Camera()
        camera.fov = Math.toRadians(80f)
        camera.translate(Vector3f(5.5f, 2f, 6.5f))
        camera.rotate(Math.toRadians(-25f), Math.toRadians(45f), Math.toRadians(15f))


        skyColor = Vector3f(0.25f, 0.25f, 0.25f)

        /**
         * Setup Skybox
         */

        skybox = loadModel(
            "assets/project_models/Skybox/anime_sky.obj",
            Math.toRadians(-90.0f),
            Math.toRadians(90.0f),
            0.0f
        ) ?: throw IllegalArgumentException("Could not load the sky")
        skybox.apply {
            scale(Vector3f(5.0f))
            translate(Vector3f(0.0f, 5.0f, 0.0f))
            rotate(0.0f, 0.0f, Math.toRadians(-90.0f))
        }

        // point lights
        pointLightList.add(
            PointLight(
                "pointLight[${pointLightList.size}]",
                Vector3f(0.96f, 0.21f, 0.09f),
                Vector3f(0f, 2f, 6f)
            )
        )

        val squirrelPic = Texture2D("assets/textures/pictures/squirrel.png", true)
        val squirrelElement = GuiElement(squirrelPic, Vector2f(-0.9f, 0.9f), Vector2f(0.1f))

        val snailPic = Texture2D("assets/textures/pictures/snail.png", true)
        val snailElement = GuiElement(snailPic, Vector2f(-0.9f, 0.6f), Vector2f(0.1f))

        guiList.add(squirrelElement)
        guiList.add(snailElement)

        /*
        val fontTexture = Texture2D("assets/fonts/sans/sans.png", true)
        val font = FontType(fontTexture, File("assets/fonts/sans/sans.fnt"))

        GUIText("This is a test text", 5.0, font, Vector2f(0f), 1.0, true)
         */

        //initial opengl state
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f); GLError.checkThrow()
        glEnable(GL_CULL_FACE); GLError.checkThrow()
        glFrontFace(GL_CCW); GLError.checkThrow()
        glCullFace(GL_BACK); GLError.checkThrow()
        glEnable(GL_DEPTH_TEST); GLError.checkThrow()
        glDepthFunc(GL_LESS); GLError.checkThrow()

        mainChar = JumpRopePlayer(Player(1, squirrel))
        secChar = JumpRopePlayer(Player(2, snail))

    }

    override fun render(dt: Float, t: Float) {
        super.render(dt, t)

        staticShader.use()

        camera.bind(staticShader)

        // bind lights
        for (pointLight in pointLightList) {
            pointLight.bind(staticShader)
        }
        staticShader.setUniform("numPointLights", pointLightList.size)

        // render objects
        staticShader.setUniform("shadingColor", skyColor)
        skybox.render(staticShader)

        for (obj in objList) {
            obj.render(staticShader)
        }

        guiShader.render(guiList)
        //TextMaster.render()
    }

    override fun update(dt: Float, t: Float) {
        if (mainChar.isAlive) {
            mainChar.player.update(dt, window, GameType.JUMP_ROPE)
        }
        if (secChar.isAlive) {
            secChar.player.update(dt, window, GameType.JUMP_ROPE)
        }

        if (isGameRunning) {
            updateGameState(dt)
        }

        if (window.getKeyState(GLFW_KEY_T) && !isGameRunning) {
            isGameRunning = true
        }
    }

    override fun onKey(key: Int, scancode: Int, action: Int, mode: Int) {}

    override fun onMouseMove(xpos: Double, ypos: Double) {}

    fun cleanup() {}

    override fun onMouseScroll(xoffset: Double, yoffset: Double) {}

    override fun changeScene(newScene: GameType) {}

    private fun updateGameState(deltaTime: Float) {
        // update rope rotation
        rope.rotateAroundPoint(deltaTime * ropeSpeed, 0f, 0f, Vector3f(1f, 1f, 3f))
        ropeRotation -= deltaTime * ropeSpeed

        // if a rotation has been completed
        if (Math.toDegrees(ropeRotation.toDouble()) <= 5) {
            // check player states
            checkPlayerState(mainChar)
            checkPlayerState(secChar)

            // reset rotation + update rope speed
            rope.setWorldMatrix(ropeMatrix)
            ropeSpeed = Math.min(10.5f, 3.5f + (getHighestScore() * 0.2f))
            ropeRotation = Math.toRadians(360f)
        }
    }

    private fun checkPlayerState(jrPlayer: JumpRopePlayer) {
        if (!jrPlayer.isAlive || !isGameRunning) return

        if (jrPlayer.player.getHeight() <= 0.5f) {
            jrPlayer.isAlive = false
            jrPlayer.player.obj.rotate(Math.toRadians(90f), 0f, 0f)
            //jrPlayer.player.resetJump()
            if (isEveryoneDead()) {
                stopGame()
                return
            }
        } else {
            jrPlayer.score++
        }
    }

    private fun stopGame() {
        isGameRunning = false
        mainChar.player.obj.rotate(Math.toRadians(-90f), 0f, 0f)
        secChar.player.obj.rotate(Math.toRadians(-90f), 0f, 0f)
        mainChar.isAlive = true
        secChar.isAlive = true
        resetRope()
        println("Score Player 1: " + mainChar.score)
        println("Score Player 2: " + secChar.score)
        println("--------------------------------")
        println(getWinnerString())
        rope.setWorldMatrix(ropeMatrix)
        mainChar.score = 0
        secChar.score = 0
    }

    private fun resetRope() {
        rope.setWorldMatrix(ropeMatrix)

        ropeRotation = Math.toRadians(360f)
        ropeSpeed = 3.5f
    }

    private fun isEveryoneDead(): Boolean {
        return !mainChar.isAlive && !secChar.isAlive
    }

    private fun getHighestScore(): Int {
        return Math.max(mainChar.score, secChar.score)
    }

    private fun getWinnerString(): String {
        val winner = if (mainChar.score > secChar.score) 1 else 2
        return "Player $winner has won the game! \nIf you want to play again press T"
    }
}