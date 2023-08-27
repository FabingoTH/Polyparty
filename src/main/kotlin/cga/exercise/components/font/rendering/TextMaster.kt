package cga.exercise.components.font.rendering

import cga.exercise.components.font.mesh.FontType
import cga.exercise.components.font.mesh.GUIText
import cga.exercise.components.font.mesh.TextMeshData
import org.lwjgl.opengl.GL30

object TextMaster {
    private val texts: MutableMap<FontType, MutableList<GUIText>> = HashMap()
    // private var renderer: FontRenderer = FontRenderer()

    fun render() {
        //renderer.render(texts)
    }

    fun loadText(text: GUIText) {
        val font: FontType = text.font
        val data: TextMeshData = font.loadText(text)
        val vao = loadToVAO(data)
        text.setMeshInfo(vao, data.vertexCount)
        var textBatch: MutableList<GUIText>? = texts[font]
        if (textBatch == null) {
            textBatch = ArrayList<GUIText>()
            texts[font] = textBatch
        }
        textBatch.add(text)
    }

    private fun loadToVAO(data: TextMeshData): Int {
        val vaoId = GL30.glGenVertexArrays()
        GL30.glBindVertexArray(vaoId)

        var vboId = GL30.glGenBuffers()
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vboId)
        GL30.glBufferData(GL30.GL_ARRAY_BUFFER, data.vertexPositions, GL30.GL_STATIC_DRAW)
        GL30.glVertexAttribPointer(0, 2, GL30.GL_FLOAT, false, 0, 0)

        vboId = GL30.glGenBuffers()
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vboId)
        GL30.glBufferData(GL30.GL_ARRAY_BUFFER, data.textureCoords, GL30.GL_STATIC_DRAW)
        GL30.glVertexAttribPointer(1, 2, GL30.GL_FLOAT, false, 0, 0)

        return vaoId
    }

    fun removeText(text: GUIText) {
        val textBatch: MutableList<GUIText>? = texts[text.font]
        textBatch!!.remove(text)
        if (textBatch.isEmpty()) {
            texts.remove(text.font)
        }
    }
}
