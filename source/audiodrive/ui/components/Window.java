package audiodrive.ui.components;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

public class Window {
	
	/** Private constructor to prevent instantiation. */
	private Window() {
		throw new IllegalStateException("This class shall not be instantiated.");
	}
	
	public static void setBorderless(boolean borderless) throws LWJGLException {
		System.setProperty("org.lwjgl.opengl.Window.undecorated", String.valueOf(borderless));
		if (Display.isCreated()) {
			DisplayMode mode = Display.getDisplayMode();
			Display.destroy();
			Display.setDisplayMode(mode);
			Display.create();
		}
	}
	
	public static void setSize(int width, int height) {
		try {
			Display.setDisplayMode(new DisplayMode(width, height));
		} catch (LWJGLException exception) {
			throw new RuntimeException(exception);
		}
	}
	
}
