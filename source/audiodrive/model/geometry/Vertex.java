package audiodrive.model.geometry;

import static org.lwjgl.opengl.GL11.glColor4d;
import static org.lwjgl.opengl.GL11.glNormal3d;
import static org.lwjgl.opengl.GL11.glTexCoord2d;
import static org.lwjgl.opengl.GL11.glVertex3d;
import audiodrive.model.buffer.FloatData;

/**
 * This class represents a vertex with a given normal and texture coordinate
 * 
 * @author Death
 *
 */
public class Vertex implements FloatData {

	public static final int Dimension = Vector.Dimension * 2 + TextureCoordinate.Dimension + Color.Dimension;

	public Vector position;
	public Vector normal;
	public TextureCoordinate textureCoordinate;
	public Color color;

	/**
	 * Sends it's information to GPU
	 */
	public void gl() {
		if (normal != null)
			glNormal3d(normal.x(), normal.y(), normal.z());
		if (textureCoordinate != null)
			glTexCoord2d(textureCoordinate.x, textureCoordinate.y);
		if (color != null)
			glColor4d(color.r, color.g, color.b, color.a);
		if (position != null)
			glVertex3d(position.x(), position.y(), position.z());
	}

	/**
	 * @param position
	 *            the position to set
	 */
	public final Vertex position(Vector position) {
		this.position = position;
		return this;
	}

	/**
	 * @param normal
	 *            the normal to set
	 */
	public final Vertex normal(Vector normal) {
		this.normal = normal;
		return this;
	}

	/**
	 * @param textureCoordinate
	 *            the textureCoordinate to set
	 */
	public final Vertex textureCoordinate(TextureCoordinate textureCoordinate) {
		this.textureCoordinate = textureCoordinate;
		return this;
	}

	/**
	 * @param color
	 *            the color to set
	 */
	public final Vertex color(Color color) {
		this.color = color;
		return this;
	}

	@Override
	public int floats() {
		return Vertex.Dimension;
	}

	@Override
	public float[] toFloats() {
		float[] floats = new float[Dimension];
		System.arraycopy(position.toFloats(), 0, floats, 0, Vector.Dimension);
		System.arraycopy(normal.toFloats(), 0, floats, Vector.Dimension, Vector.Dimension);
		if (textureCoordinate != null)
			System.arraycopy(textureCoordinate.toFloats(), 0, floats, Vector.Dimension * 2, TextureCoordinate.Dimension);
		if (color != null) {
			System.arraycopy(color.toFloats(), 0, floats, Vector.Dimension * 2 + TextureCoordinate.Dimension, Color.Dimension);
		}
		return floats;
	}

}
