package audiodrive.model.geometry;

import static org.lwjgl.opengl.GL11.*;

public class Placement {
	
	public static final Placement Default = new Placement();
	
	private final Vector position = new Vector();
	private final Vector direction = new Vector().set(Vector.Z);
	private final Vector normal = new Vector().set(Vector.Y);
	
	public Placement set(Placement placement) {
		position.set(placement.position());
		direction.set(placement.direction());
		normal.set(placement.normal());
		return this;
	}
	
	public Placement position(Vector position) {
		assertModifiable();
		this.position.set(position);
		return this;
	}
	
	public Vector position() {
		return position;
	}
	
	public Placement direction(Vector direction) {
		assertModifiable();
		this.direction.set(direction);
		update();
		return this;
	}
	
	public Vector direction() {
		return direction;
	}
	
	public Placement normal(Vector normal) {
		assertModifiable();
		this.normal.set(normal);
		update();
		return this;
	}
	
	public Vector normal() {
		return normal;
	}
	
	private void update() {
		direction.normalize();
		normal.normalize();
		Vector side = direction.cross(normal).normalize();
		normal.set(side.cross(direction).normalize());
	}
	
	public Placement reset() {
		direction.set(Vector.Z);
		normal.set(Vector.Y);
		return this;
	}
	
	public Matrix toMatrix() {
		return new Matrix().align(direction, normal).translate(position);
	}
	
	public void apply() {
		if (hasDefaultOrientation()) glTranslated(position.x(), position.y(), position.z());
		else glMultMatrix(toMatrix().toDoubleBuffer());
	}
	
	public boolean hasDefaultOrientation() {
		return direction.equals(Vector.Z) && normal.equals(Vector.Y);
	}
	
	public boolean isDefault() {
		return equals(Default);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((direction == null) ? 0 : direction.hashCode());
		result = prime * result + ((normal == null) ? 0 : normal.hashCode());
		result = prime * result + ((position == null) ? 0 : position.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Placement other = (Placement) obj;
		if (direction == null) {
			if (other.direction != null) return false;
		} else if (!direction.equals(other.direction)) return false;
		if (normal == null) {
			if (other.normal != null) return false;
		} else if (!normal.equals(other.normal)) return false;
		if (position == null) {
			if (other.position != null) return false;
		} else if (!position.equals(other.position)) return false;
		return true;
	}
	
	private void assertModifiable() {
		if (this == Default) throw new UnsupportedOperationException("Can't modify the constant placement 'Default'.");
	}
}
