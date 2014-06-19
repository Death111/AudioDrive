package audiodrive;

import java.io.File;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import audiodrive.audio.AudioFile;
import audiodrive.audio.AudioPlayer;
import audiodrive.model.Track;
import audiodrive.model.track.TrackGenerator;
import audiodrive.ui.Drive;
import audiodrive.ui.Show;
import audiodrive.ui.ShowModel;
import audiodrive.ui.Visualizer;
import audiodrive.utilities.Log;

public class AudioDrive extends Application {
	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void start(Stage stage) throws Exception {
		Log.info("AudioDrive");
		
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(new File("music"));
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Audiodateien (*.mp3, *.ogg, *.wav)", "*.mp3", "*.ogg", "*.wav"));
		File selected = fileChooser.showOpenDialog(stage);
		if (selected == null) {
			Platform.exit();
			return;
		}
		
		ShowModel.show("models/xwing/xwing");
		
		Log.info(selected.getName());
		AudioFile file = new AudioFile(selected);
		Log.debug(file.getFormat().getType());
		Visualizer.visualize(file);
		
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
