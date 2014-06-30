package audiodrive.model.buffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.List;
import java.util.Objects;

import org.lwjgl.opengl.GL15;

import audiodrive.model.geometry.Vector;
import audiodrive.utilities.Buffers;

public class VertexBuffer {
	
	private final int id;
	private final int target;
	private final int type;
	private final int size;
	
	private int step = 1;
	private int offset = 0;
	private int mode = GL_POINTS;
	
	private boolean normalize = false;
	private boolean bound = false;
	
	private IndexBuffer indexBuffer;
	
	/**
	 * Creates a vertex buffer object with the specified target and usage for {@linkplain GL15#glBufferData}.
	 */
	public VertexBuffer(Buffer buffer, int target, int usage) {
		this.target = target;
		size = buffer.limit();
		id = glGenBuffers();
		glBindBuffer(target, id);
		if (buffer instanceof DoubleBuffer) {
			glBufferData(target, (DoubleBuffer) buffer, usage);
			type = GL_DOUBLE;
		} else if (buffer instanceof FloatBuffer) {
			glBufferData(target, (FloatBuffer) buffer, usage);
			type = GL_FLOAT;
		} else if (buffer instanceof IntBuffer) {
			glBufferData(target, (IntBuffer) buffer, usage);
			type = GL_INT;
		} else if (buffer instanceof ShortBuffer) {
			glBufferData(target, (ShortBuffer) buffer, usage);
			type = GL_SHORT;
		} else if (buffer instanceof ByteBuffer) {
			glBufferData(target, (ByteBuffer) buffer, usage);
			type = GL_BYTE;
		} else {
			throw new IllegalArgumentException("Unsupported buffer type.");
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		glDeleteBuffers(id);
	};
	
	/**
	 * Creates a vertex buffer object with target {@linkplain GL15#GL_ARRAY_BUFFER} and usage {@linkplain GL15#GL_STATIC_DRAW}.
	 */
	public VertexBuffer(Buffer buffer) {
		this(buffer, GL_ARRAY_BUFFER, GL_STATIC_DRAW);
	}
	
	/**
	 * Creates a vertex buffer object with target {@linkplain GL15#GL_ARRAY_BUFFER} and usage {@linkplain GL15#GL_STATIC_DRAW} and step size = {@linkplain Vector#Dimension}.
	 */
	public VertexBuffer(Vector... vectors) {
		this(Buffers.create(vectors));
		step(Vector.Dimension);
	}
	
	/**
	 * Creates a vertex buffer object with target {@linkplain GL15#GL_ARRAY_BUFFER} and usage {@linkplain GL15#GL_STATIC_DRAW} and step size = {@linkplain Vector#Dimension}.
	 */
	public VertexBuffer(List<Vector> vectors) {
		this(Buffers.create(vectors));
		step(Vector.Dimension);
	}
	
	/**
	 * Sets the index buffer for this vertex buffer.
	 */
	public VertexBuffer indices(IndexBuffer buffer) {
		if (buffer == null || buffer.size() == 0) {
			indexBuffer = null;
			return this;
		}
		indexBuffer = buffer;
		return this;
	}
	
	/**
	 * Sets the indices for this vertex buffer using an index buffer.
	 */
	public VertexBuffer indices(int... indices) {
		indices(new IndexBuffer(indices));
		return this;
	}
	
	/**
	 * Returns the index buffer of this vertex buffer.
	 */
	public IndexBuffer indices() {
		return indexBuffer;
	}
	
	/**
	 * Binds the buffer to the GL context and sets the vertex pointer.
	 */
	public void bind() {
		glBindBuffer(target, id);
		glVertexPointer(step, type, 0, 0);
		if (indexBuffer != null) indexBuffer.bind();
		bound = true;
	}
	
	/**
	 * Sets the drawing mode. (GL_POINTS, GL_LINES, GL_TRIANGLES...)
	 */
	public VertexBuffer mode(int mode) {
		this.mode = mode;
		return this;
	}
	
	/**
	 * Sets the drawing offset.
	 */
	public VertexBuffer offset(int offset) {
		this.offset = offset;
		return this;
	}
	
	/**
	 * Draws the buffer content using glDrawElements with the indices of the given index buffer.
	 */
	public void draw(IndexBuffer indices) {
		Objects.requireNonNull(indices);
		if (!bound) bind();
		indices.bind();
		glDrawElements(mode, indices.size(), GL_UNSIGNED_INT, offset);
		bound = false;
	}
	
	/**
	 * Draws the buffer content using glDrawArrays or glDrawElements, if indices were specified using {@link #indices(int...)} or {@link #indices(IndexBuffer)}.
	 */
	public void draw() {
		if (!bound) bind();
		if (indexBuffer == null) glDrawArrays(mode, offset, size / step);
		else glDrawElements(mode, indexBuffer.size(), GL_UNSIGNED_INT, offset);
		bound = false;
	}
	
	public int id() {
		return id;
	}
	
	/**
	 * Indicates the type of the buffer. (GL_DOUBLE, GL_Float, ...)
	 */
	public int type() {
		return type;
	}
	
	public VertexBuffer normalize(boolean normalize) {
		this.normalize = normalize;
		return this;
	}
	
	public boolean normalize() {
		return normalize;
	}
	
	/**
	 * Sets the step size of the buffer. I. e. the entry size.
	 */
	public VertexBuffer step(int step) {
		this.step = step;
		return this;
	}
	
	/**
	 * Indicates the step width of the buffer. I. e. the entry size.
	 */
	public int step() {
		return step;
	}
	
	/**
	 * Indicates the number of entries in the buffer.
	 */
	public int size() {
		return size;
	}
	
}
