package cga.exercise.components.gui

import cga.exercise.components.font.mesh.FontType
import org.joml.Vector2f
import org.joml.Vector3f

data class GuiText(
    private val text: String,
    private val fontSize: Float,
    private val font: FontType,
    private val position: Vector2f,
    private val maxLineLength: Float,
    private val centered: Boolean
) {
    private val color: Vector3f = Vector3f(0f)
    private var textMeshVao: Int = 0
    private var vertexCount: Int = 0

    private val lineMaxSize: Float = 0f
    private var numberOfLines: Int = 0

    fun getFont(): FontType {
        return font
    }

    fun setColor(r: Float, g: Float, b: Float) {
        color.set(r, g, b)
    }

    fun getColor(): Vector3f {
        return color
    }

    fun getNumberOfLines(): Int {
        return numberOfLines
    }

    fun getPosition(): Vector2f {
        return position
    }

    fun getMesh(): Int {
        return textMeshVao
    }

    fun setMeshInfo(vao: Int, verticesCount: Int) {
        textMeshVao = vao
        vertexCount = verticesCount
    }

    fun getVertexCount(): Int {
        return vertexCount
    }

    fun getFontSize(): Float {
        return fontSize
    }

    fun setNumberOfLines(number: Int) {
        numberOfLines = number
    }

    fun isCentered(): Boolean {
        return centered
    }

    fun getMaxLineSize(): Float {
        return lineMaxSize
    }

    fun getTextString(): String {
        return text
    }


}