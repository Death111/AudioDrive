package audiodrive.utilities;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class Format {
	
	/** Private constructor to prevent instantiation. */
	private Format() {
		throw new IllegalStateException("This class shall not be instantiated.");
	}
	
	public static String seconds(double seconds) {
		int min = (int) (seconds / 60);
		int sec = (int) (seconds - min * 60);
		long ms = Math.round((seconds - min * 60 - sec) * 1000);
		return String.format("%02d:%02d:%03d", min, sec, ms);
	}
	
	public static URL url(File file) {
		try {
			return file.toURI().toURL();
		} catch (MalformedURLException exception) {
			throw new RuntimeException(exception);
		}
	}
	
}
