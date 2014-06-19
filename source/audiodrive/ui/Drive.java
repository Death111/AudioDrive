package audiodrive.ui;

import static org.lwjgl.opengl.GL11.*;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

import audiodrive.model.Track;
import audiodrive.model.geometry.Rotation;
import audiodrive.model.geometry.Vector;
import audiodrive.model.track.interpolation.CatmullRom;
import audiodrive.ui.components.Camera;
import audiodrive.ui.components.Window;
import audiodrive.ui.control.Input;

public class Drive {
	
	/** Application title. */
	public static final String Title = "Spline";
	/** Frame rate in frames per second. */
	public static final int Framerate = 100;
	
	public static final boolean Fullscreen = false;
	
	private static double increment;
	private static double incrementPerSecond;
	private static double elapsedSeconds;
	private static long secondTimestamp;
	private static long timestamp;
	private static int frames;
	private static int fps;
	
	private static Rotation rotation = new Rotation();
	private static Vector translate = new Vector();
	private static Vector look = new Vector();
	private static Vector up = new Vector().y(1);
	private static Vector camera = new Vector(0, 0, 2.5);
	private static int cameraIndex = -1;
	
	private static boolean thirdPersonView = false;
	private static boolean fill = false;
	private static boolean showPlayer = true;
	private static boolean showUpVectors = false;
	private static boolean showInterpolationPoints = false;
	private static boolean showSpline = false;
	private static boolean showOrthogonalSpline = false;
	private static boolean showDualSpline = true;
	private static boolean showCoordinateSystem = false;
	private static boolean adjustWidth = false;
	
	private static boolean pause = false;
	private static double sideSpeed;
	private static double sideWidth;
	private static double sidePosition;
	private static int playerOffset;
	private static int sightOffset;
	private static double sightHeight;
	private static double flightHeight;
	
	private static List<Vector> right, rightSpline;
	private static List<Vector> left, leftSpline;
	private static List<Vector> centerSpline;
	private static List<Vector> vectorinates;
	private static Vector[] sides;
	private static int points;
	
	private static CatmullRom.Type type = CatmullRom.Type.Centripetal;
	private static int resolution = 25;
	
	static {
		reset();
	}
	
	private static void reset() {
		sideSpeed = 5;
		sideWidth = 0.003;
		sidePosition = 0.0;
		playerOffset = 1;
		sightOffset = 3;
		sightHeight = 0.001;
		flightHeight = 0.002;
	}
	
	/** Private constructor to prevent instantiation. */
	private Drive() {
		throw new IllegalStateException("This class shall not be instantiated.");
	}
	
	public static void demo() {
		track(new Track(defaultVectorinates(), 15, 50));
	}
	
	public static void track(Track track) {
		int vectors = (1 + (track.getVectors().size() - 1) * track.getSmoothing());
		Drive.resolution = 1 + track.getSmoothing();
		Drive.incrementPerSecond = vectors / track.getDuration();
		Drive.vectorinates = track.getVectors();
		Drive.points = vectorinates.size();
		calculateSplines();
		timestamp = System.nanoTime();
		try {
			if (Fullscreen) Window.setBorderless(true);
			else Window.setSize(1000, 1000);
			Display.setTitle(Title);
			// Display.setVSyncEnabled(true);
			Display.create();
			Input.addObserver(observer);
			while (!Display.isCloseRequested()) {
				Drive.tick();
				Drive.render();
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
	
	private static void render() {
		moveForward();
		
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		glDisable(GL_CULL_FACE);
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		if (fill) glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
		else glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
		
		Camera.perspective(45, 1, 0.001, 100);
		Camera.position(camera);
		Camera.lookAt(look);
		
		glTranslated(translate.x(), translate.y(), translate.z());
		glRotated(rotation.x(), 1, 0, 0);
		glRotated(rotation.y(), 0, 1, 0);
		glRotated(rotation.z(), 0, 0, 1);
		
		if (showCoordinateSystem) drawCoordinateSystem();
		if (showPlayer) drawPlayer();
		else drawCameraPosition();
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
	
	private static void drawPlayer() {
		glColor4d(0, 0, 1, 0.5);
		if (cameraIndex < centerSpline.size() - playerOffset - 1) {
			Vector frontSide = sides[cameraIndex + playerOffset + 1];
			Vector backSide = sides[cameraIndex + playerOffset].clone();
			Vector front = centerSpline.get(cameraIndex + playerOffset + 1).plus(frontSide.multiplied(sidePosition)).plus(up.multiplied(sightHeight));
			Vector back = centerSpline.get(cameraIndex + playerOffset).plus(backSide.multiplied(sidePosition)).plus(up.multiplied(sightHeight));
			backSide.length(0.0001);
			if (!fill) glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
			glBegin(GL_TRIANGLES);
			front.gl();
			back.plus(backSide).gl();
			back.plus(backSide.negated()).gl();
			glEnd();
			if (!fill) glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
		}
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
		glColor4d(0.0, 0.5, 0.5, 0.5);
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
		glColor4d(0.5, 0, 0.5, 0.5);
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
				Vector n1 = a.cross(Vector.Y).normalize();
				Vector n2 = b.cross(Vector.Y).normalize();
				if (a.plus(b).equals(Vector.Null)) sideTwo = a.cross(Vector.Y).length(sideWidth);
				else sideTwo = a.plus(b).length(sideWidth / Math.cos(n1.angle(n2) * 0.5));
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
		glColor4d(0.5, 0.5, 0, 0.5);
		if (fill) {
			glBegin(GL_QUADS);
			for (int i = 0; i < rightSpline.size() - 2; i++) {
				leftSpline.get(i).gl();
				rightSpline.get(i).gl();
				rightSpline.get(i + 1).gl();
				leftSpline.get(i + 1).gl();
			}
			glEnd();
		} else {
			glBegin(GL_LINES);
			for (int i = 0; i < rightSpline.size() - 1; i++) {
				leftSpline.get(i).gl();
				rightSpline.get(i).gl();
			}
			glEnd();
		}
	}
	
	private static void calculateSplines() {
		if (vectorinates == null && vectorinates.isEmpty()) return;
		centerSpline = CatmullRom.interpolate(vectorinates, resolution, type);
		// calculate and store sides
		sides = new Vector[centerSpline.size()];
		for (int i = 0; i < centerSpline.size() - 1; i++) {
			Vector one = centerSpline.get(i);
			Vector two = centerSpline.get(i + 1);
			Vector side;
			if (i == 0) {
				side = two.minus(one).cross(Vector.Y).length(sideWidth);
				sides[0] = side;
			}
			if (i < centerSpline.size() - 2) {
				Vector three = centerSpline.get(i + 2);
				Vector n1 = two.minus(one).cross(Vector.Y).normalize();
				Vector n2 = three.minus(two).cross(Vector.Y).normalize();
				double length = adjustWidth ? (sideWidth / Math.cos(n1.angle(n2) * 0.5)) : sideWidth;
				side = n1.plus(n2).length(length);
			} else {
				side = two.minus(one).cross(Vector.Y).length(sideWidth);
			}
			sides[i + 1] = side;
		}
		// calculate right and left splines
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
		elapsedSeconds = (time - timestamp) / 1000000000.0;
		timestamp = time;
		frames++;
		// each second
		if (time - secondTimestamp >= 1000000000) {
			fps = frames;
			Drive.update();
			frames = 0;
			secondTimestamp = time;
		}
	}
	
	public static void update() {
		Display.setTitle(Title + " : " + vectorinates.size() + " > " + points + " points (" + resolution + " interpolation points per segment)  (" + fps + " FPS)");
	}
	
	public static int getFramerate() {
		return fps;
	}
	
	public static int getMovement() {
		increment += elapsedSeconds * incrementPerSecond;
		if (increment >= 1.0) {
			int movement = (int) increment;
			increment -= movement;
			return movement;
		}
		return 0;
	}
	
	public static void moveForward() {
		int movement = getMovement();
		int max = centerSpline.size() - 2;
		if (cameraIndex >= max) return;
		if (cameraIndex + movement < max) cameraIndex += movement;
		else cameraIndex = max;
		updatePosition();
	}
	
	public static void moveBackward() {
		int movement = getMovement();
		if (cameraIndex <= 0) return;
		if (cameraIndex - movement > 0) cameraIndex -= movement;
		else cameraIndex = 0;
		updatePosition();
	}
	
	public static void moveRight() {
		sidePosition = Math.min(1, sidePosition + sideSpeed * 0.01);
		updatePosition();
	}
	
	public static void moveLeft() {
		sidePosition = Math.max(-1, sidePosition - sideSpeed * 0.01);
		updatePosition();
	}
	
	public static void updatePosition() {
		if (cameraIndex < 0 || cameraIndex >= centerSpline.size() - 2) return;
		int lookIndex = Math.min(centerSpline.size() - 1, cameraIndex + sightOffset);
		Vector one = centerSpline.get(cameraIndex);
		Vector two = centerSpline.get(lookIndex);
		Vector back = one.plus(sides[cameraIndex].multiplied(sidePosition));
		Vector front = two.plus(sides[lookIndex].multiplied(sidePosition));
		camera = back.plus(up.multiplied(flightHeight));
		look = front.plus(up.multiplied(sightHeight));
		rotation.set(Vector.Null);
		translate.set(Vector.Null);
		if (thirdPersonView) {
			Vector direction = front.minus(back).length(0.1);
			Vector upward = direction.cross(sides[cameraIndex]).length(0.01);
			translate.set(direction.add(upward));
		}
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
			boolean leftControl = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL);
			boolean rightControl = Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);
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
				translate.add(look.minus(camera).length(0.1));
				break;
			case Keyboard.KEY_SUBTRACT:
				translate.add(camera.minus(look).length(0.1));
				break;
			case Keyboard.KEY_INSERT:
				sideWidth += 0.0001;
				calculateSplines();
				break;
			case Keyboard.KEY_DELETE:
				sideWidth -= 0.0001;
				if (sideWidth < 0) sideWidth = 0;
				calculateSplines();
				break;
			case Keyboard.KEY_UP:
				if (pause) moveForward();
				break;
			case Keyboard.KEY_DOWN:
				if (pause) moveBackward();
				break;
			case Keyboard.KEY_LEFT:
				moveLeft();
				break;
			case Keyboard.KEY_RIGHT:
				moveRight();
				break;
			case Keyboard.KEY_PRIOR:
				if (!leftControl && !rightControl) {
					flightHeight += 0.0001;
					sightHeight += 0.0001;
				} else {
					if (leftControl) flightHeight += 0.0001;
					if (rightControl) sightHeight += 0.0001;
				}
				updatePosition();
				break;
			case Keyboard.KEY_NEXT:
				if (!leftControl && !rightControl) {
					flightHeight -= 0.0001;
					sightHeight -= 0.0001;
				} else {
					if (leftControl) flightHeight -= 0.0001;
					if (rightControl) sightHeight -= 0.0001;
				}
				updatePosition();
				break;
			default:
				break;
			}
		}
		
		@Override
		public void keyReleased(int key, char character) {
			switch (key) {
			case Keyboard.KEY_SPACE:
				showPlayer = !showPlayer;
				break;
			case Keyboard.KEY_V:
				thirdPersonView = !thirdPersonView;
				updatePosition();
				break;
			case Keyboard.KEY_C:
				showCoordinateSystem = !showCoordinateSystem;
				break;
			case Keyboard.KEY_PAUSE:
				pause = !pause;
				break;
			case Keyboard.KEY_F:
				fill = !fill;
				break;
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
			case Keyboard.KEY_W:
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
				break;
			case Keyboard.KEY_ESCAPE:
				rotation.set(Vector.Null);
				translate.set(Vector.Null);
				reset();
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
