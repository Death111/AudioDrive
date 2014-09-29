#version 330 core

// Interpolated values from the vertex shaders
in vec4 particleColor;
in vec2 uvCoordinates;

// Ouput data
out vec4 color;

uniform sampler2D myTextureSampler;

void main(){
	// Output color = color of the texture at the specified UV
	color = texture2D( myTextureSampler, uvCoordinates) * particleColor;
}