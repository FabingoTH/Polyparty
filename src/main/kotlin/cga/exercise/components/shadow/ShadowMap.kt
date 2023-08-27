package cga.exercise.components.shadow

import cga.exercise.components.geometry.Renderable
import cga.exercise.components.geometry.Transformable
import cga.exercise.components.shader.ShaderProgram
import cga.exercise.components.texture.TextureDepth
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30

class ShadowMap(width: Int, height: Int, light: Transformable) {
    private val shader = ShaderProgram("assets/shaders/shadow_vert.glsl", "assets/shaders/shadow_frag.glsl")
    private val frameBuffer: ShadowFrameBuffer
    private var vaoId = 0

    init {
        frameBuffer = ShadowFrameBuffer(width, height, light)
    }

    fun render(objList: MutableList<Renderable>) {

        frameBuffer.bindFrameBuffer()

        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)
        GL30.glDisable(GL30.GL_ALPHA_TEST)
        shader.setUniform("lightspace_matrix", frameBuffer.calculateLightSpaceMatrix(), false)

        for (obj in objList) {
            obj.render(shader)
        }

        GL30.glEnable(GL30.GL_ALPHA_TEST)
        frameBuffer.unbindFrameBuffer()
    }

    fun prepareForShader(shader: ShaderProgram) {
        shader.setUniform("lightspace_matrix", frameBuffer.calculateLightSpaceMatrix(), false)
        shader.setUniform("shadowMap", frameBuffer.depthTex)
    }

    fun getDepthTexture(): TextureDepth {
        return frameBuffer.depthTex
    }

}