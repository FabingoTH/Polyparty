#version 330 core

in vec2 pass_textureCoords;

out vec4 out_color;

uniform vec3 color;
uniform sampler2D fontAtlas;

void main() {
    out_color = vec4(color, 1f);
}
