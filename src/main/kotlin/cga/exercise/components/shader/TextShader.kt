package cga.exercise.components.shader

import cga.exercise.components.font.mesh.FontType
import cga.exercise.components.gui.GuiText
import org.lwjgl.opengl.GL30

class TextShader {
    private val textShader: ShaderProgram =
        ShaderProgram("assets/shaders/font_vert.glsl", "assets/shaders/font_frag.glsl")

    fun render(texts: HashMap<FontType, ArrayList<GuiText>>) {
        GL30.glEnable(GL30.GL_BLEND)
        GL30.glBlendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA)
        GL30.glDisable(GL30.GL_DEPTH_TEST)

        textShader.use()

        for (font in texts.keys) {
            if (texts[font] == null) return
            font.getTextureAtlas().bind(0)

            for (text in texts[font]!!) {
                println(font.getTextureAtlas().getTexId())
                renderText(text)
            }

        }


        GL30.glDisable(GL30.GL_BLEND)
        GL30.glEnable(GL30.GL_DEPTH_TEST)
    }

    private fun renderText(text: GuiText) {
        println(text.getMesh())
        GL30.glBindVertexArray(text.getMesh())
        GL30.glEnableVertexAttribArray(0)
        GL30.glEnableVertexAttribArray(1)

        textShader.setUniform("color", text.getColor())
        textShader.setUniform("translation", text.getPosition())

        GL30.glDrawArrays(GL30.GL_TRIANGLES, 0, text.getVertexCount())

        GL30.glDisableVertexAttribArray(0)
        GL30.glDisableVertexAttribArray(1)
        GL30.glBindVertexArray(0)
    }

}