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

import audiodrive.model.geometry.Color;
import audiodrive.model.geometry.Face;
import audiodrive.model.geometry.TextureCoordinate;
import audiodrive.model.geometry.Vector;
import audiodrive.model.geometry.Vertex;
import audiodrive.utilities.Buffers;

public class VertexBuffer {
	
	public static final int FloatSize = Float.SIZE / Byte.SIZE;
	
	private final int id;
	private final int target;
	private final int type;
	private final int size;
	
	private int stride = 0;
	private int vertexSize = 1;
	private int offset = 0;
	private int mode = GL_POINTS;
	
	private boolean normalize = false;
	private boolean bound = false;
	private boolean useColor = false;
	private boolean useTexture = false;
	
	private IndexBuffer indexBuffer;
	
	/**
	 * Creates a vertex buffer object with the specified target and usage for {@linkplain GL15#glBufferData}.
	 */
	public VertexBuffer(Buffer buffer, int target, int usage, int vertexSize) {
		this.target = target;
		if (buffer.limit() % vertexSize != 0) throw new RuntimeException("Buffer limit has to be a multiple of the vertex size.");
		size = buffer.limit() / vertexSize;
		this.vertexSize = vertexSize;
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
	}
	
	/**
	 * Creates a vertex buffer object with target {@linkplain GL15#GL_ARRAY_BUFFER} and usage {@linkplain GL15#GL_STATIC_DRAW}.
	 */
	public VertexBuffer(Buffer buffer, int vertexSize) {
		this(buffer, GL_ARRAY_BUFFER, GL_STATIC_DRAW, vertexSize);
	}
	
	/**
	 * Creates a vertex buffer object with target {@linkplain GL15#GL_ARRAY_BUFFER} and usage {@linkplain GL15#GL_STATIC_DRAW} and vertex size = {@linkplain Vector#Dimension}.
	 */
	public VertexBuffer(Vector... vectors) {
		this(Buffers.create(vectors), Vector.Dimension);
	}
	
	/**
	 * Creates a vertex buffer object with target {@linkplain GL15#GL_ARRAY_BUFFER} and usage {@linkplain GL15#GL_STATIC_DRAW} and vertex size = {@linkplain Vertex#Dimension}.
	 */
	public VertexBuffer(Vertex... vertices) {
		this(Buffers.create(vertices), Vertex.Dimension);
	}
	
	/**
	 * Creates a vertex buffer object with target {@linkplain GL15#GL_ARRAY_BUFFER} and usage {@linkplain GL15#GL_STATIC_DRAW} and vertex size according to the first list entry.
	 */
	public VertexBuffer(List<? extends FloatData> data) {
		this(Buffers.create(data), determineVertexSize(data));
	}
	
	private static int determineVertexSize(List<? extends FloatData> data) {
		if (!data.isEmpty()) {
			FloatData firstEntry = data.get(0);
			if (firstEntry instanceof Vector) return Vector.Dimension;
			if (firstEntry instanceof Vertex) return Vertex.Dimension;
			if (firstEntry instanceof Face) return Vertex.Dimension;
		}
		throw new RuntimeException("Can't determine the vertex size of an empty list.");
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
		if (vertexSize == Vertex.Dimension) {
			int stride = Vertex.Dimension * FloatSize;
			int offset = Vector.Dimension * FloatSize;
			glEnableClientState(GL_VERTEX_ARRAY);
			glVertexPointer(Vector.Dimension, type, stride, 0);
			glEnableClientState(GL_NORMAL_ARRAY);
			glNormalPointer(type, stride, offset);
			if (useTexture) {
				glEnableClientState(GL_TEXTURE_COORD_ARRAY);
				glTexCoordPointer(TextureCoordinate.Dimension, type, stride, 2 * offset);
			}
			if (useColor) {
				glEnableClientState(GL_COLOR_ARRAY);
				glColorPointer(Color.Dimension, type, stride, 2 * offset + TextureCoordinate.Dimension * FloatSize);
			}
		} else {
			glEnableClientState(GL_VERTEX_ARRAY);
			glVertexPointer(vertexSize, type, stride * FloatSize, 0);
		}
		if (indexBuffer != null) indexBuffer.bind();
		bound = true;
	}
	
	/**
	 * Unbinds the buffer
	 */
	private void unbind() {
		glDisableClientState(GL_VERTEX_ARRAY);
		glDisableClientState(GL_NORMAL_ARRAY);
		glDisableClientState(GL_TEXTURE_COORD_ARRAY);
		glDisableClientState(GL_COLOR_ARRAY);
		bound = false;
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
		if (bound) unbind();
		bound = false;
	}
	
	/**
	 * Draws the buffer content using glDrawArrays or glDrawElements, if indices were specified using {@link #indices(int...)} or {@link #indices(IndexBuffer)}.
	 */
	public void draw() {
		if (!bound) bind();
		if (indexBuffer == null) glDrawArrays(mode, offset, size);
		else glDrawElements(mode, indexBuffer.size(), GL_UNSIGNED_INT, offset);
		if (bound) unbind();
		bound = false;
	}
	
	public int id() {
		return id;
	}
	
	/**
	 * Indicates the type of the buffer. (GL_DOUBLE, GL_FLOAT, ...)
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
	 * Indicates the step width of the buffer. I. e. the vertex size.
	 */
	public int vertexSize() {
		return vertexSize;
	}
	
	/**
	 * Indicates the number of entries in the buffer.
	 */
	public int size() {
		return size;
	}
	
	/**
	 * Specifies whether to use vertex colors or not.
	 */
	public VertexBuffer useColor(boolean useColor) {
		this.useColor = useColor;
		return this;
	}
	
	/**
	 * Specifies whether to use texture coordinates or not.
	 */
	public VertexBuffer useTexture(boolean useTexture) {
		this.useTexture = useTexture;
		return this;
	}
	
	public boolean useColor() {
		return useColor;
	}
	
	public boolean useTexture() {
		return useTexture;
	}
	
}
