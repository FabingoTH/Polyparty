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
class LobbyScene(override val window: GameWindow) : AScene() {

    // Jump Animation Variabeln
    private val jumpHeight = 0.05f
    private val jumpFrequency = 25.0f // Anzahl der Hüpfbewegungen pro Sekunde
    private var jumpPhase = 0.0f // Aktuelle Phase der Hüpfanimation

    private val orbitCamera: OrbitCamera
    private val camera: Camera
    private var oldMouseX = 0.0
    private var oldMouseY = 0.0
    private var firstMouseMove = true

    private val objList: MutableList<Renderable> = mutableListOf()

    private val garden: Renderable

    private val mainChar: Renderable
    private val secChar: Renderable

    private val squirrel: Renderable

    private val shovel: Renderable
    private val rake: Renderable
    private val snail: Renderable
    private val hose: Renderable
    private val skybox: Renderable
    private val skyColor: Vector3f

    init {

        garden =
            loadModel("assets/project_models/Garten/garden.obj", Math.toRadians(-90.0f), Math.toRadians(90.0f), 0.0f)
                ?: throw IllegalArgumentException("Could not load the garden")
        garden.scale(Vector3f(2.0f))
        garden.rotate(Math.toRadians(180f), 0.0f, Math.toRadians(90.0f))
        garden.preTranslate(Vector3f(0f, -0.5f, -1f))
        objList.add(garden)
        objList.add(garden)

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
            loadModel("assets/project_models/Schaufel/model.obj", Math.toRadians(-90.0f), Math.toRadians(90.0f), 0.0f)
                ?: throw IllegalArgumentException("Could not load the shovel")
        shovel.rotate(Math.toRadians(-90.0f), 0f, 0f)
        shovel.preTranslate(Vector3f(-0.11f, 0.3f, 1.87f)) // x unten/oben, y links/rechts, z nach vorn/zurück
        shovel.scale(Vector3f(0.27f))
        objList.add(shovel)

        snail = loadModel(
            "assets/project_models/Schnecke/Mesh_Snail.obj",
            Math.toRadians(-90.0f),
            Math.toRadians(90.0f),
            0.0f
        )
            ?: throw IllegalArgumentException("Could not load the snail")
        snail.rotate(0f, Math.toRadians(-30f), Math.toRadians(-90.0f))
        snail.preTranslate(Vector3f(-1.3f, 0.4f, -5.8f)) // x rechts/links, y oben/unten, z nach vorn/zurück
        snail.scale(Vector3f(0.05f))
        objList.add(snail)

        rake = loadModel("assets/project_models/Hake/rake.obj", Math.toRadians(-90.0f), Math.toRadians(90.0f), 0.0f)
            ?: throw IllegalArgumentException("Could not load the rake")
        rake.scale(Vector3f(0.5f))
        rake.preTranslate(Vector3f(0f, 0.69f, -5.8f)) // x rechts/links, y oben/unten, z zurück/nach vorn
        rake.rotate(0f, Math.toRadians(-160.0f), Math.toRadians(-150f))
        objList.add(rake)

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
        shovel.parent = garden
        hose.parent = garden
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

        /**
         * initial game state
         */

        mainChar = squirrel
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

        // render objects
        staticShader.setUniform("shadingColor", skyColor)
        skybox.render(staticShader)

        //staticShader.setUniform("shadingColor", Vector3f(0.5f, 0.5f, 0.5f))

        for (obj in objList) {
            obj.render(staticShader)
        }
    }

    override fun update(dt: Float, t: Float) {
        val moveMul = 15.0f
        val rotateMul = 2f * Math.PI.toFloat()


        // GAMESTATE NONE - Steuerung


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





        mainChar.boundingBoxList[0] =
            AABB(mainChar.getWorldPosition().add(Vector3f(-1f)), mainChar.getWorldPosition().add(Vector3f(1f)))


        // if object collides with left hand wall
        if (mainChar.boundingBoxList[0].collidesWith(garden.boundingBoxList[0])) {
            mainChar.preTranslate(mainChar.boundingBoxList[0].getAxisToCorrect(garden.boundingBoxList[0])!!.difference)
        }

        // if object collides with right hand wall
        if (mainChar.boundingBoxList[0].collidesWith(garden.boundingBoxList[1])) {
            mainChar.preTranslate(mainChar.boundingBoxList[0].getAxisToCorrect(garden.boundingBoxList[1])!!.difference)
        }

        // if object collides with middle wall
        if (mainChar.boundingBoxList[0].collidesWith(garden.boundingBoxList[2])) {
            mainChar.preTranslate(mainChar.boundingBoxList[0].getAxisToCorrect(garden.boundingBoxList[2])!!.difference)
        }


    }

    override fun onKey(key: Int, scancode: Int, action: Int, mode: Int) {}

    override fun onMouseMove(xpos: Double, ypos: Double) {

        val azimuthRate: Float = 0.1f
        val elevationRate: Float = 0.025f

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