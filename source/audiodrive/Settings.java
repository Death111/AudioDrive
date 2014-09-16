package audiodrive;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import audiodrive.model.geometry.Color;
import audiodrive.utilities.Log;
import audiodrive.utilities.Sort;

public class Settings {
	
	private final String filename;
	private final Properties properties = new Properties();
	
	public Settings(String filename) {
		this.filename = filename;
	}
	
	public void load() {
		Log.info("Loading settings...");
		new File("music").mkdir();
		set("directory", "music");
		set("model", "xwing");
		set("useSecondaryMonitor", "false");
		set("useItemBoxes", "false");
		set("risingBorderColor", "0.1,0.1,0.1,1");
		set("fallingBorderColor", "1,1,1,1");
		set("collectableColor", "0,0,1,1");
		set("obstacleColor", "0.5,0.5,0.5,1");
		set("difficulty", "0.5");
		set("smoothing", "15");
		set("analyzationWindow", "20");
		set("analyzationThreshold", "1.8");
		set("staticObstacleColor", "true");
		set("framerate", "0");
		set("vsync", "true");
		set("mouseSpeed", "1.0");
		set("playerSpeed", "1.0");
		try {
			properties.load(new FileInputStream(filename));
		} catch (IOException exception) {
			Log.warning("Couldn't find settings, using defaults.");
		}
	}
	
	public void save() {
		Log.info("Saving settings...");
		try {
			Sort.properties(properties).store(new FileOutputStream(filename), null);
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
	
	public int getInteger(String key) {
		return Integer.parseInt(get(key));
	}
	
	public double getDouble(String key) {
		return Double.parseDouble(get(key));
	}
	
	public boolean getBoolean(String key) {
		return Boolean.parseBoolean(get(key));
	}
	
	public Color getColor(String key) {
		return Color.parse(get(key));
	}
	
}
