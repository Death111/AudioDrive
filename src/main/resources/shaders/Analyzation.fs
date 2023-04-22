#define PI 3.1415926535897932384626433832795

uniform float time;
uniform vec2 resolution;

const float position = 0.0;
const float scale = 1.0;
const float intensity = 1.0;

float band(vec2 pos, float amplitude, float frequency) {
	float wave = scale * amplitude * sin(2.0 * PI * frequency * pos.x + time) / 2.05;
	float light = clamp(amplitude * frequency * 0.002, 0.001 + 0.001 / scale, 5.0) * scale / abs(wave - pos.y);
	return light;
}

void main( void ) {
	vec3 color = vec3(0.5, 0.5, 1.0);
	color = color == vec3(0.0)? vec3(0.5, 0.5, 1.0) : color;
	vec2 pos = (gl_FragCoord.xy / resolution.xy);
	pos.y += - 0.5 - position;
	
	float spectrum = 0.0;

	spectrum += band(pos, 1.0, 1.0);
	spectrum += band(pos, 0.7, 2.5);
	spectrum += band(pos, 0.4, 2.0);
	spectrum += band(pos, 0.05, 4.5);
	spectrum += band(pos, 0.1, 7.0);
	spectrum += band(pos, 0.1, 1.0);
	
	gl_FragColor = vec4(color * spectrum, spectrum);
}