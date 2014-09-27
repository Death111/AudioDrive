package audiodrive.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * <b>Provides automatic versioning when using a git repository with tags.</b><br>
 * <br>
 * The version will be updated when {@linkplain #getVersion()} is called by the program from within the git repository's working directory. This requires either a stand-alone
 * <b>git</b> version or <b>SourceTree</b> with embedded git.<br>
 * <br>
 * The version string will be obtained using "<i>git describe</i>" and therefore consist of: <br>
 * "<i>{latest tag}</i>" if the latest commit is tagged, or <br>
 * "<i>{latest tag}-[number of commits since latest tag}-g[latest commit hash}</i>" if there are commits after the latest tag.<br>
 * <br>
 * After obtaining the version it will be stored with a "<i>version</i>" file inside the source directory. This file should then be included into the executable jar, from where it
 * will be read once on every program start.<br>
 * <br>
 * If there are no tags present in the git repository, the version will be "<i>Unspecified</i>" and no version file will be created.
 */
public class Versioning {
	
	private static final String Unspecified = "Unspecified";
	
	private static String version;
	
	/** Private constructor to prevent instantiation. */
	private Versioning() {
		throw new IllegalStateException("This class shall not be instantiated.");
	}
	
	/**
	 * Returns the current version as returned by the git repository using "<i>git describe</i>".
	 * 
	 * @return the current program version
	 * @see Versioning
	 */
	public static String getVersion() {
		if (version != null) return version;
		if (gitDirectory().exists()) {
			update();
			write();
		} else {
			read();
		}
		if (version == null) {
			version = Unspecified;
			versionFile().delete();
		}
		return version;
	}
	
	private static void write() {
		if (version == null) return;
		try (FileWriter writer = new FileWriter(versionFile())) {
			writer.write(version);
		} catch (IOException exception) {
			Log.warning("Couldn't create version file.");
		}
	}
	
	private static void read() {
		InputStream stream = ClassLoader.getSystemResourceAsStream("version");
		if (stream != null) try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
			version = reader.readLine();
			return;
		} catch (IOException exception) {}
		Log.warning("Couldn't find version file.");
	}
	
	private static void update() {
		String command = "git.exe describe";
		try {
			version = execute(command);
		} catch (Exception exception) {
			try {
				List<String> gitPath = Arrays.asList(System.getenv("LOCALAPPDATA"), "Atlassian", "SourceTree", "git_local", "bin");
				command = gitPath.stream().collect(StringBuilder::new, (builder, string) -> builder.append(string).append(File.separator), StringBuilder::append) + command;
				version = execute(command);
			} catch (Exception exception2) {
				Log.warning("Couldn't update version.");
			}
		}
	}
	
	private static String execute(String command) {
		try {
			Process process = Runtime.getRuntime().exec(command);
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				return reader.readLine();
			}
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}
	
	private static File gitDirectory() {
		return new File(".git");
	}
	
	private static File versionFile() {
		List<String> possibilities = Arrays.asList("source", "src");
		Optional<File> sourceDirectory = possibilities.stream().map(File::new).filter(File::exists).findAny();
		if (!sourceDirectory.isPresent()) {
			throw new RuntimeException("Couldn't find source directory.");
		}
		return new File(sourceDirectory.get().getName() + File.separator + "version");
	}
	
}
