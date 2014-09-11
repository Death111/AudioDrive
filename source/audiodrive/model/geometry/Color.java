package audiodrive.model.geometry;

import audiodrive.model.buffer.FloatData;
import audiodrive.utilities.Arithmetic;

public class Color implements FloatData {

	public static final int Dimension = 4;

	public double r;
	public double g;
	public double b;
	public double a;

	public Color(Color color) {
		this(color.r, color.g, color.b, color.a);
	}

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

	public static Color Lerp(Color a, Color b, float t) {
		t = (float) Arithmetic.clamp((double) t);
		return new Color(a.r + (b.r - a.r) * t, a.g + (b.g - a.g) * t, a.b + (b.b - a.b) * t, a.a + (b.a - a.a) * t);
	}

	/**
	 * @param r
	 *            the r to set
	 */
	public final Color r(double r) {
		this.r = r;
		return this;
	}

	/**
	 * @param g
	 *            the g to set
	 */
	public final Color g(double g) {
		this.g = g;
		return this;
	}

	/**
	 * @param b
	 *            the b to set
	 */
	public final Color b(double b) {
		this.b = b;
		return this;
	}

	/**
	 * @param a
	 *            the a to set
	 */
	public final Color a(double a) {
		this.a = a;
		return this;
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

	/**
	 * @return the rED
	 */
	public static final Color RED() {
		return new Color(1, 0, 0);
	}

	/**
	 * @return the gREEN
	 */
	public static final Color GREEN() {
		return new Color(0, 1, 0);
	}

	/**
	 * @return the bLUE
	 */
	public static final Color BLUE() {
		return new Color(0, 0, 1);
	}

	/**
	 * @return the yELLOW
	 */
	public static final Color YELLOW() {
		return new Color(1, 1, 0);
	}

	/**
	 * @return the tUERKIS
	 */
	public static final Color TUERKIS() {
		return new Color(0, 1, 1);
	}

	/**
	 * @return the wHITE
	 */
	public static final Color WHITE() {
		return new Color(1, 1, 1);
	}

	/**
	 * @return the bLACK
	 */
	public static final Color BLACK() {
		return new Color(0, 0, 0);
	}

}
