package audiodrive.ui.scenes;

import static org.lwjgl.opengl.GL11.*;

import java.io.File;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import audiodrive.AudioDrive;
import audiodrive.model.geometry.ReflectionPlane;
import audiodrive.model.geometry.Vector;
import audiodrive.model.geometry.transform.Rotation;
import audiodrive.model.loader.Model;
import audiodrive.model.loader.ModelLoader;
import audiodrive.ui.GL;
import audiodrive.ui.components.Camera;
import audiodrive.ui.components.Scene;
import audiodrive.ui.components.Text;
import audiodrive.ui.components.Window;
import audiodrive.utilities.Buffers;
import audiodrive.utilities.Files;
import audiodrive.utilities.Log;

public class ModelSelectionScene extends Scene {
	
	private Text title;
	
	private Model model;
	private List<File> list;
	private int index;
	
	private Rotation rotation = new Rotation();
	private Vector translate = new Vector();
	private Vector look = new Vector();
	private Vector camera = new Vector(0, 1, 2.5);
	
	private ReflectionPlane flatPlane;
	private ReflectionPlane risingPlane;
	private ReflectionPlane fallingPlane;
	
	private boolean normals = false;
	private boolean bended = true;
	private boolean coordinateSystem = false;
	
	private double time;
	
	@Override
	protected void entering() {
		title = new Text("Select a Player-Model").setFont(AudioDrive.Font).setSize(48).setPosition(20, 20);
		list = Files.list("models/player", ".obj", true);
		File modelFile = list.stream().filter(file -> file.getName().endsWith(AudioDrive.Settings.get("player.model") + ".obj")).findFirst().orElse(list.get(0));
		loadModel(list.indexOf(modelFile));
		double y = -0.25;
		flatPlane = new ReflectionPlane(new Vector(-1, y, 1), new Vector(1, y, 1), new Vector(1, y, -1), new Vector(-1, y, -1));
		risingPlane = new ReflectionPlane(new Vector(-1, 2 * y, 1), new Vector(1, 2 * y, 1), new Vector(1, y, 0), new Vector(-1, y, 0));
		fallingPlane = new ReflectionPlane(new Vector(-1, y, 0), new Vector(1, y, 0), new Vector(1, 2 * y, -1), new Vector(-1, 2 * y, -1));
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
		Camera.overlay(getWidth(), getHeight());
		title.render();
		
		Camera.perspective(45, getWidth(), getHeight(), 0.1, 1000);
		
		Camera.position(camera);
		Camera.lookAt(look);
		
		glTranslated(translate.x(), translate.y(), translate.z());
		rotation.apply();
		
		if (!Mouse.isButtonDown(0) && !Mouse.isButtonDown(1)) {
			rotation.yAdd(10 * (time() - time));
		}
		time = time();
		
		if (coordinateSystem) drawCoordinateSystem(3);
		
		if (bended) {
			fallingPlane.reflect(model);
			fallingPlane.renderNormal(normals).render();
			risingPlane.reflect(model);
			risingPlane.renderNormal(normals).render();
		} else {
			flatPlane.reflect(model);
			flatPlane.renderNormal(normals).render();
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
	
	private void loadModel(int index) {
		if (index < 0) index = list.size() - 1;
		if (index > list.size() - 1) index = 0;
		this.index = index;
		File file = list.get(index);
		model = ModelLoader.loadSingleModel(file.getPath()).scale(0.1);
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
		super.keyReleased(key, character);
		switch (key) {
		case Keyboard.KEY_B:
			bended = !bended;
			break;
		case Keyboard.KEY_C:
			coordinateSystem = !coordinateSystem;
			break;
		case Keyboard.KEY_N:
			normals = !normals;
			break;
		case Keyboard.KEY_HOME:
			rotation.reset();
			translate.set(Vector.Null);
			break;
		case Keyboard.KEY_ESCAPE:
			back();
			break;
		case Keyboard.KEY_RETURN:
			AudioDrive.Settings.set("player.model", model.getName());
			Log.debug("selected model " + model.getName());
			back();
			break;
		case Keyboard.KEY_V:
			Window.toggleVSync();
			break;
		case Keyboard.KEY_RIGHT:
			loadModel(index + 1);
			break;
		case Keyboard.KEY_LEFT:
			loadModel(index - 1);
			break;
		default:
			break;
		}
	}
	
	@Override
	public void mouseDragged(int button, int x, int y, int dx, int dy) {
		super.mouseDragged(button, x, y, dx, dy);
		double horizontal = dx * -0.1;
		double vertical = dy * 0.1;
		switch (button) {
		case 0:
			rotation.xAdd(vertical).yAdd(horizontal);
			time = time();
			break;
		case 1:
			rotation.zAdd(horizontal);
			time = time();
			break;
		default:
			break;
		}
	}
	
}
