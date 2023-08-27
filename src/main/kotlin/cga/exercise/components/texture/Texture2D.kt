package cga.exercise.components.texture

import cga.framework.GLError.checkEx
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.EXTTextureFilterAnisotropic
import org.lwjgl.opengl.GL30.*
import org.lwjgl.stb.STBImage
import java.nio.ByteBuffer


/**
 * Created by Fabian on 16.09.2017.
 */
class Texture2D(imageData: ByteBuffer, width: Int, height: Int, genMipMaps: Boolean) : ITexture {
    private var texID: Int = -1
        private set

    init {
        try {
            processTexture(imageData, width, height, genMipMaps)
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
        }
    }

    companion object {
        //create texture from file
        //don't support compressed textures for now
        //instead stick to pngs
        operator fun invoke(path: String, genMipMaps: Boolean): Texture2D {
            val x = BufferUtils.createIntBuffer(1)
            val y = BufferUtils.createIntBuffer(1)
            val readChannels = BufferUtils.createIntBuffer(1)
            //flip y coordinate to make OpenGL happy
            STBImage.stbi_set_flip_vertically_on_load(true)
            val imageData = STBImage.stbi_load(path, x, y, readChannels, 4)
                ?: throw Exception("Image file \"" + path + "\" couldn't be read:\n" + STBImage.stbi_failure_reason())

            try {
                return Texture2D(imageData, x.get(), y.get(), genMipMaps)
            } catch (ex: java.lang.Exception) {
                ex.printStackTrace()
                throw ex
            } finally {
                STBImage.stbi_image_free(imageData)
            }
        }
    }

    override fun processTexture(imageData: ByteBuffer, width: Int, height: Int, genMipMaps: Boolean) {
        val tex = glGenTextures()
        if (tex == 0) {
            throw Exception("OpenGL texture object creation failed.")
        }
        glBindTexture(GL_TEXTURE_2D, tex)
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, imageData)
        //should be done but explained before
        if (checkEx()) {
            glDeleteTextures(tex)
            throw Exception("glTexImage2D call failed.")
        }
        //explain mipmaps and their creation
        if (genMipMaps) {
            glGenerateMipmap(GL_TEXTURE_2D)
            if (checkEx()) {
                glDeleteTextures(tex)
                throw Exception("Mipmap creation failed.")
            }
        }
        glBindTexture(GL_TEXTURE_2D, 0)
        texID = tex
    }

    override fun setTexParams(wrapS: Int, wrapT: Int, minFilter: Int, magFilter: Int) {
        bind(0)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, wrapS)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, wrapT)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, minFilter)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, magFilter)
        //Try out anisotopic filtering: aflevel e.g. 16.0f
        glTexParameterf(GL_TEXTURE_2D, EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT, 16.0f)
        if (checkEx()) {
            unbind()
            throw Exception("Setting texture params failed.")
        }
        unbind()
    }

    override fun bind(textureUnit: Int) {
        if (texID != 0) {
            glActiveTexture(GL_TEXTURE0 + textureUnit)
            glBindTexture(GL_TEXTURE_2D, texID)
        }
    }

    fun getTexId(): Int {
        return texID
    }

    override fun unbind() {
        glBindTexture(GL_TEXTURE_2D, 0)
    }

    override fun cleanup() {
        unbind()
        if (texID != 0) {
            glDeleteTextures(texID)
            texID = 0
        }
    }
}