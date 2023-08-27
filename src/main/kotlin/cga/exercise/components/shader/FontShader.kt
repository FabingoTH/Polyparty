package cga.exercise.components.shader

import org.joml.Vector2f
import org.joml.Vector3f


class FontShader() :
    ShaderProgram("assets/shaders/font_vert.glsl", "assets/shaders/font_frag.glsl") {

    fun loadUniforms(color: Vector3f, translation: Vector2f) {
        setUniform("color", color)
        setUniform("translation", translation)
    }
}
