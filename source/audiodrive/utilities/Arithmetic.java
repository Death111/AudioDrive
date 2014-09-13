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

	public static double fraction(double value) {
		return value - (int) value;
	}

	public static double clamp(double value) {
		if (value < 0.0)
			return 0.0;
		if (value > 1.0)
			return 1.0;
		return value;
	}

	public static double smooth(double from, double to, double fraction) {
		fraction = clamp(fraction);
		fraction = -2.0 * fraction * fraction * fraction + 3.0 * fraction * fraction;
		return to * fraction + from * (1.0 - fraction);
	}

	public static double linearScale(double current, final double minDest, final double maxDest, final double minSource, final double maxSource) {
		return (maxDest - minDest) * (current - minSource) / (maxSource - minSource) + minDest;
	}

}
