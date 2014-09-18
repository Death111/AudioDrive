package audiodrive.ui.components;

import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.PixelFormat;

import audiodrive.AudioDrive;
import audiodrive.ui.control.Input;

public class Window {
	
	/** Private constructor to prevent instantiation. */
	private Window() {
		throw new IllegalStateException("This class shall not be instantiated.");
	}
	
	private static PixelFormat pixelFormat = new PixelFormat(0, 16, 1, AudioDrive.Settings.getInteger("window.antiAliasing"));
	private static boolean open;
	private static boolean fullscreen;
	private static boolean borderless;
	private static boolean vSync;
	private static int framerate = 0;
	
	static {
		System.setProperty("org.lwjgl.input.Mouse.allowNegativeMouseCoords", "true");
	}
	
	public static void open(Scene scene) {
		if (open) return;
		open = true;
		try {
			Display.create(pixelFormat);
		} catch (LWJGLException exception) {
			throw new RuntimeException(exception);
		}
		scene.enter();
		while (open && !Display.isCloseRequested()) {
			if (Display.isVisible()) {
				Input.update();
				Scene.update();
				Display.update();
				Display.sync(framerate);
			}
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
	
	public static void setTitle(String title) {
		Display.setTitle(title);
	}
	
	public static void setFramerate(int framerate) {
		Window.framerate = framerate;
	}
	
	public static void setResizable(boolean resizable) {
		Display.setResizable(resizable);
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
		} catch (LWJGLException exception) {
			throw new RuntimeException(exception);
		}
	}
	
	public static boolean isBorderless() {
		return borderless;
	}
	
	public static void setSize(int width, int height) {
		if (Display.isCreated() && (getWidth() == width && getHeight() == height)) return;
		try {
			Display.setDisplayMode(new DisplayMode(width, height));
		} catch (LWJGLException exception) {
			throw new RuntimeException(exception);
		}
	}
	
	public static void setLocation(int x, int y) {
		Display.setLocation(x, y);
	}
	
	public static void setBounds(Rectangle bounds) {
		setLocation(bounds.x, bounds.y);
		setSize(bounds.width, bounds.height);
	}
	
	public static Rectangle getBounds() {
		if (Display.isCreated()) return new Rectangle(Display.getX(), Display.getY(), Display.getWidth(), Display.getHeight());
		return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getBounds();
	}
	
	public static int getWidth() {
		return Display.getWidth();
	}
	
	public static int getHeight() {
		return Display.getHeight();
	}
	
	public static PixelFormat getPixelFormat() {
		return pixelFormat;
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
	
	public static void useSecondaryMonitor() {
		GraphicsDevice[] monitors = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
		if (monitors.length < 2) return;
		GraphicsDevice currentMonitor = getMonitor();
		for (GraphicsDevice monitor : monitors) {
			if (!monitor.equals(currentMonitor)) setMonitor(monitor);
		}
	}
	
	public static void setMonitor(GraphicsDevice device) {
		setBounds(device.getDefaultConfiguration().getBounds());
	}
	
	public static GraphicsDevice getMonitor() {
		if (Display.isCreated()) {
			Rectangle bounds = getBounds();
			for (GraphicsDevice device : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
				if (device.getDefaultConfiguration().getBounds().equals(bounds)) return device;
			}
		}
		return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
	}
	
	public static GraphicsDevice getMonitor(int x, int y) {
		Point point = new Point(x, y);
		for (GraphicsDevice device : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
			if (device.getDefaultConfiguration().getBounds().contains(point)) return device;
		}
		return null;
	}
}
