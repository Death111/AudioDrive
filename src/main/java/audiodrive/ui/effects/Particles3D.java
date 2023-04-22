package audiodrive.ui.effects;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL31.glDrawArraysInstanced;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;

import java.util.Arrays;
import java.util.stream.IntStream;

import org.newdawn.slick.opengl.Texture;

import audiodrive.AudioDrive;
import audiodrive.Resources;
import audiodrive.model.Renderable;
import audiodrive.model.geometry.Color;
import audiodrive.model.geometry.Vector;
import audiodrive.ui.components.Camera;
import audiodrive.ui.effects.ShaderProgram.Uniform;
import audiodrive.utilities.Arithmetic;
import audiodrive.utilities.Buffers;

public class Particles3D implements Renderable {
	
	private int lastUsedParticle = 0;
	
	private Texture texture;
	
	private Particle3D[] particles;
	private float[] particlePositionData;
	private float[] particleColorData;
	
	private int particle_vertex_buffer = glGenBuffers();
	private int particles_position_buffer = glGenBuffers();
	private int particles_color_buffer = glGenBuffers();
	
	private ShaderProgram shader;
	
	private final int maxParticles;
	private float scale = 0.5f;
	private float lifetime = 2.0f;
	private float velocity = 20f;
	
	public Particles3D() {
		this(AudioDrive.Settings.getInteger("graphics.particles.3d.count"), AudioDrive.Settings.getDouble("graphics.particles.3d.scale"), AudioDrive.Settings
			.getDouble("graphics.particles.3d.lifetime"), AudioDrive.Settings.getDouble("graphics.particles.3d.velocity"));
	}
	
	public Particles3D(int count, double scale, double lifetime, double velocity) {
		maxParticles = Arithmetic.clamp(count, 1, 30000);
		this.scale *= (float) Arithmetic.clamp(scale, 0.01, 100);
		this.lifetime *= (float) Arithmetic.clamp(lifetime, 0.01, 100);
		this.velocity *= (float) Arithmetic.clamp(velocity, 0.01, 100);
		particles = new Particle3D[maxParticles];
		particlePositionData = new float[maxParticles * 4];
		particleColorData = new float[maxParticles * 4];
		texture = Resources.getTexture("textures/particle/particle.png");
		// Create particles
		for (int i = 0; i < maxParticles; i++) {
			particles[i] = new Particle3D();
			particles[i].life = -1.0f;
			particles[i].cameraDistance = -1.0f;
			particles[i].color = Color.Black;
			particles[i].position = new Vector();
			particles[i].speed = new Vector();
		}
		
		shader = new ShaderProgram("shaders/Particle.vs", "shaders/Particle.fs");
		final float s = this.scale;
		float g_vertex_buffer_data[] = {-s, -s, 0.0f, s, -s, 0.0f, -s, s, 0.0f, s, s, 0.0f};
		
		glBindBuffer(GL_ARRAY_BUFFER, particle_vertex_buffer);
		glBufferData(GL_ARRAY_BUFFER, Buffers.create(g_vertex_buffer_data), GL_STATIC_DRAW);
		
		// The VBO containing the positions and sizes of the particles
		glBindBuffer(GL_ARRAY_BUFFER, particles_position_buffer);
		// Initialize with empty (NULL) buffer : it will be updated later, each frame.
		glBufferData(GL_ARRAY_BUFFER, maxParticles * 4 * 4, GL_STREAM_DRAW);
		
		// The VBO containing the colors of the particles
		glBindBuffer(GL_ARRAY_BUFFER, particles_color_buffer);
		// Initialize with empty (NULL) buffer : it will be updated later, each frame.
		glBufferData(GL_ARRAY_BUFFER, maxParticles * 4 * 4, GL_STREAM_DRAW);
		
	}
	
	@Override
	protected void finalize() throws Throwable {
		glDeleteBuffers(particle_vertex_buffer);
		glDeleteBuffers(particles_position_buffer);
		glDeleteBuffers(particles_color_buffer);
	}
	
	private double lastTime = System.currentTimeMillis() / 1000;
	
	int particlesCount = 0;
	
	public void createParticles(Vector position, Color color, double amount) {
		
		// Calculate how many particles but limit that we could spawn one second long new ones
		int newParticles = (int) Math.min(amount * delta * maxParticles, .016f * maxParticles);
		
		// Create new particles
		IntStream.range(0, newParticles).parallel().forEach(idx -> {
			int particleIndex = findUnusedParticle();
			particles[particleIndex].life = lifetime;
			particles[particleIndex].position = position.clone();
			
			Vector maindir = new Vector(Math.random() - .5f, 10f, Math.random() - .5f);
			Vector randomdir = new Vector(Math.random() - .5, Math.random() - .5, Math.random() - .5f);
			
			particles[particleIndex].speed = maindir.add(randomdir.multiplied(velocity * amount));
			particles[particleIndex].color = Color.generateRandomColor(color);
			particles[particleIndex].size = (float) Math.random();
		});
	}
	
	double delta = 0;
	
	@Override
	public void update(double time) {
		delta = time - lastTime;
		lastTime = time;
		
		final Vector cameraPosition = Camera.position();
		
		// Simulate all particles
		particlesCount = 0;
		// TODO why particles flicker when using parallel
		Arrays.stream(particles).forEach(p -> {
			if (p.life > 0.0f) {
				// Decrease life
			p.life -= delta;
			if (p.life > 0.0f) {
				// add gravity
				p.speed.yAdd(-9.81f * delta);
				p.position.add(p.speed.multiplied(delta));
				p.color = p.color.alpha(p.life * 1 / lifetime); // fade transparency
				p.cameraDistance = p.position.minus(cameraPosition).length();
				particlesCount++;
			} else {
				p.cameraDistance = -1.0f;
			}
		}
	}	);
		
		// sort particles: Farthest particles first (needs to be drawn first)
		sortParticles();
		
		// fill gpu buffer
		for (int i = 0; i < particlesCount; i++) {
			Particle3D p = particles[i];
			
			particlePositionData[4 * i + 0] = (float) p.position.x();
			particlePositionData[4 * i + 1] = (float) p.position.y();
			particlePositionData[4 * i + 2] = (float) p.position.z();
			particlePositionData[4 * i + 3] = p.size;
			
			particleColorData[4 * i + 0] = (float) p.color.r;
			particleColorData[4 * i + 1] = (float) p.color.g;
			particleColorData[4 * i + 2] = (float) p.color.b;
			particleColorData[4 * i + 3] = (float) p.color.a;
		}
		
	}
	
	private void sortParticles() {
		Arrays.sort(particles);
	}
	
	private synchronized int findUnusedParticle() {
		
		for (int i = lastUsedParticle; i < maxParticles; i++) {
			if (particles[i].life < 0) {
				lastUsedParticle = i;
				return i;
			}
		}
		
		for (int i = 0; i < lastUsedParticle; i++) {
			if (particles[i].life < 0) {
				lastUsedParticle = i;
				return i;
			}
		}
		
		return 0; // All particles are taken, override the first one
	}
	
	@Override
	public void render() {
		
		if (particlesCount <= 0) return;
		
		glDisable(GL_CULL_FACE);
		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
		
		shader.bind();
		
		final Uniform uniform = shader.uniform("myTextureSampler");
		glActiveTexture(GL_TEXTURE0); // Tell gl that next texture will be bound to unit 0
		glBindTexture(GL_TEXTURE_2D, texture.getTextureID()); // bind texture
		glUniform1i(uniform.location, 0); // Set to same number as glActiveTexture used (shader will use this texture unit to get data)
		
		glBindBuffer(GL_ARRAY_BUFFER, particles_position_buffer);
		glBufferData(GL_ARRAY_BUFFER, maxParticles * 4 * 4, GL_STREAM_DRAW); // Buffer orphaning
		glBufferSubData(GL_ARRAY_BUFFER, 0, Buffers.create(particlePositionData));
		
		glBindBuffer(GL_ARRAY_BUFFER, particles_color_buffer);
		glBufferData(GL_ARRAY_BUFFER, maxParticles * 4 * 4, GL_STREAM_DRAW); // Buffer orphaning
		glBufferSubData(GL_ARRAY_BUFFER, 0, Buffers.create(particleColorData));
		
		// 1st attribute: particle vertices
		glEnableVertexAttribArray(0);
		glBindBuffer(GL_ARRAY_BUFFER, particle_vertex_buffer);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		
		// 2nd attribute buffer : positions of particles' centers
		glEnableVertexAttribArray(1);
		glBindBuffer(GL_ARRAY_BUFFER, particles_position_buffer);
		glVertexAttribPointer(1, 4, GL_FLOAT, false, 0, 0);
		
		// 3rd attribute buffer : particles' colors
		glEnableVertexAttribArray(2);
		glBindBuffer(GL_ARRAY_BUFFER, particles_color_buffer);
		glVertexAttribPointer(2, 4, GL_FLOAT, true, 0, 0);
		
		// These functions are specific to glDrawArrays*Instanced*.
		// The first parameter is the attribute buffer we're talking about.
		// The second parameter is the "rate at which generic vertex attributes advance when rendering multiple instances"
		// http://www.opengl.org/sdk/docs/man/xhtml/glVertexAttribDivisor.xml
		glVertexAttribDivisor(0, 0); // particles vertices : always reuse the same 4 vertices -> 0
		glVertexAttribDivisor(1, 1); // positions : one per quad (its center) -> 1
		glVertexAttribDivisor(2, 1); // color : one per quad -> 1
		
		// Draw quad particlesCount times
		glDrawArraysInstanced(GL_TRIANGLE_STRIP, 0, 4, particlesCount);
		
		// Disable buffers and attributes again
		glVertexAttribDivisor(0, 0);
		glVertexAttribDivisor(1, 0);
		glVertexAttribDivisor(2, 0);
		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(1);
		glDisableVertexAttribArray(2);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindTexture(GL_TEXTURE_2D, 0);
		shader.unbind();
		glEnable(GL_CULL_FACE);
		
	}
}

class Particle3D implements Comparable<Particle3D> {
	Vector position;
	Vector speed;
	Color color;
	float size, angle, width;
	float life;
	double cameraDistance;
	
	@Override
	public int compareTo(Particle3D that) {
		if (cameraDistance > that.cameraDistance) return -1;
		if (cameraDistance < that.cameraDistance) return 1;
		return 0;
	}
}
