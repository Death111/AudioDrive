package audiodrive.ui.scenes;

import static org.lwjgl.opengl.GL11.*;

import java.awt.GraphicsDevice;

import org.lwjgl.input.Keyboard;

import audiodrive.AudioDrive;
import audiodrive.ui.components.Camera;
import audiodrive.ui.components.Overlay;
import audiodrive.ui.components.Scene;
import audiodrive.ui.components.Text;
import audiodrive.ui.components.Text.Alignment;
import audiodrive.ui.components.Window;
import audiodrive.ui.effects.ShaderProgram;

public class TitleScene extends Scene {
	
	private Text title;
	private Overlay overlay;
	
	@Override
	public void entering() {
		title = new Text(AudioDrive.Title).setFont(AudioDrive.Font).setSize(48).setPosition(getWidth() / 2, getHeight() / 2).setAlignment(Alignment.Center);
		overlay = new Overlay().shader(new ShaderProgram("shaders/default.vs", "shaders/title.fs"));
		Camera.overlay(getWidth(), getHeight());
	}
	
	@Override
	public void update(double elapsed) {
		if (time() >= 2.0) Scene.get(MenuScene.class).enter();
	}
	
	@Override
	public void render() {
		glClear(GL_COLOR_BUFFER_BIT);
		overlay.render();
		title.render();
	}
	
	@Override
	public void exiting() {
		overlay = null;
		title = null;
	}
	
	@Override
	public void keyReleased(int key, char character) {
		switch (key) {
		case Keyboard.KEY_TAB:
			Window.useSecondaryMonitor();
			break;
		case Keyboard.KEY_ESCAPE:
			Scene.get(MenuScene.class).enter();
			break;
		default:
			break;
		}
	}
	
	@Override
	public void mouseDragged(int button, int x, int y, int dx, int dy) {
		// would require scene rebuilding
		GraphicsDevice monitor = Window.getMonitor(x, y);
		if (!Window.getMonitor().equals(monitor)) {
			Window.setMonitor(monitor);
		}
	}
	
}
