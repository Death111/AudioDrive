package audiodrive.model.loader;

import static org.lwjgl.opengl.GL11.*;

import java.util.List;

import org.newdawn.slick.opengl.Texture;

import audiodrive.model.buffer.VertexBuffer;
import audiodrive.model.geometry.Color;
import audiodrive.model.geometry.Face;
import audiodrive.model.geometry.Vector;
import audiodrive.model.geometry.transform.Placement;
import audiodrive.model.hierarchy.Node;

public class Model extends Node {
	
	private String modelName;
	private List<Face> faces;
	private Color color = Color.White;
	private Texture texture = null;
	
	private VertexBuffer vertexBuffer;
	
	public Model(String modelName, List<Face> faces) {
		this.modelName = modelName;
		this.faces = faces;
		// Check if textureCoordinates were set
		final boolean useTexture = faces.get(0).vertices.get(0).textureCoordinate != null;
		vertexBuffer = new VertexBuffer(faces).mode(GL_TRIANGLES).useTexture(useTexture);
	}
	
	@Override
	protected void finalize() throws Throwable {
		if (texture != null) texture.release();
	}
	
	@Override
	public void draw() {
		if (texture != null) {
			glEnable(GL_TEXTURE_2D);
			glBindTexture(GL_TEXTURE_2D, texture.getTextureID());
		}
		color.gl();
		vertexBuffer.draw();
		if (texture != null) {
			glBindTexture(GL_TEXTURE_2D, 0);
			glDisable(GL_TEXTURE_2D);
		}
		
	}
	
	public Model align(Vector direction, Vector up) {
		placement().direction(direction).up(up);
		return this;
	}
	
	public Model move(Vector distance) {
		placement().position().add(distance);
		return this;
	}
	
	public Model placement(Placement placement) {
		this.placement().set(placement);
		return this;
	}
	
	public Model position(Vector position) {
		placement().position().set(position);
		return this;
	}
	
	public Vector position() {
		return placement().position();
	}
	
	public Model direction(Vector direction) {
		placement().direction().set(direction);
		return this;
	}
	
	public Vector direction() {
		return placement().direction();
	}
	
	public Model up(Vector normal) {
		placement().up().set(normal);
		return this;
	}
	
	public Vector up() {
		return placement().up();
	}
	
	public Model scale(double scale) {
		scaling().scale(scale);
		return this;
	}
	
	public double scale() {
		return scaling().scale();
	}
	
	public final List<Face> getFaces() {
		return faces;
	}
	
	public final String getModelName() {
		return modelName;
	}
	
	public final void setTexture(Texture texture) {
		this.texture = texture;
	}
	
	public Texture getTexture() {
		return texture;
	}
	
	public Model color(Color color) {
		this.color = color;
		return this;
	}
}
