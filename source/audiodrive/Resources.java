package audiodrive;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import audiodrive.audio.AudioFile;

public class Resources {
	
	/** Private constructor to prevent instantiation. */
	private Resources() {
		throw new IllegalStateException("This class shall not be instantiated.");
	}
	
	public static URI get(String name) {
		try {
			URL url = ClassLoader.getSystemResource(name);
			if (url == null) throw new RuntimeException("Can't find resource \"" + name + "\".");
			return url.toURI();
		} catch (URISyntaxException exception) {
			throw new RuntimeException(exception);
		}
	}
	
	public static File getFile(String name) {
		File file = new File(name);
		if (!file.exists()) {
			file = new File(get(name));
		}
		return file;
	}
	
	public static AudioFile getAudioFile(String name) {
		return new AudioFile(getFile(name));
	}
	
}
