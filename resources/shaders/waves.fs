uniform float time;
uniform vec2 resolution;

float rand(vec2 co) {
	return fract(sin(dot(co.xy, vec2(12.98, 78.233))) * 43758.5453);
}

void main(void) {
	vec2 pos = gl_FragCoord.xy / resolution.xy;
	vec2 uPos = pos;
	uPos.x -= 0.0;
	uPos.y -= 0.5;

	vec3 color = vec3(0.0);
	float vertColor = 0.0;
	const float k = 9.;
	for (float i = 1.0; i < k; ++i) {
		float t = time * (10.0);
		uPos.y += sin(uPos.x * exp(i) - t) * 0.055;
		float fTemp = abs(1.0 / (120.0 * k) / uPos.y);
		vertColor += fTemp;
		color += vec3(pow(fTemp, 0.6) * 0.2, fTemp * i / k, fTemp * (i * 0.9));
	}

	vec4 color_final = vec4(color, 1.0);
	gl_FragColor = color_final;
	float ft = fract(time * 10.0);
	vec3 finalColor = gl_FragColor.rgb;
	finalColor += vec3(rand(pos + 112.0 + ft), rand(pos + 159.0 + ft),
			rand(pos + 111.0 + ft)) / 32.0;

	float d = distance((gl_FragCoord.yy / resolution.yy), vec2(0.5, 0.5));
	finalColor *= smoothstep(0.75, 0.0, d);
	gl_FragColor.rgb = finalColor;
}

