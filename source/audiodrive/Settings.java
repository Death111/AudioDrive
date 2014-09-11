package audiodrive;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import audiodrive.utilities.Log;

public class Settings {
	
	private final String filename;
	private final Properties properties = new Properties();
	
	public Settings(String filename) {
		this.filename = filename;
	}
	
	public void load() {
		Log.info("Loading settings...");
		try {
			properties.load(new FileInputStream(filename));
		} catch (IOException exception) {
			Log.info("Creating default settings...");
			File directory = new File("music");
			directory.mkdir();
			set("directory", directory.getPath());
			set("model", "xwing");
		}
	}
	
	public void save() {
		Log.info("Saving settings...");
		try {
			properties.store(new FileOutputStream(filename), null);
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}
	
	public void set(String key, String value) {
		properties.setProperty(key, value);
	}
	
	public String get(String key) {
		return properties.getProperty(key);
	}
	
	public double getDouble(String key) {
		return Double.parseDouble(properties.getProperty(key));
	}
	
}
