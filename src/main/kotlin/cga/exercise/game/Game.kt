package cga.exercise.game

import cga.exercise.game.scene.AScene
import cga.exercise.game.scene.JumpRythmGameScene
import cga.exercise.game.scene.MemorizeGameScene
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
    title: String = "PolyParty",
    GLVersionMajor: Int = 3,
    GLVersionMinor: Int = 3
) : GameWindow(width, height, fullscreen, vsync, GLVersionMajor, GLVersionMinor, title, 4, 120.0f) {


    private var activeScene: GameType
    private var mainMenuScene: LobbyScene
    private var jumpRopeScene: JumpRythmGameScene
    private var memoryScene: MemorizeGameScene
    private var racingScene: RacingGameScene
    //private var actualScene: AScene

    init {
        setCursorVisible(false)
        activeScene = GameType.MEMORIZE

        mainMenuScene = LobbyScene(this)
        jumpRopeScene = JumpRythmGameScene(this)
        memoryScene = MemorizeGameScene(this)
        racingScene = RacingGameScene(this)

        //actualScene = memoryScene
    }

    private fun activeScene(): AScene {
        return when (activeScene) {
            GameType.LOBBY -> mainMenuScene
            GameType.JUMP_ROPE -> jumpRopeScene
            GameType.MEMORIZE -> memoryScene
            GameType.RACING -> racingScene
        }
    }

    override fun changeScene(newGame: GameType) {
        activeScene = newGame
    }

    override fun shutdown() {}

    override fun update(dt: Float, t: Float) {

        activeScene().update(dt, t)

        if (activeScene().window.getKeyState(GLFW.GLFW_KEY_O)) {
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