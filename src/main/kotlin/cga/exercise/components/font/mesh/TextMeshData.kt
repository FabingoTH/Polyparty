package cga.exercise.components.font.mesh

/**
 * Stores the vertex data for all the quads on which a text will be rendered.
 */
class TextMeshData(val vertexPositions: FloatArray, val textureCoords: FloatArray) {

    val vertexCount: Int
        get() = vertexPositions.size / 2
}
