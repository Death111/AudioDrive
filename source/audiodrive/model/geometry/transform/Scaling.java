package audiodrive.model.geometry.transform;

import static org.lwjgl.opengl.GL11.glScaled;

public class Scaling extends Transformation {
	
	private double x = 1, y = 1, z = 1, scale = 1;
	
	public Scaling x(double x) {
		this.x = x;
		return this;
	}
	
	public Scaling y(double y) {
		this.y = y;
		return this;
	}
	
	public Scaling z(double z) {
		this.z = z;
		return this;
	}
	
	public Scaling scale(double scale) {
		this.scale = scale;
		return this;
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
	
	public double scale() {
		return scale;
	}
	
	@Override
	public boolean ignorable() {
		return x * scale == 1 && y * scale == 1 && z * scale == 1;
	}
	
	@Override
	public void apply() {
		if (ignorable()) return;
		glScaled(x * scale, y * scale, z * scale);
	}
	
}
