package audiodrive.audio;

import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class AudioPlayer {
	
	private boolean stop;

	public void play(AudioFile file) {
		stop = false;
		new Thread(() -> {
			try {
				AudioInputStream stream = new AudioDecoder().decode(file);
				rawplay(stream);
				stream.close();
			} catch (IOException | LineUnavailableException exception) {
				throw new RuntimeException(exception);
			}
		}).start();
	}
	
	public void stop() {
		stop = true;
	}

	private void rawplay(AudioInputStream stream) throws IOException, LineUnavailableException {
		SourceDataLine line = AudioSystem.getSourceDataLine(stream.getFormat());
		byte[] buffer = new byte[4096];
		line.open();
		line.start();
		while (!stop) {
			int n = stream.read(buffer, 0, buffer.length);
			if (n == -1) break;
			line.write(buffer, 0, n);
		}
		line.drain();
		line.stop();
		line.close();
	}
	
}