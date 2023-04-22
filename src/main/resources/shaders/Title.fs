#define PI 3.1415926535897932384626433832795

uniform float time;
uniform vec2 resolution;

const vec3 color = vec3(0.5, 0.5, 1.0);

void main( void ) {
	vec2 pos = ( gl_FragCoord.xy / resolution.xy );
	pos.x += 0.01;
	pos.y -= 0.4;
	float wave = 0.01 * sin(2.0 * PI * 8.0 * pos.x + 10.0 * time);
	float light = 0.1 * length(pos) + pos.x / (50.0 * abs(pos.y - wave));
	gl_FragColor = vec4(color * light, 1.0);

}