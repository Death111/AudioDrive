package audiodrive.model.buffer;

import static org.lwjgl.opengl.GL11.GL_INT;
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
	
	public void bind() {
		glBindBuffer(Target, id);
	}
	
	public int size() {
		return entries;
	}
	
	public void delete() {
		glDeleteBuffers(id);
	}
	
}
