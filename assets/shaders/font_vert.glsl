#version 330 core

in vec2 position;
in vec2 textureCoords;

out vec2 pass_textureCoords;

uniform vec2 translation;

void main() {
    gl_Position = vec4(position + translation * vec2(2f, -2f), 0f, 1f);
    pass_textureCoords = textureCoords;
}
