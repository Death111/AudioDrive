#define PI 3.1415926535897932384626433832795

const int MAX_BANDS = 100;

struct Band {
	float amplitude;
	float frequency;
};

uniform vec3 color;
uniform vec2 resolution;
uniform float time;
uniform float position;
uniform float intensity;
uniform float scale;
uniform int numberOfBands;
uniform Band bands[MAX_BANDS];

void limit() {
	scale = (scale <= 0.0)? 1.0 : scale;
	intensity = (intensity <= 0.0)? 1.0 : intensity;
	color = (color == vec3(0.0))? vec3(0.5, 0.5, 1.0) : color;
	numberOfBands = min(numberOfBands, MAX_BANDS);
}

float band(Band band, vec2 pos) {
	float wave = scale * band.amplitude * sin(2.0 * PI * band.frequency * pos.x) / 2.05;
	return clamp(intensity * band.amplitude * band.frequency * 0.002, 0.001 + 0.001 / scale, 5.0) * scale / abs(wave - pos.y);
}

void main( void ) {
	limit();
	vec2 pos = (gl_FragCoord.xy / resolution.xy);
	pos.y += - 0.5 - position;
	float spectrum = 0.0;
	for (int n = 0; n < numberOfBands; n++) {
		spectrum += band(bands[n], pos);
	}
	if(numberOfBands == 0) spectrum += band(Band(1.0, 1.0), pos);
	gl_FragColor = vec4(color * spectrum, spectrum);
	
}