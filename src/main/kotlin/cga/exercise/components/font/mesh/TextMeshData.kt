package cga.exercise.components.font.mesh

/**
 * Stores the vertex data for all the quads on which a text will be rendered.
 */
class TextMeshData(private val vertexPositions: FloatArray, private val textureCoords: FloatArray) {
    fun getVertexPositions(): FloatArray {
        return vertexPositions
    }

    fun getTextureCoords(): FloatArray {
        return textureCoords
    }

    fun getVertexCount(): Int {
        return vertexPositions.size / 2
    }
}