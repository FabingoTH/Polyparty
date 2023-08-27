package cga.exercise.components.geometry

import cga.exercise.components.gui.GuiElement
import cga.exercise.components.shader.ShaderProgram
import org.lwjgl.opengl.GL30.*

class GuiQuad() {
    private var vaoId = 0
    private var vboId = 0

    private val vertexData = floatArrayOf(-1f, 1f, -1f, -1f, 1f, 1f, 1f, -1f)

    init {
        vaoId = glGenVertexArrays()
        glBindVertexArray(vaoId)

        vboId = glGenBuffers()
        glBindBuffer(GL_ARRAY_BUFFER, vboId)

        glBufferData(GL_ARRAY_BUFFER, vertexData, GL_STATIC_DRAW)
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0)

        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glBindVertexArray(0)
    }

    fun render(quadShader: ShaderProgram, guis: List<GuiElement>) {
        glBindVertexArray(vaoId)
        glEnableVertexAttribArray(0)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glDisable(GL_DEPTH_TEST)
        for (gui: GuiElement in guis) {
            gui.prepareTexture(quadShader)
            glDrawArrays(GL_TRIANGLE_STRIP, 0, vertexData.size / 2)
        }
        glDisable(GL_BLEND)
        glEnable(GL_DEPTH_TEST)
        glDisableVertexAttribArray(0)
        glBindVertexArray(0)
    }

}