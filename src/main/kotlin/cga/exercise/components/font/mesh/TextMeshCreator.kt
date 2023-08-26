package cga.exercise.components.font.mesh

import cga.exercise.components.gui.GuiText
import java.io.File
import java.lang.Integer.parseInt

class TextMeshCreator(metaFile: File) {
    companion object {
        val LINE_HEIGHT = 0.03f
        val SPACE_ASCII = 32
    }

    private val metaData: MetaFile

    init {
        metaData = MetaFile(metaFile)
    }

    fun createTextMesh(text: GuiText) {
        val lines = createStructure(text)
        val data = createQuadVertices(text, lines)
        return data
    }

    private fun createStructure(text: GuiText): ArrayList<Line> {
        val chars: CharArray = text.getTextString().toCharArray()
        val lines: ArrayList<Line> = ArrayList<Line>()
        var currentLine = Line(metaData.getSpaceWidth(), text.getFontSize(), text.getMaxLineSize())
        var currentWord = Word(text.getFontSize())

        for (c in chars) {
            val ascii = parseInt(c.toString())

            if (ascii == SPACE_ASCII) {
                val added = currentLine.addWord(currentWord)
                if (!added) {
                    lines.add(currentLine)
                    currentLine = Line(metaData.getSpaceWidth(), text.getFontSize(), text.getMaxLineSize())
                    currentLine.addWord(currentWord)
                }
                currentWord = Word(text.getFontSize())
                continue
            }

            val character: Character = metaData.getCharacter(ascii)
            currentWord.addCharacter(character)
        }
        completeStructure(lines, currentLine, currentWord, text)
        return lines
    }

    private fun completeStructure(lines: ArrayList<Line>, currentLine: Line, currentWord: Word, text: GuiText) {
        var currLine = currentLine
        val added = currentLine.addWord(currentWord)
        if (!added) {
            lines.add(currentLine)
            currLine = Line(metaData.getSpaceWidth(), text.getFontSize(), text.getMaxLineSize())
            currLine.addWord(currentWord)
        }
        lines.add(currLine)
    }

    private fun createQuadVertices(text: GuiText, lines: ArrayList<Line>) {
        text.setNumberOfLines(lines.size)
        var cursorX = 0f
        var cursorY = 0f
        val vertices = ArrayList<Float>()
        val textureCoords = ArrayList<Float>()

        for (line in lines) {
            if (text.isCentered()) {
                cursorX = (line.getMaxLength() - line.getLineLength()) / 2
            }
            for (word in line.getWords()) {
                for (letter in word.getCharacters()) {
                    addVerticesForCharacter(cursorX, cursorY, letter, text.getFontSize(), vertices)
                    addDataToFloatList(
                        textureCoords,
                        letter.xTextureCoord,
                        letter.yTextureCoord,
                        letter.xMaxTextureCoord,
                        letter.yMaxTextureCoord
                    )
                    cursorX += letter.xAdvance * text.getFontSize()
                }
                cursorX += metaData.getSpaceWidth() * text.getFontSize()
            }
            cursorX = 0f
            cursorY += LINE_HEIGHT * text.getFontSize()
        }
        return TextMeshData(vertices.toArray(), textureCoords.toArray())
    }

    private fun addVerticesForCharacter(
        cursorX: Float,
        cursorY: Float,
        character: Character,
        fontSize: Float,
        vertices: ArrayList<Float>
    ) {
        val x = cursorX + (character.xOffset * fontSize)
        val y = cursorY + (character.yOffset * fontSize)
        val maxX = x + (character.sizeX * fontSize)
        val maxY = y + (character.sizeY * fontSize)
        val properX = (2 * x) - 1
        val properY = (-2 * y) + 1
        val properMaxX = (2 * maxX) - 1
        val properMaxY = (-2 * maxY) + 1
        addDataToFloatList(vertices, properX, properY, properMaxX, properMaxY)
    }

    private fun addDataToFloatList(vertices: ArrayList<Float>, x: Float, y: Float, maxX: Float, maxY: Float) {
        vertices.add(x)
        vertices.add(y)

        vertices.add(x)
        vertices.add(maxY)

        vertices.add(maxX)
        vertices.add(maxY)

        vertices.add(maxX)
        vertices.add(maxY)

        vertices.add(maxX)
        vertices.add(y)

        vertices.add(x)
        vertices.add(y)
    }

}