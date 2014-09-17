package audiodrive.ui;

import static org.lwjgl.opengl.GL11.*;

import java.nio.FloatBuffer;

import audiodrive.model.geometry.Vector;
import audiodrive.utilities.Buffers;

public class GL {
	
	/** Private constructor to prevent instantiation. */
	private GL() {
		throw new IllegalStateException("This class shall not be instantiated.");
	}
	
	public static void pushMatrix() {
		glPushMatrix();
	}
	
	public static void popMatrix() {
		glPopMatrix();
	}
	
	public static void pushAttributes() {
		glPushAttrib(GL_ALL_ATTRIB_BITS);
	}
	
	public static void popAttributes() {
		glPopAttrib();
	}
	
	public static float getDepth(int x, int y) {
		FloatBuffer buffer = Buffers.createFloatBuffer(1);
		glReadPixels(x, y, 1, 1, GL_DEPTH_COMPONENT, GL_FLOAT, buffer);
		return buffer.get(0);
	}
	
	public static void rotate(double angle, Vector axis) {
		glRotated(angle, axis.x(), axis.y(), axis.z());
	}
	
	public static void translate(Vector vector) {
		glTranslated(vector.x(), vector.y(), vector.z());
	}
	
}
