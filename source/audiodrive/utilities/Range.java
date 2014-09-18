package audiodrive.utilities;

import java.util.List;

public class Range {
	
	public final double minimum;
	public final double maximum;
	
	public Range(double minimum, double maximum) {
		this.minimum = minimum;
		this.maximum = maximum;
	}
	
	public boolean contains(double value) {
		return minimum < value && value < maximum;
	}
	
	public double span() {
		return maximum - minimum;
	}
	
	@Override
	public String toString() {
		return "[" + minimum + ", " + maximum + "]";
	}
	
	public static Range of(float... values) {
		float minimum = 0, maximum = 0;
		for (float value : values) {
			minimum = Math.min(minimum, value);
			maximum = Math.max(maximum, value);
		}
		return new Range(minimum, maximum);
	}
	
	public static Range of(double... values) {
		double minimum = 0, maximum = 0;
		for (double value : values) {
			minimum = Math.min(minimum, value);
			maximum = Math.max(maximum, value);
		}
		return new Range(minimum, maximum);
	}
	
	public static Range of(List<? extends Number> numbers) {
		double minimum = 0, maximum = 0;
		for (Number number : numbers) {
			minimum = Math.min(minimum, number.doubleValue());
			maximum = Math.max(maximum, number.doubleValue());
		}
		return new Range(minimum, maximum);
	}
	
}
