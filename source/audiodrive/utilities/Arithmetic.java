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
		return clamp(value, 0.0, 1.0);
	}
	
	public static double clamp(double value, double minimum, double maximum) {
		if (value < minimum) return minimum;
		if (value > maximum) return maximum;
		return value;
	}
	
	public static int clamp(int value, int minimum, int maximum) {
		if (value < minimum) return minimum;
		if (value > maximum) return maximum;
		return value;
	}
	
	public static double smooth(double from, double to, double fraction) {
		fraction = clamp(fraction);
		fraction = -2.0 * fraction * fraction * fraction + 3.0 * fraction * fraction;
		return to * fraction + from * (1.0 - fraction);
	}
	
	public static double scaleLinear(double value, double from, double to, double minimum, double maximum) {
		return from + (to - from) * (value - minimum) / (maximum - minimum);
	}
	
	public static double scaleLogarithmic(double value, double from, double to, double minimum, double maximum) {
		return Math.sqrt(scaleLinear(value, from, to, minimum, maximum));
	}
}
