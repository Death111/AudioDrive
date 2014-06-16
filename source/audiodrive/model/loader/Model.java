package audiodrive.model.loader;

import java.util.List;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;

import audiodrive.model.geometry.Face;
import audiodrive.model.geometry.VertexObject;

public class Model {

	private String modelName;
	private List<Face> faces;

	private Texture texture = null;

	public final void setTexture(Texture texture) {
		this.texture = texture;
	}

	public Model(String modelName, List<Face> faces) {
		this.modelName = modelName;
		this.faces = faces;
	}

	public void render() {

		if (texture != null) {
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getTextureID());
		}

		for (Face face : faces) {
			List<VertexObject> vertexes = face.vertexes;
			GL11.glBegin(GL11.GL_TRIANGLES);
			for (VertexObject vertexObject : vertexes) {
				vertexObject.gl();
			}
			GL11.glEnd();
		}

		if (texture != null) {
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
		}
	}

	public final List<Face> getFaces() {
		return faces;
	}

	public final String getModelName() {
		return modelName;
	}

	public Texture getTexture() {
		return this.texture;
	}
}
