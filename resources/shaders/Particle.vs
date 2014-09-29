#version 150 compatibility
#extension GL_ARB_explicit_attrib_location : enable

// Input vertex data, different for all executions of this shader.
layout(location = 0) in vec3 a; //TODO dunno why i cant us that
layout(location = 3) in vec3 squareVertices; // same as above
layout(location = 1) in vec4 xyzs; 
layout(location = 2) in vec4 color; 

// Output data ; will be interpolated for each fragment.
out vec2 uvCoordinates;
out vec4 particleColor;

// Values that stay constant for the whole mesh.

void main()
{
	float particleSize = xyzs.w;
	vec3 particleCenter_wordspace = xyzs.xyz;
	
	mat4 matrix = gl_ModelViewMatrix; 
	//mat4 matrix = gl_ModelViewProjectionMatrix;
	//mat4 matrix = gl_ProjectionMatrix;
	
	vec3 CameraRight_worldspace = vec3( matrix[0][0], matrix[1][0], matrix[2][0]); 
	vec3 CameraUp_worldspace = vec3( matrix[0][1], matrix[1][1], matrix[2][1]); 
	
	vec3 vertexPosition_worldspace = 
		particleCenter_wordspace
		+ CameraRight_worldspace * squareVertices.x * particleSize
		+ CameraUp_worldspace * squareVertices.y * particleSize;

	// Output position of the vertex
	gl_Position = gl_ModelViewProjectionMatrix * vec4(vertexPosition_worldspace, 1.0f);
	
	uvCoordinates = squareVertices.xy + vec2(0.5, 0.5);
	particleColor = color;
	
}

