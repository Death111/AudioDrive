uniform float time;
uniform vec2 resolution;

void main( void ) {
	vec2 pos = ( gl_FragCoord.xy / resolution.xy ) - 0.5;
	float sx = 0.2 * (pos.x + 0.5) * sin(25.0 * pos.x - 15.0 * time);
	float dy = 1.0 / (50.0 * abs(pos.y - sx));
	dy += 1.0 / (20.0 * length(pos - vec2(pos.x, 0.0)));
	gl_FragColor = vec4( (pos.x + 0.5) * dy, 0.5 * dy, dy, 1.0);

}