package audiodrive.ui.scenes;

import static org.lwjgl.opengl.GL11.*;
import audiodrive.AudioDrive;
import audiodrive.audio.AnalyzedAudio;
import audiodrive.audio.AudioAnalyzer;
import audiodrive.ui.components.Camera;
import audiodrive.ui.components.Overlay;
import audiodrive.ui.components.Scene;
import audiodrive.ui.components.Text;
import audiodrive.ui.effects.ShaderProgram;
import audiodrive.utilities.Log;

public class AnalyzationScene extends Scene {
	
	private Text title;
	private AudioAnalyzer analyzer;
	private Overlay background;
	
	@Override
	public void entering() {
		title = new Text("Analyzing audio...").setFont(AudioDrive.Font).setSize(48).setPosition(20, 20);
		background = new Overlay().shader(new ShaderProgram("shaders/Default.vs", "shaders/Analyzation.fs"));
		Camera.overlay(getWidth(), getHeight());
		analyzer = new AudioAnalyzer();
		Thread thread = new Thread(() -> {
			analyzer.analyze(AudioDrive.getSelectedAudio());
		});
		thread.setName("Analyzation Thread");
		thread.start();
	}
	
	@Override
	public void update(double elapsed) {
		if (analyzer.isDone()) {
			AnalyzedAudio results = analyzer.getResults();
			if (results == null) Log.error("Couldn't analyze audio file \"" + AudioDrive.getSelectedAudio() + "\".");
			AudioDrive.setAnalyzedAudio(results);
			switch (AudioDrive.getAction()) {
			case None:
				Scene.get(MenuScene.class).enter();
				break;
			case Play:
				Scene.get(GenerationScene.class).enter();
				break;
			case Visualize:
				Scene.get(VisualizationScene.class).enter();
				break;
			}
		}
	}
	
	@Override
	public void render() {
		glClear(GL_COLOR_BUFFER_BIT);
		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
		background.render();
		title.render();
	}
	
	@Override
	public void exiting() {
		background = null;
		title = null;
	}
	
}
