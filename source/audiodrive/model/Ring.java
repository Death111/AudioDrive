package audiodrive.model;

import static org.lwjgl.opengl.GL11.glDepthMask;

import java.util.ArrayList;
import java.util.List;

import org.newdawn.slick.opengl.Texture;

import audiodrive.model.geometry.Color;
import audiodrive.model.geometry.Face;
import audiodrive.model.geometry.TextureCoordinate;
import audiodrive.model.geometry.Vector;
import audiodrive.model.geometry.Vertex;
import audiodrive.model.geometry.transform.Placement;
import audiodrive.model.loader.Model;
import audiodrive.model.loader.ModelLoader;
import audiodrive.utilities.Arithmetic;

public class Ring {
	
	public static final Texture Default = ModelLoader.getTexture("models/ring/ring.png");
	public static final Texture Pulse = ModelLoader.getTexture("models/ring/ring-pulse.png");
	private static final Model Model = createModel();
	
	private static Model createModel() {
		List<Face> faces = new ArrayList<Face>();
		
		Vertex v1 = new Vertex().position(new Vector(-1, 1, 0)).normal(Vector.Z).textureCoordinate(new TextureCoordinate(0, 1));
		Vertex v2 = new Vertex().position(new Vector(-1, -1, 0)).normal(Vector.Z).textureCoordinate(new TextureCoordinate(0, 0));
		Vertex v3 = new Vertex().position(new Vector(1, -1, 0)).normal(Vector.Z).textureCoordinate(new TextureCoordinate(1, 0));
		Vertex v4 = new Vertex().position(new Vector(1, 1, 0)).normal(Vector.Z).textureCoordinate(new TextureCoordinate(1, 1));
		
		Face f1 = new Face(v1, v2, v4);
		Face f2 = new Face(v2, v3, v4);
		
		faces.add(f1);
		faces.add(f2);
		
		Model model = new Model("ring", faces);
		model.setTexture(Default);
		return model;
	}
	
	private Color color;
	private Placement placement;
	private double scale = 2;
	private double pulse;
	private int iteration;
	
	public Ring(int iteration, Color color, Placement placement) {
		this.iteration = iteration;
		this.color = color;
		this.placement = placement;
	}
	
	public void render() {
		glDepthMask(false); // disable depth
		Model.setTexture(Default);
		Model.placement(placement).color(color).scale(scale + scale * 0.15 * pulse).render();
		if (pulse > 0) {
			Model.setTexture(Pulse);
			Model.color(color.alpha(pulse)).render();
		}
		glDepthMask(true); // enable depth
	}
	
	public int iteration() {
		return iteration;
	}
	
	public Placement placement() {
		return placement;
	}
	
	public Ring pulse(double pulse) {
		this.pulse = Arithmetic.clamp(pulse);
		return this;
	}
	
	public Ring scale(double scale) {
		this.scale = scale;
		return this;
	}
	
}
