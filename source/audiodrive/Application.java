package audiodrive;

import org.apache.log4j.Logger;

import audiodrive.audio.AudioFile;
import audiodrive.audio.AudioPlayer;
import audiodrive.model.Track;
import audiodrive.model.track.TrackGenerator;
import audiodrive.ui.Drive;
import audiodrive.ui.Show;

public class Application {

	private static Logger logger = Logger.getLogger(Application.class);

	public static void main(String[] args) {
		AudioFile file = new AudioFile("music/a.mp3");
		logger.debug(file.getFormat().getType());
		TrackGenerator trackGenerator = new TrackGenerator();
		Track track = trackGenerator.generate(file, 15);
		AudioPlayer player = new AudioPlayer();

		Show.track(track);
		player.play(file);
		Drive.track(track);
		player.stop();
	}

}
