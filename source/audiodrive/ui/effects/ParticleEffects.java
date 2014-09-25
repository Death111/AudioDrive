package audiodrive.ui.effects;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.lwjgl.opengl.Display;
import org.newdawn.slick.opengl.Texture;

import audiodrive.Resources;
import audiodrive.model.geometry.Color;
import audiodrive.model.geometry.Face;
import audiodrive.model.geometry.TextureCoordinate;
import audiodrive.model.geometry.Vector;
import audiodrive.model.geometry.Vertex;
import audiodrive.model.loader.Model;
import audiodrive.model.track.Block;
import audiodrive.ui.components.Scene;

public class ParticleEffects {
	
	private static int rows = 2;
	private static int columns = 2;
	private static int particleCount = 10;
	
	private Texture texture = Resources.getParticleTexture();
	private List<Model> models = new ArrayList<>(rows * columns);
	private List<ParticleWave> particles = new ArrayList<>();
	
	private boolean visible = true;
	
	public ParticleEffects() {
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
				int size = 50;
				model.position().xAdd(size / 2);
				model.position().yAdd(size / 2);
				model.scale(size);
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
		
		final ArrayList<Particle> particleList = new ArrayList<Particle>(particleCount);
		final int x = Display.getWidth() / 2 + Display.getWidth() / 3 * block.rail();
		final Vector startPosition = new Vector(x, -50, 0);
		for (int i = 0; i < particleCount; i++) {
			Particle particle = new Particle();
			particle.color = Color.generateRandomColor(block.color());
			particle.model = models.get(i % models.size());
			particle.startPosition = startPosition;
			particle.speed = Display.getWidth() * (float) (1f + Math.random());
			particle.velocity = new Vector(Math.random() - .5, Math.random(), 0);
			particleList.add(particle);
		}
		
		final ParticleWave wave = new ParticleWave(particleList);
		particles.add(wave);
	}
	
	public void render() {
		if (!visible) return;
		Iterator<ParticleWave> iterator = particles.iterator();
		while (iterator.hasNext()) {
			ParticleWave particle = iterator.next();
			if (particle.lifeTime < particle.elapsedTime) iterator.remove();
			else particle.render();
		}
	}
	
	public ParticleEffects visible(boolean visible) {
		this.visible = visible;
		return this;
	}
	
	public boolean visible() {
		return visible;
	}
	
}

class Particle {
	Model model;
	Vector startPosition;
	Color color;
	Vector velocity;
	float speed;
	
	/**
	 * @return the model
	 */
	public final Model getModel() {
		return model;
	}
	
	/**
	 * @param model the model to set
	 */
	public final void setModel(Model model) {
		this.model = model;
	}
	
	/**
	 * @return the color
	 */
	public final Color getColor() {
		return color;
	}
	
	/**
	 * @param color the color to set
	 */
	public final void setColor(Color color) {
		this.color = color;
	}
	
	/**
	 * @return the velocity
	 */
	public final Vector getVelocity() {
		return velocity;
	}
	
	/**
	 * @param velocity the velocity to set
	 */
	public final void setVelocity(Vector velocity) {
		this.velocity = velocity;
	}
	
	/**
	 * @return the speed
	 */
	public final float getSpeed() {
		return speed;
	}
	
	/**
	 * @param speed the speed to set
	 */
	public final void setSpeed(float speed) {
		this.speed = speed;
	}
}

class ParticleWave {
	List<Particle> particles;
	double startTime;
	double lifeTime = 2;
	double elapsedTime;
	
	public ParticleWave(List<Particle> particles) {
		startTime = Scene.time();
		this.particles = particles;
	}
	
	public void render() {
		elapsedTime = Scene.time() - startTime;
		final double d = (1f / lifeTime); // multiplicator for alpha fading
		for (Particle particle : particles) {
			final double factor = particle.speed * elapsedTime;
			Vector newPos = particle.startPosition.clone().add(particle.velocity.multiplied(factor));
			final Color color = particle.getColor();
			final double alpha = color.a - elapsedTime * d;
			particle.model.position(newPos).color(color.alpha(alpha));
			particle.model.render();
		}
	}
}
