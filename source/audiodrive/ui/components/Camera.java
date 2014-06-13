package audiodrive.ui.components;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.*;
import audiodrive.model.geometry.Vector;

public class Camera {
	
	private static Vector eye = new Vector();
	private static Vector at = new Vector();
	private static Vector up = new Vector();

	static {
		reset();
	}

	/** Private constructor to prevent instantiation. */
	private Camera() {
		throw new IllegalStateException("This class shall not be instantiated.");
	}
	
	public static void reset() {
		orthograpic(-1, -1, 2, 2, 0, 2);
		eye.set(0, 0, 1);
		at.set(0, 0, 0);
		up.set(0, 1, 0);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glEnable(GL_BLEND);
		glEnable(GL_CULL_FACE);
	}
	
	public static void perspective(double fov, double aspect, double near, double far) {
		projection(() -> {
			gluPerspective((float) fov, (float) aspect, (float) near, (float) far);
		});
		glEnable(GL_DEPTH_TEST);
	}

	public static void perspective(double fov, double width, double height, double near, double far) {
		perspective(fov, width / height, near, far);
	}
	
	public static void perspective(double x, double y, double width, double height, double near, double far) {
		projection(() -> {
			glFrustum(x, x + width, y + height, y, near, far);
		});
		glEnable(GL_DEPTH_TEST);
	}

	public static void orthograpic(double x, double y, double width, double height, double near, double far) {
		projection(() -> {
			glOrtho(x, x + width, y + height, y, near, far);
		});
		glDisable(GL_DEPTH_TEST);
	}
	
	public static void overlay(int width, int height) {
		projection(() -> {
			glOrtho(0, width, height, 0, 0, -1);
		});
		glDisable(GL_DEPTH_TEST);
	}

	public static void lookAt(Vector position, Vector up) {
		Camera.at.set(position);
		Camera.up.set(up);
		update();
	}
	
	public static void lookAt(Vector position) {
		Camera.at.set(position);
		update();
	}
	
	public static void lookAt(double x, double y, double z) {
		lookAt(new Vector(x, y, z));
	}
	
	public static void position(Vector position) {
		Camera.eye.set(position);
		update();
	}

	public static void position(double x, double y, double z) {
		position(new Vector(x, y, z));
	}
	
	private static void update() {
		glLoadIdentity();
		gluLookAt((float) eye.x(), (float) eye.y(), (float) eye.z(), (float) at.x(), (float) at.y(), (float) at.z(), (float) up.x(), (float) up.y(), (float) up.z());
	}

	public static void projection(Runnable runnable) {
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		runnable.run();
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
	}
	
}
