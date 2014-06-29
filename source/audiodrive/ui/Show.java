package audiodrive.ui;

import static org.lwjgl.opengl.GL11.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

import audiodrive.model.geometry.Rotation;
import audiodrive.model.geometry.Vector;
import audiodrive.model.track.Track;
import audiodrive.model.track.interpolation.CatmullRom;
import audiodrive.ui.components.Camera;
import audiodrive.ui.components.Window;
import audiodrive.ui.control.Input;

public class Show {
	
	/** Application title. */
	public static final String Title = "Spline";
	/** Frame rate in frames per second. */
	public static final int Framerate = 100;
	
	public static final boolean Fullscreen = false;
	
	private static long secondTimestamp;
	private static int frames;
	private static int fps;
	
	private static Rotation rotation = new Rotation();
	private static Vector translate = new Vector();
	private static Vector look = new Vector();
	private static Vector up = new Vector().y(1);
	private static Vector camera = new Vector(0, 0, 2.5);
	private static int cameraIndex = -1;
	
	private static boolean showUpVectors = false;
	private static boolean showInterpolationPoints = false;
	private static boolean showSpline = false;
	private static boolean showOrthogonalSpline = false;
	private static boolean showDualSpline = false;
	private static boolean adjustWidth = false;
	
	private static double flightHeight = 0.005;
	private static double sideWidth = 0.1;
	
	private static CatmullRom.Type type = CatmullRom.Type.Centripetal;
	private static int resolution = 3;
	private static List<Vector> right, rightSpline;
	private static List<Vector> left, leftSpline;
	private static List<Vector> centerSpline;
	private static List<Vector> vectorinates;
	private static int points;
	
	/** Private constructor to prevent instantiation. */
	private Show() {
		throw new IllegalStateException("This class shall not be instantiated.");
	}
	
	public static void track(Track track) {
		Show.vectorinates = track.getVectors();
		Show.points = vectorinates.size();
		calculateSplines();
		try {
			if (Fullscreen) Window.setBorderless(true);
			else Window.setSize(1000, 1000);
			Display.setTitle(Title);
			Display.create();
			Input.addObserver(observer);
			while (!Display.isCloseRequested()) {
				Show.tick();
				Show.render();
				Display.update();
				Display.sync(Framerate);
				Input.update();
			}
			Display.destroy();
		} catch (LWJGLException exception) {
			throw new RuntimeException(exception);
		}
	}
	
	private static List<Vector> defaultVectorinates() {
		List<Vector> vectorinates = new ArrayList<>();
		vectorinates.add(new Vector(-1, 0, 0));
		vectorinates.add(new Vector(-0.75, 0.1, 0));
		vectorinates.add(new Vector(-0.5, 0.2, 0.5));
		vectorinates.add(new Vector(-0.25, 0.1, 0));
		vectorinates.add(new Vector(0, -0.2, 0));
		vectorinates.add(new Vector(0.25, 0.2, 0));
		vectorinates.add(new Vector(0.5, 0.3, -0.2));
		vectorinates.add(new Vector(0.75, 0.1, 0));
		vectorinates.add(new Vector(1, 0, 0));
		return vectorinates;
	}
	
	static long lasttime;
	
	private static void render() {
		long time = System.currentTimeMillis();
		if (Keyboard.isKeyDown(Keyboard.KEY_RCONTROL) && time - lasttime > 50) {
			if (Keyboard.isKeyDown(Keyboard.KEY_UP)) moveForward();
			if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) moveBackward();
			lasttime = time;
		}
		
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		glDisable(GL_CULL_FACE);
		glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
		
		Camera.perspective(45, 1, 0.001, 1000);
		Camera.position(camera);
		Camera.lookAt(look);
		
		glTranslated(translate.x(), translate.y(), translate.z());
		glRotated(rotation.x(), 1, 0, 0);
		glRotated(rotation.y(), 0, 1, 0);
		glRotated(rotation.z(), 0, 0, 1);
		
		drawCoordinateSystem();
		drawCameraPosition();
		drawVectorinates();
		drawSpline();
		if (showInterpolationPoints) drawInterpolationPoints();
		if (showSpline) drawSplineArea();
		if (showOrthogonalSpline) drawOrthogonalSplineArea();
		if (showDualSpline) {
			drawDualSpline();
			drawLinesBetweenDualSpline();
		}
	}
	
	private static void drawInterpolationPoints() {
		glColor4d(1, 1, 1, 1);
		glPointSize(3);
		glBegin(GL_POINTS);
		centerSpline.forEach(Vector::gl);
		glEnd();
	}
	
	private static void drawVectorinates() {
		glColor4d(1, 1, 1, 1);
		glPointSize(6);
		glBegin(GL_POINTS);
		vectorinates.forEach(Vector::gl);
		glEnd();
	}
	
	private static void drawCameraPosition() {
		glPointSize(5);
		glBegin(GL_POINTS);
		glColor4d(1, 0, 0, 1);
		camera.gl();
		glColor4d(1, 0, 1, 1);
		look.gl();
		glEnd();
	}
	
	private static void drawCoordinateSystem() {
		glBegin(GL_LINES);
		glColor4d(1, 0, 0, 1);
		glVertex3d(1, 0, 0);
		glVertex3d(-1, 0, 0);
		glColor4d(0, 1, 0, 1);
		glVertex3d(0, 1, 0);
		glVertex3d(0, -1, 0);
		glColor4d(0, 0, 1, 1);
		glVertex3d(0, 0, 1);
		glVertex3d(0, 0, -1);
		glEnd();
	}
	
	private static void drawSpline() {
		glColor4d(1, 1, 1, 1);
		glBegin(GL_LINES);
		if (centerSpline != null && centerSpline.size() > 1) for (int i = 0; i < centerSpline.size() - 1; i++) {
			Vector one = centerSpline.get(i);
			Vector two = centerSpline.get(i + 1);
			one.gl();
			two.gl();
			if (showUpVectors) {
				one.gl();
				one.plus(up.multiplied(0.1)).gl();
			}
		}
		glEnd();
	}
	
	private static void drawSplineArea() {
		glColor4d(0.0, 0.5, 0.5, 1);
		glBegin(GL_QUADS);
		Vector sideOne = null;
		if (centerSpline != null && centerSpline.size() > 1) for (int i = 0; i < centerSpline.size() - 1; i++) {
			Vector one = centerSpline.get(i);
			Vector two = centerSpline.get(i + 1);
			Vector sideTwo;
			if (sideOne == null) sideOne = two.minus(one).cross(Vector.Y).length(sideWidth);
			if (i < centerSpline.size() - 2) {
				Vector three = centerSpline.get(i + 2);
				Vector n1 = two.minus(one).cross(Vector.Y).normalize();
				Vector n2 = three.minus(two).cross(Vector.Y).normalize();
				sideTwo = n1.plus(n2).length(sideWidth / Math.cos(n1.angle(n2) * 0.5));
			} else {
				sideTwo = two.minus(one).cross(Vector.Y).length(sideWidth);
			}
			one.plus(sideOne.negated()).gl();
			one.plus(sideOne).gl();
			two.plus(sideTwo).gl();
			two.plus(sideTwo.negated()).gl();
			sideOne = sideTwo;
		}
		glEnd();
	}
	
	private static void drawOrthogonalSplineArea() {
		glColor4d(0.5, 0, 0.5, 1);
		glBegin(GL_QUADS);
		Vector sideOne = null;
		if (centerSpline != null && centerSpline.size() > 1) for (int i = 0; i < centerSpline.size() - 1; i++) {
			Vector one = centerSpline.get(i);
			Vector two = centerSpline.get(i + 1);
			Vector sideTwo;
			if (sideOne == null) sideOne = two.minus(one).cross(Vector.Y).length(sideWidth);
			if (i < centerSpline.size() - 2) {
				Vector three = centerSpline.get(i + 2);
				Vector a = one.minus(two).normalize();
				Vector b = three.minus(two).normalize();
				Vector n1 = one.minus(two).cross(Vector.Y).normalize();
				Vector n2 = two.minus(three).cross(Vector.Y).normalize();
				sideTwo = a.plus(b).length(sideWidth / Math.cos(n1.angle(n2) * 0.5));
				if (sideOne.angle(sideTwo) > Math.toRadians(90)) sideTwo.negate();
			} else {
				sideTwo = two.minus(one).cross(Vector.Y).length(sideWidth);
			}
			one.plus(sideOne.negated()).gl();
			one.plus(sideOne).gl();
			two.plus(sideTwo).gl();
			two.plus(sideTwo.negated()).gl();
			sideOne = sideTwo;
		}
		glEnd();
	}
	
	private static void drawDualSpline() {
		glColor4d(1, 1, 0, 1);
		glBegin(GL_LINES);
		for (int i = 0; i < rightSpline.size() - 1; i++) {
			rightSpline.get(i).gl();
			rightSpline.get(i + 1).gl();
		}
		for (int i = 0; i < leftSpline.size() - 1; i++) {
			leftSpline.get(i).gl();
			leftSpline.get(i + 1).gl();
		}
		glEnd();
	}
	
	private static void drawLinesBetweenDualSpline() {
		glColor4d(0.5, 0.5, 0, 1);
		glBegin(GL_LINES);
		for (int i = 0; i < rightSpline.size() - 1; i++) {
			leftSpline.get(i).gl();
			rightSpline.get(i).gl();
		}
		glEnd();
	}
	
	private static void calculateSplines() {
		if (vectorinates == null && vectorinates.isEmpty()) return;
		centerSpline = CatmullRom.interpolate(vectorinates, resolution, type);
		right = new ArrayList<>();
		left = new ArrayList<>();
		for (int i = 0; i < vectorinates.size() - 1; i++) {
			Vector one = vectorinates.get(i);
			Vector two = vectorinates.get(i + 1);
			Vector side;
			if (i == 0) {
				side = two.minus(one).cross(Vector.Y).length(sideWidth);
				right.add(one.plus(side));
				left.add(one.plus(side.negated()));
			}
			if (i < vectorinates.size() - 2) {
				Vector three = vectorinates.get(i + 2);
				Vector n1 = two.minus(one).cross(Vector.Y).normalize();
				Vector n2 = three.minus(two).cross(Vector.Y).normalize();
				double length = adjustWidth ? (sideWidth / Math.cos(n1.angle(n2) * 0.5)) : sideWidth;
				side = n1.plus(n2).length(length);
			} else {
				side = two.minus(one).cross(Vector.Y).length(sideWidth);
			}
			right.add(two.plus(side));
			left.add(two.plus(side.negated()));
		}
		rightSpline = CatmullRom.interpolate(right, resolution, type);
		leftSpline = CatmullRom.interpolate(left, resolution, type);
	}
	
	private static void tick() {
		long time = System.nanoTime();
		if (time - secondTimestamp >= TimeUnit.SECONDS.toNanos(1)) {
			fps = frames;
			Show.update();
			frames = 0;
			secondTimestamp = time;
		}
		frames++;
	}
	
	public static void update() {
		Display.setTitle(Title + " : " + vectorinates.size() + " > " + points + " points (" + resolution + " interpolation points per segment)  (" + fps + " FPS)");
	}
	
	public static int getFramerate() {
		return fps;
	}
	
	public static void moveForward() {
		if (cameraIndex > centerSpline.size() - 3) return;
		cameraIndex++;
		Vector one = centerSpline.get(cameraIndex);
		Vector two = centerSpline.get(cameraIndex + 1);
		camera = one.plus(up.multiplied(flightHeight));
		look = two.plus(up.multiplied(flightHeight));
		rotation.set(Vector.Null);
		translate.set(Vector.Null);
	}
	
	public static void moveBackward() {
		if (cameraIndex < 1) return;
		if (cameraIndex > centerSpline.size() - 3) cameraIndex = centerSpline.size() - 3;
		else cameraIndex--;
		Vector one = centerSpline.get(cameraIndex);
		Vector two = centerSpline.get(cameraIndex + 1);
		camera = one.plus(up.multiplied(flightHeight));
		look = two.plus(up.multiplied(flightHeight));
		rotation.set(Vector.Null);
		translate.set(Vector.Null);
	}
	
	private static Input.Observer observer = new Input.Observer() {
		@Override
		public void mouseWheelRotated(int rotation, int x, int y) {
			int increment = (rotation == 0 ? 0 : (rotation > 0 ? 1 : -1));
			resolution = Math.max(2, resolution + increment);
			calculateSplines();
			points = centerSpline.size();
			update();
		}
		
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
			case Keyboard.KEY_INSERT:
				sideWidth += 0.001;
				calculateSplines();
				break;
			case Keyboard.KEY_DELETE:
				sideWidth -= 0.001;
				if (sideWidth < 0) sideWidth = 0;
				calculateSplines();
				break;
			case Keyboard.KEY_UP:
				moveForward();
				break;
			case Keyboard.KEY_DOWN:
				moveBackward();
				break;
			default:
				break;
			}
		}
		
		@Override
		public void keyReleased(int key, char character) {
			switch (key) {
			case Keyboard.KEY_U:
				showUpVectors = !showUpVectors;
				break;
			case Keyboard.KEY_P:
				showInterpolationPoints = !showInterpolationPoints;
				break;
			case Keyboard.KEY_S:
				showSpline = !showSpline;
				break;
			case Keyboard.KEY_D:
				showDualSpline = !showDualSpline;
				break;
			case Keyboard.KEY_O:
				showOrthogonalSpline = !showOrthogonalSpline;
				break;
			case Keyboard.KEY_SPACE:
				adjustWidth = !adjustWidth;
				calculateSplines();
				break;
			case Keyboard.KEY_1:
				type = CatmullRom.Type.Uniform;
				calculateSplines();
				break;
			case Keyboard.KEY_2:
				type = CatmullRom.Type.Chordal;
				calculateSplines();
				break;
			case Keyboard.KEY_3:
				type = CatmullRom.Type.Centripetal;
				calculateSplines();
				break;
			case Keyboard.KEY_HOME:
				camera.set(0, 0, 2.5);
				look.set(Vector.Null);
				up.set(0, 1, 0);
				cameraIndex = -1;
			case Keyboard.KEY_ESCAPE:
				rotation.set(Vector.Null);
				translate.set(Vector.Null);
				break;
			default:
				break;
			}
		};
		
		@Override
		public void mouseDragged(int button, int mouseX, int mouseY, int dx, int dy) {
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
