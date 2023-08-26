package cga.exercise.components.font.mesh

/**
 * During the loading of a text this represents one word in the text.
 *
 */
class Word(private val fontSize: Float) {
    private val characters: ArrayList<Character> = ArrayList<Character>()
    private var width: Float = 0f

    fun addCharacter(char: Character) {
        characters.add(char)
        width += char.xAdvance * fontSize
    }

    fun getCharacters(): ArrayList<Character> {
        return characters
    }

    fun getWordWidth(): Float {
        return width
    }
}