package audiodrive.model.geometry;

import java.util.stream.Stream;

import audiodrive.utilities.Matrices;

public class Matrix {
	
	public static final int Dimension = 4;
	
	public static final Matrix Null = new Matrix();
	public static final Matrix Identity = new Matrix().setIdentity();
	
	private final double[][] M = new double[Dimension][Dimension];
	
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
	
	public Matrix set(Matrix matrix) {
		set(matrix.M);
		return this;
	}
	
	public Matrix setIdentity() {
		for (int i = 0; i < Dimension; i++) {
			M[i][i] = 1.0;
		}
		return this;
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
		return multiply(rotation(angle, vector));
	}
	
	public Matrix rotated(double angle, Vector vector) {
		return multiplied(rotation(angle, vector));
	}
	
	public static Matrix rotation(double angle, Vector vector) {
		return new Matrix().setIdentity().insert(Matrices.rotation(Math.toRadians(angle), vector.toArray()));
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (int m = 0; m < Dimension; m++) {
			builder.append("[");
			for (int n = 0; n < Dimension; n++) {
				if (n > 0) builder.append(", ");
				builder.append(M[m][n]);
			}
			builder.append("]").append(System.lineSeparator());
		}
		return builder.toString();
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
	
	private void assertModifiable() {
		if (Stream.of(Null, Identity).anyMatch(matrix -> matrix == this)) throw new UnsupportedOperationException("Can't modify a constant vector.");
	}
	
}
