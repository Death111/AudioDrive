package audiodrive.model.geometry.transform;

import static org.lwjgl.opengl.GL11.glMultMatrix;
import static org.lwjgl.opengl.GL11.glTranslated;
import audiodrive.model.geometry.Matrix;
import audiodrive.model.geometry.Vector;

public class Placement extends Transformation implements Cloneable {
	
	public static final Placement Default = new Placement().unmodifiable();
	
	private final Vector position = new Vector();
	private final Vector direction = new Vector().set(Vector.Z);
	private final Vector up = new Vector().set(Vector.Y);
	private boolean modifiable = true;
	
	public Placement set(Placement placement) {
		position.set(placement.position());
		direction.set(placement.direction());
		up.set(placement.up());
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
	
	public Placement up(Vector up) {
		assertModifiable();
		this.up.set(up);
		update();
		return this;
	}
	
	public Vector up() {
		return up;
	}
	
	/** calculates the side vector i.e. the cross product of {@linkplain #direction()} and {@linkplain #up()} */
	public Vector side() {
		return direction.cross(up).normalize();
	}
	
	private void update() {
		direction.normalize();
		up.normalize();
		Vector side = direction.cross(up).normalize();
		up.set(side.cross(direction).normalize());
	}
	
	public Placement reset() {
		direction.set(Vector.Z);
		up.set(Vector.Y);
		return this;
	}
	
	public Matrix toMatrix() {
		return new Matrix().alignment(direction, up).translate(position);
	}
	
	@Override
	public boolean ignorable() {
		return isDefault();
	}
	
	@Override
	public void apply() {
		if (ignorable()) return;
		if (hasDefaultOrientation()) glTranslated(position.x(), position.y(), position.z());
		else glMultMatrix(toMatrix().toDoubleBuffer());
	}
	
	public boolean hasDefaultOrientation() {
		return direction.equals(Vector.Z) && up.equals(Vector.Y);
	}
	
	public boolean isDefault() {
		return equals(Default);
	}
	
	@Override
	public Placement clone() {
		return new Placement().position(position).direction(direction).up(up);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((direction == null) ? 0 : direction.hashCode());
		result = prime * result + ((up == null) ? 0 : up.hashCode());
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
		if (up == null) {
			if (other.up != null) return false;
		} else if (!up.equals(other.up)) return false;
		if (position == null) {
			if (other.position != null) return false;
		} else if (!position.equals(other.position)) return false;
		return true;
	}
	
	private Placement modifiable() {
		modifiable = true;
		return this;
	}
	
	private Placement unmodifiable() {
		modifiable = false;
		return this;
	}
	
	private void assertModifiable() {
		if (!modifiable) throw new UnsupportedOperationException("Can't modify a constant matrix.");
	}
	
	@Override
	public String toString() {
		return "Placement [position=" + position + ", direction=" + direction + ", up=" + up + "]";
	}
	
}
