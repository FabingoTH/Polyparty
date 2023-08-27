#version 330
layout (location = 0) in vec3 pos;

uniform mat4 lightspace_matrix;
uniform mat4 model_matrix;

void main() {
    gl_Position = lightspace_matrix * model_matrix * vec4(pos, 1.0f);
}
