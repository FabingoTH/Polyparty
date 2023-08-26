package cga.exercise.components.font.mesh

/**
 * Represents a line of text during loading of a text
 */
class Line(spaceWidth: Float, fontSize: Float, private val maxLength: Float) {
    private var spaceSize: Float

    private val words: ArrayList<Word> = ArrayList<Word>()
    private var currentLineLength: Float = 0f

    init {
        spaceSize = spaceWidth * fontSize
    }

    /**
     * Attempt to add a word to the line. If the line can fit the word in
     * without reaching the maximum line length then the word is added and the
     * line length increased.
     *
     * @param word the word to try to add.
     * @return true if the word has successfully been added to the line.
     */
    fun addWord(word: Word): Boolean {
        var additionalLength: Float = word.getWordWidth()
        additionalLength += if (words.isEmpty()) 0f else spaceSize

        if (currentLineLength + additionalLength <= maxLength) {
            words.add(word)
            currentLineLength += additionalLength
            return true
        }
        return false
    }

    fun getMaxLength(): Float {
        return maxLength
    }

    fun getLineLength(): Float {
        return currentLineLength
    }

    fun getWords(): ArrayList<Word> {
        return words
    }
}