package audiodrive;

import audiodrive.audio.AudioFile;
import audiodrive.audio.AudioPlayer;
import audiodrive.model.Track;
import audiodrive.model.track.TrackGenerator;
import audiodrive.ui.Drive;
import audiodrive.ui.Show;

public class Application {
	
	public static void main(String[] args) {
		AudioFile file = new AudioFile("music/stereotest2.mp3");
		System.out.println(file.getFormat().getType());
		TrackGenerator trackGenerator = new TrackGenerator();
		Track track = trackGenerator.generate(file, 15);
		AudioPlayer player = new AudioPlayer();
		
		Show.track(track);
		player.play(file);
		Drive.track(track);
		player.stop();
	}
	
}
