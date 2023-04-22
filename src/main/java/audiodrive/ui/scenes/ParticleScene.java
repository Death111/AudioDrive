package audiodrive.ui.scenes;

import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.input.Keyboard;

import audiodrive.model.geometry.Vector;
import audiodrive.model.geometry.transform.Rotation;
import audiodrive.ui.components.Camera;
import audiodrive.ui.components.Scene;
import audiodrive.ui.effects.Particles3D;

public class ParticleScene extends Scene {
	
	Particles3D particles;
	private Rotation rotation = new Rotation();
	
	@Override
	public void entering() {
		particles = new Particles3D();
		Camera.perspective(45, getWidth(), getHeight(), 1, 1000);
		
	}
	
	@Override
	public void update(double elapsed) {
		particles.update(time());
	}
	
	@Override
	public void render() {
		glClearColor(.5f, .5f, .5f, 1f);
		glClear(GL_COLOR_BUFFER_BIT);
		Camera.position(new Vector(0, 0, -60));
		Camera.lookAt(new Vector(0, 0, 0));
		rotation.apply();
		particles.render();
	}
	
	@Override
	public void exiting() {
		particles = null;
	}
	
	@Override
	public void keyReleased(int key, char character) {
		super.keyReleased(key, character);
		switch (key) {
		case Keyboard.KEY_ESCAPE:
			Scene.get(MenuScene.class).enter();
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
			break;
		case 1:
			rotation.zAdd(horizontal);
			break;
		default:
			break;
		}
	}
	
}
