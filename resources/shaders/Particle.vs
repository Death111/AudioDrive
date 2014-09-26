#version 330 core

// Input vertex data, different for all executions of this shader.
layout(location = 0) in vec3 a; // dunno why i cant us that
layout(location = 3) in vec3 squareVertices; // same as above
layout(location = 1) in vec4 xyzs; // Position of the center of the particule and size of the square
layout(location = 2) in vec4 color; // Position of the center of the particule and size of the square

// Output data ; will be interpolated for each fragment.
out vec2 uvCoordinates;
out vec4 particleColor;

// Values that stay constant for the whole mesh.

void main()
{
	// Output position of the vertex
	gl_Position = ftransform() + vec4(xyzs.xyz,0);
	uvCoordinates = squareVertices.xy + vec2(0.5, 0.5);
	particleColor = vec4(xyzs.xyz,1);//color;
	
}

