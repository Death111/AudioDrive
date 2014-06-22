package audiodrive.ui.scenes;

import static org.lwjgl.opengl.GL11.*;
import audiodrive.AudioDrive;
import audiodrive.ui.components.Camera;
import audiodrive.ui.components.Scene;
import audiodrive.ui.components.Text;

public class TitleScene extends Scene {
	
	private Text title;
	private double duration;
	
	@Override
	public void entering() {
		title = new Text(AudioDrive.Title).setFont(AudioDrive.Font).setSize(48).setCentered(getWidth() / 2, getHeight() / 2);
		Camera.overlay(getWidth(), getHeight());
	}
	
	@Override
	public void update(double elapsed) {
		duration += elapsed;
		title.setText(AudioDrive.Title);
		if (duration >= 2.0) Scene.get(AudioSelectionScene.class).enter();
	}
	
	@Override
	public void render() {
		glClear(GL_COLOR_BUFFER_BIT);
		title.render();
	}
	
	@Override
	public void exiting() {
		title = null;
	}
	
}
