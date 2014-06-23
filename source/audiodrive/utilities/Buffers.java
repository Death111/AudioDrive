package audiodrive.utilities;

import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

public class Buffers {
	
	/** Private constructor to prevent instantiation. */
	private Buffers() {
		throw new IllegalStateException("This class shall not be instantiated.");
	}
	
	public static FloatBuffer create(float... values) {
		return (FloatBuffer) BufferUtils.createFloatBuffer(values.length).put(values).rewind();
	}
	
	public static DoubleBuffer create(double... values) {
		return (DoubleBuffer) BufferUtils.createDoubleBuffer(values.length).put(values).rewind();
	}
	
}
