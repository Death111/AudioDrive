package audiodrive.ui.scenes;

import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.input.Keyboard;

import audiodrive.AudioDrive;
import audiodrive.audio.AnalyzedAudio;
import audiodrive.audio.AudioAnalyzer;
import audiodrive.ui.components.Camera;
import audiodrive.ui.components.Overlay;
import audiodrive.ui.components.Scene;
import audiodrive.ui.components.Text;
import audiodrive.ui.dialog.Dialog;
import audiodrive.ui.dialog.Dialog.DialogAnswer;
import audiodrive.ui.dialog.Dialog.DialogType;
import audiodrive.ui.effects.ShaderProgram;
import audiodrive.utilities.Log;

public class AnalyzationScene extends Scene {
	
	private Text title;
	private AudioAnalyzer analyzer;
	private Overlay background;
	private Dialog dialog;
	
	@Override
	public void entering() {
		title = new Text("Analyzing audio...").setFont(AudioDrive.Font).setSize(48).setPosition(20, 20);
		background = new Overlay().shader(new ShaderProgram("shaders/Default.vs", "shaders/Analyzation.fs"));
		Camera.overlay(getWidth(), getHeight());
		// limit duration, to prevent memory issues
		if (AudioDrive.getSelectedAudio().getDuration() > AudioDrive.Settings.getDouble("audio.duration.limit") * 60.0) {
			showErrorDialog();
			return;
		}
		analyzer = new AudioAnalyzer();
		Thread thread = new Thread(() -> {
			analyzer.analyze(AudioDrive.getSelectedAudio());
		});
		thread.setName("Analyzation Thread");
		thread.start();
	}
	
	@Override
	public void update(double elapsed) {
		if (dialog != null) {
			if (dialog.answer == DialogAnswer.CONFIRM) {
				Scene.get(SelectionScene.class).enter();
			}
		} else if (analyzer.isDone()) {
			AnalyzedAudio results = analyzer.getResults();
			if (results == null) {
				showErrorDialog();
				return;
			}
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
	
	private void showErrorDialog() {
		Log.error("Couldn't analyze audio file \"" + AudioDrive.getSelectedAudio() + "\".");
		dialog = new Dialog("Couldn't analyze audio file.", DialogType.CONFIRM).activate();
	}
	
	@Override
	public void render() {
		glClear(GL_COLOR_BUFFER_BIT);
		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
		background.render();
		title.render();
		if (dialog != null) dialog.render();
	}
	
	@Override
	public void exiting() {
		background = null;
		title = null;
		dialog = null;
	}
	
	@Override
	public void keyReleased(int key, char character) {
		if (dialog != null) {
			switch (key) {
			case Keyboard.KEY_ESCAPE:
			case Keyboard.KEY_RETURN:
				dialog.confirm();
				return;
			}
		}
	}
	
}
