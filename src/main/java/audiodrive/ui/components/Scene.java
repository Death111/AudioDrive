package audiodrive.ui.components;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;

import java.awt.GraphicsDevice;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.input.Keyboard;

import audiodrive.ui.control.Input;

public class Scene implements Input.Observer {
	
	private static Map<Class<? extends Scene>, Scene> scenes = new HashMap<>();
	private static Scene active;
	private static Scene entering;
	private static long frameTimestamp;
	private static double secondTimestamp;
	private static double deltaTime;
	private static double time;
	private static int framerate;
	private static int frames;
	
	@SuppressWarnings("unchecked")
	public static <T extends Scene> T get(Class<T> clazz) {
		if (scenes.containsKey(clazz)) return (T) scenes.get(clazz);
		try {
			T scene = clazz.newInstance();
			scenes.put(clazz, scene);
			return scene;
		} catch (InstantiationException | IllegalAccessException exception) {
			throw new RuntimeException(exception);
		}
	}
	
	public static void update() {
		long nanoTime = System.nanoTime();
		deltaTime = (nanoTime - frameTimestamp) / 1000000000.0;
		time += deltaTime;
		frameTimestamp = nanoTime;
		frames++;
		if (time - secondTimestamp >= 1.0) {
			framerate = frames;
			frames = 0;
			secondTimestamp = time;
		}
		if (active != null) {
			active.update(deltaTime);
			active.render();
		} else {
			glClear(GL_COLOR_BUFFER_BIT);
		}
	}
	
	public static void destroy() {
		if (active != null) active.exit();
	}
	
	public static Scene getActive() {
		return active;
	}
	
	public static Scene getEntering() {
		return entering;
	}
	
	public static int getFramerate() {
		return framerate;
	}
	
	public Scene() {}
	
	public final void enter() {
		if (active == this) return;
		entering = this;
		if (active != null) active.exit();
		active = this;
		Input.addObserver(this);
		entering();
		entering = null;
		frameTimestamp = System.nanoTime();
	}
	
	/**
	 * Called when entering the scene.
	 */
	protected void entering() {}
	
	/**
	 * Called every frame.
	 *
	 * @param elapsed elapsed time since the last frame, in seconds
	 */
	protected void update(double elapsed) {}
	
	/**
	 * Called every frame.
	 */
	protected void render() {}
	
	/**
	 * Called when exiting the scene.
	 */
	protected void exiting() {}
	
	public final void exit() {
		exit(null);
	}
	
	private void exit(Scene next) {
		if (active != this) return;
		exiting();
		Input.removeObserver(this);
		active = null;
		if (next != null) next.enter();
	}
	
	/** return time since game start, in seconds */
	public static double time() {
		return time;
	}
	
	/** return time since last frame, in seconds */
	public static double deltaTime() {
		return deltaTime;
	}
	
	public int getWidth() {
		return Window.getWidth();
	}
	
	public int getHeight() {
		return Window.getHeight();
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
	
	@Override
	public void keyPressed(int key, char character) {}
	
	@Override
	public void keyReleased(int key, char character) {
		switch (key) {
		case Keyboard.KEY_TAB:
			Window.switchMonitor();
			break;
		}
	}
	
	@Override
	public void mouseButtonPressed(int button, int x, int y) {}
	
	@Override
	public void mouseButtonReleased(int button, int x, int y) {}
	
	@Override
	public void mouseDragged(int button, int x, int y, int dx, int dy) {
		GraphicsDevice monitor = Window.getMonitor(x, y);
		if (!Window.getMonitor().equals(monitor)) {
			Window.setMonitor(monitor);
		}
	}
	
	@Override
	public void mouseMoved(int x, int y, int dx, int dy) {}
	
	@Override
	public void mouseWheelRotated(int rotation, int x, int y) {}
	
}
