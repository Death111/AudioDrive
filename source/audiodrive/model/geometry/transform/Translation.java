package audiodrive.model.geometry.transform;

import static org.lwjgl.opengl.GL11.glTranslated;
import audiodrive.model.geometry.Vector;

public class Translation extends Transformation {
	
	private Vector vector = new Vector();
	
	public Translation x(double x) {
		vector.x(x);
		return this;
	}
	
	public Translation y(double y) {
		vector.y(y);
		return this;
	}
	
	public Translation z(double z) {
		vector.z(z);
		return this;
	}
	
	public double x() {
		return vector.x();
	}
	
	public double y() {
		return vector.y();
	}
	
	public double z() {
		return vector.z();
	}
	
	public Translation set(Translation translation) {
		vector.set(translation.vector);
		return this;
	}
	
	public Translation set(Vector vector) {
		this.vector.set(vector);
		return this;
	}
	
	public Vector vector() {
		return vector;
	}
	
	public Translation reset() {
		vector.set(Vector.Null);
		return this;
	}
	
	public Translation invert() {
		vector.negate();
		return this;
	}
	
	public Translation inverted() {
		return new Translation().set(vector.negated());
	}
	
	@Override
	public boolean ignorable() {
		return vector.isNull();
	}
	
	@Override
	public void apply() {
		if (ignorable()) return;
		glTranslated(vector.x(), vector.y(), vector.z());
	}
	
	@Override
	public String toString() {
		return "Translation [vector=" + vector + "]";
	}
	
}
