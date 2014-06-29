package audiodrive.model.geometry;

import static org.lwjgl.opengl.GL11.*;

/**
 * This class represents a vertex with a given normal and texture coordinate
 * 
 * @author Death
 *
 */
public class Vertex {
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
	
}
