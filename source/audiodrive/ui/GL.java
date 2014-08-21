package audiodrive.ui;

import static org.lwjgl.opengl.GL11.*;

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
	
}
