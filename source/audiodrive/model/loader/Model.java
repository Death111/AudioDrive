package audiodrive.model.loader;

import static org.lwjgl.opengl.GL11.*;

import java.util.List;

import org.newdawn.slick.opengl.Texture;

import audiodrive.model.geometry.Face;
import audiodrive.model.geometry.Matrix;
import audiodrive.model.geometry.Vector;
import audiodrive.model.geometry.Vertex;

public class Model {
	
	private String modelName;
	private List<Face> faces;
	private Vector position = new Vector();
	private Matrix rotation = new Matrix().setIdentity();
	private double scaling = 1.0;
	
	private Texture texture = null;
	
	public Model(String modelName, List<Face> faces) {
		this.modelName = modelName;
		this.faces = faces;
	}
	
	@Override
	protected void finalize() throws Throwable {
		if (texture != null) texture.release();
	}
	
	public void render() {
		boolean translate = !position.equals(Vector.Null);
		boolean rotate = !rotation.equals(Matrix.Identity);
		boolean scale = scaling != 1.0;
		
		if (texture != null) {
			glEnable(GL_TEXTURE_2D);
			glBindTexture(GL_TEXTURE_2D, texture.getTextureID());
		}
		
		if (translate || scale || rotate) glPushMatrix();
		if (translate) glTranslated(position.x(), position.y(), position.z());
		if (scale) glScaled(scaling, scaling, scaling);
		if (rotate) glMultMatrix(rotation.toDoubleBuffer());
		for (Face face : faces) {
			List<Vertex> vertexes = face.vertexes;
			glBegin(GL_TRIANGLES);
			for (Vertex vertexObject : vertexes) {
				vertexObject.gl();
			}
			glEnd();
		}
		if (translate || scale || rotate) glPopMatrix();
		
		if (texture != null) {
			glBindTexture(GL_TEXTURE_2D, 0);
			glDisable(GL_TEXTURE_2D);
		}
		
	}
	
	public Model align(Vector direction, Vector up) {
		rotation.align(direction, up);
		return this;
	}
	
	public Model move(Vector distance) {
		position.add(position);
		return this;
	}
	
	public Model position(Vector position) {
		this.position.set(position);
		return this;
	}
	
	public Vector position() {
		return position;
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
