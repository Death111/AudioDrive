package audiodrive.model.geometry;

import java.nio.DoubleBuffer;
import java.util.Arrays;
import java.util.stream.Stream;

import org.lwjgl.BufferUtils;

import audiodrive.utilities.Matrices;

public class Matrix {
	
	public static final int Dimension = 4;
	
	public static final Matrix Null = new Matrix();
	public static final Matrix Identity = new Matrix().identity();
	
	public final double[][] M = new double[Dimension][Dimension];
	
	public Matrix() {}
	
	public double get(int m, int n) {
		return M[m][n];
	}
	
	public Matrix set(double[][] matrix) {
		assertModifiable();
		if (matrix.length < Dimension || matrix[0].length < Dimension) {
			throw new IllegalArgumentException("Matrix dimensions have to be >= " + Dimension + "x" + Dimension);
		}
		for (int x = 0; x < Dimension; x++) {
			for (int y = 0; y < Dimension; y++) {
				M[x][y] = matrix[x][y];
			}
		}
		return this;
	}
	
	public Matrix insert(double[][] matrix) {
		assertModifiable();
		if (matrix.length > Dimension || matrix[0].length > Dimension) {
			throw new IllegalArgumentException("Matrix dimensions have to be <= " + Dimension + "x" + Dimension);
		}
		for (int x = 0; x < matrix.length; x++) {
			for (int y = 0; y < matrix[0].length; y++) {
				M[x][y] = matrix[x][y];
			}
		}
		return this;
	}
	
	public Matrix insertInColumn0(Vector vector) {
		M[0][0] = vector.x();
		M[0][1] = vector.y();
		M[0][2] = vector.z();
		return this;
	}
	
	public Matrix insertInColumn1(Vector vector) {
		M[1][0] = vector.x();
		M[1][1] = vector.y();
		M[1][2] = vector.z();
		return this;
	}
	
	public Matrix insertInColumn2(Vector vector) {
		M[2][0] = vector.x();
		M[2][1] = vector.y();
		M[2][2] = vector.z();
		return this;
	}
	
	public Matrix insertInColumn3(Vector vector) {
		M[3][0] = vector.x();
		M[3][1] = vector.y();
		M[3][2] = vector.z();
		return this;
	}
	
	public Matrix set(Matrix matrix) {
		set(matrix.M);
		return this;
	}
	
	/**
	 * Sets all matrix entries to zero.
	 */
	public Matrix empty() {
		for (int i = 0; i < Dimension; i++) {
			M[i][i] = 0.0;
		}
		return this;
	}
	
	/**
	 * Sets the matrix to identity.
	 */
	public Matrix identity() {
		for (int i = 0; i < Dimension; i++) {
			M[i][i] = 1.0;
		}
		return this;
	}
	
	/**
	 * Sets the matrix to a rotation matrix with the specified rotation angles.
	 * 
	 * @param aroundX rotation angle around x-axis, in degrees
	 * @param aroundY rotation angle around y-axis, in degrees
	 * @param aroundZ rotation angle around z-axis, in degrees
	 */
	public Matrix rotation(double aroundX, double aroundY, double aroundZ) {
		return identity().insert(Matrices.rotation(Math.toRadians(aroundX), Math.toRadians(aroundY), Math.toRadians(aroundZ)));
	}
	
	/**
	 * Sets the matrix to a rotation matrix with the specified rotation around the given angle.
	 * 
	 * @param angle rotation angle, in degrees
	 * @param vector rotation vector, with x,y,z
	 */
	public Matrix rotation(double angle, Vector vector) {
		return identity().insert(Matrices.rotation(Math.toRadians(angle), vector.toArray()));
	}
	
	public Vector multiplied(Vector vector) {
		return new Vector().set(Matrices.multiply(M, vector.toHomogeneous()));
	}
	
	public Matrix multiply(Matrix matrix) {
		return set(Matrices.multiply(M, matrix.M));
	}
	
	public Matrix multiplied(Matrix matrix) {
		return new Matrix().set(Matrices.multiply(M, matrix.M));
	}
	
	public Matrix rotate(double angle, Vector vector) {
		return multiply(new Matrix().rotation(angle, vector));
	}
	
	public Matrix rotated(double angle, Vector vector) {
		return multiplied(new Matrix().rotation(angle, vector));
	}
	
	public double getRotationAroundXAxis() {
		return Math.toDegrees(Matrices.rotationAroundXAxis(M));
	}
	
	public double getRotationAroundYAxis() {
		return Math.toDegrees(Matrices.rotationAroundYAxis(M));
	}
	
	public double getRotationAroundZAxis() {
		return Math.toDegrees(Matrices.rotationAroundZAxis(M));
	}
	
	public Matrix align(Vector direction, Vector normal) {
		identity();
		Vector z = direction.normalized();
		Vector y = normal;
		Vector x = z.cross(y).normalize();
		y = z.cross(x).normalize();
		insertInColumn0(x);
		insertInColumn1(y);
		insertInColumn2(z);
		return this;
	}
	
	public Matrix translate(Vector translation) {
		M[3][0] += translation.x();
		M[3][1] += translation.y();
		M[3][2] += translation.z();
		return this;
	}
	
	@Override
	public int hashCode() {
		return 31 + Arrays.hashCode(M);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		return Matrices.equals(M, ((Matrix) obj).M);
	}
	
	@Override
	public String toString() {
		return "Matrix:" + System.lineSeparator() + Matrices.string(M);
	}
	
	public double[][] toArray() {
		double[][] matrix = new double[Dimension][Dimension];
		for (int x = 0; x < Dimension; x++) {
			for (int y = 0; y < Dimension; y++) {
				matrix[x][y] = M[x][y];
			}
		}
		return matrix;
	}
	
	public DoubleBuffer toDoubleBuffer() {
		DoubleBuffer buffer = BufferUtils.createDoubleBuffer(Dimension * Dimension);
		for (int x = 0; x < Dimension; x++) {
			for (int y = 0; y < Dimension; y++) {
				buffer.put(M[x][y]);
			}
		}
		buffer.rewind();
		return buffer;
	}
	
	private void assertModifiable() {
		if (Stream.of(Null, Identity).anyMatch(matrix -> matrix == this)) throw new UnsupportedOperationException("Can't modify a constant matrix.");
	}
	
}
