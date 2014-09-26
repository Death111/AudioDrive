package audiodrive.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import audiodrive.Resources;

public class Versioning {
	
	private static String version = "Unspecified";
	
	/** Private constructor to prevent instantiation. */
	private Versioning() {
		throw new IllegalStateException("This class shall not be instantiated.");
	}
	
	public static String getVersion() {
		if (version != null) return version;
		if (new File(".git").exists()) {
			updateVersion();
			store();
		} else {
			read();
		}
		return version;
	}
	
	private static void store() {
		try (FileWriter writer = new FileWriter(new File("source/version"))) {
			writer.write(version);
		} catch (IOException exception) {
			Log.warning("Couldn't create version file.");
		}
	}
	
	private static void read() {
		InputStream stream = Resources.stream("version");
		if (stream != null) try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
			version = reader.readLine();
			return;
		} catch (IOException exception) {}
		Log.warning("Couldn't find version file.");
	}
	
	public static void updateVersion() {
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
	
}
