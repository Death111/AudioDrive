package audiodrive.ui.control;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class Input {
	
	private static final Set<Observer> observers = new HashSet<>();
	private static final Map<Integer, Character> pressedKeys = new HashMap<>();
	
	/** Private constructor to prevent instantiation. */
	private Input() {
		throw new IllegalStateException("This class shall not be instantiated.");
	}
	
	public static void update() {
		while (Mouse.next()) {
			int x = Mouse.getEventX();
			int y = Mouse.getEventY();
			int dx = Mouse.getEventDX();
			int dy = Mouse.getEventDY();
			int rotation = Mouse.getEventDWheel();
			int dragged = -1;
			for (int i = 0; i < Mouse.getButtonCount(); i++) {
				if (Mouse.isButtonDown(i)) {
					dragged = i;
					break;
				}
			}
			if (dx != 0 || dy != 0) {
				if (dragged > -1) {
					int button = dragged;
					fire(o -> o.mouseDragged(button, x, y, dx, dy));
				} else {
					fire(o -> o.mouseMoved(x, y, dx, dy));
				}
			}
			if (rotation != 0) {
				fire(o -> o.mouseWheelRotated(rotation, x, y));
			}
			boolean pressed = Mouse.getEventButtonState();
			int button = Mouse.getEventButton();
			if (pressed) {
				fire(o -> o.mouseButtonPressed(button, x, y));
			} else {
				fire(o -> o.mouseButtonReleased(button, x, y));
			}
		}
		while (Keyboard.next()) {
			boolean pressed = Keyboard.getEventKeyState();
			int key = Keyboard.getEventKey();
			char character = Keyboard.getEventCharacter();
			if (pressed) {
				fire(o -> o.keyPressed(key, character));
				synchronized (pressedKeys) {
					pressedKeys.put(key, character);
				}
				
			} else {
				fire(o -> o.keyReleased(key, character));
				synchronized (pressedKeys) {
					pressedKeys.remove(key);
				}
			}
		}
		pressedKeys.entrySet().forEach(entry -> fire(observer -> observer.keyPressed(entry.getKey(), entry.getValue())));
	}
	
	public static void fire(Consumer<? super Observer> action) {
		new ArrayList<>(observers).forEach(action);
	}
	
	public static void addObserver(Observer observer) {
		observers.add(observer);
	}
	
	public static void addObservers(Observer... observers) {
		addObservers(Arrays.asList(observers));
	}
	
	public static void addObservers(List<Observer> observers) {
		Input.observers.addAll(observers);
	}
	
	public static void removeObserver(Observer observer) {
		observers.remove(observer);
	}
	
	public static void removeObservers(Observer... observers) {
		removeObservers(Arrays.asList(observers));
	}
	
	public static void removeObservers(List<Observer> observers) {
		Input.observers.removeAll(observers);
	}
	
	public static interface Observer {
		
		default void mouseMoved(int x, int y, int dx, int dy) {}
		
		default void mouseDragged(int button, int x, int y, int dx, int dy) {}
		
		default void mouseButtonPressed(int button, int x, int y) {}
		
		default void mouseButtonReleased(int button, int x, int y) {}
		
		default void mouseWheelRotated(int rotation, int x, int y) {}
		
		default void keyPressed(int key, char character) {}
		
		default void keyReleased(int key, char character) {}
		
	}
	
}
