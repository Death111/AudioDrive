package audiodrive.model.geometry.transform;

import static org.lwjgl.opengl.GL11.glMultMatrix;
import audiodrive.model.geometry.Matrix;

public class Transformatrix extends Transformation {
	
	private Matrix matrix = new Matrix().identity();
	
	public Transformatrix matrix(Matrix matrix) {
		this.matrix = matrix;
		return this;
	}
	
	public Matrix matrix() {
		return matrix;
	}
	
	@Override
	public boolean ignorable() {
		return matrix.equals(Matrix.Identity);
	}
	
	@Override
	public void apply() {
		if (ignorable()) return;
		glMultMatrix(matrix.toDoubleBuffer());
	}
	
}
