package cga.exercise.components.shader

import cga.exercise.components.geometry.GuiQuad
import cga.exercise.components.gui.GuiElement

class GuiShader {
    private val guiShader: ShaderProgram = ShaderProgram("assets/shaders/gui_vert.glsl", "assets/shaders/gui_frag.glsl")
    private val quad: GuiQuad = GuiQuad()

    fun render(guis: List<GuiElement>) {
        guiShader.use()
        quad.render(guiShader, guis)
    }

}