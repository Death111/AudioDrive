package audiodrive.ui.effects;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glUniform1i;

import java.util.Arrays;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL33;
import org.newdawn.slick.opengl.Texture;

import audiodrive.Resources;
import audiodrive.model.Renderable;
import audiodrive.model.geometry.Color;
import audiodrive.model.geometry.Vector;
import audiodrive.ui.components.Camera;
import audiodrive.ui.effects.ShaderProgram.Uniform;
import audiodrive.utilities.Buffers;

class Particle implements Comparable<Particle> {
	Vector position;
	Vector speed;
	Color color;
	float size, angle, width;
	float life;
	double cameraDistance;
	
	@Override
	public int compareTo(Particle that) {
		if (this.cameraDistance > that.cameraDistance) return -1;
		if (this.cameraDistance < that.cameraDistance) return 1;
		return 0;
	}
}

public class Particles implements Renderable {
	
	private static final int maxParticles = 10000;
	Particle particles[] = new Particle[maxParticles];
	int lastUsedParticle = 0;
	
	Texture texture;
	
	float particlePositionData[] = new float[maxParticles * 4];
	float particleColorData[] = new float[maxParticles * 4];
	
	// int VertexArrayID = GL30.glGenVertexArrays();
	int vertexArray_buffer = glGenBuffers();
	int particle_vertex_buffer = glGenBuffers();
	int particles_position_buffer = glGenBuffers();
	int particles_color_buffer = glGenBuffers();
	
	ShaderProgram shader;
	
	public Particles() {
		texture = Resources.getTexture("textures/particle/particle.png");
		// GL30.glBindVertexArray(VertexArrayID);
		for (int i = 0; i < maxParticles; i++) {
			particles[i] = new Particle();
			particles[i].life = -1.0f;
			particles[i].cameraDistance = -1.0f;
		}
		
		System.out.println(GL20.GL_MAX_VERTEX_ATTRIBS);
		shader = new ShaderProgram("shaders/Particle.vs", "shaders/Particle.fs");
		final float scale = .5f;
		float g_vertex_buffer_data[] = {-scale, -scale, 0.0f, scale, -scale, 0.0f, -scale, scale, 0.0f, scale, scale, 0.0f,};
		
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
		
		GL30.glBindVertexArray(0);
		
	}
	
	private double lastTime = System.currentTimeMillis() / 1000;
	int particlesCount = 0;
	
	@Override
	public void update(double time) {
		double delta = time - lastTime;
		lastTime = time;
		
		final Vector cameraPosition = Camera.position();
		// Calc how many particles per second
		int newParticles = (int) Math.min(delta * maxParticles, .0001f * maxParticles);
		final float particleLifetime = 10.0f;
		
		for (int i = 0; i < newParticles; i++) {
			int particleIndex = findUnusedParticle();
			particles[particleIndex].life = particleLifetime; // This particle will live 5 seconds.
			particles[particleIndex].position = new Vector(0, 0, 0.0f);
			
			float spread = 8f;
			Vector maindir = new Vector(Math.random() - .5f, 10f + Math.random(), Math.random() - .5f);
			// Very bad way to generate a random direction;
			// See for instance http://stackoverflow.com/questions/5408276/python-uniform-spherical-distribution instead,
			// combined with some user-controlled parameters (main direction, spread, etc)
			Vector randomdir = new Vector(Math.random() - .5, Math.random() - .5, Math.random() - .5);
			
			particles[particleIndex].speed = maindir.add(randomdir.multiplied(spread));
			
			// Very bad way to generate a random color
			particles[particleIndex].color = Color.generateRandomColor(Color.Green);
			
			particles[particleIndex].size = (float) 1;// (Math.random() % 1000) / 2000.0f + 0.1f;
		}
		
		// Simulate all particles
		particlesCount = 0;
		for (int i = 0; i < maxParticles; i++) {
			
			Particle p = particles[i];
			
			if (p.life > 0.0f) {
				
				// Decrease life
				p.life -= delta;
				if (p.life > 0.0f) {
					// Simulate simple physics : gravity only, no collisions
					p.speed.add(new Vector(0.0f, -9.81f, 0.0f).multiply(delta * 0.5f));
					p.position.add(p.speed.multiplied(delta));
					p.cameraDistance = p.position.minus(cameraPosition).length();
					particlesCount++;
				} else {
					// Particles that just died will be put at the end of the buffer in SortParticles();
					p.cameraDistance = -1.0f;
				}
			}
		}
		
		sortParticles();
		
		// fill gpu buffer, farest particles first
		for (int i = 0; i < particlesCount; i++) {
			Particle p = particles[i]; // shortcut
			
			// fade transparency
			// p.color = p.color.alpha(p.life * 1 / particleLifetime);
			
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
	
	private int findUnusedParticle() {
		
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
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		// glDisable(GL_LIGHTING);
		glDisable(GL_CULL_FACE);
		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
		drawCoordinateSystem(1);
		
		shader.bind();
		// GL30.glBindVertexArray(VertexArrayID);
		// shader.uniform("CameraRight_worldspace");
		// shader.uniform("CameraUp_worldspace");
		final Uniform uniform = shader.uniform("myTextureSampler");
		glUniform1i(uniform.location, 0);
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, texture.getTextureID());
		
		glBindBuffer(GL_ARRAY_BUFFER, particles_position_buffer);
		glBufferData(GL_ARRAY_BUFFER, maxParticles * 4 * 4, GL_STREAM_DRAW);// Buffer orphaning, a common way to improve streaming perf. See above link for
		glBufferSubData(GL_ARRAY_BUFFER, 0, Buffers.create(particlePositionData));
		
		glBindBuffer(GL_ARRAY_BUFFER, particles_color_buffer);
		glBufferData(GL_ARRAY_BUFFER, maxParticles * 4 * 4, GL_STREAM_DRAW); // Buffer orphaning, a common way to improve streaming perf. See above link for
		glBufferSubData(GL_ARRAY_BUFFER, 0, Buffers.create(particleColorData));
		
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		
		// Bind our texture in Texture Unit 0
		// texture.bind();
		// Same as the billboards tutorial
		// System.out.println("attrib");
		// 1rst attribute buffer : vertices
		GL20.glEnableVertexAttribArray(0);
		glBindBuffer(GL_ARRAY_BUFFER, particle_vertex_buffer);
		GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);
		
		GL20.glEnableVertexAttribArray(3);
		glBindBuffer(GL_ARRAY_BUFFER, particle_vertex_buffer);
		GL20.glVertexAttribPointer(3, 3, GL11.GL_FLOAT, false, 0, 0);
		
		// 2nd attribute buffer : positions of particles' centers
		GL20.glEnableVertexAttribArray(1);
		glBindBuffer(GL_ARRAY_BUFFER, particles_position_buffer);
		GL20.glVertexAttribPointer(1, 4, GL11.GL_FLOAT, false, 0, 0);
		
		// 3rd attribute buffer : particles' colors
		GL20.glEnableVertexAttribArray(2);
		glBindBuffer(GL_ARRAY_BUFFER, particles_color_buffer);
		GL20.glVertexAttribPointer(2, 4, GL11.GL_FLOAT, true, 0, 0);
		
		// These functions are specific to glDrawArrays*Instanced*.
		// The first parameter is the attribute buffer we're talking about.
		// The second parameter is the "rate at which generic vertex attributes advance when rendering multiple instances"
		// http://www.opengl.org/sdk/docs/man/xhtml/glVertexAttribDivisor.xml
		GL33.glVertexAttribDivisor(0, 0); // particles vertices : always reuse the same 4 vertices -> 0
		GL33.glVertexAttribDivisor(3, 0); // particles vertices : always reuse the same 4 vertices -> 0
		GL33.glVertexAttribDivisor(1, 1); // positions : one per quad (its center) -> 1
		GL33.glVertexAttribDivisor(2, 1); // color : one per quad -> 1
		
		// Draw the particules !
		// This draws many times a small triangle_strip (which looks like a quad).
		// This is equivalent to :
		// for(i in ParticlesCount) : glDrawArrays(GL_TRIANGLE_STRIP, 0, 4),
		// but faster.
		GL31.glDrawArraysInstanced(GL_TRIANGLE_STRIP, 0, 4, particlesCount);
		
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(2);
		GL20.glDisableVertexAttribArray(3);
		// GL30.glBindVertexArray(0);
		shader.unbind();
		
		// Color.White.gl();
		// glPointSize(6);
		// glBegin(GL_POINTS);
		//
		// for (Particle particle : particles) {
		// final Vector position = particle.position;
		// System.out.println(position);
		// if (position != null) position.glVertex();
		// }
		//
		// glEnd();
		
	}
	
	private void drawCoordinateSystem(int length) {
		glBegin(GL_LINES);
		glColor4d(length, 0, 0, length);
		glVertex3d(length, 0, 0);
		glVertex3d(-length, 0, 0);
		glColor4d(0, length, 0, length);
		glVertex3d(0, length, 0);
		glVertex3d(0, -length, 0);
		glColor4d(0, 0, length, length);
		glVertex3d(0, 0, length);
		glVertex3d(0, 0, -length);
		glEnd();
	}
	
}
