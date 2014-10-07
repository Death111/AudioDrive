package audiodrive.model.geometry;

import audiodrive.model.buffer.FloatData;

public class TextureCoordinate implements FloatData {

	public static final int Dimension = 2;

	public double x;
	public double y;

	public TextureCoordinate(double x, double y) {
		super();
		this.x = x;
		this.y = y;
	}

	@Override
	public int floats() {
		return TextureCoordinate.Dimension;
	}

	@Override
	public float[] toFloats() {
		return new float[] { (float) x, (float) y };
	}

	@Override
	public String toString() {
		return this.x + "/" + this.y;
	}
}
