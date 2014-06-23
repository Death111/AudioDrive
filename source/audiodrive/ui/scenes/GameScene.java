package audiodrive.ui.scenes;

import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.input.Keyboard;

import audiodrive.model.Track;
import audiodrive.ui.components.Scene;
import audiodrive.utilities.Log;

public class GameScene extends Scene {
	
	private Track track;
	
	public void enter(Track track) {
		this.track = track;
		
	}
	
	@Override
	protected void entering() {
		Log.info("starting game...");
		super.entering();
	}
	
	@Override
	protected void render() {
		glClear(GL_COLOR_BUFFER_BIT);
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
	
}
