package audiodrive.model.geometry;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents an openGL-Face ( 3 VertexObjects )
 * 
 * @author Death
 *
 */
public class Face {
	public List<Vertex> vertexes;
	
	public Face(Vertex vo1, Vertex vo2, Vertex vo3) {
		vertexes = new ArrayList<Vertex>(3);
		vertexes.add(vo1);
		vertexes.add(vo2);
		vertexes.add(vo3);
	}
}
