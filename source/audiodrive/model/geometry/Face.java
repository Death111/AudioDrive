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
	public List<VertexObject> vertexes;

	public Face(VertexObject vo1, VertexObject vo2, VertexObject vo3) {
		vertexes = new ArrayList<VertexObject>(3);
		vertexes.add(vo1);
		vertexes.add(vo2);
		vertexes.add(vo3);
	}
}
