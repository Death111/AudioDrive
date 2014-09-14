package audiodrive.ui.scenes.overlays;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.Display;
import org.newdawn.slick.opengl.Texture;

import audiodrive.model.geometry.Color;
import audiodrive.model.geometry.Face;
import audiodrive.model.geometry.TextureCoordinate;
import audiodrive.model.geometry.Vector;
import audiodrive.model.geometry.Vertex;
import audiodrive.model.loader.Model;
import audiodrive.model.loader.ModelLoader;

public class ParticleEffects {
	
	private Texture particleTexture;
	private static int rows = 2;
	private static int columns = 2;
	
	private static int particleCount = 10;
	
	private static List<Model> models = null;
	private static List<ParticleInstance> particles = new ArrayList<>();
	
	public ParticleEffects() {
		particleTexture = ModelLoader.getTexture("models/particles/particles.png");
		
		if (models == null) {
			models = new ArrayList<>(rows * columns);
			createModels();
		}
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
				model.setTexture(particleTexture);
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
	 * @param color
	 * @param time
	 */
	public static void createParticles(Color color, double time) {
		
		final ArrayList<Particle> particleList = new ArrayList<Particle>(particleCount);
		final int x = (int) (Math.random() * Display.getWidth());
		final Vector startPosition = new Vector(x, -50, 0);
		for (int i = 0; i < particleCount; i++) {
			Particle particle = new Particle();
			particle.color = Color.generateRandomColor(color);
			particle.model = models.get(i % models.size());
			particle.startPosition = startPosition;
			particle.speed = Display.getWidth() * (float) (1f + Math.random());
			particle.velocity = new Vector(Math.random() - .5, Math.random(), 0);
			particleList.add(particle);
		}
		
		final ParticleInstance particleInstanz = new ParticleInstance(particleList, time);
		particles.add(particleInstanz);
	}
	
	public void render(double time) {
		List<ParticleInstance> toDelete = new ArrayList<ParticleInstance>(0);
		
		// TODO somehow draw behind the track
		for (ParticleInstance particleInstanz : particles) {
			particleInstanz.render(time);
			// Check if needed to delete particle
			if (particleInstanz.lifeTime < particleInstanz.elapsedTime) {
				toDelete.add(particleInstanz);
			}
		}
		
		particles.removeAll(toDelete);
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

class ParticleInstance {
	List<Particle> particles;
	double startTime;
	double lifeTime = 2;
	double elapsedTime;
	
	public ParticleInstance(List<Particle> particles, double startTime) {
		this.startTime = startTime;
		this.particles = particles;
	}
	
	public void render(double time) {
		this.elapsedTime = time - startTime;
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
