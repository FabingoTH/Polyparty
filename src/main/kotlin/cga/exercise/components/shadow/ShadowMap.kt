package cga.exercise.components.shadow

import cga.exercise.components.geometry.Renderable
import cga.exercise.components.geometry.Transformable
import cga.exercise.components.shader.ShaderProgram
import cga.exercise.components.texture.TextureDepth
import org.joml.Matrix4f
import org.lwjgl.opengl.GL30

class ShadowMap(width: Int, height: Int, light: Transformable) {
    private val shader = ShaderProgram("assets/shaders/shadow_vert.glsl", "assets/shaders/shadow_frag.glsl")
    private val frameBuffer: ShadowFrameBuffer
    private var lightSpaceMatrix: Matrix4f? = null

    init {
        frameBuffer = ShadowFrameBuffer(width, height, light)
    }

    fun render(objList: MutableList<Renderable>) {
        lightSpaceMatrix = frameBuffer.calculateLightSpaceMatrix()

        GL30.glDisable(GL30.GL_ALPHA_TEST)
        shader.setUniform("lightspace_matrix", lightSpaceMatrix!!, false)

        frameBuffer.bindFrameBuffer()

        for (obj in objList) {
            obj.render(shader)
        }

        GL30.glEnable(GL30.GL_ALPHA_TEST)
        frameBuffer.unbindFrameBuffer()
    }

    fun prepareForShader(shader: ShaderProgram) {
        shader.setUniform("lightspace_matrix", lightSpaceMatrix!!, false)
        shader.setUniform("shadowMap", frameBuffer.depthTex)
    }

    fun getDepthTexture(): TextureDepth {
        return frameBuffer.depthTex
    }

}