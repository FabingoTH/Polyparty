package cga.exercise.components.font.rendering

import cga.exercise.components.font.mesh.FontType
import cga.exercise.components.font.mesh.GUIText
import cga.exercise.components.shader.FontShader
import org.lwjgl.opengl.GL30

class FontRenderer {
    private val shader: FontShader = FontShader()

    fun render(texts: Map<FontType, List<GUIText>>) {
        prepare()
        for (font in texts.keys) {
            font.textureAtlas.bind(0)
            for (text in texts[font]!!) {
                renderText(text)
            }
        }
        endRendering()
    }

    private fun prepare() {
        GL30.glEnable(GL30.GL_BLEND)
        GL30.glBlendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA)
        GL30.glDisable(GL30.GL_DEPTH_TEST)
        shader.use()
    }

    private fun renderText(text: GUIText) {
        GL30.glBindVertexArray(text.mesh)
        GL30.glEnableVertexAttribArray(0)
        GL30.glEnableVertexAttribArray(1)
        shader.setUniform("color", text.getColor())
        shader.setUniform("position", text.position)
        GL30.glDrawArrays(GL30.GL_TRIANGLES, 0, text.vertexCount)
        GL30.glDisableVertexAttribArray(0)
        GL30.glDisableVertexAttribArray(1)
        GL30.glBindVertexArray(0)
    }

    private fun endRendering() {
        shader.cleanup()
        GL30.glDisable(GL30.GL_BLEND)
        GL30.glEnable(GL30.GL_DEPTH_TEST)
    }
}
