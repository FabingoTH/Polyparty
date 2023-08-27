package cga.exercise.components.font.mesh

/**
 * During the loading of a text this represents one word in the text.
 */
class Word(private val fontSize: Double) {
    val characters: MutableList<Character> = ArrayList()

    var wordWidth = 0.0
        private set

    /**
     * Adds a character to the end of the current word and increases the screen-space width of the word.
     *
     * @param character - the character to be added.
     */
    fun addCharacter(character: Character) {
        characters.add(character)
        wordWidth += character.xAdvance * fontSize
    }
}
