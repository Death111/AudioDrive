package audiodrive;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Properties;

import audiodrive.model.geometry.Color;
import audiodrive.utilities.Log;
import audiodrive.utilities.Sort;

public class Settings {
	
	public static final Settings Default = new Settings("default");
	
	private final String filename;
	private final Properties properties = new Properties();
	
	public Settings(String filename) {
		this.filename = filename;
		set("audio.analyzation.threshold", "1.8");
		set("audio.analyzation.window", "20");
		set("color.collectable", "0,0,1,1");
		set("color.collectable.static", "true");
		set("color.intense", "1,1,1,1");
		set("color.obstacle", "0.5,0.5,0.5,1");
		set("color.relaxed", "0.1,0.1,0.1,1");
		set("game.difficulty", "0.5");
		set("input.mouse.speed", "1.0");
		set("input.keyboard.speed", "1.0");
		set("interface.useBoxes", "false");
		set("interface.volume", "1.0");
		set("music.directory", "music");
		set("music.volume", "1.0");
		set("player.model", "xwing");
		set("sound.volume", "1.0");
		set("track.smoothing", "15");
		set("window.antialiasing", "true");
		set("window.supersampling", "4");
		set("window.framerate", "0");
		set("window.useSecondaryMonitor", "false");
		set("window.vsync", "true");
	}
	
	public void load() {
		Log.info("Loading settings...");
		new File("music").mkdir();
		try {
			properties.load(new FileInputStream(filename));
		} catch (IOException exception) {
			Log.warning("Couldn't find settings, using defaults.");
		}
	}
	
	public void save() {
		Log.info("Saving settings...");
		new HashSet<>(properties.entrySet()).stream().map(Entry::getKey).filter(key -> !Default.properties.containsKey(key)).forEach(properties::remove);
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
	
	public boolean contains(String key) {
		return properties.containsKey(key);
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
