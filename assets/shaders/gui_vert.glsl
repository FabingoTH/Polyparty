#version 330 core

in vec2 position;

out vec2 textureCoords;

uniform mat4 trans_matrix;

void main(void){

    gl_Position = trans_matrix * vec4(position, 0.0, 1.0);
    textureCoords = vec2((position.x+1.0)/2.0, 1 - (position.y+1.0)/2.0);
}