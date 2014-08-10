package audiodrive.model.loader;

import static org.lwjgl.opengl.GL11.*;

import java.util.List;

import org.newdawn.slick.opengl.Texture;

import audiodrive.model.buffer.VertexBuffer;
import audiodrive.model.geometry.Face;
import audiodrive.model.geometry.Placement;
import audiodrive.model.geometry.Rotation;
import audiodrive.model.geometry.Vector;

public class Model {
	
	private String modelName;
	private List<Face> faces;
	private Placement placement = new Placement();
	private Rotation rotation = new Rotation();
	private double scaling = 1.0;
	
	private Texture texture = null;
	
	private VertexBuffer vertexBuffer;
	
	public Model(String modelName, List<Face> faces) {
		this.modelName = modelName;
		this.faces = faces;
		vertexBuffer = new VertexBuffer(faces).mode(GL_TRIANGLES);
	}
	
	@Override
	protected void finalize() throws Throwable {
		if (texture != null) texture.release();
	}
	
	public void render() {
		boolean place = !placement.isDefault();
		boolean rotate = !rotation.isNull();
		boolean scale = scaling != 1.0;
		
		if (texture != null) {
			glEnable(GL_TEXTURE_2D);
			glBindTexture(GL_TEXTURE_2D, texture.getTextureID());
		}
		
		if (place || rotate || scale) glPushMatrix();
		if (place) placement.apply();
		if (rotate) rotation.apply();
		if (scale) glScaled(scaling, scaling, scaling);
		vertexBuffer.draw();
		if (place || scale || rotate) glPopMatrix();
		
		if (texture != null) {
			glBindTexture(GL_TEXTURE_2D, 0);
			glDisable(GL_TEXTURE_2D);
		}
		
	}
	
	public Model align(Vector direction, Vector normal) {
		placement.direction(direction).normal(normal);
		return this;
	}
	
	public Model move(Vector distance) {
		placement.position().add(distance);
		return this;
	}
	
	public Model placement(Placement placement) {
		this.placement.set(placement);
		return this;
	}
	
	public Placement placement() {
		return placement;
	}
	
	public Model position(Vector position) {
		placement.position().set(position);
		return this;
	}
	
	public Vector position() {
		return placement.position();
	}
	
	public Model direction(Vector direction) {
		placement.direction().set(direction);
		return this;
	}
	
	public Vector direction() {
		return placement.direction();
	}
	
	public Model normal(Vector normal) {
		placement.normal().set(normal);
		return this;
	}
	
	public Vector normal() {
		return placement.normal();
	}
	
	public Model scale(double scaling) {
		this.scaling = scaling;
		return this;
	}
	
	public double scale() {
		return scaling;
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
}
