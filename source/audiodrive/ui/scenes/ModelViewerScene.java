package audiodrive.ui.scenes;

import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.input.Keyboard;

import audiodrive.model.geometry.ReflectionPlane;
import audiodrive.model.geometry.Vector;
import audiodrive.model.geometry.transform.Rotation;
import audiodrive.model.loader.Model;
import audiodrive.model.loader.ModelLoader;
import audiodrive.ui.GL;
import audiodrive.ui.components.Camera;
import audiodrive.ui.components.Scene;
import audiodrive.ui.components.Window;
import audiodrive.utilities.Buffers;

public class ModelViewerScene extends Scene {
	
	private Rotation rotation = new Rotation();
	private Vector translate = new Vector();
	private Vector look = new Vector();
	private Vector camera = new Vector(0, 0, 2.5);
	private Model model;
	private boolean bended = true;
	private ReflectionPlane flatPlane;
	private ReflectionPlane risingPlane;
	private ReflectionPlane fallingPlane;
	
	@Override
	protected void entering() {
		model = ModelLoader.loadSingleModel("models/xwing/xwing").scale(0.1);
		double y = -0.25;
		flatPlane = new ReflectionPlane(new Vector(-1, y, 1), new Vector(1, y, 1), new Vector(1, y, -1), new Vector(-1, y, -1));
		risingPlane = new ReflectionPlane(new Vector(-1, 2 * y, 1), new Vector(1, 2 * y, 1), new Vector(1, y, 0), new Vector(-1, y, 0)).renderNormal(true);
		fallingPlane = new ReflectionPlane(new Vector(-1, y, 0), new Vector(1, y, 0), new Vector(1, 2 * y, -1), new Vector(-1, 2 * y, -1)).renderNormal(true);
		Camera.perspective(45, getWidth(), getHeight(), 0.001, 1000);
		GL.pushAttributes();
		glEnable(GL_CULL_FACE);
		glEnable(GL_NORMALIZE);
		glEnable(GL_LIGHT0);
		glLight(GL_LIGHT0, GL_DIFFUSE, Buffers.create(1f, 1f, 1f, 1f));
		glEnable(GL_COLOR_MATERIAL);
		glColorMaterial(GL_FRONT, GL_DIFFUSE);
		glShadeModel(GL_SMOOTH);
		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glEnable(GL_LIGHTING);
	}
	
	@Override
	protected void render() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		Camera.position(camera);
		Camera.lookAt(look);
		
		glTranslated(translate.x(), translate.y(), translate.z());
		rotation.apply();
		
		drawCoordinateSystem(3);
		
		if (bended) {
			fallingPlane.reflect(model);
			fallingPlane.render();
			risingPlane.reflect(model);
			risingPlane.render();
		} else {
			flatPlane.reflect(model);
			flatPlane.render();
		}
		
		model.render();
	}
	
	@Override
	protected void exiting() {
		GL.popAttributes();
	}
	
	private void drawCoordinateSystem(int length) {
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
		case Keyboard.KEY_B:
			bended = !bended;
			break;
		case Keyboard.KEY_HOME:
			rotation.reset();
			translate.set(Vector.Null);
			break;
		case Keyboard.KEY_ESCAPE:
			back();
			break;
		case Keyboard.KEY_V:
			Window.toggleVSync();
			break;
		default:
			break;
		}
	}
	
	@Override
	public void mouseDragged(int button, int mouseX, int mouseY, int dx, int dy) {
		double horizontal = dx * -0.1;
		double vertical = dy * 0.1;
		switch (button) {
		case 0:
			rotation.xAdd(vertical).yAdd(horizontal);
			break;
		case 1:
			rotation.zAdd(horizontal);
			break;
		default:
			break;
		}
	}
	
}
