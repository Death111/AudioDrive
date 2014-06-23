package audiodrive.ui.scenes;

import static org.lwjgl.opengl.GL11.*;
import audiodrive.AudioDrive;
import audiodrive.audio.AudioAnalyzer;
import audiodrive.audio.AudioFile;
import audiodrive.ui.components.Camera;
import audiodrive.ui.components.Scene;
import audiodrive.ui.components.Text;
import audiodrive.utilities.Log;

public class AnalyzationScene extends Scene {
	
	private Text title;
	private AudioFile file;
	private AudioAnalyzer analyzer;
	
	public void enter(AudioFile file) {
		this.file = file;
		super.enter();
	}
	
	@Override
	public void entering() {
		title = new Text("Analyzing audio...").setFont(AudioDrive.Font).setSize(48).setPosition(20, 20);
		Camera.overlay(getWidth(), getHeight());
		analyzer = new AudioAnalyzer();
		new Thread(() -> {
			Log.info("analyzing audio...");
			analyzer.analyze(file);
			Log.info("analyzation complete");
		}).start();
	}
	
	@Override
	public void update(double elapsed) {
		if (analyzer.isDone()) Scene.get(MenuScene.class).enter(analyzer.getResults());
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
