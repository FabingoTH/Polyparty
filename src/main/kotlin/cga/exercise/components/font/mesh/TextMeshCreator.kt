package cga.exercise.components.font.mesh

import java.io.File

class TextMeshCreator(metaFile: File) {
    private val metaData: MetaFile

    init {
        metaData = MetaFile(metaFile)
    }

    fun createTextMesh(text: GUIText): TextMeshData {
        val lines = createStructure(text)
        return createQuadVertices(text, lines)
    }

    private fun createStructure(text: GUIText): List<Line> {
        val chars = text.textString.toCharArray()
        val lines: MutableList<Line> = ArrayList()
        var currentLine = Line(metaData.spaceWidth, text.fontSize, text.maxLineLength)
        var currentWord = Word(text.fontSize.toDouble())
        for (c in chars) {
            val ascii = c.code
            if (ascii == SPACE_ASCII) {
                val added = currentLine.attemptToAddWord(currentWord)
                if (!added) {
                    lines.add(currentLine)
                    currentLine = Line(metaData.spaceWidth, text.fontSize, text.maxLineLength)
                    currentLine.attemptToAddWord(currentWord)
                }
                currentWord = Word(text.fontSize)
                continue
            }
            val character = metaData.getCharacter(ascii)
                ?: throw IllegalArgumentException("Could not get character for that ASCII code")
            currentWord.addCharacter(character)
        }
        completeStructure(lines, currentLine, currentWord, text)
        return lines
    }

    private fun completeStructure(lines: MutableList<Line>, currentLine: Line, currentWord: Word, text: GUIText) {
        var currentLine = currentLine
        val added = currentLine.attemptToAddWord(currentWord)
        if (!added) {
            lines.add(currentLine)
            currentLine = Line(metaData.spaceWidth, text.fontSize.toDouble(), text.maxLineLength.toDouble())
            currentLine.attemptToAddWord(currentWord)
        }
        lines.add(currentLine)
    }

    private fun createQuadVertices(text: GUIText, lines: List<Line>): TextMeshData {
        text.numberOfLines = lines.size
        var cursorX = 0.0
        var cursorY = 0.0
        val vertices: MutableList<Float> = ArrayList()
        val textureCoords: MutableList<Float> = ArrayList()
        for (line in lines) {
            if (text.centered) {
                cursorX = (line.maxLength - line.lineLength) / 2
            }
            for (word in line.getWords()) {
                for (letter in word.characters) {
                    addVerticesForCharacter(cursorX, cursorY, letter, text.fontSize.toDouble(), vertices)
                    addToFloatArray(
                        textureCoords, letter.xTextureCoord, letter.yTextureCoord,
                        letter.xMaxTextureCoord, letter.yMaxTextureCoord
                    )
                    cursorX += letter.xAdvance * text.fontSize
                }
                cursorX += metaData.spaceWidth * text.fontSize
            }
            cursorX = 0.0
            cursorY += LINE_HEIGHT * text.fontSize
        }
        return TextMeshData(vertices.toFloatArray(), textureCoords.toFloatArray())
    }

    private fun addVerticesForCharacter(
        cursorX: Double, cursorY: Double, character: Character, fontSize: Double,
        vertices: MutableList<Float>
    ) {
        val x: Double = cursorX + character.xOffset * fontSize
        val y: Double = cursorY + character.yOffset * fontSize
        val maxX: Double = x + character.sizeX * fontSize
        val maxY: Double = y + character.sizeY * fontSize
        val properX = 2 * x - 1
        val properY = -2 * y + 1
        val properMaxX = 2 * maxX - 1
        val properMaxY = -2 * maxY + 1
        addToFloatArray(vertices, properX, properY, properMaxX, properMaxY)
    }

    companion object {
        const val LINE_HEIGHT = 0.03
        const val SPACE_ASCII = 32
        private fun addToFloatArray(floatArray: MutableList<Float>, x: Double, y: Double, maxX: Double, maxY: Double) {
            floatArray.add(x.toFloat())
            floatArray.add(y.toFloat())
            floatArray.add(x.toFloat())
            floatArray.add(maxY.toFloat())
            floatArray.add(maxX.toFloat())
            floatArray.add(maxY.toFloat())
            floatArray.add(maxX.toFloat())
            floatArray.add(maxY.toFloat())
            floatArray.add(maxX.toFloat())
            floatArray.add(y.toFloat())
            floatArray.add(x.toFloat())
            floatArray.add(y.toFloat())
        }
    }
}
