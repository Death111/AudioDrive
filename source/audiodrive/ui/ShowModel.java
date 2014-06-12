package audiodrive.ui;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_FRONT_AND_BACK;
import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glColor4d;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glPolygonMode;
import static org.lwjgl.opengl.GL11.glRotated;
import static org.lwjgl.opengl.GL11.glTranslated;
import static org.lwjgl.opengl.GL11.glVertex3d;

import java.nio.FloatBuffer;
import java.util.concurrent.TimeUnit;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import audiodrive.model.geometry.Rotation;
import audiodrive.model.geometry.Vector;
import audiodrive.model.loader.Model;
import audiodrive.model.loader.ModelLoader;
import audiodrive.ui.components.Camera;
import audiodrive.ui.components.Window;
import audiodrive.ui.control.Input;

public class ShowModel {

	/** Application title. */
	public static final String Title = "Modeltest";
	/** Frame rate in frames per second. */
	public static final int Framerate = 100;

	public static final boolean Fullscreen = false;

	private static long lasttime;
	private static long secondTimestamp;
	private static int frames;
	private static int fps;

	private static Rotation rotation = new Rotation();
	private static Vector translate = new Vector();
	private static Vector look = new Vector();
	private static Vector up = new Vector().y(1);
	private static Vector camera = new Vector(0, 0, 2.5);

	private static Model model = null;

	/** Private constructor to prevent instantiation. */
	private ShowModel() {
		throw new IllegalStateException("This class shall not be instantiated.");
	}

	public static void show(String modelPath) {
		model = ModelLoader.loadModel(modelPath);
		try {
			if (Fullscreen)
				Window.setBorderless(true);
			else
				Window.setSize(1000, 1000);
			Display.setTitle(Title);
			Display.create();
			Input.addObserver(observer);
			while (!Display.isCloseRequested()) {
				ShowModel.tick();
				ShowModel.render();
				Display.update();
				Display.sync(Framerate);
				Input.update();
			}
			Display.destroy();
		} catch (LWJGLException exception) {
			throw new RuntimeException(exception);
		}
	}

	private static void render() {
		long time = System.currentTimeMillis();
		if (Keyboard.isKeyDown(Keyboard.KEY_RCONTROL) && time - lasttime > 50) {
			lasttime = time;
		}

		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		glDisable(GL_CULL_FACE);

		// Light settings
		glDisable(GL11.GL_LIGHTING);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		GL11.glEnable(GL11.GL_LIGHT0);
		FloatBuffer grey01 = getFloatBuffer(1f, 1f, 1f, 1);
		GL11.glLight(GL11.GL_LIGHT0, GL11.GL_AMBIENT, grey01);

		glPolygonMode(GL_FRONT_AND_BACK, GL11.GL_FILL);

		GL11.glEnable(GL11.GL_NORMALIZE);

		Camera.perspective(45, 1, 0.001, 1000);
		Camera.position(camera);
		Camera.lookAt(look);

		glTranslated(translate.x(), translate.y(), translate.z());
		glRotated(rotation.x(), 1, 0, 0);
		glRotated(rotation.y(), 0, 1, 0);
		glRotated(rotation.z(), 0, 0, 1);

		drawCoordinateSystem(3);

		GL11.glEnable(GL11.GL_LIGHTING);

		drawObject();

	}

	private static FloatBuffer getFloatBuffer(float f, float g, float h, int i) {
		FloatBuffer floatBuffer = BufferUtils.createFloatBuffer(4);
		floatBuffer.put(f).put(g).put(h).put(i).flip();

		return floatBuffer;
	}

	private static void drawObject() {

		GL11.glPushMatrix();
		float scaleFactor = .1f;
		GL11.glScalef(scaleFactor, scaleFactor, scaleFactor);
		model.render();
		// logger.info(model);
		GL11.glPopMatrix();
	}

	private static void drawCoordinateSystem(int length) {
		glBegin(GL_LINES);
		glColor4d(length, 0, 0, length);
		glVertex3d(length, 0, 0);
		glVertex3d(-length, 0, 0);
		glColor4d(0, length, 0, length);
		glVertex3d(0, length, 0);
		glVertex3d(0, -length, 0);
		glColor4d(0, 0, length, length);
		glVertex3d(0, 0, length);
		glVertex3d(0, 0, -length);
		glEnd();
	}

	private static void tick() {
		long time = System.nanoTime();
		if (time - secondTimestamp >= TimeUnit.SECONDS.toNanos(1)) {
			fps = frames;
			ShowModel.update();
			frames = 0;
			secondTimestamp = time;
		}
		frames++;
	}

	public static void update() {
		Display.setTitle(Title + " (" + fps + " FPS)");
	}

	public static int getFramerate() {
		return fps;
	}

	private static Input.Observer observer = new Input.Observer() {

		@Override
		public void keyPressed(int key, char character) {
			switch (key) {
			case Keyboard.KEY_NUMPAD0:
				translate.add(0, 0, -0.01);
				break;
			case Keyboard.KEY_NUMPAD2:
				translate.add(0, 0.01, 0);
				break;
			case Keyboard.KEY_NUMPAD4:
				translate.add(0.01, 0, 0);
				break;
			case Keyboard.KEY_NUMPAD5:
				translate.add(0, 0, 0.01);
				break;
			case Keyboard.KEY_NUMPAD6:
				translate.add(-0.01, 0, 0);
				break;
			case Keyboard.KEY_NUMPAD8:
				translate.add(0, -0.01, 0);
				break;
			case Keyboard.KEY_ADD:
				camera.add(look.minus(camera).length(0.1));
				break;
			case Keyboard.KEY_SUBTRACT:
				camera.add(camera.minus(look).length(0.1));
				break;

			default:
				break;
			}
		}

		@Override
		public void keyReleased(int key, char character) {
			switch (key) {
			case Keyboard.KEY_ESCAPE:
				rotation.set(Vector.Null);
				translate.set(Vector.Null);
				break;
			default:
				break;
			}
		};

		@Override
		public void mouseDragged(int button, int mouseX, int mouseY, int dx,
				int dy) {
			double horizontal = dx * 0.1;
			double vertical = dy * -0.1;
			switch (button) {
			case 0:
				rotation.add(vertical, horizontal, 0);
				break;
			case 1:
				rotation.add(vertical, 0, horizontal);
				break;
			case 2:
				rotation.add(0, vertical, horizontal);
				break;
			default:
				break;
			}
		}
	};

}
