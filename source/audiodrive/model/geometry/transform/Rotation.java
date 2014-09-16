package audiodrive.model.geometry.transform;

import static org.lwjgl.opengl.GL11.*;
import audiodrive.model.geometry.Matrix;
import audiodrive.model.geometry.Vector;

public class Rotation extends Transformation {
	
	/**
	 * No rotation. I. e. the rotation matrix is equal to identity.
	 */
	public static final Rotation Null = new Rotation();
	
	private double x, y, z;
	private final Matrix matrix = new Matrix().identity();
	
	public Rotation x(double angle) {
		set(angle, y, z);
		return this;
	}
	
	public Rotation y(double angle) {
		set(x, angle, z);
		return this;
	}
	
	public Rotation z(double angle) {
		set(x, y, angle);
		return this;
	}
	
	public Rotation xAdd(double angle) {
		add(angle, Vector.X);
		return this;
	}
	
	public Rotation yAdd(double angle) {
		add(angle, Vector.Y);
		return this;
	}
	
	public Rotation zAdd(double angle) {
		add(angle, Vector.Z);
		return this;
	}
	
	public double x() {
		if (isMultiplex()) throw new RuntimeException("Euler angles are ambiguous for a rotation around more than one axis.");
		return x;
	}
	
	public double y() {
		if (isMultiplex()) throw new RuntimeException("Euler angles are ambiguous for a rotation around more than one axis.");
		return y;
	}
	
	public double z() {
		if (isMultiplex()) throw new RuntimeException("Euler angles are ambiguous for a rotation around more than one axis.");
		return z;
	}
	
	public Rotation add(double angle, Vector axis) {
		assertModifiable();
		if (!isMultiplex()) {
			if (axis.equals(Vector.X) || axis.equals(Vector.X.negated())) x += angle;
			else if (axis.equals(Vector.Y) || axis.equals(Vector.Y.negated())) y += angle;
			else if (axis.equals(Vector.Z) || axis.equals(Vector.Z.negated())) z += angle;
		}
		matrix.rotate(unify180(angle), axis);
		return this;
	}
	
	public Rotation set(Rotation rotation) {
		assertModifiable();
		set(rotation.x, rotation.y, rotation.z);
		return this;
	}
	
	public Rotation set(Matrix matrix) {
		assertModifiable();
		markAsMultiplex(); // since we can't determine unambiguous Euler angles from a rotation matrix
		this.matrix.set(matrix);
		return this;
	}
	
	public Rotation set(double x, double y, double z) {
		assertModifiable();
		this.x = x;
		this.y = y;
		this.z = z;
		matrix.rotation(x, y, z);
		return this;
	}
	
	public Rotation align(Vector direction, Vector normal) {
		assertModifiable();
		markAsMultiplex();
		matrix.alignment(direction, normal);
		return this;
	}
	
	/**
	 * Resets the rotation to {@linkplain #Null}. I. e. sets the rotation matrix to identity.
	 */
	public Rotation reset() {
		assertModifiable();
		x = 0;
		y = 0;
		z = 0;
		matrix.identity();
		return this;
	}
	
	public Rotation invert() {
		x = -x;
		y = -y;
		z = -z;
		matrix.invert();
		return this;
	}
	
	public Rotation inverted() {
		return new Rotation().set(matrix.inverted());
	}
	
	@Override
	public boolean ignorable() {
		return isNull();
	}
	
	@Override
	public void apply() {
		if (ignorable()) return;
		if (isMultiplex()) {
			glMultMatrix(matrix.toDoubleBuffer());
		} else {
			if (x != 0) glRotated(x, 1, 0, 0);
			else if (y != 0) glRotated(y, 0, 1, 0);
			else glRotated(z, 0, 0, 1);
		}
	}
	
	/**
	 * Returns a rotated version of the given vector.
	 */
	public Vector rotate(Vector vector) {
		return matrix.multiplied(vector);
	}
	
	/**
	 * Returns a rotated version of the given matrix.
	 */
	public Matrix rotate(Matrix matrix) {
		return matrix.multiplied(this.matrix);
	}
	
	private void markAsMultiplex() {
		x = y = z = Double.NaN;
	}
	
	/**
	 * Indicates whether the rotation is multiplex, i.e. a rotation around more than one axis.
	 */
	public boolean isMultiplex() {
		if (x == 0 && y == 0) return false;
		if (x == 0 && z == 0) return false;
		if (y == 0 && z == 0) return false;
		return true;
	}
	
	public boolean isNull() {
		return equals(Null);
	}
	
	private void assertModifiable() {
		if (this == Null) throw new UnsupportedOperationException("Can't modify the constant rotation 'Null'.");
	}
	
	@Override
	public int hashCode() {
		return 31 + matrix.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		return matrix.equals(((Rotation) obj).matrix);
	}
	
	public Matrix toMatrix() {
		return new Matrix().set(matrix);
	}
	
	@Override
	public String toString() {
		return "Rotation [matrix=" + matrix + "]";
	}
	
	/**
	 * Limits the given angle to the range 0 to 360. <br>
	 * <br>
	 * <i>For example: -5.0 -> 0.0 and 365.0 -> 360.0</i>
	 */
	public static double limit(double angle) {
		if (angle < 0.0) angle = 0.0;
		else if (angle > 360.0) angle = 360.0;
		return angle;
	}
	
	/**
	 * Unifies the given angle into the range 0 to 360. <br>
	 * <br>
	 * <i>For example: -5.0 -> 355.0 and 365.0 -> 5.0</i>
	 */
	public static double unify(double angle) {
		boolean negative = angle < 0.0;
		angle = angle % 360.0;
		if (angle == -0.0) return 0.0;
		if (negative) return 360 + angle;
		return angle;
	}
	
	/**
	 * Unifies the given angle into the range -180 to 180. <br>
	 * <br>
	 * <i>For example: 185.0 -> -175.0 and -185.0 -> 175.0</i>
	 */
	public static double unify180(double angle) {
		double remainder = Math.IEEEremainder(angle, 2 * 180.0);
		if (remainder == -0.0) return 0.0;
		return remainder;
	}
	
}
