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
	public static final Color Orange = new Color(1, 0.7, 0);
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
	
	public static Color lerp(Color from, Color to, double fraction) {
		fraction = Arithmetic.clamp(fraction);
		return new Color(from.r + (to.r - from.r) * fraction, from.g + (to.g - from.g) * fraction, from.b + (to.b - from.b) * fraction, from.a + (to.a - from.a) * fraction);
	}
	
	public static Color lerp(Color from, Color over, Color to, double fraction) {
		fraction = Arithmetic.clamp(fraction);
		return lerp(lerp(from, over, 2.0 * fraction), lerp(over, to, 2.0 * fraction - 1.0), fraction);
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
	public Color intensity(double intensity) {
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
	
	public Color inverse() {
		return new Color(1 - r, 1 - g, 1 - b);
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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(a);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(b);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(g);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(r);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Color other = (Color) obj;
		if (Double.doubleToLongBits(a) != Double.doubleToLongBits(other.a)) return false;
		if (Double.doubleToLongBits(b) != Double.doubleToLongBits(other.b)) return false;
		if (Double.doubleToLongBits(g) != Double.doubleToLongBits(other.g)) return false;
		if (Double.doubleToLongBits(r) != Double.doubleToLongBits(other.r)) return false;
		return true;
	}
	
	@Override
	public String toString() {
		return r + "," + g + "," + b + "," + a;
	}
	
	public static Color parse(String string) {
		if (string == null || string.isEmpty()) return null;
		String[] splits = string.split(",");
		if (splits.length != 4) throw new RuntimeException("couldn't parse color string: " + string);
		return new Color(Double.parseDouble(splits[0]), Double.parseDouble(splits[1]), Double.parseDouble(splits[2]), Double.parseDouble(splits[3]));
	}
	
	public void gl() {
		glColor4d(r, g, b, a);
	}
	
	public static Color generateRandomColor(Color mix) {
		double red = Math.random();
		double green = Math.random();
		double blue = Math.random();
		
		// mix the color
		if (mix != null) {
			red = (red + mix.r) / 2;
			green = (green + mix.g) / 2;
			blue = (blue + mix.b) / 2;
		}
		
		Color color = new Color(red, green, blue);
		return color;
	}
	
}
