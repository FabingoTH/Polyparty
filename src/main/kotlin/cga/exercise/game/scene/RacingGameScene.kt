package cga.exercise.game

import cga.exercise.components.camera.Aspectratio.Companion.custom
import cga.exercise.components.camera.Camera
import cga.exercise.components.collision.*
import cga.exercise.components.geometry.Material
import cga.exercise.components.geometry.Mesh
import cga.exercise.components.geometry.Renderable
import cga.exercise.components.geometry.VertexAttribute
import cga.exercise.components.light.PointLight
import cga.exercise.components.light.SpotLight
import cga.exercise.components.shader.ShaderProgram
import cga.exercise.components.camera.OrbitCamera
import cga.exercise.components.texture.Texture2D
import cga.exercise.game.scene.AScene
import cga.framework.GLError
import cga.framework.GameWindow
import cga.framework.ModelLoader.loadModel
import cga.framework.OBJLoader.loadOBJ
import org.joml.Math
import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL11.*

/**
 * Created by Fabian on 16.09.2017.
 */
class RacingGameScene(override val window: GameWindow) : AScene() {

    /** CAMERA **/
    private val p1Camera: Camera
    private val p2Camera : Camera

    /** LIGHTS **/
    private val pointLightList = mutableListOf<PointLight>()
    private val spotLightList = mutableListOf<SpotLight>()

    /** OBJECTS **/
    private val objList: MutableList<Renderable> = mutableListOf()
    private val snail: Renderable
    private val mainChar: Renderable
    private val secChar: Renderable
    private val squirrel: Renderable
    private val racetrack: Renderable

    /** SKYDOME **/
    private val skybox: Renderable
    private val skyColor: Vector3f

    /**
     * RACE STARTING POSITIONS
     */
    private val mainCharPos: Vector3f
    private val secCharPos: Vector3f
    var movementEnabled: Boolean = true

    /**
     * WIN TEXT
     */
    private val p1Win: Renderable
    private val p2Win: Renderable

    //scene setup
    init {

        /** MODELS **/
        racetrack =
            loadModel(
                "assets/project_models/Rennstrecke/autodraha.obj",
                Math.toRadians(-90.0f),
                Math.toRadians(90.0f),
                0.0f
            )
                ?: throw IllegalArgumentException("Could not load the hose")
        racetrack.apply {
            rotate(0f, Math.toRadians(-90.0f), Math.toRadians(-90f))
            scale(Vector3f(0.6f))
        }
        objList.add(racetrack)

        squirrel = loadModel(
            "assets/project_models/Eichhoernchen/squirrel.obj", 0f, Math.toRadians(-22f), 0f
        ) ?: throw IllegalArgumentException("Could not load the squirrel")
        squirrel.apply {
            rotate(0.0f, Math.toRadians(180f), 0.0f)
            translate(Vector3f(-1.0f, 0.0f, 0.0f))
        }
        objList.add(squirrel)

        snail = loadModel(
            "assets/project_models/Schnecke/Mesh_Snail.obj",
            0f, Math.toRadians(180f),
            0.0f
        )
            ?: throw IllegalArgumentException("Could not load the snail")
        snail.apply {
            rotate(0.0f, Math.toRadians(180f), 0.0f)
            translate(Vector3f(1.0f, 0.0f, 0.0f))
            scale(Vector3f(0.8f))
        }
        objList.add(snail)

        /** CAMERAS **/
        p1Camera = Camera(
            custom(window.framebufferWidth / 2, window.framebufferHeight),
            Math.toRadians(90.0f),
            0.1f,
            5000.0f
        )
        p1Camera.rotate(Math.toRadians(-25.0f), 0.0f, 0.0f)
        p1Camera.translate(Vector3f(0.0f, 1.0f, 5.0f))

        p2Camera = Camera(
            custom(window.framebufferWidth / 2, window.framebufferHeight),
            Math.toRadians(90.0f),
            0.1f,
            5000.0f
        )
        p2Camera.rotate(Math.toRadians(-25.0f), 0.0f, 0.0f)
        p2Camera.translate(Vector3f(0.0f, 1.0f, 5.0f))


        /**
         * WIN TEXTS
         */
        p1Win = loadModel(
            "assets/project_models/Rennstrecke/p1Win.obj",
            Math.toRadians(90f), Math.toRadians(180f),
            0.0f
        ) ?: throw IllegalArgumentException("Could not load the text")
        p1Win.scale(Vector3f(0.3f))
        p1Win.preTranslate(Vector3f(0f, -1f, 0f))
        objList.add(p1Win)


        p2Win = loadModel(
            "assets/project_models/Rennstrecke/p2Win.obj",
            Math.toRadians(90f), Math.toRadians(180f),
            0.0f
        ) ?: throw IllegalArgumentException("Could not load the text")
        p2Win.scale(Vector3f(0.3f))
        p2Win.preTranslate(Vector3f(0f, -1f, 0f))
        objList.add(p2Win)

        /** SKYBOX **/
        skyColor = Vector3f(1f)
        skybox = loadModel(
            "assets/project_models/Skybox/anime_sky.obj",
            Math.toRadians(-90.0f),
            Math.toRadians(90.0f),
            0.0f
        ) ?: throw IllegalArgumentException("Could not load the sky")
        skybox.apply {
            scale(Vector3f(5.0f))
            translate(Vector3f(0.0f,5.0f,0.0f))
            rotate(0.0f, 0.0f, Math.toRadians(-90.0f))
        }
        objList.add(skybox)

        /** LIGHTS **/
        pointLightList.add(PointLight("pointLight[${pointLightList.size}]", Vector3f(0.0f, 2.0f, 2.0f), Vector3f(-10.0f, 2.0f, -10.0f)))
        pointLightList.add(PointLight("pointLight[${pointLightList.size}]", Vector3f(2.0f, 0.0f, 0.0f), Vector3f(10.0f, 2.0f, 10.0f)))
        spotLightList.add(SpotLight("spotLight[${spotLightList.size}]", Vector3f(10.0f, 300.0f, 300.0f), Vector3f(6.0f, 2.0f, 4.0f), Math.toRadians(20.0f), Math.toRadians(30.0f)))
        spotLightList.last().rotate(Math.toRadians(20f), Math.toRadians(60f), 0f)

        /** CHARACTER - CAMERA ASSIGNMENT **/
        mainChar = squirrel
        p1Camera.parent = mainChar

        secChar = snail
        p2Camera.parent = secChar

        objList.add(mainChar)
        objList.add(secChar)


        /**
         * GOAL BOUNDING BOX
         */

        mainCharPos = mainChar.getWorldPosition() // -1, 0, 8.74
        racetrack.boundingBoxList[0] = AABB(Vector3f(-6f, 0f, -6f), mainCharPos.add(6f, 0f, -5.9f))
        secCharPos = secChar.getWorldPosition()

        //initial opengl state
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f); GLError.checkThrow()
        glEnable(GL_CULL_FACE); GLError.checkThrow()
        glFrontFace(GL_CCW); GLError.checkThrow()
        glCullFace(GL_BACK); GLError.checkThrow()
        glEnable(GL_DEPTH_TEST); GLError.checkThrow()
        glDepthFunc(GL_LESS); GLError.checkThrow()
    }

    override fun render(dt: Float, t: Float) {
        super.render(dt, t)

        glViewport(0, 0, window.framebufferWidth / 2, window.framebufferHeight)
        p1Camera.bind(staticShader)
        renderGameScene(p1Camera)

        // Render Player 2 View
        glViewport(window.framebufferWidth / 2, 0, window.framebufferWidth / 2, window.framebufferHeight)
        p2Camera.bind(staticShader)
        renderGameScene(p2Camera)

        // bind lights
        for (pointLight in pointLightList) {
            pointLight.bind(staticShader)
        }
        staticShader.setUniform("numPointLights", pointLightList.size)
        for (spotLight in spotLightList) {
            spotLight.bind(staticShader, p1Camera.calculateViewMatrix())
            spotLight.bind(staticShader, p2Camera.calculateViewMatrix())
        }
        staticShader.setUniform("numSpotLights", spotLightList.size)

        staticShader.setUniform("shadingColor", skyColor)

        for (obj in objList) {
            obj.render(staticShader)
        }
    }

    private fun renderGameScene(camera : Camera) {
        // Setzen Sie die View- und Projektionsmatrix der übergebenen Kamera
        staticShader.setUniform("view_matrix", camera.calculateViewMatrix(), false)
        staticShader.setUniform("proj_matrix", camera.calculateProjectionMatrix(), false)

        mainChar.render(staticShader)
        secChar.render(staticShader)
        // Rendern Sie andere Objekte in der Szene, wenn vorhanden
        for (obj in objList) {
            obj.render(staticShader)
        }
    }

    override fun update(dt: Float, t: Float) {
        val moveMul = 30.0f
        val rotateMul = 1f * Math.PI.toFloat()

        if (movementEnabled) {
            if (window.getKeyState(GLFW_KEY_W)) {
                mainChar.translate(Vector3f(0.0f, 0.0f, -dt * moveMul))
            }
            if (window.getKeyState(GLFW_KEY_S)) {
                mainChar.translate(Vector3f(0.0f, 0.0f, dt * moveMul))
            }
            if (window.getKeyState(GLFW_KEY_A)) {
                mainChar.rotate(0.0f, dt * rotateMul, 0.0f)
            }
            if (window.getKeyState(GLFW_KEY_D)) {
                mainChar.rotate(0.0f, -dt * rotateMul, 0.0f)
            }

            if (window.getKeyState(GLFW_KEY_UP)) {
                secChar.translate(Vector3f(0.0f, 0.0f, -dt * moveMul))
            }
            if (window.getKeyState(GLFW_KEY_DOWN)) {
                secChar.translate(Vector3f(0.0f, 0.0f, dt * moveMul))
            }
            if (window.getKeyState(GLFW_KEY_LEFT)) {
                secChar.rotate(0.0f, dt * rotateMul, 0.0f)
            }
            if (window.getKeyState(GLFW_KEY_RIGHT)) {
                secChar.rotate(0.0f, -dt * rotateMul, 0.0f)
            }
        }


        if (window.getKeyState(GLFW_KEY_P)) {
            println(mainChar.getWorldPosition())
        }


        // Kollision: Win Detection

        // wird hier gesetzt, damit es immer geupdated wird
        mainChar.boundingBoxList[0] = AABB(
            Vector3f(mainChar.getWorldPosition().add(Vector3f(-1f))),
            Vector3f(mainChar.getWorldPosition().add(Vector3f(1f)))
        )
        secChar.boundingBoxList[0] = AABB(
            Vector3f(secChar.getWorldPosition().add(Vector3f(-1f))),
            Vector3f(secChar.getWorldPosition().add(Vector3f(1f)))
        )

        if (mainChar.boundingBoxList[0].collidesWith(racetrack.boundingBoxList[0])) {
            movementEnabled = false
            mainChar.setWorldPosition(Vector3f(-1f, 0f, 0f))
            mainChar.lookAt(Vector3f(-1f, 0f, 1f))
            secChar.setWorldPosition(Vector3f(1f, 0f, 0f))
            secChar.lookAt(Vector3f(1f, 0f, 1f))

            p1Win.translate(Vector3f(0f, 15f, 0f))

            println("Player 1 won!")
        }

        if (secChar.boundingBoxList[0].collidesWith(racetrack.boundingBoxList[0])) {
            movementEnabled = false
            mainChar.setWorldPosition(Vector3f(-1f, 0f, 0f))
            mainChar.lookAt(Vector3f(-1f, 0f, 1f))
            secChar.setWorldPosition(Vector3f(1f))
            secChar.lookAt(Vector3f(1f, 0f, 1f))

            p2Win.preTranslate(Vector3f(0f, 15f, 0f))

            println("Player 2 won!")
        }

    }

    override fun onKey(key: Int, scancode: Int, action: Int, mode: Int) {}

    override fun onMouseMove(xpos: Double, ypos: Double) {

    }

    fun cleanup() {}

    override fun onMouseScroll(xoffset: Double, yoffset: Double) {

    }

    override fun changeScene(newScene: GameType) {}
}