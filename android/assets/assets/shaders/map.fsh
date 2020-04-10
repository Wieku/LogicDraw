#version 130

#ifdef GL_ES
precision mediump float;
#endif

in vec2 v_texCoords;

uniform sampler2DRect texture_states;
uniform sampler2DRect texture_element_states;
uniform sampler2DRect texture_idle;
uniform sampler2DRect texture_active;
uniform ivec2 size;

out vec4 color;

void main()
{
    vec4 pixel = texelFetch(texture_element_states, ivec2(v_texCoords * size));
    bool first = texelFetch(texture_states, ivec2(pixel.xy * size)).r > 0.5;
    bool second = texelFetch(texture_states, ivec2(pixel.zw * size)).r > 0.5;

    color = vec4(texelFetch(!first && !second ? texture_idle : texture_active, ivec2(v_texCoords * size)).rgb, 1);

    color *= first && second ? 1.2 : 1;
}
