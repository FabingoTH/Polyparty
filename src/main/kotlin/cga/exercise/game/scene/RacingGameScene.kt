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

    /** GROUND **/
    private val ground: Renderable
    private val groundMaterial: Material
    private val groundColor: Vector3f

    /** LIGHTS **/
    private val bikePointLight: PointLight
    private val pointLightList = mutableListOf<PointLight>()

    private val bikeSpotLight: SpotLight
    private val spotLightList = mutableListOf<SpotLight>()

    /** CAMERA **/
    private val orbitCamera: OrbitCamera
    private val camera: Camera
    private var oldMouseX = 0.0
    private var oldMouseY = 0.0
    private var firstMouseMove = true

    /** OBJECTS **/
    private val objList: MutableList<Renderable> = mutableListOf()
    private val bike: Renderable
    private val mainChar: Renderable
    private val secChar: Renderable
    private val squirrel: Renderable
    private val shovel: Renderable
    private val rake: Renderable
    private val snail: Renderable
    private val hose: Renderable
    private val racetrack: Renderable

    /** SKYDOME **/
    private val skybox: Renderable
    private val skyColor: Vector3f

    //scene setup
    init {

        //load textures
        val groundDiff = Texture2D("assets/textures/ground_diff.png", true)
        groundDiff.setTexParams(GL_REPEAT, GL_REPEAT, GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR)
        val groundSpecular = Texture2D("assets/textures/ground_spec.png", true)
        groundSpecular.setTexParams(GL_REPEAT, GL_REPEAT, GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR)
        val groundEmit = Texture2D("assets/textures/ground_emit.png", true)
        groundEmit.setTexParams(GL_REPEAT, GL_REPEAT, GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR)
        groundMaterial = Material(groundDiff, groundEmit, groundSpecular, 60f, Vector2f(64.0f, 64.0f))

        //load an object and create a mesh
        val gres = loadOBJ("assets/models/ground.obj")
        //Create the mesh
        val stride = 8 * 4
        val atr1 = VertexAttribute(3, GL_FLOAT, stride, 0)     //position attribute
        val atr2 = VertexAttribute(2, GL_FLOAT, stride, 3 * 4) //texture coordinate attribute
        val atr3 = VertexAttribute(3, GL_FLOAT, stride, 5 * 4) //normal attribute
        val vertexAttributes = arrayOf(atr1, atr2, atr3)
        //Create renderable
        ground = Renderable()
        for (m in gres.objects[0].meshes) {
            val mesh = Mesh(m.vertexData, m.indexData, vertexAttributes, groundMaterial)
            ground.meshes.add(mesh)
        }
        bike = loadModel(
            "assets/Light Cycle/Light Cycle/HQ_Movie cycle.obj",
            Math.toRadians(-90.0f),
            Math.toRadians(90.0f),
            0.0f
        ) ?: throw IllegalArgumentException("Could not load the bike")
        bike.scale(Vector3f(0.8f, 0.8f, 0.8f))

        squirrel = loadModel(
            "assets/project_models/Eichhoernchen/squirrel.obj", 0f, Math.toRadians(-22f), 0f
        ) ?: throw IllegalArgumentException("Could not load the squirrel")
        squirrel.scale(Vector3f(0.7f))
        squirrel.translate(Vector3f(0f, 6f, 0f))

        /** kleinere Gegenstände:
         ** Setup Schaufel
         */
        shovel =
            loadModel("assets/project_models/Schaufel/model.obj", Math.toRadians(-90.0f), Math.toRadians(90.0f), 0.0f)
                ?: throw IllegalArgumentException("Could not load the shovel")
        shovel.rotate(Math.toRadians(-90.0f), 0f, 0f)
        shovel.preTranslate(Vector3f(-0.11f, 0.3f, 1.87f)) // x unten/oben, y links/rechts, z nach vorn/zurück
        shovel.scale(Vector3f(0.27f))
        objList.add(shovel)

        /**
         ** Setup Schnecke
         */
        snail = loadModel(
            "assets/project_models/Schnecke/Mesh_Snail.obj",
            Math.toRadians(-90.0f),
            Math.toRadians(90.0f),
            0.0f
        )
            ?: throw IllegalArgumentException("Could not load the snail")
        snail.rotate(0f, Math.toRadians(-30f), Math.toRadians(-90.0f))
        snail.preTranslate(Vector3f(-1.3f, 0.4f, -5.8f)) // x rechts/links, y oben/unten, z nach vorn/zurück
        snail.scale(Vector3f(1.0f))
        objList.add(snail)

        /**
         ** Setup Hake
         */
        rake = loadModel("assets/project_models/Hake/rake.obj", Math.toRadians(-90.0f), Math.toRadians(90.0f), 0.0f)
            ?: throw IllegalArgumentException("Could not load the rake")
        rake.scale(Vector3f(0.5f))
        rake.preTranslate(Vector3f(0f, 0.69f, -5.8f)) // x rechts/links, y oben/unten, z zurück/nach vorn
        rake.rotate(0f, Math.toRadians(-160.0f), Math.toRadians(-150f))
        objList.add(rake)

        /**
         ** Setup Gartenschlauch
         */
        hose =
            loadModel("assets/project_models/Schlauch/model.obj", Math.toRadians(-90.0f), Math.toRadians(90.0f), 0.0f)
                ?: throw IllegalArgumentException("Could not load the hose")
        hose.translate(
            Vector3f(
                0f,
                0.2f,
                -1.4f
            )
        ) // object space: y links, -y rechts, x runter, -x hoch, z vorwärts, -z rückwärts
        hose.rotate(
            Math.toRadians(-150f),
            Math.toRadians(10.0f),
            Math.toRadians(-17.0f)
        ) // pitch rotiert um vertikale Achse, yaw kippt nach hinten/vorne, roll links/rechts
        hose.scale(Vector3f(0.1f))
        objList.add(hose)

        racetrack =
            loadModel("assets/project_models/Rennstrecke/Rennstrecke.obj", Math.toRadians(-90.0f), Math.toRadians(90.0f), 0.0f)
                ?: throw IllegalArgumentException("Could not load the hose")
        racetrack.apply {
            rotate(0f, Math.toRadians(-90.0f), Math.toRadians(-90f))
            translate(Vector3f(2f,-10f,-10f))
            scale(Vector3f(0.6f))
        }

        objList.add(racetrack)

        //setup camera
        camera = Camera(
            custom(window.framebufferWidth, window.framebufferHeight),
            Math.toRadians(90.0f),
            0.1f,
            1000.0f
        )

        camera.rotate(Math.toRadians(-25.0f), 0.0f, 0.0f)
        camera.translate(Vector3f(0.0f, 1.0f, 5.0f))

        groundColor = Vector3f(0.0f, 1.0f,0.0f)
        skyColor = Vector3f(1f)

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
            translate(Vector3f(0.0f,5.0f,0.0f))
            rotate(0.0f, 0.0f, Math.toRadians(-90.0f))
        }

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


        mainChar = bike
        camera.parent = mainChar
        orbitCamera = OrbitCamera(mainChar)
        secChar = snail
        secChar.translate(Vector3f(1f, 0f, 1f))
        secChar.parent = squirrel

        objList.add(mainChar)
        objList.add(secChar)

    }

    override fun render(dt: Float, t: Float) {
        super.render(dt, t)

        orbitCamera.bind(staticShader)
        orbitCamera.updateCameraPosition()

        val changingColor = Vector3f(Math.abs(Math.sin(t)), 0f, Math.abs(Math.cos(t)))
        bikePointLight.lightColor = changingColor

        // bind lights
        for (pointLight in pointLightList) {
            pointLight.bind(staticShader)
        }
        staticShader.setUniform("numPointLights", pointLightList.size)
        for (spotLight in spotLightList) {
            spotLight.bind(staticShader, camera.calculateViewMatrix())
        }
        staticShader.setUniform("numSpotLights", spotLightList.size)

        // render objects
        //staticShader.setUniform("shadingColor", groundColor)
        //ground.render(staticShader)
        staticShader.setUniform("shadingColor", changingColor)
        bike.render(staticShader)
        staticShader.setUniform("shadingColor", skyColor)
        skybox.render(staticShader)

        //staticShader.setUniform("shadingColor", Vector3f(0.5f, 0.5f, 0.5f))

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
    }

    override fun onKey(key: Int, scancode: Int, action: Int, mode: Int) {}

    override fun onMouseMove(xpos: Double, ypos: Double) {

        var azimuthRate: Float = 0.1f
        var elevationRate: Float = 0.025f

        if (firstMouseMove) {
            val yawAngle = (xpos - oldMouseX).toFloat() * azimuthRate
            val pitchAngle = (ypos - oldMouseY).toFloat() * elevationRate

            // Ändere die elevation und azimuth Winkel der OrbitCamera
            orbitCamera.azimuth -= yawAngle

            // Begrenze die elevation, um nicht unter -45 Grad zu gehen
            val newElevation = orbitCamera.elevation - pitchAngle
            orbitCamera.elevation = newElevation.coerceIn(10.0f, 70.0f)

            // Speichere die Mausposition für den nächsten Aufruf
            oldMouseX = xpos
            oldMouseY = ypos
        }
    }

    fun cleanup() {}

    override fun onMouseScroll(xoffset: Double, yoffset: Double) {
        val zoom = orbitCamera.distance + Math.toRadians(yoffset.toFloat()) * -10.0f
        orbitCamera.distance = zoom.coerceAtMost(7.0f) // Max Zoom Out
    }

    override fun changeScene(newScene: GameType) {}
}