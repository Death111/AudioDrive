package audiodrive.ui.components;

import static org.lwjgl.opengl.GL11.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.lwjgl.opengl.Display;

import audiodrive.AudioDrive;
import audiodrive.ui.control.Input;

public class Scene implements Input.Observer {
	
	private static Map<Class<? extends Scene>, Scene> scenes = new HashMap<>();
	private static Set<Scene> hierarchy = new LinkedHashSet<>();
	private static Scene active;
	private static long frameTimestamp;
	
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
		long time = System.nanoTime();
		double elapsedSeconds = (time - frameTimestamp) / 1000000000.0;
		frameTimestamp = time;
		if (active != null) {
			active.update(elapsedSeconds);
			active.render();
		} else {
			glClear(GL_COLOR_BUFFER_BIT);
		}
	}
	
	public static void destroy() {
		if (active != null) active.exit(false);
	}
	
	public static Scene getActive() {
		return active;
	}
	
	public Scene() {}
	
	public final void enter() {
		if (active == this) return;
		if (active != null) active.exit(false);
		active = this;
		Input.addObserver(this);
		hierarchy.add(this);
		entering();
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
		exit(false);
		AudioDrive.exit();
	}
	
	public final void back() {
		exit(true);
	}
	
	private void exit(boolean enterPredecessor) {
		if (active != this) return;
		exiting();
		Input.removeObserver(this);
		active = null;
		if (!enterPredecessor) return;
		Scene predecessor = getPredecessor();
		hierarchy.remove(this);
		if (predecessor != null) predecessor.enter();
	}
	
	public Scene getPredecessor() {
		if (hierarchy.isEmpty()) return null;
		Iterator<Scene> iterator = hierarchy.iterator();
		Scene current = null;
		while (iterator.hasNext()) {
			Scene next = iterator.next();
			if (next.equals(this)) return current;
			current = next;
		}
		return null;
	}
	
	public int getWidth() {
		return Display.getWidth();
	}
	
	public int getHeight() {
		return Display.getHeight();
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
	
	@Override
	public void keyPressed(int key, char character) {}
	
	@Override
	public void keyReleased(int key, char character) {}
	
	@Override
	public void mouseButtonPressed(int button, int x, int y) {}
	
	@Override
	public void mouseButtonReleased(int button, int x, int y) {}
	
	@Override
	public void mouseDragged(int button, int x, int y, int dx, int dy) {}
	
	@Override
	public void mouseMoved(int x, int y, int dx, int dy) {}
	
	@Override
	public void mouseWheelRotated(int rotation, int x, int y) {}
	
}
