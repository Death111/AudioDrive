package audiodrive.model.geometry;

import org.lwjgl.opengl.GL11;

/**
 * This class represents a vertex with a given normal and texture coordinate
 * 
 * @author Death
 *
 */
public class VertexObject {
	public Vector position;
	public Vector normal;
	public TextureCoordinate textureCoordinate;
	
	/**
	 * Sends it's information to GPU
	 */
	public void gl() {
		if (normal != null) GL11.glNormal3d(normal.x(), normal.y(), normal.z());
		if (textureCoordinate != null) GL11.glTexCoord2d(textureCoordinate.x, textureCoordinate.y);
		if (position != null) GL11.glVertex3d(position.x(), position.y(), position.z());
	}
	
}
