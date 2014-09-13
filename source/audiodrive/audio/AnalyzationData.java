package audiodrive.audio;

import java.util.Iterator;
import java.util.stream.DoubleStream;

import audiodrive.utilities.Arithmetic;
import audiodrive.utilities.Primitives;

public class AnalyzationData implements Iterable<Float> {
	
	public final float[] values;
	public final float maximumAbsolute;
	public final float minimum;
	public final float maximum;
	
	public AnalyzationData(float[] values) {
		this.values = values;
		maximumAbsolute = (float) stream().map(Math::abs).max().orElse(0.0);
		minimum = (float) stream().min().orElse(0.0);
		maximum = (float) stream().max().orElse(0.0);
	}
	
	public DoubleStream stream() {
		return Primitives.stream(values);
	}
	
	/**
	 * Clamps the value to the range [-1, 1] proportional to the absolute maximum.
	 */
	public float clamp(float value) {
		return (float) Arithmetic.clamp(proportional(value));
	}
	
	/**
	 * Returns the value's proportion to the absolute maximum.
	 */
	public float proportional(float value) {
		return value / maximumAbsolute;
	}
	
	public float getClamped(int index) {
		return clamp(values[index]);
	}
	
	public float get(int index) {
		return values[index];
	}
	
	public int size() {
		return values.length;
	}
	
	@Override
	public Iterator<Float> iterator() {
		return new Iterator<Float>() {
			private int index = 0;
			
			@Override
			public boolean hasNext() {
				return index < values.length;
			}
			
			@Override
			public Float next() {
				return values[index++];
			}
		};
	}
}
