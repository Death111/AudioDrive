package audiodrive.model.buffer;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.util.ArrayList;
import java.util.List;

public class VertexArray {
	
	public final int id;
	private int index;
	
	private List<VertexBuffer> buffers = new ArrayList<>();
	
	public VertexArray() {
		id = glGenVertexArrays();
	}
	
	public VertexArray addAttributes(VertexBuffer buffer) {
		buffers.add(buffer);
		return this;
	}
	
	public void bind() {
		glBindVertexArray(id);
		buffers.forEach(buffer -> {
			buffer.bind();
			glEnableVertexAttribArray(index);
			glVertexAttribPointer(index++, buffer.vertexSize(), buffer.type(), buffer.normalize(), 0, 0);
		});
	}
	
	public void unbind() {
		glBindVertexArray(0);
	}
	
}
