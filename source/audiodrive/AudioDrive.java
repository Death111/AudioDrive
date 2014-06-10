package audiodrive;

import java.io.File;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import org.apache.log4j.Logger;

import audiodrive.audio.AudioFile;
import audiodrive.audio.AudioPlayer;
import audiodrive.model.Track;
import audiodrive.model.track.TrackGenerator;
import audiodrive.ui.Drive;
import audiodrive.ui.Show;
import audiodrive.ui.ShowModel;

public class AudioDrive extends Application {
	
	private static Logger logger = Logger.getLogger(AudioDrive.class);
	
	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void start(Stage stage) throws Exception {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(new File("music"));
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Audiodateien (*.mp3, *.ogg, *.wav)", "*.mp3", "*.ogg", "*.wav"));
		File selected = fileChooser.showOpenDialog(stage);
		if (selected == null) {
			Platform.exit();
			return;
		}

		ShowModel.show("models/xwing/xwing.obj");
		
		AudioFile file = new AudioFile(selected);
		logger.debug(file.getFormat().getType());
		TrackGenerator trackGenerator = new TrackGenerator();
		Track track = trackGenerator.generate(file, 25);
		AudioPlayer player = new AudioPlayer();
		Show.track(track);
		player.play(file);
		Drive.track(track);
		player.stop();
		
		Platform.exit();
	}
}
