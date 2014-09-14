package audiodrive.model.geometry;

import static org.lwjgl.opengl.GL11.glColor4d;
import audiodrive.model.buffer.FloatData;
import audiodrive.utilities.Arithmetic;

public class Color implements FloatData {
	
	public static final int Dimension = 4;
	
	public static final Color Black = new Color(0, 0, 0);
	public static final Color White = new Color(1, 1, 1);
	public static final Color Gray = new Color(0.5, 0.5, 0.5);
	public static final Color Red = new Color(1, 0, 0);
	public static final Color Green = new Color(0, 1, 0);
	public static final Color Blue = new Color(0, 0, 1);
	public static final Color Yellow = new Color(1, 1, 0);
	public static final Color Magenta = new Color(1, 0, 1);
	public static final Color Cyan = new Color(0, 1, 1);
	public static final Color Transparent = new Color(0, 0, 0, 0);
	public static final Color TransparentBlack = new Color(0, 0, 0, 0.5);
	public static final Color TransparentWhite = new Color(1, 1, 1, 0.5);
	public static final Color TransparentGray = new Color(0.5, 0.5, 0.5, 0.5);
	public static final Color TransparentRed = new Color(1, 0, 0, 0.5);
	public static final Color TransparentGreen = new Color(0, 1, 0, 0.5);
	public static final Color TransparentBlue = new Color(0, 0, 1, 0.5);
	public static final Color TransparentYellow = new Color(1, 1, 0, 0.5);
	public static final Color TransparentMagenta = new Color(1, 0, 1, 0.5);
	public static final Color TransparentCyan = new Color(0, 1, 1, 0.5);
	
	public final double r;
	public final double g;
	public final double b;
	public final double a;
	
	public Color(Color color) {
		this(color.r, color.g, color.b, color.a);
	}
	
	public Color(double r, double g, double b) {
		this.r = r;
		this.g = g;
		this.b = b;
		a = 1;
	}
	
	public Color(double r, double g, double b, double a) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
	}
	
	public static Color Lerp(Color a, Color b, float t) {
		t = (float) Arithmetic.clamp(t);
		return new Color(a.r + (b.r - a.r) * t, a.g + (b.g - a.g) * t, a.b + (b.b - a.b) * t, a.a + (b.a - a.a) * t);
	}
	
	@Override
	public Color clone() {
		return new Color(this);
	}
	
	/**
	 * Returns a derived color with modified intensity.
	 * 
	 * @param itensity [0.0, 1.0]
	 * @return the derived color
	 */
	public Color itensity(double intensity) {
		intensity = Arithmetic.clamp(intensity);
		return new Color(r * intensity, g * intensity, b * intensity);
	}
	
	/**
	 * Returns a derived color with modified red.
	 */
	public final Color red(double red) {
		return new Color(red, g, b, a);
	}
	
	/**
	 * Returns a derived color with modified green.
	 */
	public final Color green(double green) {
		return new Color(r, green, b, a);
	}
	
	/**
	 * Returns a derived color with modified blue.
	 */
	public final Color blue(double blue) {
		return new Color(r, g, blue, a);
	}
	
	/**
	 * Returns a derived color with modified alpha.
	 */
	public final Color alpha(double alpha) {
		return new Color(r, g, b, alpha);
	}
	
	@Override
	public int floats() {
		return Color.Dimension;
	}
	
	@Override
	public float[] toFloats() {
		return new float[]{(float) r, (float) g, (float) b, (float) a};
	}
	
	@Override
	public String toString() {
		return r + "," + g + "," + b + "," + a;
	}
	
	public static Color parse(String string) {
		String[] splits = string.split(",");
		if (splits.length != 4) throw new RuntimeException("couldn't parse color string: " + string);
		return new Color(Double.parseDouble(splits[0]), Double.parseDouble(splits[1]), Double.parseDouble(splits[2]), Double.parseDouble(splits[3]));
	}
	
	public void gl() {
		glColor4d(r, g, b, a);
	}
	
}
