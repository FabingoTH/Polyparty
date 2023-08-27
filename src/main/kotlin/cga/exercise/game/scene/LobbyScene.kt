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
import cga.framework.OBJLoader
import cga.framework.OBJLoader.loadOBJ
import org.joml.Math
import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.*

/**
 * Created by Fabian on 16.09.2017.
 */
class LobbyScene(override val window: GameWindow) : AScene() {

    // HÜPF ANIMATION
    private val jumpHeight = 0.05f
    private val jumpFrequency = 25.0f // Anzahl der Hüpfbewegungen pro Sekunde
    private var jumpPhase = 0.0f // Aktuelle Phase der Hüpfanimation

    // CAMERA
    private val orbitCamera: OrbitCamera
    private val camera: Camera
    private var oldMouseX = 0.0
    private var oldMouseY = 0.0
    private var firstMouseMove = true

    private val objList: MutableList<Renderable> = mutableListOf()

    // GROUND
    private val groundMaterial: Material
    private val ground: Renderable
    private val groundColor: Vector3f

    // SIGNS
    private val signRace: Renderable
    private val signJump: Renderable
    private val signMemo: Renderable

    // OTHER OBJECTS
    private val garden: Renderable
    private val mainChar: Renderable
    private val secChar: Renderable
    private val squirrel: Renderable
    private val shovel: Renderable
    private val rake: Renderable
    private val snail: Renderable
    private val backpack: Renderable


    //SKYBOX
    private val skybox: Renderable
    private val skyColor: Vector3f

    init {

        // GROUND
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

        garden =
            loadModel("assets/project_models/Garten/garden.obj", Math.toRadians(-90.0f), Math.toRadians(90.0f), 0.0f)
                ?: throw IllegalArgumentException("Could not load the garden")
        garden.scale(Vector3f(2.0f))
        garden.rotate(Math.toRadians(180f), 0.0f, Math.toRadians(90.0f))
        garden.preTranslate(Vector3f(0f, -0.5f, -1f))
        objList.add(garden)


        /**
         * Schilder: erklären Navigation
         */

        // says "Race Track"
        signRace =
            loadModel("assets/project_models/Schilder/signRace.obj", Math.toRadians(-90f), Math.toRadians(10f), 0f)
                ?: throw IllegalArgumentException("Could not load the signR")
        objList.add(signRace)
        signRace.preTranslate(Vector3f(4f, 0.1f, 2f))
        signRace.scale(Vector3f(0.5f))

        // says "Campsite"
        signJump =
            loadModel("assets/project_models/Schilder/signJump.obj", Math.toRadians(-95f), Math.toRadians(-90f), 0.0f)
                ?: throw IllegalArgumentException("Could not load the signJ")
        objList.add(signJump)
        signJump.preTranslate(Vector3f(-3.7f, 0.1f, 2.1f))
        signJump.scale(Vector3f(0.5f))

        // says "Dump"
        signMemo =
            loadModel("assets/project_models/Schilder/signMemo.obj", Math.toRadians(-85f), Math.toRadians(-150f), 0.0f)
                ?: throw IllegalArgumentException("Could not load the signM")
        objList.add(signMemo)
        signMemo.preTranslate(Vector3f(-1.7f, 0.1f, -5f))
        signMemo.scale(Vector3f(0.5f))

        // bounding box links
        garden.boundingBoxList[0] = AABB(min = Vector3f(-4.8f, 0f, -6f), max = Vector3f(-4.6f, 0f, 3f))

        // bounding box rechts
        garden.boundingBoxList.add(AABB(min = Vector3f(4.9f, 0f, -6f), max = Vector3f(5.1f, 0f, 3f)))

        // bounding box hinten
        garden.boundingBoxList.add(AABB(min = Vector3f(-4.8f, 0f, -6f), max = Vector3f(5.1f, 0f, -6f)))

        squirrel = loadModel(
            "assets/project_models/Eichhoernchen/squirrel.obj", 0f, Math.toRadians(-22f), 0f
        ) ?: throw IllegalArgumentException("Could not load the squirrel")
        squirrel.scale(Vector3f(0.7f))
        squirrel.translate(Vector3f(0f, 6f, 0f))

        shovel =
            loadModel("assets/project_models/Schaufel/model.obj", Math.toRadians(-60.0f), Math.toRadians(40.0f), 0f)
                ?: throw IllegalArgumentException("Could not load the shovel")
        shovel.scale(Vector3f(0.1f))
        shovel.preTranslate(Vector3f(-0.2f, 0.6f, 1.8f))
        objList.add(shovel)

        snail = loadModel(
            "assets/project_models/Schnecke/Mesh_Snail.obj",
            0f, Math.toRadians(180f),
            0.0f
        )
            ?: throw IllegalArgumentException("Could not load the snail")
        snail.scale(Vector3f(0.8f))
        objList.add(snail)

        rake = loadModel("assets/project_models/Hake/rake.obj", Math.toRadians(-40.0f), Math.toRadians(60.0f), 0.0f)
            ?: throw IllegalArgumentException("Could not load the rake")
        rake.preTranslate(Vector3f(-0.2f, 0.4f, 1.7f))
        rake.scale(Vector3f(0.1f))
        objList.add(rake)

        backpack =
            loadModel("assets/project_models/Rucksack/backpack.obj", Math.toRadians(-90f), Math.toRadians(-90f), 0.0f)
                ?: throw IllegalArgumentException("Could not load the backpack")
        objList.add(backpack)
        backpack.preTranslate(Vector3f(-4f, 0.45f, 1.9f))
        backpack.scale(Vector3f(0.2f))

        rake.parent = garden
        shovel.parent = garden

        /**
         * Wenn der Garten nachträglich transformiert wird,
         * bewegen sich ab hier die entsprechenden Gegenstände mit.
         */

        garden.scale(Vector3f(1.4f)) // Gesamtgarten größer gemacht
        // nur kurz höher gemacht, damit man den original Boden nicht dadurch sieht.
        // Sobald wir den alten Boden entfernen, kann diese Translation entfernt werden.
        garden.preTranslate(Vector3f(0f, 0.2f, 0f))

        //setup camera
        camera = Camera(
            custom(window.framebufferWidth, window.framebufferHeight),
            Math.toRadians(90.0f),
            0.1f,
            1000.0f
        )

        camera.rotate(Math.toRadians(-25.0f), 0.0f, 0.0f)
        camera.translate(Vector3f(0.0f, 1.0f, 5.0f))

        skyColor = Vector3f(1.0f, 1.0f, 1.0f)

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


        //initial opengl state
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f); GLError.checkThrow()
        glEnable(GL_CULL_FACE); GLError.checkThrow()
        glFrontFace(GL_CCW); GLError.checkThrow()
        glCullFace(GL_BACK); GLError.checkThrow()
        glEnable(GL_DEPTH_TEST); GLError.checkThrow()
        glDepthFunc(GL_LESS); GLError.checkThrow()

        mainChar = squirrel
        camera.parent = mainChar
        orbitCamera = OrbitCamera(mainChar)
        secChar = snail
        secChar.translate(Vector3f(1f, 0f, 0.5f))
        secChar.parent = squirrel

        objList.add(mainChar)
        objList.add(secChar)
    }

    override fun render(dt: Float, t: Float) {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)
        staticShader.use()

        staticShader.setUniform("shadingColor", groundColor)
        //ground.render(staticShader)

        orbitCamera.bind(staticShader)
        orbitCamera.updateCameraPosition()



        // render objects
        staticShader.setUniform("shadingColor", skyColor)
        skybox.render(staticShader)

        //staticShader.setUniform("shadingColor", Vector3f(0.5f, 0.5f, 0.5f))

        for (obj in objList) {
            obj.render(staticShader)
        }
    }

    override fun update(dt: Float, t: Float) {

        val moveMul = 5.0f
        val rotateMul = 2f * Math.PI.toFloat()

        if (window.getKeyState(GLFW_KEY_W)) {
            mainChar.translate(Vector3f(0.0f, 0.0f, -dt * moveMul))

            // Hüpfanimation
            jumpPhase += dt * jumpFrequency
            val verticalOffset = jumpHeight * Math.sin(jumpPhase)
            mainChar.translate(Vector3f(0.0f, verticalOffset, 0.0f))
        }

        if (window.getKeyState(GLFW_KEY_S)) {
            mainChar.translate(Vector3f(0.0f, 0.0f, dt * moveMul))

            // Hüpfanimation
            jumpPhase += dt * jumpFrequency
            val verticalOffset = jumpHeight * Math.sin(jumpPhase)
            mainChar.translate(Vector3f(0.0f, verticalOffset, 0.0f))
        }

        // Setzt den Character wieder direkt auf den Boden
        if (!window.getKeyState(GLFW_KEY_W) && !window.getKeyState(GLFW_KEY_S)) {
            val currentPosition = mainChar.getWorldPosition()
            mainChar.translate(Vector3f(0.0f, -currentPosition.y, 0.0f))
            jumpPhase = 0.0f
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


    override fun onMouseScroll(xoffset: Double, yoffset: Double) {
        camera.fov += Math.toRadians(yoffset.toFloat())
        val zoom = orbitCamera.distance + Math.toRadians(yoffset.toFloat()) * -10.0f
        orbitCamera.distance = zoom.coerceAtMost(7.0f) // Max Zoom Out
    }

    override fun changeScene(newScene: GameType) {}
}