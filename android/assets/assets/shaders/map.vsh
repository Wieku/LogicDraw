#version 130

uniform mat4 u_projTrans;

in vec4 a_position;
//attribute vec4 a_color;
in vec2 a_texCoord0;

//varying vec4 v_color;
out vec2 v_texCoords;

void main() {
   //v_color = vec4(1, 1, 1, 1);
   v_texCoords = a_texCoord0;
   gl_Position = u_projTrans * a_position;
}