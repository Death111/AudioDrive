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
		set("audio.duration.limit", "15");
		set("block.collectable.color", "1,1,1,1");
		set("block.collectable.color.static", "true");
		set("block.collectable.glowing", "true");
		set("block.obstacle.color", ".4,.4,.4,1");
		set("block.obstacle.color.static", "false");
		set("block.obstacle.glowing", "false");
		set("color.intense", "1,0,0,1");
		set("color.average", "0,0.6,0,1");
		set("color.relaxed", "0,0,1,1");
		set("game.environment", "true");
		set("game.visualization", "true");
		set("game.night", "true");
		set("game.sky", "true");
		set("game.sight", "200");
		set("game.difficulty", "0.9");
		set("graphics.glow", "true");
		set("graphics.particles", "true");
		set("graphics.particles.2d.count", "20");
		set("graphics.particles.2d.lifetime", "1.0");
		set("graphics.particles.2d.scale", "1.0");
		set("graphics.particles.2d.velocity", "1.0");
		set("graphics.particles.3d.count", "3000");
		set("graphics.particles.3d.lifetime", "1.0");
		set("graphics.particles.3d.scale", "1.0");
		set("graphics.particles.3d.velocity", "1.0");
		set("graphics.reflections", "true");
		set("input.mouse.speed", "1.0");
		set("input.keyboard.speed", "1.0");
		set("interface.useBoxes", "false");
		set("interface.volume", "1.0");
		set("music.directory", "music");
		set("music.volume", "1.0");
		set("player.model", "lightjet");
		set("sound.volume", "1.0");
		set("track.smoothing", "20");
		set("window.antialiasing", "true");
		set("window.multisampling", "2");
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
