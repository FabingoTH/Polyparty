package cga.exercise.components.shadow

import cga.exercise.components.geometry.Transformable
import cga.exercise.components.texture.TextureDepth
import org.joml.Matrix4f
import org.joml.Vector3f
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30

class ShadowFrameBuffer(private val width: Int, private val height: Int, private val light: Transformable) {
    var fboId = 0
        private set
    val depthTex: TextureDepth

    init {
        fboId = createFrameBuffer()
        depthTex = createDepthBufferAttachment()

        if (GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) != GL30.GL_FRAMEBUFFER_COMPLETE) {
            throw Exception("Framebuffer is in an invalid state!")
        }

        unbindFrameBuffer()
    }

    private fun createFrameBuffer(): Int {
        val frameBuffer = GL30.glGenFramebuffers();
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, frameBuffer);
        return frameBuffer
    }

    private fun createDepthBufferAttachment(): TextureDepth {
        val texture = TextureDepth(width, height)
        GL30.glFramebufferTexture2D(
            GL30.GL_FRAMEBUFFER,
            GL30.GL_DEPTH_ATTACHMENT,
            GL30.GL_TEXTURE_2D,
            texture.texID,
            0
        );

        GL30.glDrawBuffer(GL30.GL_NONE);
        GL30.glReadBuffer(GL30.GL_NONE);

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0)

        return texture;
    }

    fun cleanup() {
        GL30.glDeleteFramebuffers(fboId)
        depthTex.cleanup()
    }

    fun bindFrameBuffer() {
        GL30.glBindTexture(GL30.GL_TEXTURE_2D, 0)
        GL30.glViewport(0, 0, width, height);
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, fboId);
        GL30.glClear(GL11.GL_DEPTH_BUFFER_BIT)
    }

    fun unbindFrameBuffer() {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        GL30.glViewport(0, 0, 1280, 720);
        GL30.glClear(GL30.GL_COLOR_BUFFER_BIT or GL30.GL_DEPTH_BUFFER_BIT)
    }

    fun getDepthTexture(): TextureDepth {
        return depthTex
    }

    fun calculateLightSpaceMatrix(): Matrix4f {
        val projMatrix = Matrix4f().ortho(-10f, 10f, -10f, 10f, 1f, 7.5f)
        val viewMatrix = Matrix4f().setLookAt(light.getWorldPosition(), Vector3f(0f), Vector3f(0f, 1f, 0f))

        return projMatrix.mul(viewMatrix)
            ?: throw IllegalArgumentException("Error while calculating light space matrix")
    }
}
