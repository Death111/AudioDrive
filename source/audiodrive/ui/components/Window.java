package audiodrive.ui.components;

import java.awt.Dimension;
import java.awt.Toolkit;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.PixelFormat;

import audiodrive.ui.control.Input;

public class Window {
	
	/** Private constructor to prevent instantiation. */
	private Window() {
		throw new IllegalStateException("This class shall not be instantiated.");
	}
	
	private static boolean open;
	private static boolean fullscreen;
	private static boolean borderless;
	private static boolean vSync;
	private static int framerate = 100;
	
	public static void open(Scene scene) {
		if (open) return;
		open = true;
		try {
			Display.create(new PixelFormat(0, 8, 1));
		} catch (LWJGLException exception) {
			throw new RuntimeException(exception);
		}
		scene.enter();
		while (open && !Display.isCloseRequested()) {
			Input.update();
			Scene.update();
			Display.update();
			Display.sync(framerate);
		}
		Scene.destroy();
		Display.destroy();
		close();
	}
	
	public static void close() {
		open = false;
	}
	
	public static Scene getScene() {
		return Scene.getActive();
	}
	
	public static void setScene(Scene scene) {
		scene.enter();
	}
	
	public static void setFramerate(int framerate) {
		Window.framerate = framerate;
	}
	
	public static void setFullscreen(boolean fullscreen) {
		if (Window.fullscreen == fullscreen) return;
		Window.fullscreen = fullscreen;
		try {
			if (borderless) {
				Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
				setSize(screen.width, screen.height);
			} else {
				Display.setFullscreen(fullscreen);
			}
			if (Display.isCreated()) {
				Display.destroy();
				Display.create();
			}
		} catch (LWJGLException exception) {
			throw new RuntimeException(exception);
		}
	}
	
	public static boolean isFullscreen() {
		return fullscreen;
	}
	
	public static void setBorderless(boolean borderless) {
		if (Window.borderless == borderless) return;
		Window.borderless = borderless;
		System.setProperty("org.lwjgl.opengl.Window.undecorated", String.valueOf(borderless));
		try {
			Display.setFullscreen(false);
			if (Display.isCreated()) {
				Display.destroy();
				Display.create();
			}
		} catch (LWJGLException exception) {
			throw new RuntimeException(exception);
		}
	}
	
	public static boolean isBorderless() {
		return borderless;
	}
	
	public static void setSize(int width, int height) {
		if (getWidth() == width && getHeight() == height) return;
		try {
			Display.setDisplayMode(new DisplayMode(width, height));
		} catch (LWJGLException exception) {
			throw new RuntimeException(exception);
		}
	}
	
	public static int getWidth() {
		return Display.getWidth();
	}
	
	public static int getHeight() {
		return Display.getHeight();
	}
	
	public static void toggleVSync() {
		setVSyncEnabled(!vSync);
	}
	
	public static void setVSyncEnabled(boolean vSync) {
		Window.vSync = vSync;
		Display.setVSyncEnabled(vSync);
	}
	
	public static boolean isVSyncEnabled() {
		return vSync;
	}
	
}
