#define PI 3.1415926535897932384626433832795

const int MAX_BANDS = 100;

struct Band {
	float amplitude;
	float frequency;
};

uniform vec3 color; // default: vec3(0.5, 0.5, 1.0)
uniform vec2 resolution; // canvas resolution
uniform float time; // time since start in seconds
uniform float position; // default: 0.0
uniform float intensity; // default: 1.0
uniform float scale; // default: 1.0
uniform int numberOfBands; // range: 1 - 100
uniform Band bands[MAX_BANDS];

float band(Band band, vec2 pos) {
	float wave = scale * band.amplitude * sin(2.0 * PI * band.frequency * pos.x) / 2.05;
	float light = clamp(intensity * band.amplitude * band.frequency * 0.002, 0.001 + 0.001 / scale, 5.0) * scale / abs(wave - pos.y);
	return light * light;
}

void main( void ) {
	vec2 pos = (gl_FragCoord.xy / resolution.xy);
	pos.y += - 0.5 - position;
	float spectrum = 0.0;
	for (int n = 0; n < numberOfBands; n++) {
        if(bands[n].amplitude != 0.0) spectrum += band(bands[n], pos);
	}
	if(numberOfBands == 0) spectrum += band(Band(1.0, 1.0), pos);
	gl_FragColor = vec4(color * spectrum, spectrum);
	
}