package cga.exercise.components.font.mesh

/**
 * Represents a line of text during the loading of a text.
 */
class Line(spaceWidth: Double, fontSize: Double, val maxLength: Double) {
    private val spaceSize: Double
    private val words: MutableList<Word> = ArrayList()

    var lineLength = 0.0
        private set

    init {
        spaceSize = spaceWidth * fontSize
    }

    /**
     * Attempt to add a word to the line. If the line can fit the word in
     * without reaching the maximum line length then the word is added and the
     * line length increased.
     *
     * @param word the word to try to add.
     * @return `true` if the word has successfully been added to the line.
     */
    fun attemptToAddWord(word: Word): Boolean {
        var additionalLength = word.wordWidth
        additionalLength += if (words.isNotEmpty()) spaceSize else 0.0
        return if (lineLength + additionalLength <= maxLength) {
            words.add(word)
            lineLength += additionalLength
            true
        } else {
            false
        }
    }

    /**
     * @return The list of words in the line.
     */
    fun getWords(): List<Word> {
        return words
    }
}
