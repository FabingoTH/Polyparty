package cga.exercise.components.gui

import cga.exercise.components.shader.ShaderProgram
import cga.exercise.components.texture.Texture2D
import org.joml.Math
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f

class GuiElement(private val texture: Texture2D, var position: Vector2f, var scale: Vector2f) {
    fun getTexture(): Texture2D {
        return texture
    }

    fun prepareTexture(shader: ShaderProgram) {
        texture.bind(0)
        val transformationMatrix = createTransformationMatrix()
        shader.setUniform("trans_matrix", transformationMatrix, false)
    }

    private fun createTransformationMatrix(): Matrix4f {
        val matrix = Matrix4f()
        matrix.translate(Vector3f(position, 0f))
        matrix.rotateZ(Math.toRadians(180f))
        matrix.scale(Vector3f(scale.x, scale.y, 1f))
        return matrix
    }
}