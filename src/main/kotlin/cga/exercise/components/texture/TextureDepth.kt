package cga.exercise.components.texture

import cga.framework.GLError.checkEx
import org.lwjgl.opengl.GL30.*


class TextureDepth(width: Int, height: Int) {
    var texID: Int = -1
        private set

    init {
        try {
            processTexture(width, height)
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
        }
    }

    private fun processTexture(width: Int, height: Int) {
        val tex = glGenTextures()
        if (tex == 0) {
            throw Exception("OpenGL texture object creation failed.")
        }
        glBindTexture(GL_TEXTURE_2D, tex)
        glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, width, height, 0, GL_DEPTH_COMPONENT, GL_FLOAT, 0)

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_NONE)

        //should be done but explained before
        if (checkEx()) {
            glDeleteTextures(tex)
            throw Exception("glTexImage2D call failed.")
        }

        glBindTexture(GL_TEXTURE_2D, 0)
        texID = tex
    }

    fun bind(textureUnit: Int) {
        if (texID != 0) {
            glActiveTexture(GL_TEXTURE0 + textureUnit)
            glBindTexture(GL_TEXTURE_2D, texID)
        }
    }

    fun unbind() {
        glBindTexture(GL_TEXTURE_2D, 0)
    }

    fun cleanup() {
        unbind()
        if (texID != 0) {
            glDeleteTextures(texID)
            texID = 0
        }
    }
}