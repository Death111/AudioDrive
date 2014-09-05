package audiodrive.model.geometry.transform;

import static org.lwjgl.opengl.GL11.glMultMatrix;
import audiodrive.model.geometry.Matrix;
import audiodrive.model.geometry.Vector;

public class Rotation extends Transformation {
	
	/**
	 * No rotation. I. e. the rotation matrix is equal to identity.
	 */
	public static final Rotation Null = new Rotation();
	
	private final Matrix matrix = new Matrix().identity();
	
	public Rotation x(double angle) {
		xAdd(angle - x());
		return this;
	}
	
	public Rotation y(double angle) {
		yAdd(angle - y());
		return this;
	}
	
	public Rotation z(double angle) {
		zAdd(angle - z());
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
		return matrix.getRotationAroundXAxis();
	}
	
	public double y() {
		return matrix.getRotationAroundYAxis();
	}
	
	public double z() {
		return matrix.getRotationAroundZAxis();
	}
	
	public Rotation add(double angle, Vector axis) {
		assertModifiable();
		matrix.rotate(unify180(angle), axis);
		return this;
	}
	
	public Rotation set(Rotation rotation) {
		assertModifiable();
		matrix.set(rotation.matrix);
		return this;
	}
	
	public Rotation set(Matrix matrix) {
		assertModifiable();
		this.matrix.set(matrix);
		return this;
	}
	
	public Rotation set(double aroundX, double aroundY, double aroundZ) {
		assertModifiable();
		matrix.rotation(aroundX, aroundY, aroundZ);
		return this;
	}
	
	public Rotation align(Vector direction, Vector normal) {
		matrix.alignment(direction, normal);
		return this;
	}
	
	/**
	 * Resets the rotation to {@linkplain #Null}. I. e. sets the rotation matrix to identity.
	 */
	public Rotation reset() {
		assertModifiable();
		matrix.identity();
		return this;
	}
	
	public Rotation invert() {
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
		glMultMatrix(matrix.toDoubleBuffer());
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
