package audiodrive.model.buffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;

import java.nio.IntBuffer;

import audiodrive.utilities.Buffers;

public class IndexBuffer {
	
	public static final int Target = GL_ELEMENT_ARRAY_BUFFER;
	public static final int Type = GL_INT;
	
	private final int id;
	private final int entries;
	
	public IndexBuffer(int... indices) {
		this(Buffers.create(indices));
	}
	
	public IndexBuffer(IntBuffer buffer) {
		entries = buffer.limit();
		id = glGenBuffers();
		glBindBuffer(Target, id);
		glBufferData(Target, buffer, GL_STATIC_DRAW);
	}
	
	@Override
	protected void finalize() throws Throwable {
		glDeleteBuffers(id);
	}
	
	/**
	 * Binds the buffer to the GL context.
	 */
	public void bind() {
		glBindBuffer(Target, id);
	}
	
	/**
	 * Indicates the number of entries in the buffer.
	 */
	public int size() {
		return entries;
	}
	
	/**
	 * Creates quad strip indices for a given number of quads.
	 */
	public static IndexBuffer quadStripIndices(int numberOfQuads, int startIndex, int followOffset) {
		IntBuffer indices = Buffers.createIntBuffer(numberOfIndices(numberOfQuads));
		int index = startIndex;
		for (int i = 0; i <= numberOfQuads; i++) {
			indices.put(index);
			indices.put(index + followOffset);
			index += 4;
		}
		indices.flip();
		return new IndexBuffer(indices);
	}
	
	public static int numberOfQuads(int numberOfIndices) {
		if (numberOfIndices % 4 != 2) throw new IllegalArgumentException("For quad strips the number of indices has to be a power of 2. (indices = 2 * quads + 2)");
		return (numberOfIndices - 2) / 2;
	}
	
	public static int numberOfIndices(int numberOfQuads) {
		return numberOfQuads * 2 + 2;
	}
	
}
