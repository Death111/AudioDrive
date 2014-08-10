package audiodrive.model.geometry;

import java.util.ArrayList;
import java.util.List;

import audiodrive.model.buffer.FloatData;

/**
 * This class represents an openGL-Face ( 3 VertexObjects )
 * 
 * @author Death
 *
 */
public class Face implements FloatData {
	
	public List<Vertex> vertices;
	
	public Face(Vertex vo1, Vertex vo2, Vertex vo3) {
		vertices = new ArrayList<Vertex>(3);
		vertices.add(vo1);
		vertices.add(vo2);
		vertices.add(vo3);
	}
	
	@Override
	public int floats() {
		return vertices.size() * Vertex.Dimension;
	}
	
	@Override
	public float[] toFloats() {
		float[] floats = new float[Vertex.Dimension * vertices.size()];
		int index = 0;
		for (Vertex vertex : vertices) {
			System.arraycopy(vertex.toFloats(), 0, floats, Vertex.Dimension * index++, Vertex.Dimension);
		}
		return floats;
	}
}
