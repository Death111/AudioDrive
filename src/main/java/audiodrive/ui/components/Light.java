package audiodrive.ui.components;

import static org.lwjgl.opengl.GL11.*;
import audiodrive.model.geometry.Color;
import audiodrive.model.geometry.Vector;
import audiodrive.utilities.Buffers;

public class Light {
	
	public static final Light Zero = new Light(GL_LIGHT0);
	public static final Light One = new Light(GL_LIGHT1);
	public static final Light Two = new Light(GL_LIGHT2);
	
	private static boolean enabled;
	
	private int id;
	private Vector position;
	private Vector direction;
	private Color ambient;
	private Color diffuse;
	private Color specular;
	private boolean directional;
	private boolean on;
	
	private Light(int id) {
		this.id = id;
	}
	
	public Light position(Vector position) {
		this.position = position;
		direction = null;
		directional = false;
		glLight(id, GL_POSITION, Buffers.create((float) position.x(), (float) position.y(), (float) position.z(), 1));
		return this;
	}
	
	public Vector position() {
		return position;
	}
	
	public Light direction(Vector direction) {
		direction = direction.negated();
		position = Vector.Null;
		this.direction = direction;
		directional = true;
		glLight(id, GL_POSITION, Buffers.create((float) direction.x(), (float) direction.y(), (float) direction.z(), 0));
		return this;
	}
	
	public Vector direction() {
		return direction;
	}
	
	public Light ambient(Color ambient) {
		this.ambient = ambient;
		glLight(id, GL_AMBIENT, Buffers.create(ambient.toFloats()));
		return this;
	}
	
	public Color ambient() {
		return ambient;
	}
	
	public Light diffuse(Color diffuse) {
		this.diffuse = diffuse;
		glLight(id, GL_DIFFUSE, Buffers.create(diffuse.toFloats()));
		return this;
	}
	
	public Color diffuse() {
		return diffuse;
	}
	
	public Light specular(Color specular) {
		this.specular = specular;
		glLight(id, GL_SPECULAR, Buffers.create(specular.toFloats()));
		return this;
	}
	
	public Color specular() {
		return specular;
	}
	
	public boolean directional() {
		return directional;
	}
	
	public Light on() {
		on = true;
		glEnable(id);
		return this;
	}
	
	public Light off() {
		on = true;
		glDisable(id);
		return this;
	}
	
	public boolean isOn() {
		return on;
	}
	
	/**
	 * Enables lighting.
	 */
	public static void enable() {
		glEnable(GL_LIGHTING);
	}
	
	/**
	 * Disables lighting.
	 */
	public static void disable() {
		glDisable(GL_LIGHTING);
	}
	
	/**
	 * Indicates, whether lighting is enabled.
	 */
	public static boolean isEnabled() {
		return enabled;
	}
	
}
