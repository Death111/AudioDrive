package audiodrive.model.geometry;

import static org.lwjgl.opengl.GL11.*;
import audiodrive.model.buffer.FloatData;

/**
 * This class represents a vertex with a given normal and texture coordinate
 * 
 * @author Death
 *
 */
public class Vertex implements FloatData {
	
	public static final int Dimension = Vector.Dimension * 2 + TextureCoordinate.Dimension;
	
	public Vector position;
	public Vector normal;
	public TextureCoordinate textureCoordinate;
	
	/**
	 * Sends it's information to GPU
	 */
	public void gl() {
		if (normal != null) glNormal3d(normal.x(), normal.y(), normal.z());
		if (textureCoordinate != null) glTexCoord2d(textureCoordinate.x, textureCoordinate.y);
		if (position != null) glVertex3d(position.x(), position.y(), position.z());
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
		if (textureCoordinate != null) System.arraycopy(textureCoordinate.toFloats(), 0, floats, Vector.Dimension * 2, TextureCoordinate.Dimension);
		return floats;
	}
	
}
