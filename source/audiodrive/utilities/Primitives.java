package audiodrive.utilities;

import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

public class Primitives {
	
	/** Private constructor to prevent instantiation. */
	private Primitives() {
		throw new IllegalStateException("This class shall not be instantiated.");
	}
	
	public static DoubleStream stream(float... floats) {
		return IntStream.range(0, floats.length).mapToDouble(i -> floats[i]);
	}
}
