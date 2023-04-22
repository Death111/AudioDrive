package audiodrive.model.geometry;

import static org.lwjgl.opengl.GL11.*;
import audiodrive.model.buffer.IndexBuffer;
import audiodrive.model.buffer.VertexBuffer;

public class CuboidStripRenderer {
	
	private IndexBuffer frontIndices;
	private IndexBuffer topIndices;
	private IndexBuffer leftIndices;
	private IndexBuffer rightIndices;
	private IndexBuffer bottomIndices;
	private IndexBuffer backIndices;
	
	public CuboidStripRenderer(int length) {
		frontIndices = frontQuadStripIndices();
		topIndices = topQuadStripIndices(length);
		leftIndices = leftQuadStripIndices(length);
		rightIndices = rightQuadStripIndices(length);
		bottomIndices = bottomQuadStripIndices(length);
		backIndices = backQuadStripIndices(length);
	}
	
	public void render(VertexBuffer vertexBuffer) {
		vertexBuffer.mode(GL_QUAD_STRIP);
		vertexBuffer.draw(frontIndices);
		vertexBuffer.draw(topIndices);
		vertexBuffer.draw(leftIndices);
		vertexBuffer.draw(rightIndices);
		vertexBuffer.draw(bottomIndices);
		vertexBuffer.draw(backIndices);
	}
	
	public static IndexBuffer frontQuadStripIndices() {
		return new IndexBuffer(0, 1, 2, 3);
	}
	
	public static IndexBuffer topQuadStripIndices(int length) {
		return IndexBuffer.quadStripIndices(length, 0, +2);
	}
	
	public static IndexBuffer leftQuadStripIndices(int length) {
		return IndexBuffer.quadStripIndices(length, 1, -1);
	}
	
	public static IndexBuffer rightQuadStripIndices(int length) {
		return IndexBuffer.quadStripIndices(length, 2, +1);
	}
	
	public static IndexBuffer bottomQuadStripIndices(int length) {
		return IndexBuffer.quadStripIndices(length, 3, -2);
	}
	
	public static IndexBuffer backQuadStripIndices(int length) {
		int indices = IndexBuffer.numberOfIndices(length);
		return new IndexBuffer(indices - 2, indices - 1, indices - 4, indices - 3);
	}
	
}
