package cga.exercise.game

import cga.exercise.game.scene.AScene
import cga.exercise.game.scene.JumpRythmGameScene
import cga.framework.GameWindow
import org.lwjgl.glfw.GLFW

/*
  Created by Fabian on 16.09.2017.
 */
class Game(
    width: Int,
    height: Int,
    fullscreen: Boolean = false,
    vsync: Boolean = false,
    title: String = "Testgame",
    GLVersionMajor: Int = 3,
    GLVersionMinor: Int = 3
) : GameWindow(width, height, fullscreen, vsync, GLVersionMajor, GLVersionMinor, title, 4, 120.0f) {


    private lateinit var activeScene: GameType
    private lateinit var mainMenuScene: LobbyScene
    private lateinit var jumpRopeScene: JumpRythmGameScene
    private lateinit var memoryScene: MemorizeGameScene
    private lateinit var racingScene: RacingGameScene
    private lateinit var actualScene: AScene

    init {
        setCursorVisible(false)
        activeScene = GameType.LOBBY

        mainMenuScene = LobbyScene(this)
        jumpRopeScene = JumpRythmGameScene(this)
        memoryScene = MemorizeGameScene(this)
        racingScene = RacingGameScene(this)

        actualScene = mainMenuScene
    }

    private fun activeScene(): AScene {
        return when (activeScene) {
            GameType.LOBBY -> mainMenuScene
            GameType.JUMP_ROPE -> jumpRopeScene
            GameType.MEMORIZE -> memoryScene
            GameType.RACING -> racingScene
        }
    }

    override fun changeScene(newScene: GameType) {
        activeScene = newScene
    }

    override fun shutdown() {}

    override fun update(dt: Float, t: Float) {

        activeScene().update(dt, t)

        if (activeScene().window.getKeyState(GLFW.GLFW_KEY_1)) {
            changeScene(GameType.LOBBY)
        }
        if (activeScene().window.getKeyState(GLFW.GLFW_KEY_2)) {
            changeScene(GameType.JUMP_ROPE)
        }
        if (activeScene().window.getKeyState(GLFW.GLFW_KEY_3)) {
            changeScene(GameType.MEMORIZE)
        }
        if (activeScene().window.getKeyState(GLFW.GLFW_KEY_4)) {
            changeScene(GameType.RACING)
        }
    }


    override fun render(dt: Float, t: Float) {
        activeScene().render(dt, t)
    }

    override fun onMouseMove(xpos: Double, ypos: Double) {
        activeScene().onMouseMove(xpos, ypos)
    }

    override fun onMouseScroll(xoffset: Double, yoffset: Double) {
        activeScene().onMouseScroll(xoffset, yoffset)
    }

    override fun onKey(key: Int, scancode: Int, action: Int, mode: Int) {
        activeScene().onKey(key, scancode, action, mode)
    }

}