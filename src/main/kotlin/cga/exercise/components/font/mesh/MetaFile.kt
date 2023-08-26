package cga.exercise.components.font.mesh

import java.io.BufferedReader
import java.io.File
import java.io.IOException

class MetaFile(file: File) {
    private val PAD_TOP = 0
    private val PAD_LEFT = 1
    private val PAD_BOTTOM = 2
    private val PAD_RIGHT = 3

    private val DESIRED_PADDING = 3

    private val SPLITTER = " "
    private val NUMBER_SEPERATOR = ","

    private val aspectRatio: Float = 16f / 9f

    private var verticalPerPixelSize: Float = 0.0f
    private var horizontalPerPixelSize: Float = 0.0f
    private var spaceWidth: Float = 0.0f
    private var padding: IntArray = intArrayOf()
    private var paddingWidth: Int = 0
    private var paddingHeight: Int = 0

    private val metaData = HashMap<Int, Character>()

    private lateinit var reader: BufferedReader
    private var values = HashMap<String, String>()

    init {
        openFile(file)
        loadPaddingData()
        loadLineSizes()
        val imageWidth = getValueOfVariable("scaleW")
        loadCharacterData(imageWidth)
        close()
    }

    fun getSpaceWidth(): Float {
        return spaceWidth
    }

    fun getCharacter(ascii: Int): Character {
        return metaData[ascii] ?: throw IllegalArgumentException("Character with that ASCII code not found")
    }

    /**
     * Read in the next line and store the variable values
     * @return true if the end of the file hasn't been reached
     */
    private fun processNextLine(): Boolean {
        values = HashMap<String, String>()
        var line: String? = null
        try {
            line = reader.readLine()
        } catch (_: IOException) {
        }

        if (line == null) return false

        for (part: String in line.split(SPLITTER)) {
            val valuePairs: List<String> = part.split("=")
            if (valuePairs.size == 2) {
                values[valuePairs[0]] = valuePairs[1]
            }
        }
        return true
    }

    /**
     * Gets the int value of the variable with the given name on the current line
     *
     * @param variable the name of the variable
     * @return the value of the variable
     */
    private fun getValueOfVariable(variable: String): Int {
        return Integer.parseInt(values[variable])
    }

    /**
     * Gets the array of ints associated with a variable on the current line.
     *
     * @param variable
     *            - the name of the variable.
     * @return The int array of values associated with the variable.
     */
    private fun getValuesOfVariable(variable: String): IntArray {
        val numbers: List<String> = values[variable]?.split(NUMBER_SEPERATOR)
            ?: throw IllegalArgumentException("Could not find values for that variable")
        val actualValues: IntArray = IntArray(numbers.size)

        for (i in 0..actualValues.size) {
            actualValues[i] = Integer.parseInt(numbers[i])
        }
        return actualValues
    }

    /**
     * Closes the font file after finishing reading.
     */
    private fun close() {
        try {
            reader.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * Opens the font file, ready for reading.
     *
     * @param file
     *            - the font file.
     */
    private fun openFile(file: File) {
        try {
            reader = file.bufferedReader()
        } catch (e: Exception) {
            e.printStackTrace()
            System.err.println("Could not read the font meta file!")
        }
    }


    /**
     * Loads the data about how much padding is used around each character in
     * the texture atlas.
     */
    private fun loadPaddingData() {
        processNextLine()
        padding = getValuesOfVariable("padding")
        paddingWidth = padding[PAD_LEFT] + padding[PAD_RIGHT]
        paddingHeight = padding[PAD_TOP] + padding[PAD_BOTTOM]
    }

    /**
     * Loads information about the line height for this font in pixels, and uses
     * this as a way to find the conversion rate between pixels in the texture
     * atlas and screen-space.
     */
    private fun loadLineSizes() {
        processNextLine()
        val lineHeightPixels = getValueOfVariable("lineHeight") - paddingHeight
        verticalPerPixelSize = TextMeshCreator.LINE_HEIGHT / lineHeightPixels.toFloat()
        horizontalPerPixelSize = verticalPerPixelSize / aspectRatio
    }

    /**
     * Loads in data about each character and stores the data in the
     * Character class.
     *
     * @param imageWidth the width of the texture atlas in pixels.
     */
    private fun loadCharacterData(imageWidth: Int) {
        processNextLine()
        processNextLine()
        while (processNextLine()) {
            val c = loadCharacter(imageWidth)
            if (c != null) {
                metaData[c.id] = c
            }
        }
    }

    /**
     * Loads all the data about one character in the texture atlas and converts
     * it all from 'pixels' to 'screen-space' before storing. The effects of
     * padding are also removed from the data.
     *
     * @param imageSize the size of the texture atlas in pixels.
     * @return The data about the character.
     */
    private fun loadCharacter(imageSize: Int): Character? {
        val id = getValueOfVariable("id")
        if (id == TextMeshCreator.SPACE_ASCII) {
            spaceWidth = (getValueOfVariable("xadvance") - paddingWidth) * horizontalPerPixelSize
            return null
        }
        val xTex = (getValueOfVariable("x").toFloat() + (padding[PAD_LEFT] - DESIRED_PADDING)) / imageSize
        val yTex = (getValueOfVariable("y").toFloat() + (padding[PAD_TOP] - DESIRED_PADDING)) / imageSize

        val width = getValueOfVariable("width") - (paddingWidth - (2 * DESIRED_PADDING))
        val height = getValueOfVariable("height") - (paddingHeight - (2 * DESIRED_PADDING))

        val quadWidth = width * horizontalPerPixelSize
        val quadHeight = height * verticalPerPixelSize

        val xTexSize = width.toFloat() / imageSize.toFloat()
        val yTexSize = height.toFloat() / imageSize.toFloat()

        val xOff = (getValueOfVariable("xoffset") + padding[PAD_LEFT] - DESIRED_PADDING) * horizontalPerPixelSize
        val yOff = (getValueOfVariable("yoffset") + padding[PAD_TOP] - DESIRED_PADDING) * verticalPerPixelSize
        val xAdvance = (getValueOfVariable("xadvance") - paddingWidth) * horizontalPerPixelSize

        return Character(id, xTex, yTex, xTexSize, yTexSize, xOff, yOff, quadWidth, quadHeight, xAdvance)
    }
}