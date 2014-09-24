package audiodrive.utilities;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;

import audiodrive.Resources;

public class Natives {
	
	private static String TempDirectory = System.getProperty("java.io.tmpdir");
	private static File Directory = new File(TempDirectory, "AudioDrive");
	
	static {
		Directory.mkdir();
		Arrays.stream(Directory.listFiles()).forEach(File::delete);
	}
	
	/** Private constructor to prevent instantiation. */
	private Natives() {
		throw new IllegalStateException("This class shall not be instantiated.");
	}
	
	public static void load() {
		Log.debug("Extracting native libraries...");
		if (Resources.getClasspath().endsWith(".jar")) {
			Resources.list().stream().filter(path -> path.endsWith(".dll")).forEach(Natives::extract);
			System.setProperty("org.lwjgl.librarypath", Directory.getAbsolutePath());
		} else {
			System.setProperty("org.lwjgl.librarypath", Resources.getClasspath());
		}
	}
	
	public static void extract(String resource) {
		Log.trace("Extracting native library \"" + resource + "\" to temporary directory.");
		InputStream source = Resources.stream(resource);
		String name = Resources.getName(resource);
		String type = Resources.getType(resource);
		try {
			File.createTempFile(name, "." + type, Directory.getCanonicalFile());
			File file = new File(Directory, name + "." + type);
			file.deleteOnExit();
			Files.copy(source, file.toPath());
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}
	
}