package audiodrive.audio;

import java.io.File;
import java.net.URL;

import javax.sound.sampled.AudioInputStream;

import audiodrive.Resources;

public class AudioResource {
	
	private URL url;
	private String path;
	private String name;
	
	public AudioResource(URL url) {
		this.url = url;
		path = Resources.getPath(url);
		name = path.substring(path.lastIndexOf("/") + 1);
	}
	
	public AudioResource(String name) {
		this(Resources.get(name));
	}
	
	public AudioResource(File file) {
		this(Resources.get(file));
	}
	
	public String getName() {
		return name;
	}
	
	public String getPath() {
		return path;
	}
	
	public URL getUrl() {
		return url;
	}
	
	/**
	 * Opens an audio input stream on the file.
	 */
	public AudioInputStream open() {
		return AudioDecoder.stream(this);
	}
	
	public Playback play(double volume) {
		if (volume == 1.0) return play();
		return new Playback(this).setVolume(volume).start();
	}
	
	public Playback play() {
		return new Playback(this).start();
	}
	
	@Override
	public String toString() {
		return name;
	}
	
}
