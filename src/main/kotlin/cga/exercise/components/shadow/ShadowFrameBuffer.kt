package cga.exercise.components.shadow

import cga.exercise.components.geometry.Transformable
import cga.exercise.components.texture.TextureDepth
import org.joml.Matrix4f
import org.joml.Vector3f
import org.lwjgl.opengl.GL30
import org.lwjgl.opengl.GL32

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
        GL32.glFramebufferTexture(
            GL30.GL_FRAMEBUFFER,
            GL30.GL_DEPTH_ATTACHMENT,
            texture.texID,
            0
        );

        GL30.glDrawBuffer(GL30.GL_NONE);
        GL30.glReadBuffer(GL30.GL_NONE);

        return texture;
    }

    fun cleanup() {
        GL30.glDeleteFramebuffers(fboId)
        depthTex.cleanup()
    }

    fun bindFrameBuffer() {
        GL30.glBindTexture(GL30.GL_TEXTURE_2D, 0);
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, fboId);
        GL30.glViewport(0, 0, width, height);
    }

    fun unbindFrameBuffer() {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        GL30.glViewport(0, 0, 1280, 720);
    }

    fun getDepthTexture(): TextureDepth {
        return depthTex
    }

    fun calculateLightSpaceMatrix(): Matrix4f {
        val projMatrix = Matrix4f().ortho(-10f, 10f, -10f, 10f, 10f, 50f)
        val viewMatrix = Matrix4f().setLookAt(Vector3f(0f, 20f, 0f), Vector3f(0f), Vector3f(0f, 1f, 0f))

        return projMatrix.mul(viewMatrix)
            ?: throw IllegalArgumentException("Error while calculating light space matrix")
    }
}
