uniform float time;
uniform vec2 resolution;

float length2(vec2 p) { return dot(p, p); }

float noise(vec2 p){
	return fract(sin(fract(sin(p.x) * (4313.13311)) + p.y) * 3131.0011);
}

float worley(vec2 p) {
	float d = 1e30;
	vec2 tp;
	tp = floor(p) + vec2(-1.0, -1.0);
	d = min(d, length2(p - tp - vec2(noise(tp))));
	tp = floor(p) + vec2(0.0, -1.0);
	d = min(d, length2(p - tp - vec2(noise(tp))));
	tp = floor(p) + vec2(1.0, -1.0);
	d = min(d, length2(p - tp - vec2(noise(tp))));

	tp = floor(p) + vec2(-1.0, 0.0);
	d = min(d, length2(p - tp - vec2(noise(tp))));
	tp = floor(p) + vec2(0.0, 0.0);
	d = min(d, length2(p - tp - vec2(noise(tp))));
	tp = floor(p) + vec2(1.0, 0.0);
	d = min(d, length2(p - tp - vec2(noise(tp))));

	tp = floor(p) + vec2(-1.0, 1.0);
	d = min(d, length2(p - tp - vec2(noise(tp))));
	tp = floor(p) + vec2(0.0, 1.0);
	d = min(d, length2(p - tp - vec2(noise(tp))));
	tp = floor(p) + vec2(1.0, 1.0);
	d = min(d, length2(p - tp - vec2(noise(tp))));
	return 3.*exp(-4.*abs(2.*d - 1.));
}

float fworley(vec2 p) {
	return pow(
		pow(worley(p - time*1.0), 2.) *
		worley(p*2. + 1.3 + time*.5) *
		worley(p*4. + 2.3 + time*-.25) *
		worley(p*8. + 3.3 + time*.125) *
		worley(p*32. + 4.3 + time*.125) *
		sqrt(worley(p * 64. + 5.3 + time * -.00625)) *
		pow(worley(p * -128. + 7.3), 1.0/4.0), 0.225);
}

void main() {
	vec2 uv = gl_FragCoord.xy /resolution.xy;
	float t = fworley(uv * resolution.xy / 2000.);
	t *= exp(-length2(abs(2.*uv - 1.)));
	float r = length(abs(2.*uv - 1.) * resolution.xy);
	gl_FragColor = vec4(t * vec3(1.0*t, 1.0*t, 2.0*t), 1.0);
}