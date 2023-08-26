package cga.exercise.components.font.mesh

/**
 * @param id
 * the ASCII value of the character
 * @param xTextureCoord
 * the x texture coordinate for the top left corner of the
 * character in the texture atlas
 * @param yTextureCoord
 * the y texture coordinate for the top left corner of the
 * character in the texture atlas
 * @param xTexSize
 * the width of the character in the texture atlas
 * @param yTexSize
 * the height of the character in the texture atlas
 * @param xOffset
 * the x distance from the cursor to the left edge of the char's quad
 * @param yOffset
 * the y distance from the cursor to the top edge of the char's quad
 * @param sizeX
 * the width of the char's quad in screen space
 * @param sizeY
 * the height of the char's quad in screen space
 * @param xAdvance
 * how far in pixels the cursor should advance after adding this char
 */
data class Character(
    val id: Int,
    val xTextureCoord: Float,
    val yTextureCoord: Float,
    val xTexSize: Float,
    val yTexSize: Float,
    val xOffset: Float,
    val yOffset: Float,
    val sizeX: Float,
    val sizeY: Float,
    val xAdvance: Float
) {
    val xMaxTextureCoord: Float = xTexSize + xTextureCoord
    val yMaxTextureCoord: Float = yTexSize + yTextureCoord
}