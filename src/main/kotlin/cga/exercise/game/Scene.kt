package cga.exercise.game

import cga.exercise.components.camera.Aspectratio.Companion.custom
import cga.exercise.components.camera.TronCamera
import cga.exercise.components.geometry.Material
import cga.exercise.components.geometry.Mesh
import cga.exercise.components.geometry.Renderable
import cga.exercise.components.geometry.VertexAttribute
import cga.exercise.components.light.PointLight
import cga.exercise.components.light.SpotLight
import cga.exercise.components.shader.ShaderProgram
import cga.exercise.components.texture.Texture2D
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
class Scene(private val window: GameWindow) {
    private val staticShader: ShaderProgram =
        ShaderProgram("assets/shaders/tron_vert.glsl", "assets/shaders/tron_frag.glsl")

    private val ground: Renderable
    private val bike: Renderable
    private val skybox: Renderable

    private val skyColor: Vector3f

    private val groundMaterial: Material
    private val groundColor: Vector3f

    //Lights
    private val bikePointLight: PointLight
    private val pointLightList = mutableListOf<PointLight>()

    private val bikeSpotLight: SpotLight
    private val spotLightList = mutableListOf<SpotLight>()

    //camera
    private val camera: TronCamera
    private var oldMouseX = 0.0
    private var oldMouseY = 0.0
    private var firstMouseMove = true

    /** PROJECT MODELS
     *  Modell als .obj-File, Material als .mtl-File und Texturen als .png-Files in "assets".
     *  -> Texture Maps von Meike in Blender hinzugefügt.
     *  Alles in Scene ladbar mit vorhandener loadModel()-Methode (vgl. Motorcycle aus Praktikum).
     *  --------------------------------------------------------------------------------------------
     *  --------------------------------------------------------------------------------------------
     *  Garten als Overworld-Model:
     *  -> Modell "Cloister Garden" von Bruno Oliveira via PolyPizza.
     */
    private val garden: Renderable

    /** Haufen aus Schaufel, Hake und Schnecke:
     * Symbolisiert das "Memory"/Sortier-Spiel. Anvisieren und drücken auf "E" soll
     * teleportieren/Spiel starten.
     * -> Modell "Garden Trovel" von Pookage Hayes via PolyPizza.
     * -> Modell "Hand Rake" von Jarlan Perez via PolyPizza.
     * -> Modell "Snail" von Poly by Google via PolyPizza.
     */
    private val shovel: Renderable
    private val rake: Renderable
    private val snail: Renderable

    /** Gartenschlauch:
     * Symbolisiert das Springseil-Spiel. Anvisieren und drücken auf "E" soll
     * teleportieren/Spiel starten.
     * -> Modell "TIME HOTEL 4.28" von S. Paul Michael via PolyPizza.
     */
    private val hose: Renderable

    private val ruler: Renderable

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
        ) ?: throw IllegalArgumentException("Could not load the model")
        bike.scale(Vector3f(0.8f, 0.8f, 0.8f))

        skybox = loadModel(
            "assets/Skybox/anime_sky.obj",
            Math.toRadians(-90.0f),
            Math.toRadians(90.0f),
            0.0f
        ) ?: throw IllegalArgumentException("Could not load the model")
        skybox.apply {
            scale(Vector3f(0.5f))
            rotate(0.0f, 0.0f, Math.toRadians(-90.0f))
        }

        /**
         * Orientierung im World-Koordinatensystem (Ausrichtung vom Spawn aus (vor der offenen Gartenseite mit Blick auf Garten))
         *
         * x geht nach rechts, -x geht nach links
         * y geht nach oben, -y geht nach unten
         * z geht nach hinten (Richtung Cam/"Rückwärtsbewegung"), -z geht nach vorne (von Cam weg/in die Ferne)
         *
         * Falls der gesamte Garten verschoben werden soll, dies erst NACH den parent-Setzungen unten machen!! Sonst ist alles hinüber
         *
         */

        /** Overworld-Setup:
         ** Setup Garten
         */
        garden = loadModel("assets/Garten/finGarden.obj", Math.toRadians(-90.0f), Math.toRadians(90.0f), 0.0f)
            ?: throw IllegalArgumentException("Could not load the garden")
        garden.scale(Vector3f(2.0f))
        garden.rotate(Math.toRadians(180f), 0.0f, Math.toRadians(90.0f))
        garden.preTranslate(Vector3f(0f, 0.4f, -1f))


        /** kleinere Gegenstände:
         ** Setup Schaufel
         */
        shovel =
            loadModel("assets/Schaufel/gardening_shovel/model.obj", Math.toRadians(-90.0f), Math.toRadians(90.0f), 0.0f)
                ?: throw IllegalArgumentException("Could not load the shovel")
        shovel.rotate(Math.toRadians(-90.0f), 0f, 0f)
        shovel.preTranslate(Vector3f(-0.11f, 0.3f, 1.87f)) // x unten/oben, y links/rechts, z nach vorn/zurück
        shovel.scale(Vector3f(0.27f))

        /**
         ** Setup Schnecke
         */
        snail = loadModel("assets/Schnecke/Mesh_Snail.obj", Math.toRadians(-90.0f), Math.toRadians(90.0f), 0.0f)
            ?: throw IllegalArgumentException("Could not load the shovel")
        snail.rotate(0f, Math.toRadians(-30f), Math.toRadians(-90.0f))
        snail.preTranslate(Vector3f(-1.3f, 0.4f, -5.8f)) // x rechts/links, y oben/unten, z nach vorn/zurück
        snail.scale(Vector3f(0.05f))

        /**
         ** Setup Hake
         */
        rake = loadModel("assets/Hake/rake.obj", Math.toRadians(-90.0f), Math.toRadians(90.0f), 0.0f)
            ?: throw IllegalArgumentException("Could not load the rake")
        rake.scale(Vector3f(0.5f))
        rake.preTranslate(Vector3f(0f, 0.69f, -5.8f)) // x rechts/links, y oben/unten, z zurück/nach vorn
        rake.rotate(0f, Math.toRadians(-160.0f), Math.toRadians(-150f))


        /**
         ** Setup Gartenschlauch
         */
        hose = loadModel("assets/Schlauch/model.obj", Math.toRadians(-90.0f), Math.toRadians(90.0f), 0.0f)
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

        ruler = loadModel("assets/Lineal/ruler.obj", Math.toRadians(-90f), Math.toRadians(90f), 0f)
            ?: throw IllegalArgumentException("Could not load the ruler")
        ruler.preTranslate(Vector3f(0f, 0.15f, 15f))
        ruler.rotate(0f, Math.toRadians(90f), Math.toRadians(90f))
        ruler.scale(Vector3f(0.5f))

        //setup camera
        camera = TronCamera(
            custom(window.framebufferWidth, window.framebufferHeight),
            Math.toRadians(90.0f),
            0.1f,
            1000.0f
        )

        camera.parent = bike
        camera.rotate(Math.toRadians(-25.0f), 0.0f, 0.0f)
        camera.translate(Vector3f(0.0f, 5.0f, 8.0f))

        groundColor = Vector3f(0.0f, 1.0f, 0.0f)
        skyColor = Vector3f(1.0f, 1.0f, 1.0f)

        //bike point light
        bikePointLight =
            PointLight("pointLight[${pointLightList.size}]", Vector3f(0.0f, 2.0f, 0.0f), Vector3f(0.0f, 0.5f, 0.0f))
        bikePointLight.parent = bike
        pointLightList.add(bikePointLight)

        //bike spot light
        bikeSpotLight = SpotLight(
            "spotLight[${spotLightList.size}]",
            Vector3f(3.0f, 3.0f, 3.0f),
            Vector3f(0.0f, 1.0f, -2.0f),
            Math.toRadians(20.0f),
            Math.toRadians(30.0f)
        )
        bikeSpotLight.rotate(Math.toRadians(-10.0f), 0.0f, 0.0f)
        bikeSpotLight.parent = bike
        spotLightList.add(bikeSpotLight)

        // additional lights in the scene
        pointLightList.add(
            PointLight(
                "pointLight[${pointLightList.size}]",
                Vector3f(0.0f, 2.0f, 2.0f),
                Vector3f(-10.0f, 2.0f, -10.0f)
            )
        )
        pointLightList.add(
            PointLight(
                "pointLight[${pointLightList.size}]",
                Vector3f(2.0f, 0.0f, 0.0f),
                Vector3f(10.0f, 2.0f, 10.0f)
            )
        )
        spotLightList.add(
            SpotLight(
                "spotLight[${spotLightList.size}]",
                Vector3f(10.0f, 300.0f, 300.0f),
                Vector3f(6.0f, 2.0f, 4.0f),
                Math.toRadians(20.0f),
                Math.toRadians(30.0f)
            )
        )
        spotLightList.last().rotate(Math.toRadians(20f), Math.toRadians(60f), 0f)

        //initial opengl state
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f); GLError.checkThrow()
        glEnable(GL_CULL_FACE); GLError.checkThrow()
        glFrontFace(GL_CCW); GLError.checkThrow()
        glCullFace(GL_BACK); GLError.checkThrow()
        glEnable(GL_DEPTH_TEST); GLError.checkThrow()
        glDepthFunc(GL_LESS); GLError.checkThrow()
    }

    fun render(dt: Float, t: Float) {
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        staticShader.use()
        camera.bind(staticShader)

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
        staticShader.setUniform("shadingColor", groundColor)
        ground.render(staticShader)
        staticShader.setUniform("shadingColor", changingColor)
        bike.render(staticShader)
        staticShader.setUniform("shadingColor", skyColor)
        skybox.render(staticShader)

        garden.render(staticShader)
        shovel.render(staticShader)
        hose.render(staticShader)
        rake.render(staticShader)
        snail.render(staticShader)
        ruler.render(staticShader)
    }

    var jumpVelocity = 0f
    var isJumping = false
    var height = 0f
    val gravityMul = 0.6f
    val groundLevel = 0f

    var isJumpRopeGameRunning = false
    var ropeRotation = Math.toRadians(360f)
    var jumperScore = 0
    var ropeSpeed = 3.5f

    fun update(dt: Float, t: Float) {
        val moveMul = 15.0f
        val rotateMul = 0.5f * Math.PI.toFloat()
        if (window.getKeyState(GLFW_KEY_W)) {
            bike.translate(Vector3f(0.0f, 0.0f, -dt * moveMul))
        }
        if (window.getKeyState(GLFW_KEY_S)) {
            bike.translate(Vector3f(0.0f, 0.0f, dt * moveMul))
        }
        if (window.getKeyState(GLFW_KEY_A) and window.getKeyState(GLFW_KEY_W)) {
            bike.rotate(0.0f, dt * rotateMul, 0.0f)
        }
        if (window.getKeyState(GLFW_KEY_D) and window.getKeyState(GLFW_KEY_W)) {
            bike.rotate(0.0f, -dt * rotateMul, 0.0f)
        }
        if (window.getKeyState(GLFW_KEY_F)) {
            bikeSpotLight.rotate(Math.PI.toFloat() * dt, 0.0f, 0.0f)
        }
        if (window.getKeyState(GLFW_KEY_T)) {
            isJumpRopeGameRunning = true
        }
        if (window.getKeyState(GLFW_KEY_SPACE)) {
            if (!isJumping) {
                jumpVelocity = 15f
                isJumping = true
            }
        }

        if (isJumpRopeGameRunning) {
            ruler.rotateAroundPoint(dt * ropeSpeed, 0f, 0f, Vector3f(0f, 2.5f, 15f))
            ropeRotation -= dt * ropeSpeed
            if (Math.toDegrees(ropeRotation.toDouble()) <= 2) {
                if (height <= 0.5f) {
                    System.out.println("You Lost! Final Score: " + jumperScore)
                    isJumpRopeGameRunning = false
                    jumperScore = 0
                    resetRuler()
                } else {
                    jumperScore++
                }
                ropeSpeed = Math.min(9.5f, 3.5f + (jumperScore * 0.2f))
                ropeRotation = Math.toRadians(360f)
            }
        }
        calculateJump(dt)
    }

    fun onKey(key: Int, scancode: Int, action: Int, mode: Int) {}

    fun onMouseMove(xpos: Double, ypos: Double) {
        if (!firstMouseMove) {
            val yawAngle = (xpos - oldMouseX).toFloat() * 0.002f
            val pitchAngle = (ypos - oldMouseY).toFloat() * 0.0005f
            if (!window.getKeyState(GLFW_KEY_LEFT_ALT)) {
                bike.rotate(0.0f, -yawAngle, 0.0f)
            } else {
                camera.rotateAroundPoint(0.0f, -yawAngle, 0.0f, Vector3f(0.0f, 0.0f, 0.0f))
            }
        } else firstMouseMove = false
        oldMouseX = xpos
        oldMouseY = ypos
    }

    fun cleanup() {}

    fun onMouseScroll(xoffset: Double, yoffset: Double) {
        camera.fov -= Math.toRadians(yoffset.toFloat())
    }

    private fun calculateJump(deltaTime: Float) {
        if (isJumping) {
            val newHeight = height + deltaTime * jumpVelocity
            bike.translate(Vector3f(0f, deltaTime * jumpVelocity, 0f))
            jumpVelocity -= gravityMul
            height = Math.max(0f, newHeight)
            if (height == groundLevel) {
                jumpVelocity = 0f
                isJumping = false
            }
        }
    }

    private fun resetRuler() {
        ruler.setRotation(0f, Math.toRadians(90f), Math.toRadians(90f))
        ruler.scale(Vector3f(0.5f))
    }
}