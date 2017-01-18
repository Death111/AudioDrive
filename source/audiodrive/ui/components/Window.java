package audiodrive.ui.components;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_MULTISAMPLE;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Optional;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.ContextCapabilities;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.opengl.PixelFormat;

import audiodrive.AudioDrive;
import audiodrive.Resources;
import audiodrive.ui.control.Input;
import audiodrive.utilities.Arithmetic;
import audiodrive.utilities.Get;
import audiodrive.utilities.Log;

public class Window {
	
	/** Private constructor to prevent instantiation. */
	private Window() {
		throw new IllegalStateException("This class shall not be instantiated.");
	}
	
	private static GraphicsDevice monitor = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
	private static PixelFormat pixelFormat = new PixelFormat(8, 16, 1);
	private static Rectangle bounds;
	private static boolean recreate;
	private static boolean recreating;
	private static boolean open;
	private static boolean fullscreen;
	private static boolean borderless;
	private static boolean antialiasing;
	private static boolean vSync;
	private static int framerate = 0;
	
	static {
		System.setProperty("org.lwjgl.input.Mouse.allowNegativeMouseCoords", "true");
	}
	
	public static void open(Scene scene) {
		if (open) return;
		open = true;
		create();
		scene.enter();
		while (open && !Display.isCloseRequested()) {
			Window.update();
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
	
	private static void update() {
		if (recreate) create();
		else {
			bounds.x = Display.getX();
			bounds.y = Display.getY();
		}
	}
	
	private static void create() {
		recreate = false;
		recreating = true;
		Optional<Scene> scene = Get.optional(Scene.getActive());
		Log.info("Creating window...");
		checkCapabilities();
		scene.ifPresent(Scene::exiting);
		Display.destroy();
		Resources.destroy();
		Text.destroy();
		System.gc();
		Rectangle bounds = monitor.getDefaultConfiguration().getBounds();
		if (fullscreen) {
			// use monitor bounds
		} else if (Window.bounds != null && bounds.contains(Window.bounds)) {
			bounds = Window.bounds;
		} else {
			int inset = bounds.height / 10;
			bounds = new Rectangle(bounds.x + inset, bounds.y + inset, bounds.width - 2 * inset, bounds.height - 2 * inset);
		}
		System.setProperty("org.lwjgl.opengl.Window.undecorated", String.valueOf(borderless));
		Log.debug(
			"monitor: %s\nsize: %s x %s\nposition: %s,%s\nfullscreen: %s\nborderless: %s\nantialiasing: %s\nvSync: %s",
			monitor.getIDstring().substring(1),
			bounds.width,
			bounds.height,
			bounds.x,
			bounds.y,
			fullscreen,
			borderless,
			antialiasing,
			vSync);
		try {
			Display.setDisplayMode(new DisplayMode(bounds.width, bounds.height));
			Display.setLocation(bounds.x, bounds.y);
			Display.create(pixelFormat);
			Window.bounds = bounds;
		} catch (LWJGLException exception) {
			throw new RuntimeException(exception);
		}
		Camera.reset();
		setAntialiasingEnabled(antialiasing);
		setVSyncEnabled(vSync);
		scene.ifPresent(Scene::entering);
		recreating = false;
	}
	
	private static void checkCapabilities() {
		try {
			if (!Display.isCreated()) Display.create();
		} catch (LWJGLException exception) {
			throw new RuntimeException(exception);
		}
		ContextCapabilities capabilities = GLContext.getCapabilities();
		int samples = capabilities.GL_ARB_multisample ? Arithmetic.clamp(Arithmetic.nextPowerOfTwo(AudioDrive.Settings.getInteger("window.multisampling")), 0, 16) : 0;
		if (pixelFormat.getSamples() != samples) pixelFormat = pixelFormat.withSamples(samples);
		// TODO check OpenGL version etc
		Log.debug("OpenGL Version: " + glGetString(GL_VERSION));
	}
	
	public static void close() {
		open = false;
	}
	
	public static boolean isRecreating() {
		return recreating;
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
		recreate = true;
	}
	
	public static boolean isFullscreen() {
		return fullscreen;
	}
	
	public static void setBorderless(boolean borderless) {
		if (Window.borderless == borderless) return;
		Window.borderless = borderless;
		recreate = true;
	}
	
	public static boolean isBorderless() {
		return borderless;
	}
	
	public static void setSize(int width, int height) {
		bounds.width = width;
		bounds.height = height;
	}
	
	public static void setLocation(int x, int y) {
		bounds.x = x;
		bounds.y = y;
	}
	
	public static void setBounds(Rectangle bounds) {
		if (Window.bounds.equals(bounds)) return;
		Window.bounds = bounds;
		recreate = true;
	}
	
	public static void setPixelFormat(PixelFormat pixelFormat) {
		Window.pixelFormat = pixelFormat;
		recreate = true;
	}
	
	public static Rectangle getBounds() {
		return bounds;
	}
	
	public static int getX() {
		return bounds.x;
	}
	
	public static int getY() {
		return bounds.y;
	}
	
	public static int getWidth() {
		return bounds.width;
	}
	
	public static int getHeight() {
		return bounds.height;
	}
	
	public static PixelFormat getPixelFormat() {
		return pixelFormat;
	}
	
	public static void toggleVSync() {
		setVSyncEnabled(!vSync);
	}
	
	public static void setVSyncEnabled(boolean enabled) {
		vSync = enabled;
		Display.setVSyncEnabled(enabled);
	}
	
	public static boolean isVSyncEnabled() {
		return vSync;
	}
	
	public static void toggleAntialiasing() {
		setAntialiasingEnabled(!antialiasing);
	}
	
	public static void setAntialiasingEnabled(boolean enabled) {
		antialiasing = enabled;
		if (Display.isCreated()) {
			antialiasing = antialiasing && GLContext.getCapabilities().GL_ARB_multisample;
			if (antialiasing) glEnable(GL_MULTISAMPLE);
			else glDisable(GL_MULTISAMPLE);
		}
	}
	
	public static boolean isAntialiasingEnabled() {
		return antialiasing;
	}
	
	public static void setMultisampling(int value) {
		setPixelFormat(pixelFormat.withSamples(value));
	}
	
	public static void useSecondaryMonitor(boolean yes) {
		if (yes) Window.setMonitor(Window.getSecondaryMonitor());
		else Window.setMonitor(Window.getPrimaryMonitor());
	}
	
	public static void switchMonitor() {
		GraphicsDevice primaryMonitor = getPrimaryMonitor();
		if (monitor.equals(primaryMonitor)) setMonitor(getSecondaryMonitor());
		else setMonitor(primaryMonitor);
	}
	
	public static GraphicsDevice getPrimaryMonitor() {
		return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
	}
	
	public static GraphicsDevice getSecondaryMonitor() {
		GraphicsDevice[] monitors = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
		if (monitors.length < 2) return null;
		GraphicsDevice currentMonitor = getPrimaryMonitor();
		for (GraphicsDevice monitor : monitors) {
			if (!monitor.equals(currentMonitor)) return monitor;
		}
		return null;
	}
	
	public static void setMonitor(GraphicsDevice monitor) {
		if (Window.monitor.equals(monitor) || monitor == null) return;
		Window.monitor = monitor;
		recreate = true;
	}
	
	public static GraphicsDevice getMonitor() {
		return monitor;
	}
	
	public static GraphicsDevice getMonitor(int x, int y) {
		Point point = new Point(x, Display.getHeight() - y);
		point.x += Display.getX();
		point.y += Display.getY();
		for (GraphicsDevice device : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
			if (device.getDefaultConfiguration().getBounds().contains(point)) return device;
		}
		return null;
	}
}
