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

    /** LIGHTS **/
    private val bikePointLight: PointLight
    private val pointLightList = mutableListOf<PointLight>()

    private val bikeSpotLight: SpotLight
    private val spotLightList = mutableListOf<SpotLight>()

    /** CAMERA **/
    private val p1Camera: Camera
    private val p2Camera : Camera

    /** OBJECTS **/
    private val objList: MutableList<Renderable> = mutableListOf()
    private val snail: Renderable
    private val bike: Renderable
    private val mainChar: Renderable
    private val secChar: Renderable
    private val squirrel: Renderable
    private val racetrack: Renderable

    /** SKYDOME **/
    private val skybox: Renderable
    private val skyColor: Vector3f

    //scene setup
    init {

        bike = loadModel(
            "assets/Light Cycle/Light Cycle/HQ_Movie cycle.obj",
            Math.toRadians(-90.0f),
            Math.toRadians(90.0f),
            0.0f
        ) ?: throw IllegalArgumentException("Could not load the bike")
        bike.apply {
            rotate(0.0f,Math.toRadians(180f),0.0f)
            translate(Vector3f(-1.0f,0.0f,0.0f))
            scale(Vector3f(0.8f, 0.8f, 0.8f))
        }
        objList.add(bike)

        squirrel = loadModel(
            "assets/project_models/Eichhoernchen/squirrel.obj", 0f, Math.toRadians(-22f), 0f
        ) ?: throw IllegalArgumentException("Could not load the squirrel")
        squirrel.apply {
            rotate(0.0f,Math.toRadians(180f),0.0f)
            translate(Vector3f(1.0f,0.0f,0.0f))
        }
        objList.add(squirrel)

        racetrack =
            loadModel("assets/project_models/Rennstrecke/autodraha.obj", Math.toRadians(-90.0f), Math.toRadians(90.0f), 0.0f)
                ?: throw IllegalArgumentException("Could not load the hose")
        racetrack.apply {
            rotate(0f, Math.toRadians(-90.0f), Math.toRadians(-90f))
            scale(Vector3f(0.6f))
        }
        objList.add(racetrack)

        snail = loadModel(
            "assets/project_models/Schnecke/Mesh_Snail.obj",
            0f, Math.toRadians(180f),
            0.0f
        )
            ?: throw IllegalArgumentException("Could not load the snail")
        snail.apply {
            rotate(0.0f,Math.toRadians(180f),0.0f)
            translate(Vector3f(1.0f,0.0f,0.0f))
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

        //bike pointlight
        bikePointLight =
            PointLight(
                "pointLight[${pointLightList.size}]",
                Vector3f(0.0f, 2.0f, 0.0f),
                Vector3f(0.0f, 0.5f, 0.0f))
        bikePointLight.parent = bike
        pointLightList.add(bikePointLight)

        //bike spotlight
        bikeSpotLight = SpotLight(
            "spotLight[${spotLightList.size}]",
            Vector3f(1f),
            Vector3f(0f, 1f, -2f),
            Math.toRadians(0.0f),
            Math.toRadians(0.0f)
        )
        //bikeSpotLight.rotate(Math.toRadians(-10.0f), 0.0f, 0.0f)
        bikeSpotLight.parent = bike
        spotLightList.add(bikeSpotLight)

        // additional lights in the scene
        pointLightList.add(PointLight("pointLight[${pointLightList.size}]", Vector3f(0.0f, 2.0f, 2.0f), Vector3f(-10.0f, 2.0f, -10.0f)))
        pointLightList.add(PointLight("pointLight[${pointLightList.size}]", Vector3f(2.0f, 0.0f, 0.0f), Vector3f(10.0f, 2.0f, 10.0f)))
        spotLightList.add(SpotLight("spotLight[${spotLightList.size}]", Vector3f(10.0f, 300.0f, 300.0f), Vector3f(6.0f, 2.0f, 4.0f), Math.toRadians(20.0f), Math.toRadians(30.0f)))
        spotLightList.last().rotate(Math.toRadians(20f), Math.toRadians(60f), 0f)

        //initial opengl state
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f); GLError.checkThrow()
        glEnable(GL_CULL_FACE); GLError.checkThrow()
        glFrontFace(GL_CCW); GLError.checkThrow()
        glCullFace(GL_BACK); GLError.checkThrow()
        glEnable(GL_DEPTH_TEST); GLError.checkThrow()
        glDepthFunc(GL_LESS); GLError.checkThrow()

        mainChar = snail
        p1Camera.parent = mainChar

        secChar = squirrel
        p2Camera.parent = secChar

        objList.add(mainChar)
        objList.add(secChar)
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

        val changingColor = Vector3f(Math.abs(Math.sin(t)), 0f, Math.abs(Math.cos(t)))
        bikePointLight.lightColor = changingColor

        // bind lights
        for (pointLight in pointLightList) {
            pointLight.bind(staticShader)
        }
        staticShader.setUniform("numPointLights", pointLightList.size)
        for (spotLight in spotLightList) {
            spotLight.bind(staticShader, p1Camera.calculateViewMatrix())
        }
        staticShader.setUniform("numSpotLights", spotLightList.size)

        staticShader.setUniform("shadingColor", skyColor)
        bike.render(staticShader)

        for (obj in objList) {
            obj.render(staticShader)
        }
    }

    private fun renderGameScene(camera : Camera) {
        // Setzen Sie die View- und Projektionsmatrix der übergebenen Kamera
        staticShader.setUniform("view_matrix", camera.calculateViewMatrix(), false)
        staticShader.setUniform("proj_matrix", camera.calculateProjectionMatrix(), false)

        // Binden Sie die Lichter für die übergebene Kamera
        for (pointLight in pointLightList) {
            pointLight.bind(staticShader)
        }
        staticShader.setUniform("numPointLights", pointLightList.size)
        for (spotLight in spotLightList) {
            spotLight.bind(staticShader, camera.calculateViewMatrix())
        }
        staticShader.setUniform("numSpotLights", spotLightList.size)
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

    override fun onKey(key: Int, scancode: Int, action: Int, mode: Int) {}

    override fun onMouseMove(xpos: Double, ypos: Double) {

    }

    fun cleanup() {}

    override fun onMouseScroll(xoffset: Double, yoffset: Double) {

    }

    override fun changeScene(newScene: GameType) {}
}