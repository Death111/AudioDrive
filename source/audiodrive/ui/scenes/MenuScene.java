package audiodrive.ui.scenes;

import static org.lwjgl.opengl.GL11.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import org.lwjgl.input.Keyboard;

import audiodrive.AudioDrive;
import audiodrive.audio.AnalyzedAudio;
import audiodrive.ui.components.Camera;
import audiodrive.ui.components.Scene;
import audiodrive.ui.components.Text;
import audiodrive.utilities.Log;

public class MenuScene extends Scene {
	
	private Text title;
	private Text filename;
	private List<Text> options;
	private AnalyzedAudio audio;
	
	public void enter(AnalyzedAudio audio) {
		this.audio = audio;
		super.enter();
	}
	
	@Override
	public void entering() {
		Log.info("menu");
		title = new Text("Menu").setFont(AudioDrive.Font).setSize(48).setPosition(20, 20);
		filename = new Text("Audiofile: " + audio.getFile().getName()).setFont(AudioDrive.Font).setSize(25).setPosition(50, 100);
		IntegerProperty y = new SimpleIntegerProperty(300);
		options = Stream.of("Visualize (v)", "Play (p)", "Select Audio (a)", "Select Model (m)", "Exit (esc)").map(Text::new).peek(text -> {
			text.setFont(AudioDrive.Font).setSize(25).setPosition(50, y.get());
			y.set(y.get() + 75);
		}).collect(Collectors.toList());
		Camera.overlay(getWidth(), getHeight());
	}
	
	@Override
	public void update(double elapsed) {}
	
	@Override
	public void render() {
		glClear(GL_COLOR_BUFFER_BIT);
		title.render();
		filename.render();
		options.forEach(Text::render);
	}
	
	@Override
	public void exiting() {
		title = null;
	}
	
	@Override
	public void keyReleased(int key, char character) {
		switch (key) {
		case Keyboard.KEY_V:
			Scene.get(VisualizerScene.class).enter(audio);
			break;
		case Keyboard.KEY_P:
			Scene.get(GameScene.class).enter();
			break;
		case Keyboard.KEY_A:
			Scene.get(AudioSelectionScene.class).enter();
			break;
		case Keyboard.KEY_M:
			Scene.get(ModelViewerScene.class).enter();
			break;
		case Keyboard.KEY_ESCAPE:
			exit();
			break;
		default:
			break;
		}
	}
	
}
