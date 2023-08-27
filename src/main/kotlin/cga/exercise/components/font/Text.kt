package cga.exercise.components.font

import cga.exercise.components.font.mesh.FontType
import cga.exercise.components.gui.GuiText
import cga.exercise.components.shader.TextShader
import org.lwjgl.opengl.GL30

class Text {
    private val textShader = TextShader()
    private val texts: HashMap<FontType, ArrayList<GuiText>> = HashMap()
    private var vaoId = 0

    fun loadText(text: GuiText) {
        val font = text.getFont()
        val data = font.loadText(text)
        loadText(data.getVertexPositions(), data.getTextureCoords())
        text.setMeshInfo(vaoId, data.getVertexCount())

        var textBatch = texts[font]
        if (textBatch == null) {
            textBatch = ArrayList<GuiText>()
            texts[font] = textBatch
        }
        textBatch.add(text)
    }

    fun removeText(text: GuiText) {
        val textBatch = texts[text.getFont()] ?: return
        textBatch.remove(text)
        if (textBatch.isEmpty()) {
            texts.remove(text.getFont())
        }
    }

    fun render() {
        textShader.render(texts)
    }

    private fun loadText(vertexData: FloatArray, textureCoords: FloatArray) {
        vaoId = GL30.glGenVertexArrays()
        GL30.glBindVertexArray(vaoId)

        var vboId = GL30.glGenBuffers()
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vboId)

        GL30.glBufferData(GL30.GL_ARRAY_BUFFER, vertexData, GL30.GL_STATIC_DRAW)
        GL30.glVertexAttribPointer(0, 2, GL30.GL_FLOAT, false, 0, 0)

        vboId = GL30.glGenBuffers()
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vboId)

        GL30.glBufferData(GL30.GL_ARRAY_BUFFER, textureCoords, GL30.GL_STATIC_DRAW)
        GL30.glVertexAttribPointer(1, 2, GL30.GL_FLOAT, false, 0, 0)

        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0)
        GL30.glBindVertexArray(0)

    }
}