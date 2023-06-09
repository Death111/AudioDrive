package audiodrive.ui.scenes;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;

import audiodrive.AudioDrive;
import audiodrive.audio.AudioResource;
import audiodrive.ui.components.Camera;
import audiodrive.ui.components.Overlay;
import audiodrive.ui.components.Scene;
import audiodrive.ui.components.Text;
import audiodrive.ui.components.Text.Alignment;
import audiodrive.ui.effects.ShaderProgram;

public class TitleScene extends Scene {
	
	private Text title;
	private Overlay background;
	
	static {
		new AudioResource("sounds/Title.wav").play();
	}
	
	@Override
	public void entering() {
		title = new Text(AudioDrive.Title).setFont(AudioDrive.Font).setSize(48).setPosition(getWidth() / 2, getHeight() / 2).setAlignment(Alignment.Center);
		background = new Overlay().shader(new ShaderProgram("shaders/Default.vs", "shaders/Title.fs"));
		Camera.overlay(getWidth(), getHeight());
	}
	
	@Override
	public void update(double elapsed) {
		if (time() >= 2.2) Scene.get(MenuScene.class).enter();
	}
	
	@Override
	public void render() {
		glClear(GL_COLOR_BUFFER_BIT);
		background.render();
		title.render();
	}
	
	@Override
	public void exiting() {
		background = null;
		title = null;
	}
	
}
