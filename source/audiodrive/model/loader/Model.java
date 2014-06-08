package audiodrive.model.loader;

import java.util.List;

import org.lwjgl.opengl.GL11;

import audiodrive.model.geometry.Face;
import audiodrive.model.geometry.VertexObject;

public class Model {
	private List<Face> faces;

	public Model(List<Face> faces) {
		this.faces = faces;
	}

	public void render() {
		// logger.info("rendering");
		for (Face face : faces) {

			List<VertexObject> vertexes = face.vertexes;
			GL11.glBegin(GL11.GL_TRIANGLES);
			for (VertexObject vertexObject : vertexes) {
				vertexObject.gl();
			}
			GL11.glEnd();
		}
	}
}
