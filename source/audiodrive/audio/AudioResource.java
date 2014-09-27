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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		AudioResource other = (AudioResource) obj;
		if (url == null) {
			if (other.url != null) return false;
		} else if (!url.equals(other.url)) return false;
		return true;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
}
