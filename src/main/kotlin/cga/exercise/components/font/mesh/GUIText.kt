package cga.exercise.components.font.mesh


import cga.exercise.components.font.rendering.TextMaster
import org.joml.Vector2f
import org.joml.Vector3f

/**
 * Represents a piece of text in the game.
 */
class GUIText(
    val textString: String,
    val fontSize: Double,
    val font: FontType,
    val position: Vector2f,
    val maxLineLength: Double,
    val centered: Boolean
) {
    var mesh = 0
        private set
    var vertexCount = 0
        private set

    var numberOfLines = 0

    private val color: Vector3f = Vector3f(0f, 0f, 0f)

    init {
        TextMaster.loadText(this)
    }

    /**
     * Remove the text from the screen.
     */
    fun remove() {
        TextMaster.removeText(this)
    }

    fun setColor(r: Float, g: Float, b: Float) {
        color.set(r, g, b)
    }

    fun getColor(): Vector3f {
        return color
    }

    fun setMeshInfo(vao: Int, verticesCount: Int) {
        mesh = vao
        vertexCount = verticesCount
    }
}
