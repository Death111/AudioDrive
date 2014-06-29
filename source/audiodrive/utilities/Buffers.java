package audiodrive.utilities;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.List;

import org.lwjgl.PointerBuffer;

import audiodrive.model.geometry.Vector;

/**
 * Utility class to create buffers.
 */
public class Buffers {
	
	/** Private constructor to prevent instantiation. */
	private Buffers() {
		throw new IllegalStateException("This class shall not be instantiated.");
	}
	
	/**
	 * Construct a direct native-ordered FloatBuffer with the specified vertices.
	 * 
	 * @param vectors the buffer elements
	 * @return a FloatBuffer
	 */
	public static FloatBuffer create(List<Vector> vectors) {
		FloatBuffer buffer = createFloatBuffer(vectors.size() * 3);
		vectors.stream().map(Vector::toFloats).forEach(buffer::put);
		buffer.flip();
		return buffer;
	}
	
	/**
	 * Construct a direct native-ordered FloatBuffer with the specified vertices.
	 * 
	 * @param vectors the buffer elements
	 * @return a FloatBuffer
	 */
	public static FloatBuffer create(Vector... vectors) {
		FloatBuffer buffer = createFloatBuffer(vectors.length * 3);
		for (Vector vector : vectors) {
			buffer.put(vector.toFloats());
		}
		buffer.flip();
		return buffer;
	}
	
	/**
	 * Construct a direct native-ordered ByteBuffer with the specified elements.
	 * 
	 * @param values the buffer elements
	 * @return a ByteBuffer
	 */
	public static ByteBuffer create(byte... values) {
		return (ByteBuffer) createByteBuffer(values.length).put(values).flip();
	}
	
	/**
	 * Construct a direct native-ordered ShortBuffer with the specified elements.
	 * 
	 * @param values the buffer elements
	 * @return a ShortBuffer
	 */
	public static ShortBuffer create(short... values) {
		return (ShortBuffer) createShortBuffer(values.length).put(values).flip();
	}
	
	/**
	 * Construct a direct native-ordered CharBuffer with the specified elements.
	 * 
	 * @param values the buffer elements
	 * @return a CharBuffer
	 */
	public static CharBuffer create(char... values) {
		return (CharBuffer) createCharBuffer(values.length).put(values).flip();
	}
	
	/**
	 * Construct a direct native-ordered IntBuffer with the specified elements.
	 * 
	 * @param values the buffer elements
	 * @return a IntBuffer
	 */
	public static IntBuffer create(int... values) {
		return (IntBuffer) createIntBuffer(values.length).put(values).flip();
	}
	
	/**
	 * Construct a direct native-ordered LongBuffer with the specified elements.
	 * 
	 * @param values the buffer elements
	 * @return a LongBuffer
	 */
	public static LongBuffer create(long... values) {
		return (LongBuffer) createLongBuffer(values.length).put(values).flip();
	}
	
	/**
	 * Construct a direct native-ordered FloatBuffer with the specified elements.
	 * 
	 * @param values the buffer elements
	 * @return a FloatBuffer
	 */
	public static FloatBuffer create(float... values) {
		return (FloatBuffer) createFloatBuffer(values.length).put(values).flip();
	}
	
	/**
	 * Construct a direct native-ordered DoubleBuffer with the specified elements.
	 * 
	 * @param values the buffer elements
	 * @return a DoubleBuffer
	 */
	public static DoubleBuffer create(double... values) {
		return (DoubleBuffer) createDoubleBuffer(values.length).put(values).flip();
	}
	
	/**
	 * Construct a direct native-ordered ByteBuffer with the specified size.
	 * 
	 * @param size The size, in bytes
	 * @return a ByteBuffer
	 */
	public static ByteBuffer createByteBuffer(int size) {
		return ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder());
	}
	
	/**
	 * Construct a direct native-order ShortBuffer with the specified number of elements.
	 * 
	 * @param size The size, in shorts
	 * @return a ShortBuffer
	 */
	public static ShortBuffer createShortBuffer(int size) {
		return createByteBuffer(size << 1).asShortBuffer();
	}
	
	/**
	 * Construct a direct native-order CharBuffer with the specified number of elements.
	 * 
	 * @param size The size, in chars
	 * @return an CharBuffer
	 */
	public static CharBuffer createCharBuffer(int size) {
		return createByteBuffer(size << 1).asCharBuffer();
	}
	
	/**
	 * Construct a direct native-order IntBuffer with the specified number of elements.
	 * 
	 * @param size The size, in ints
	 * @return an IntBuffer
	 */
	public static IntBuffer createIntBuffer(int size) {
		return createByteBuffer(size << 2).asIntBuffer();
	}
	
	/**
	 * Construct a direct native-order LongBuffer with the specified number of elements.
	 * 
	 * @param size The size, in longs
	 * @return an LongBuffer
	 */
	public static LongBuffer createLongBuffer(int size) {
		return createByteBuffer(size << 3).asLongBuffer();
	}
	
	/**
	 * Construct a direct native-order FloatBuffer with the specified number of elements.
	 * 
	 * @param size The size, in floats
	 * @return a FloatBuffer
	 */
	public static FloatBuffer createFloatBuffer(int size) {
		return createByteBuffer(size << 2).asFloatBuffer();
	}
	
	/**
	 * Construct a direct native-order DoubleBuffer with the specified number of elements.
	 * 
	 * @param size The size, in floats
	 * @return a FloatBuffer
	 */
	public static DoubleBuffer createDoubleBuffer(int size) {
		return createByteBuffer(size << 3).asDoubleBuffer();
	}
	
	/**
	 * Construct a PointerBuffer with the specified number of elements.
	 * 
	 * @param size The size, in memory addresses
	 * @return a PointerBuffer
	 */
	public static PointerBuffer createPointerBuffer(int size) {
		return PointerBuffer.allocateDirect(size);
	}
	
}
