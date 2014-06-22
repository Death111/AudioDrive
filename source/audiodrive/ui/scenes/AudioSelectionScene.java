package audiodrive.ui.scenes;

import static org.lwjgl.opengl.GL11.*;

import java.io.File;

import javafx.stage.FileChooser;
import audiodrive.AudioDrive;
import audiodrive.audio.AudioFile;
import audiodrive.ui.components.Camera;
import audiodrive.ui.components.Scene;
import audiodrive.ui.components.Text;
import audiodrive.ui.components.Window;
import audiodrive.utilities.Log;

public class AudioSelectionScene extends Scene {
	
	private Text title;
	private double duration;
	private boolean fullscreen;
	private boolean prepared;
	private boolean chosen;
	int screenWidth;
	int screenHeight;
	
	private File file;
	
	@Override
	public void entering() {
		title = new Text("Select an audio file").setFont(AudioDrive.Font).setSize(48).setPosition(20, 20);
		Camera.overlay(getWidth(), getHeight());
		fullscreen = Window.isFullscreen();
		if (fullscreen) {
			screenWidth = Window.getWidth();
			screenHeight = Window.getHeight();
		}
	}
	
	@Override
	public void update(double elapsed) {
		duration += elapsed;
		if (!chosen && duration > 0.5) {
			if (Window.isFullscreen() && !prepared) {
				screenWidth = Window.getWidth();
				screenHeight = Window.getHeight();
				Window.setSize(screenWidth, screenHeight - 1);
				prepared = true;
			} else {
				chosen = true;
				Log.info("waiting for audio file selection...");
				file = showFileChooser();
				if (fullscreen) Window.setSize(screenWidth, screenHeight);
				if (file == null) {
					Log.info("none selected, exit");
					exit();
				} else {
					Log.info("\"%s\" selected", file.getName());
					Scene.get(AnalyzationScene.class).enter(new AudioFile(file));
				}
			}
		}
	}
	
	private File showFileChooser() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(new File("music"));
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Audiodateien (*.mp3, *.ogg, *.wav)", "*.mp3", "*.ogg", "*.wav"));
		return fileChooser.showOpenDialog(AudioDrive.getStage());
	}
	
	@Override
	public void render() {
		glClear(GL_COLOR_BUFFER_BIT);
		title.render();
	}
	
	@Override
	public void exiting() {
		title = null;
		prepared = false;
		chosen = false;
	}
	
}
