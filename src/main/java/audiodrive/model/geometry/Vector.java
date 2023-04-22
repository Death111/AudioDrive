package audiodrive.model.geometry;

import static org.lwjgl.opengl.GL11.glNormal3d;
import static org.lwjgl.opengl.GL11.glVertex3d;
import audiodrive.model.buffer.FloatData;
import audiodrive.utilities.Matrices;

public class Vector implements Cloneable, FloatData {
	
	public static final int Dimension = 3;
	
	/** Null vector. */
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
	
	public Vector set(double[] values) {
		if (values.length < Dimension) throw new IllegalArgumentException("Array length has to be >= " + Dimension);
		set(values[0], values[1], values[2]);
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
		assertModifiable();
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}
	
	public Vector subtract(Vector vector) {
		subtract(vector.x, vector.y, vector.z);
		return this;
	}
	
	public Vector subtract(double x, double y, double z) {
		assertModifiable();
		this.x -= x;
		this.y -= y;
		this.z -= z;
		return this;
	}
	
	public Vector xAdd(double x) {
		assertModifiable();
		this.x += x;
		return this;
	}
	
	public Vector yAdd(double y) {
		assertModifiable();
		this.y += y;
		return this;
	}
	
	public Vector zAdd(double z) {
		assertModifiable();
		this.z += z;
		return this;
	}
	
	public Vector xSubtract(double x) {
		assertModifiable();
		this.x -= x;
		return this;
	}
	
	public Vector ySubtract(double y) {
		assertModifiable();
		this.y -= y;
		return this;
	}
	
	public Vector zSubtract(double z) {
		assertModifiable();
		this.z -= z;
		return this;
	}
	
	public Vector multiply(double factor) {
		assertModifiable();
		x *= factor;
		y *= factor;
		z *= factor;
		return this;
	}
	
	public Vector divide(double factor) {
		assertModifiable();
		x /= factor;
		y /= factor;
		z /= factor;
		return this;
	}
	
	public Vector negate() {
		assertModifiable();
		x = -x;
		y = -y;
		z = -z;
		return this;
	}
	
	public Vector x(double x) {
		assertModifiable();
		this.x = x;
		return this;
	}
	
	public Vector y(double y) {
		assertModifiable();
		this.y = y;
		return this;
	}
	
	public Vector z(double z) {
		assertModifiable();
		this.z = z;
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
	
	/**
	 * Sets all vector entries to zero.
	 */
	public Vector empty() {
		set(0, 0, 0);
		return this;
	}
	
	public Vector normalize() {
		divide(length());
		return this;
	}
	
	public Vector length(double length) {
		if (x == 0 && y == 0 && z == 0) throw new RuntimeException("Can't set the length of a vector without direction.");
		return normalize().multiply(length);
	}
	
	public double length() {
		return Math.sqrt(x * x + y * y + z * z);
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
	
	public Vector multiplied(Matrix matrix) {
		return new Vector().set(Matrices.multiply(toHomogeneous(), matrix.toArray()));
	}
	
	public boolean isNull() {
		return equals(Null);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(x);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(z);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Vector other = (Vector) obj;
		if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x)) return false;
		if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y)) return false;
		if (Double.doubleToLongBits(z) != Double.doubleToLongBits(other.z)) return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "(" + x + ", " + y + ", " + z + ")";
	}
	
	public double[] toArray() {
		return new double[]{x, y, z};
	}
	
	public double[] toHomogeneous() {
		return new double[]{x, y, z, 1};
	}
	
	@Override
	public int floats() {
		return Vector.Dimension;
	}
	
	@Override
	public float[] toFloats() {
		return new float[]{(float) x, (float) y, (float) z};
	}
	
	public void glVertex() {
		glVertex3d(x(), y(), z());
	}
	
	public void glNormal() {
		glNormal3d(x(), y(), z());
	}
	
	private void assertModifiable() {
		if (this == Null || this == X || this == Y || this == Z) throw new UnsupportedOperationException("Can't modify a constant vector.");
	}
	
}
