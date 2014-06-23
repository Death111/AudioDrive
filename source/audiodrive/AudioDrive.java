package audiodrive;

import java.awt.Font;
import java.io.File;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import audiodrive.audio.AudioAnalyzer;
import audiodrive.audio.AudioFile;
import audiodrive.audio.AudioPlayer;
import audiodrive.model.Track;
import audiodrive.model.track.TrackGenerator;
import audiodrive.ui.Drive;
import audiodrive.ui.components.Text;
import audiodrive.ui.components.Window;
import audiodrive.utilities.Log;

public class AudioDrive extends Application {
	
	public static final String Title = "AudioDrive";
	public static final Font Font = Text.getFont("Shojumaru");
	
	private static Stage stage;
	
	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void start(Stage stage) throws Exception {
		Log.info("AudioDrive");
		AudioDrive.stage = stage;
		
		// Window.setBorderless(true);
		// Window.setFullscreen(true);
		// Window.open(Scene.get(TitleScene.class));
		
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(new File("music"));
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Audiodateien (*.mp3, *.ogg, *.wav)", "*.mp3", "*.ogg", "*.wav"));
		File selected = fileChooser.showOpenDialog(stage);
		if (selected == null) {
			Platform.exit();
			return;
		}
		AudioFile file = new AudioFile(selected);
		AudioAnalyzer analyzer = new AudioAnalyzer();
		analyzer.analyze(file);
		TrackGenerator trackGenerator = new TrackGenerator();
		Track track = trackGenerator.generate(analyzer.getResults(), 25);
		AudioPlayer player = new AudioPlayer();
		// Show.track(track);
		player.play(file);
		Drive.track(track);
		player.stop();
		
		Platform.exit();
	}
	
	public static void exit() {
		Window.close();
	}
	
	public static Stage getStage() {
		return stage;
	}
}
