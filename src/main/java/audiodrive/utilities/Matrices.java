package audiodrive.utilities;

/**
 * Utility class for matrix operations.
 */
public class Matrices {
	
	/** Private constructor to prevent instantiation. */
	private Matrices() {
		throw new IllegalStateException("This class shall not be instantiated.");
	}
	
	/** @return n-by-n identity matrix I */
	public static double[][] identity(int n) {
		double[][] matrix = new double[n][n];
		for (int i = 0; i < n; i++)
			matrix[i][i] = 1;
		return matrix;
	}
	
	/** @return x^T y */
	public static double dot(double[] x, double[] y) {
		if (x.length != y.length) throw new RuntimeException("Illegal vector dimensions.");
		double sum = 0.0;
		for (int i = 0; i < x.length; i++)
			sum += x[i] * y[i];
		return sum;
	}
	
	/** @return C = A^T */
	public static double[][] transpose(double[][] matrix) {
		int M = matrix.length;
		int N = matrix[0].length;
		double[][] transposedMatrix = new double[N][M];
		for (int m = 0; m < M; m++)
			for (int n = 0; n < N; n++)
				transposedMatrix[n][m] = matrix[m][n];
		return transposedMatrix;
	}
	
	/** @return C = A + B */
	public static double[][] add(double[][] matrixA, double[][] matrixB) {
		int M = matrixA.length;
		int N = matrixA[0].length;
		double[][] matrixC = new double[M][N];
		for (int m = 0; m < M; m++)
			for (int n = 0; n < N; n++)
				matrixC[m][n] = matrixA[m][n] + matrixB[m][n];
		return matrixC;
	}
	
	/** @return C = A - B */
	public static double[][] subtract(double[][] matrixA, double[][] matrixB) {
		int M = matrixA.length;
		int N = matrixA[0].length;
		double[][] matrixC = new double[M][N];
		for (int m = 0; m < M; m++)
			for (int n = 0; n < N; n++)
				matrixC[m][n] = matrixA[m][n] - matrixB[m][n];
		return matrixC;
	}
	
	/** @return C = A * B */
	public static double[][] multiply(double[][] matrixA, double[][] matrixB) {
		int AM = matrixA.length;
		int AN = matrixA[0].length;
		int BM = matrixB.length;
		int BN = matrixA[0].length;
		if (AN != BM) throw new RuntimeException("Illegal matrix dimensions.");
		double[][] matrixC = new double[AM][BN];
		for (int am = 0; am < AM; am++)
			for (int bn = 0; bn < BN; bn++)
				for (int an = 0; an < AN; an++)
					matrixC[am][bn] += (matrixA[am][an] * matrixB[an][bn]);
		return matrixC;
	}
	
	/** matrix-vector multiplication (y = A * x) */
	public static double[] multiply(double[][] matrix, double[] vector) {
		int M = matrix.length;
		int N = matrix[0].length;
		if (vector.length != N) throw new RuntimeException("Illegal matrix dimensions.");
		double[] y = new double[M];
		for (int m = 0; m < M; m++)
			for (int n = 0; n < N; n++)
				y[m] += (matrix[m][n] * vector[n]);
		return y;
	}
	
	/** vector-matrix multiplication (y = x^T A) */
	public static double[] multiply(double[] vector, double[][] matrix) {
		int M = matrix.length;
		int N = matrix[0].length;
		if (vector.length != M) throw new RuntimeException("Illegal matrix dimensions.");
		double[] y = new double[N];
		for (int n = 0; n < N; n++)
			for (int m = 0; m < M; m++)
				y[n] += (matrix[m][n] * vector[m]);
		return y;
	}
	
	/** @return matrix multiplied by a factor */
	public static double[][] multiply(double[][] matrix, double value) {
		int M = matrix.length;
		int N = matrix[0].length;
		double[][] miltiplied = new double[M][N];
		for (int m = 0; m < M; m++)
			for (int n = 0; n < N; n++)
				miltiplied[m][n] = matrix[m][n] * value;
		return miltiplied;
	}
	
	/**
	 * rotation around x-axis
	 * 
	 * @param angle rotation angle, in radians
	 * @return the rotation matrix, 3x3
	 */
	public static double[][] rotationAroundXAxis(double angle) {
		double sin = Math.sin(angle);
		double cos = Math.cos(angle);
		double[][] matrix = new double[3][3];
		matrix[0][0] = 1;
		matrix[1][1] = cos;
		matrix[2][1] = -sin;
		matrix[1][2] = sin;
		matrix[2][2] = cos;
		return matrix;
	}
	
	/**
	 * determines the rotation around x-axis of a given matrix
	 * 
	 * @param matrix rotation matrix
	 * @return rotation angle, in radians
	 * @deprecated can't determine unambiguous Euler angles from a rotation matrix
	 */
	@Deprecated
	public static double rotationAroundXAxis(double[][] matrix) {
		double x = Math.atan2(matrix[2][1], matrix[2][2]);
		if (x == -0.0) return 0.0;
		return x;
	}
	
	/**
	 * rotation around y-axis
	 * 
	 * @param angle rotation angle, in radians
	 * @return the rotation matrix, 3x3
	 */
	public static double[][] rotationAroundYAxis(double angle) {
		double sin = Math.sin(angle);
		double cos = Math.cos(angle);
		double[][] matrix = new double[3][3];
		matrix[0][0] = cos;
		matrix[2][0] = sin;
		matrix[1][1] = 1;
		matrix[0][2] = -sin;
		matrix[2][2] = cos;
		return matrix;
	}
	
	/**
	 * determines the rotation around y-axis of a given matrix
	 * 
	 * @param matrix rotation matrix
	 * @return rotation angle, in radians
	 * @deprecated can't determine unambiguous Euler angles from a rotation matrix
	 */
	@Deprecated
	public static double rotationAroundYAxis(double[][] matrix) {
		double m21 = matrix[2][1];
		double m22 = matrix[2][2];
		double y = Math.atan2(-matrix[2][0], Math.sqrt(m21 * m21 + m22 * m22));
		if (y == -0.0) return 0.0;
		return y;
	}
	
	/**
	 * rotation around z-axis
	 * 
	 * @param angle rotation angle, in radians
	 * @return the rotation matrix, 3x3
	 */
	public static double[][] rotationAroundZAxis(double angle) {
		double sin = Math.sin(angle);
		double cos = Math.cos(angle);
		double[][] matrix = new double[3][3];
		matrix[0][0] = cos;
		matrix[1][0] = -sin;
		matrix[0][1] = sin;
		matrix[1][1] = cos;
		matrix[2][2] = 1;
		return matrix;
	}
	
	/**
	 * determines the rotation around z-axis of a given matrix
	 * 
	 * @param matrix rotation matrix
	 * @return rotation angle, in radians
	 * @deprecated can't determine unambiguous Euler angles from a rotation matrix
	 */
	@Deprecated
	public static double rotationAroundZAxis(double[][] matrix) {
		double z = Math.atan2(matrix[1][0], matrix[0][0]);
		if (z == -0.0) return 0.0;
		return z;
	}
	
	/**
	 * rotation around vector
	 * 
	 * @param angle rotation angle, in radians
	 * @param vector rotation vector, with x,y,z
	 * @return the rotation matrix, 3x3
	 */
	public static double[][] rotation(double angle, double[] vector) {
		if (vector.length < 3) throw new RuntimeException("Illegal vector dimensions.");
		double x = vector[0];
		double y = vector[1];
		double z = vector[2];
		double cos = Math.cos(angle);
		double sin = Math.sin(angle);
		double sub = 1 - Math.cos(angle);
		double[][] matrix = new double[3][3];
		matrix[0][0] = sub * x * x + cos;
		matrix[1][0] = sub * x * y + sin * z;
		matrix[2][0] = sub * x * z - sin * y;
		matrix[0][1] = sub * x * y - sin * z;
		matrix[1][1] = sub * y * y + cos;
		matrix[2][1] = sub * y * z + sin * x;
		matrix[0][2] = sub * x * z + sin * y;
		matrix[1][2] = sub * y * z - sin * x;
		matrix[2][2] = sub * z * z + cos;
		return matrix;
	}
	
	/**
	 * rotation around x-, y-, and z-axis
	 * 
	 * @param aroundX rotation angle around x-axis, in radians
	 * @param aroundY rotation angle around y-axis, in radians
	 * @param aroundZ rotation angle around z-axis, in radians
	 * @return the rotation matrix, 3x3
	 */
	public static double[][] rotation(double aroundX, double aroundY, double aroundZ) {
		double[][] matrix = new double[3][3];
		double sinGamma = Math.sin(aroundX);
		double cosGamma = Math.cos(aroundX);
		double sinBeta = Math.sin(aroundY);
		double cosBeta = Math.cos(aroundY);
		double sinAlpha = Math.sin(aroundZ);
		double cosAlpha = Math.cos(aroundZ);
		matrix[0][0] = cosAlpha * cosBeta;
		matrix[1][0] = cosAlpha * sinBeta * sinGamma - sinAlpha * cosGamma;
		matrix[2][0] = cosAlpha * sinBeta * cosGamma + sinAlpha * sinGamma;
		matrix[0][1] = sinAlpha * cosBeta;
		matrix[1][1] = sinAlpha * sinBeta * sinGamma + cosAlpha * cosGamma;
		matrix[2][1] = sinAlpha * sinBeta * cosGamma - cosAlpha * sinGamma;
		matrix[0][2] = -sinBeta;
		matrix[1][2] = cosBeta * sinGamma;
		matrix[2][2] = cosBeta * cosGamma;
		return matrix;
	}
	
	/**
	 * true if matrixA is equal to matrixB, false otherwise
	 */
	public static boolean equals(double[][] matrixA, double[][] matrixB) {
		int M = matrixA.length;
		int N = matrixA[0].length;
		if (matrixB.length != M || matrixB[0].length != N) return false;
		for (int m = 0; m < M; m++)
			for (int n = 0; n < N; n++)
				if (matrixA[m][n] != matrixB[m][n]) return false;
		return true;
	}
	
	public static String string(double[][] matrix) {
		StringBuilder builder = new StringBuilder();
		int M = rows(matrix);
		int N = columns(matrix);
		for (int m = 0; m < M; m++) {
			if (m > 0) builder.append(System.lineSeparator());
			builder.append("[");
			for (int n = 0; n < N; n++) {
				if (n > 0) builder.append(" ");
				builder.append(String.format("% .5f", matrix[m][n]));
			}
			builder.append("]");
		}
		return builder.toString();
	}
	
	/**
	 * @param matrix [rows][columns]
	 * @return the matrix's number of rows
	 */
	public static int rows(double[][] matrix) {
		if (matrix.length == 0) throw new RuntimeException("an array of size 0 is no matrix");
		return matrix.length;
	}
	
	/**
	 * @param matrix [rows][columns]
	 * @return the matrix's number of columns
	 */
	public static int columns(double[][] matrix) {
		if (matrix.length == 0) throw new RuntimeException("an array of size 0 is no matrix");
		return matrix[0].length;
	}
	
	/**
	 * @return sub-matrix by excluding a specific row and column
	 */
	public static double[][] submatrix(double[][] matrix, int excludingRow, int excludingColumn) {
		int M = rows(matrix);
		int N = columns(matrix);
		double[][] submatrix = new double[M - 1][N - 1];
		int r = 0;
		for (int i = 0; i < M; i++) {
			if (i == excludingRow) continue;
			int c = 0;
			for (int j = 0; j < N; j++) {
				if (j == excludingColumn) continue;
				submatrix[r][c] = matrix[i][j];
				c++;
			}
			r++;
		}
		return submatrix;
	}
	
	/**
	 * returns cofactor of a matrix A is matrix C, so that the value of element Cij equals the determinant of a matrix created by removing row i and column j from matrix A
	 */
	public static double[][] cofactor(double[][] matrix) {
		double[][] cofactor = new double[rows(matrix)][columns(matrix)];
		int M = rows(matrix);
		int N = columns(matrix);
		for (int i = 0; i < M; i++) {
			for (int j = 0; j < N; j++) {
				cofactor[i][j] = determinantSign(i) * determinantSign(j) * determinant(submatrix(matrix, i, j));
			}
		}
		return cofactor;
	}
	
	/**
	 * @return 1 if index is even and -1 otherwise
	 */
	public static int determinantSign(int index) {
		return index % 2 == 0 ? 1 : -1;
	}
	
	/**
	 * @param matrix a square matrix with non-zero determinant
	 * @return inverted matrix
	 */
	public static double[][] invert(double[][] matrix) {
		if (rows(matrix) != columns(matrix)) throw new RuntimeException("Can't invert a non-square matrix.");
		double determinant = determinant(matrix);
		if (determinant == 0) throw new RuntimeException("Can't invert a matrix with determinant equal to zero.");
		return multiply(transpose(cofactor(matrix)), 1.0 / determinant);
	}
	
	/**
	 * calculates the determinant of a given matrix
	 * 
	 * @param matrix the matrix
	 * @return the matrix's determinant
	 * @author Dr. Nicholas Duchon
	 */
	public static double determinant(double[][] matrix) {
		if (matrix.length == 0 || matrix.length != matrix[0].length) throw new RuntimeException("not a square matrix");
		int n = matrix.length - 1;
		if (n < 0) return 0;
		double M[][][] = new double[n + 1][][];
		
		M[n] = matrix; // initialize first, largest, M to a
		
		// create working arrays
		for (int i = 0; i < n; i++)
			M[i] = new double[i + 1][i + 1];
		
		return determinant(M, n);
	}
	
	private static double determinant(double[][][] M, int m) {
		if (m == 0) return M[0][0][0];
		int e = 1;
		
		// initialize sub-array to upper left MxM sub-matrix
		for (int i = 0; i < m; i++)
			for (int j = 0; j < m; j++)
				M[m - 1][i][j] = M[m][i][j];
		double sum = M[m][m][m] * determinant(M, m - 1);
		
		// walk through rest of rows of M
		for (int i = m - 1; i >= 0; i--) {
			for (int j = 0; j < m; j++)
				M[m - 1][i][j] = M[m][i + 1][j];
			e = -e;
			sum += e * M[m][i][m] * determinant(M, m - 1);
		} // end for each row of matrix
		
		return sum;
	}
	
}
