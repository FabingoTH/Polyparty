package cga.exercise.components.font.mesh

import cga.exercise.components.gui.GuiText
import cga.exercise.components.texture.Texture2D
import java.io.File

/**
 * Represents a font. It holds the font's texture atlas as well as having the
 * ability to create the quad vertices for any text using this font.
 *
 */
class FontType(private val textureAtlas: Texture2D, fontFile: File) {
    private val loader: TextMeshCreator

    init {
        loader = TextMeshCreator(fontFile)
    }

    fun getTextureAtlas(): Texture2D {
        return textureAtlas
    }

    /**
     * Takes in an unloaded text and calculate all of the vertices for the quads
     * on which this text will be rendered. The vertex positions and texture
     * coords and calculated based on the information from the font file.
     *
     * @param text the unloaded text.
     * @return Information about the vertices of all the quads.
     */
    fun loadText(text: GuiText): TextMeshData {
        return loader.createTextMesh(text)
    }

}