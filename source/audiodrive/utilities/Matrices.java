package audiodrive.utilities;

public class Matrices {
	
	/** Private constructor to prevent instantiation. */
	private Matrices() {
		throw new IllegalStateException("This class shall not be instantiated.");
	}
	
	/** return n-by-n identity matrix I */
	public static double[][] identity(int n) {
		double[][] matrix = new double[n][n];
		for (int i = 0; i < n; i++)
			matrix[i][i] = 1;
		return matrix;
	}
	
	/** return x^T y */
	public static double dot(double[] x, double[] y) {
		if (x.length != y.length) throw new RuntimeException("Illegal vector dimensions.");
		double sum = 0.0;
		for (int i = 0; i < x.length; i++)
			sum += x[i] * y[i];
		return sum;
	}
	
	/** return C = A^T */
	public static double[][] transpose(double[][] matrix) {
		int M = matrix.length;
		int N = matrix[0].length;
		double[][] transposedMatrix = new double[N][M];
		for (int m = 0; m < M; m++)
			for (int n = 0; n < N; n++)
				transposedMatrix[n][m] = matrix[m][n];
		return transposedMatrix;
	}
	
	/** return C = A + B */
	public static double[][] add(double[][] matrixA, double[][] matrixB) {
		int M = matrixA.length;
		int N = matrixA[0].length;
		double[][] matrixC = new double[M][N];
		for (int m = 0; m < M; m++)
			for (int n = 0; n < N; n++)
				matrixC[m][n] = matrixA[m][n] + matrixB[m][n];
		return matrixC;
	}
	
	/** return C = A - B */
	public static double[][] subtract(double[][] matrixA, double[][] matrixB) {
		int M = matrixA.length;
		int N = matrixA[0].length;
		double[][] matrixC = new double[M][N];
		for (int m = 0; m < M; m++)
			for (int n = 0; n < N; n++)
				matrixC[m][n] = matrixA[m][n] - matrixB[m][n];
		return matrixC;
	}
	
	/** return C = A * B */
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
	
}
