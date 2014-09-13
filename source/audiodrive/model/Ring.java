package audiodrive.model;

import static org.lwjgl.opengl.GL11.glDepthMask;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import org.newdawn.slick.opengl.TextureLoader;

import audiodrive.model.geometry.Color;
import audiodrive.model.geometry.Face;
import audiodrive.model.geometry.TextureCoordinate;
import audiodrive.model.geometry.Vector;
import audiodrive.model.geometry.Vertex;
import audiodrive.model.geometry.transform.Placement;
import audiodrive.model.loader.Model;
import audiodrive.utilities.Log;

public class Ring {

	private static double scale = 2;
	private static Model model;
	private Color color;
	private Placement placement;

	public Ring(Color color, Placement placement) {
		this.color = color;
		this.placement = placement;

		if (model != null)
			return;
		// Create a plane
		List<Face> faces = new ArrayList<Face>();

		Vertex v1 = new Vertex().position(new Vector(-1, 1, 0)).normal(Vector.Z).textureCoordinate(new TextureCoordinate(0, 1));
		Vertex v2 = new Vertex().position(new Vector(-1, -1, 0)).normal(Vector.Z).textureCoordinate(new TextureCoordinate(0, 0));
		Vertex v3 = new Vertex().position(new Vector(1, -1, 0)).normal(Vector.Z).textureCoordinate(new TextureCoordinate(1, 0));
		Vertex v4 = new Vertex().position(new Vector(1, 1, 0)).normal(Vector.Z).textureCoordinate(new TextureCoordinate(1, 1));

		Face f1 = new Face(v1, v2, v4);
		Face f2 = new Face(v2, v3, v4);

		faces.add(f1);
		faces.add(f2);

		model = new Model("ring", faces);
		try {
			model.setTexture(TextureLoader.getTexture("PNG", new FileInputStream(new File("models/ring/ring.png"))));
		} catch (Exception e) {
			Log.error(e.toString());
		}
	}

	public void render() {
		glDepthMask(false); // disable depth
		model.placement(placement).color(color).scale(scale).render();
		glDepthMask(true); // enable depth
	}

	public Ring scale(double scale) {
		this.scale = scale;
		return this;
	}
}
