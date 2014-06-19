package audiodrive.utilities;

public class Arithmetic {
	
	/** Private constructor to prevent instantiation. */
	private Arithmetic() {
		throw new IllegalStateException("This class shall not be instantiated.");
	}
	
	/**
	 * Returns a value which is a power of two and larger or equal to value.
	 */
	public static int nextPowerOfTwo(int value) {
		value--;
		value |= value >> 1;
		value |= value >> 2;
		value |= value >> 4;
		value |= value >> 8;
		value |= value >> 16;
		value++;
		return value;
	}
	
}
