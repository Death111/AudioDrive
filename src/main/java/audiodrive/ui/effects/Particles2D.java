package audiodrive.ui.effects;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.lwjgl.opengl.Display;
import org.newdawn.slick.opengl.Texture;

import audiodrive.AudioDrive;
import audiodrive.Resources;
import audiodrive.model.Renderable;
import audiodrive.model.geometry.Color;
import audiodrive.model.geometry.Face;
import audiodrive.model.geometry.TextureCoordinate;
import audiodrive.model.geometry.Vector;
import audiodrive.model.geometry.Vertex;
import audiodrive.model.loader.Model;
import audiodrive.model.track.Block;
import audiodrive.ui.components.Scene;
import audiodrive.utilities.Arithmetic;

public class Particles2D implements Renderable {
	
	private static int rows = 2;
	private static int columns = 2;
	
	private Texture texture = Resources.getParticleTexture();
	private List<Model> models = new ArrayList<>(rows * columns);
	private List<ParticleWave> particles = new ArrayList<>();
	
	private boolean visible = true;
	
	private int particleCount;
	private int particleSize = 40;
	private double lifetime = 1.0;
	private double velocity = 0.5;
	
	public Particles2D() {
		this(AudioDrive.Settings.getInteger("graphics.particles.2d.count"), AudioDrive.Settings.getDouble("graphics.particles.2d.scale"), AudioDrive.Settings
			.getDouble("graphics.particles.2d.lifetime"), AudioDrive.Settings.getDouble("graphics.particles.2d.velocity"));
	}
	
	public Particles2D(int count, double scale, double lifetime, double velocity) {
		particleCount = Arithmetic.clamp(count, 1, 200);
		particleSize *= Arithmetic.clamp(scale, 0.01, 100);
		this.lifetime *= Arithmetic.clamp(lifetime, 0.01, 100);
		this.velocity *= Arithmetic.clamp(velocity, 0.01, 100);
		createModels();
	}
	
	/**
	 * Creates models for all particles (different sprites)
	 */
	private void createModels() {
		double sizeX = 1.0 / columns;
		double sizeY = 1.0 / rows;
		for (int column = 0; column < columns; column++)
			for (int row = 0; row < rows; row++) {
				int index = row + column;
				int columnIndex = index % columns;
				int rowIndex = index / columns;
				
				// (columnIndex* sizeX * 1f, 1.0f - sizeY - rowIndex * sizeY, sizeX, sizeY);
				TextureCoordinate tC1 = new TextureCoordinate(columnIndex * sizeX * 1f, 1.0f - sizeY - rowIndex * sizeY);
				TextureCoordinate tC2 = new TextureCoordinate(columnIndex * sizeX * 1f, 1.0f - sizeY - rowIndex * sizeY - sizeY);
				TextureCoordinate tC3 = new TextureCoordinate(columnIndex * sizeX * 1f + sizeX, 1.0f - sizeY - rowIndex * sizeY - sizeY);
				TextureCoordinate tC4 = new TextureCoordinate(columnIndex * sizeX * 1f + sizeX, 1.0f - sizeY - rowIndex * sizeY);
				
				List<Face> faces = new ArrayList<Face>(2);
				Vertex v1 = new Vertex().position(new Vector(0, 1, 0)).normal(Vector.Z).textureCoordinate(tC1);
				Vertex v2 = new Vertex().position(new Vector(0, 0, 0)).normal(Vector.Z).textureCoordinate(tC2);
				Vertex v3 = new Vertex().position(new Vector(1, 0, 0)).normal(Vector.Z).textureCoordinate(tC3);
				Vertex v4 = new Vertex().position(new Vector(1, 1, 0)).normal(Vector.Z).textureCoordinate(tC4);
				Face f1 = new Face(v4, v2, v1);
				Face f2 = new Face(v4, v3, v2);
				faces.add(f1);
				faces.add(f2);
				final Model model = new Model(row + "_" + column + "_particle", faces);
				model.setTexture(texture);
				model.position().xAdd(particleSize / 2);
				model.position().yAdd(particleSize / 2);
				model.scale(particleSize);
				models.add(model);
			}
	}
	
	/**
	 * Creates a particle instance with the given color
	 * 
	 * @param block.color()
	 * @param time
	 */
	public void createParticles(Block block) {
		final ArrayList<Particle2D> particleList = new ArrayList<Particle2D>(particleCount);
		final int x = Display.getWidth() / 2 + Display.getWidth() / 3 * block.rail();
		final Vector startPosition = new Vector(x, -50, 0);
		for (int i = 0; i < particleCount; i++) {
			Particle2D particle = new Particle2D();
			particle.color = Color.generateRandomColor(block.color());
			particle.model = models.get(i % models.size());
			particle.startPosition = startPosition;
			particle.velocity = Display.getWidth() * (float) (velocity + Math.random());
			particle.direction = new Vector(Math.random() - .5, Math.random(), 0);
			particleList.add(particle);
		}
		
		final ParticleWave wave = new ParticleWave(particleList, lifetime);
		particles.add(wave);
	}
	
	@Override
	public void render() {
		if (!visible) return;
		Iterator<ParticleWave> iterator = particles.iterator();
		while (iterator.hasNext()) {
			ParticleWave particle = iterator.next();
			if (particle.lifeTime < particle.elapsedTime) iterator.remove();
			else particle.render();
		}
	}
	
	public Particles2D visible(boolean visible) {
		this.visible = visible;
		return this;
	}
	
	public boolean visible() {
		return visible;
	}
	
}

class Particle2D {
	Model model;
	Vector startPosition;
	Color color;
	Vector direction;
	float velocity;
}

class ParticleWave {
	List<Particle2D> particles;
	double startTime;
	double lifeTime;
	double elapsedTime;
	
	public ParticleWave(List<Particle2D> particles, double lifeTime) {
		startTime = Scene.time();
		this.particles = particles;
		this.lifeTime = lifeTime;
	}
	
	public void render() {
		elapsedTime = Scene.time() - startTime;
		final double d = (1f / lifeTime); // multiplicator for alpha fading
		for (Particle2D particle : particles) {
			final double factor = particle.velocity * elapsedTime;
			Vector newPos = particle.startPosition.clone().add(particle.direction.multiplied(factor));
			final Color color = particle.color;
			final double alpha = color.a - elapsedTime * d;
			particle.model.position(newPos).color(color.alpha(alpha));
			particle.model.render();
		}
	}
}
