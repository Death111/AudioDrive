package audiodrive.ui.scenes;

import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.input.Keyboard;

import audiodrive.model.geometry.Rotation;
import audiodrive.model.geometry.Vector;
import audiodrive.model.track.Track;
import audiodrive.ui.components.Camera;
import audiodrive.ui.components.Scene;
import audiodrive.utilities.Log;

public class GameScene extends Scene {
	
	private Track track;
	private Rotation rotation = new Rotation();
	private Vector translate = new Vector();
	
	private Vector look = new Vector();
	private Vector camera = new Vector(0, 0, 0.01);
	
	public void enter(Track track) {
		this.track = track;
		super.enter();
	}
	
	@Override
	protected void entering() {
		Log.info("starting game...");
		track.update();
	}
	
	@Override
	protected void render() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		glDisable(GL_CULL_FACE);
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
		
		Camera.perspective(45, getWidth(), getHeight(), 0.001, 100);
		Camera.position(camera);
		Camera.lookAt(look);
		
		glTranslated(translate.x(), translate.y(), translate.z());
		glRotated(rotation.x(), 1, 0, 0);
		glRotated(rotation.y(), 0, 1, 0);
		glRotated(rotation.z(), 0, 0, 1);
		
		track.render();
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
		default:
			break;
		}
	}
	
	@Override
	public void keyReleased(int key, char character) {
		switch (key) {
		case Keyboard.KEY_ESCAPE:
			back();
			break;
		default:
			break;
		}
	}
	
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
	
}
