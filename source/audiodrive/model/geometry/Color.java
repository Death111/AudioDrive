package audiodrive.model.geometry;

import audiodrive.model.buffer.FloatData;

public class Color implements FloatData {

	public static final int Dimension = 4;

	public double r;
	public double g;
	public double b;
	public double a;

	public Color(double r, double g, double b) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = 1;
	}

	public Color(double r, double g, double b, double a) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
	}

	@Override
	public int floats() {
		return Color.Dimension;
	}

	@Override
	public float[] toFloats() {
		return new float[] { (float) r, (float) g, (float) b, (float) a };
	}

	@Override
	public String toString() {
		return this.r + "/" + this.g + "/" + this.b + "/" + this.a;
	}

}
