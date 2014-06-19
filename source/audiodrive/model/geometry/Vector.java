package audiodrive.model.geometry;

import static org.lwjgl.opengl.GL11.glVertex3d;

public class Vector implements Cloneable {
	
	/** Unit vector in x direction. */
	public static final Vector Null = new Vector();
	/** Unit vector in x direction. */
	public static final Vector X = new Vector().x(1);
	/** Unit vector in y direction. */
	public static final Vector Y = new Vector().y(1);
	/** Unit vector in z direction. */
	public static final Vector Z = new Vector().z(1);
	
	private double x, y, z;
	
	public Vector() {};
	
	public Vector(double x, double y, double z) {
		set(x, y, z);
	}
	
	public Vector set(Vector vector) {
		set(vector.x, vector.y, vector.z);
		return this;
	}
	
	public Vector set(double x, double y, double z) {
		x(x).y(y).z(z);
		return this;
	}
	
	public Vector add(Vector vector) {
		add(vector.x, vector.y, vector.z);
		return this;
	}
	
	public Vector add(double x, double y, double z) {
		x(x() + x);
		y(y() + y);
		z(z() + z);
		return this;
	}
	
	public Vector subtract(Vector vector) {
		subtract(vector.x, vector.y, vector.z);
		return this;
	}
	
	public Vector subtract(double x, double y, double z) {
		this.x -= x;
		this.y -= y;
		this.z -= z;
		return this;
	}
	
	public Vector multiply(double factor) {
		x *= factor;
		y *= factor;
		z *= factor;
		return this;
	}
	
	public Vector divide(double factor) {
		x /= factor;
		y /= factor;
		z /= factor;
		return this;
	}
	
	/**
	 * Calculates the dot product with the given vector.
	 */
	public double dot(Vector vector) {
		return x * vector.x + y * vector.y + z * vector.z;
	}
	
	/**
	 * Calculates the cross product with the given vector.
	 */
	public Vector cross(Vector vector) {
		return new Vector(y * vector.z - z * vector.y, vector.x * z - vector.z * x, x * vector.y - y * vector.x);
	}
	
	/**
	 * Calculates the angle between two vectors, in degrees.
	 *
	 * @see #angle(Vector)
	 */
	public double degrees(Vector vector) {
		return Math.toDegrees(angle(vector));
	}
	
	/**
	 * Calculates the angle between two vectors, in radian.
	 *
	 * @see #degrees(Vector)
	 */
	public double angle(Vector vector) {
		double n = dot(vector) / (length() * vector.length());
		if (n < -1.0) n = -1.0;
		else if (n > 1.0) n = 1.0;
		return Math.acos(n);
	}
	
	public Vector normalize() {
		divide(length());
		return this;
	}
	
	public Vector negate() {
		x = -x;
		y = -y;
		z = -z;
		return this;
	}
	
	public Vector x(double x) {
		this.x = x;
		return this;
	}
	
	public Vector y(double y) {
		this.y = y;
		return this;
	}
	
	public Vector z(double z) {
		this.z = z;
		return this;
	}
	
	public Vector length(double length) {
		if (x == 0 && y == 0 && z == 0) throw new RuntimeException("Can't set the length of a vector without direction.");
		return normalize().multiply(length);
	}
	
	public double x() {
		return x;
	}
	
	public double y() {
		return y;
	}
	
	public double z() {
		return z;
	}
	
	public double length() {
		return Math.sqrt(x * x + y * y + z * z);
	}
	
	@Override
	public Vector clone() {
		try {
			return (Vector) super.clone();
		} catch (CloneNotSupportedException exception) {
			throw new RuntimeException(exception);
		}
	}
	
	public Vector plus(Vector vector) {
		return clone().add(vector);
	}
	
	public Vector plus(double x, double y, double z) {
		return clone().add(x, y, z);
	}
	
	public Vector minus(Vector vector) {
		return clone().subtract(vector);
	}
	
	public Vector minus(double x, double y, double z) {
		return clone().subtract(x, y, z);
	}
	
	public Vector multiplied(double factor) {
		return clone().multiply(factor);
	}
	
	public Vector divided(double factor) {
		return clone().divide(factor);
	}
	
	public Vector normalized() {
		return clone().normalize();
	}
	
	public Vector negated() {
		return clone().negate();
	}
	
	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (object == null) return false;
		if (getClass() != object.getClass()) return false;
		Vector other = (Vector) object;
		return (x == other.x && y == other.y && z == other.z);
	}
	
	@Override
	public String toString() {
		return "(" + x + ", " + y + ", " + z + ")";
	}
	
	public void gl() {
		glVertex3d(x(), y(), z());
	}
	
}
