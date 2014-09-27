package audiodrive.ui.scenes;

import static org.lwjgl.opengl.GL11.*;

import java.util.Arrays;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

import audiodrive.AudioDrive;
import audiodrive.audio.AudioResource;
import audiodrive.ui.components.Camera;
import audiodrive.ui.components.Overlay;
import audiodrive.ui.components.Scene;
import audiodrive.ui.components.Text;
import audiodrive.ui.components.Text.Alignment;
import audiodrive.ui.components.Window;
import audiodrive.ui.control.Input;
import audiodrive.ui.effects.ShaderProgram;
import audiodrive.ui.menu.Menu;
import audiodrive.ui.menu.item.Item;
import audiodrive.ui.menu.item.ItemListener;
import audiodrive.ui.menu.item.MenuItem;
import audiodrive.ui.menu.item.SettingsItem;
import audiodrive.utilities.Log;

/**
 * 
 * @author Death
 *
 */
public class SettingsScene extends Scene implements ItemListener {
	
	private static final List<Boolean> booleanValues = Arrays.asList(false, true);
	private static final List<Integer> multisamplingValues = Arrays.asList(0, 2, 4, 8);
	private static final List<Integer> sightValues = Arrays.asList(50, 100, 150, 200, 250, 300);
	private static final List<Double> difficultyValues = Arrays.asList(.1, .2, .3, .4, .5, .6, .7, .8, .9, 1.);
	private static final List<Double> controlValues = Arrays.asList(.5, .6, .7, .8, .9, 1., 1.2, 1.4, 1.6, 1.8, 2.0);
	private static final List<Double> volumeValues = Arrays.asList(0., .1, .2, .3, .4, .5, .6, .7, .8, .9, 1.);
	
	private Menu graphicMenu;
	private Menu soundMenu;
	private Menu inputMenu;
	private Menu gameMenu;
	private SettingsItem<Boolean> antialiasing;
	private SettingsItem<Integer> multisampling;
	private SettingsItem<Boolean> vSync;
	private SettingsItem<Boolean> glow;
	private SettingsItem<Boolean> particles;
	private SettingsItem<Boolean> reflections;
	private SettingsItem<Boolean> environment;
	private SettingsItem<Boolean> visualization;
	private SettingsItem<Integer> sight;
	private SettingsItem<Boolean> night;
	private SettingsItem<Boolean> sky;
	private SettingsItem<Boolean> staticCollectableColor;
	private SettingsItem<Boolean> glowingCollectables;
	private SettingsItem<Boolean> staticObstacleColor;
	private SettingsItem<Boolean> glowingObstacles;
	private SettingsItem<Double> difficulty;
	private SettingsItem<Double> keyboard;
	private SettingsItem<Double> mouse;
	private SettingsItem<Double> interfaceVolume;
	private SettingsItem<Double> musicVolume;
	private SettingsItem<Double> soundVolume;
	
	private Menu saveMenu;
	private MenuItem saveItem;
	private MenuItem closeItem;
	
	private Text titleText;
	private Text graphicSettingsText;
	private Text soundSettingsText;
	private Text inputSettingsText;
	private Text gameSettingsText;
	private Overlay background;
	private AudioResource selectAudio;
	
	private double volume;
	
	@Override
	public void entering() {
		titleText = new Text("Settings").setFont(AudioDrive.Font).setSize(48).setPosition(20, 20);
		
		int height = Display.getHeight() * 2 / 3;
		int y = (Display.getHeight() - height) / 2 + titleText.getHeight() / 2;
		int itemHeight = height / 15 - 1;
		int headingSize = itemHeight - 5;
		int width = (Display.getWidth() - 3 * itemHeight) / 2;
		int graphicMenuY = y;
		graphicSettingsText = new Text("Graphic").setFont(AudioDrive.Font).setSize(headingSize).setAlignment(Alignment.LowerLeft).setPosition(itemHeight, graphicMenuY);
		graphicMenu = new Menu(itemHeight, graphicMenuY, width + 1, height, 1);
		int soundMenuY = y;
		soundSettingsText = new Text("Sound")
			.setFont(AudioDrive.Font)
			.setSize(headingSize)
			.setAlignment(Alignment.LowerLeft)
			.setPosition(itemHeight + itemHeight + width, soundMenuY);
		soundMenu = new Menu(itemHeight + itemHeight + width, soundMenuY, width + 1, height / 3, 1);
		int inputMenuY = y + (itemHeight + 1) * 5;
		inputSettingsText = new Text("Input")
			.setFont(AudioDrive.Font)
			.setSize(headingSize)
			.setAlignment(Alignment.LowerLeft)
			.setPosition(itemHeight + itemHeight + width, inputMenuY);
		inputMenu = new Menu(itemHeight + itemHeight + width, inputMenuY, width + 1, height / 3, 1);
		int gameMenuY = y + (itemHeight + 1) * 14;
		gameSettingsText = new Text("Game").setFont(AudioDrive.Font).setSize(headingSize).setAlignment(Alignment.LowerLeft).setPosition(itemHeight + itemHeight + width, gameMenuY);
		gameMenu = new Menu(itemHeight + itemHeight + width, gameMenuY, width + 1, height / 3, 1);
		
		saveMenu = new Menu(20, Display.getHeight() - MenuItem.MENU_ITEM_HEIGHT - 20, Display.getWidth() - itemHeight, MenuItem.MENU_ITEM_HEIGHT + 1, 1);
		
		saveItem = new MenuItem("Save", this);
		closeItem = new MenuItem("Return", this);
		
		saveMenu.addItem(saveItem);
		saveMenu.addItem(closeItem);
		
		antialiasing = new SettingsItem<Boolean>("Anti-Aliasing", booleanValues, width, itemHeight);
		multisampling = new SettingsItem<Integer>("Multisampling", multisamplingValues, width, itemHeight);
		vSync = new SettingsItem<Boolean>("V-Sync", booleanValues, width, itemHeight);
		glow = new SettingsItem<Boolean>("Glow", booleanValues, width, itemHeight);
		particles = new SettingsItem<Boolean>("Particles", booleanValues, width, itemHeight);
		reflections = new SettingsItem<Boolean>("Reflections", booleanValues, width, itemHeight);
		environment = new SettingsItem<Boolean>("Environment", booleanValues, width, itemHeight);
		visualization = new SettingsItem<Boolean>("Visualization", booleanValues, width, itemHeight);
		sight = new SettingsItem<Integer>("Sight Range", sightValues, width, itemHeight);
		night = new SettingsItem<Boolean>("Night", booleanValues, width, itemHeight);
		sky = new SettingsItem<Boolean>("Sky", booleanValues, width, itemHeight);
		staticCollectableColor = new SettingsItem<Boolean>("Static Collectable Color", booleanValues, width, itemHeight);
		glowingCollectables = new SettingsItem<Boolean>("Glowing Collectables", booleanValues, width, itemHeight);
		staticObstacleColor = new SettingsItem<Boolean>("Static Obstacle Color", booleanValues, width, itemHeight);
		glowingObstacles = new SettingsItem<Boolean>("Glowing Obstacles", booleanValues, width, itemHeight);
		difficulty = new SettingsItem<Double>("Difficulty", difficultyValues, width, itemHeight);
		keyboard = new SettingsItem<Double>("Keyboard Sensitivity", controlValues, width, itemHeight);
		mouse = new SettingsItem<Double>("Mouse Sensitivity", controlValues, width, itemHeight);
		interfaceVolume = new SettingsItem<Double>("Interface Volume", volumeValues, width, itemHeight);
		musicVolume = new SettingsItem<Double>("Music Volume", volumeValues, width, itemHeight);
		soundVolume = new SettingsItem<Double>("Sound Volume", volumeValues, width, itemHeight);
		
		graphicMenu.addItem(antialiasing);
		graphicMenu.addItem(multisampling);
		graphicMenu.addItem(vSync);
		graphicMenu.addItem(glow);
		graphicMenu.addItem(particles);
		graphicMenu.addItem(reflections);
		graphicMenu.addItem(visualization);
		graphicMenu.addItem(environment);
		graphicMenu.addItem(sky);
		graphicMenu.addItem(night);
		graphicMenu.addItem(staticCollectableColor);
		graphicMenu.addItem(glowingCollectables);
		graphicMenu.addItem(staticObstacleColor);
		graphicMenu.addItem(glowingObstacles);
		graphicMenu.addItem(sight);
		soundMenu.addItem(interfaceVolume);
		soundMenu.addItem(musicVolume);
		soundMenu.addItem(soundVolume);
		inputMenu.addItem(keyboard);
		inputMenu.addItem(mouse);
		gameMenu.addItem(difficulty);
		
		selectAudio = new AudioResource("sounds/Select.mp3");
		volume = AudioDrive.Settings.getDouble("interface.volume");
		
		background = new Overlay().shader(new ShaderProgram("shaders/default.vs", "shaders/title.fs"));
		updateSettings();
		Camera.overlay(getWidth(), getHeight());
		Input.addObservers(saveMenu, graphicMenu, soundMenu, inputMenu, gameMenu);
	}
	
	private void updateSettings() {
		antialiasing.setValue(AudioDrive.Settings.getBoolean("window.antialiasing"));
		multisampling.setValue(AudioDrive.Settings.getInteger("window.multisampling"));
		vSync.setValue(AudioDrive.Settings.getBoolean("window.vsync"));
		glow.setValue(AudioDrive.Settings.getBoolean("graphics.glow"));
		particles.setValue(AudioDrive.Settings.getBoolean("graphics.particles"));
		reflections.setValue(AudioDrive.Settings.getBoolean("graphics.reflections"));
		environment.setValue(AudioDrive.Settings.getBoolean("game.environment"));
		visualization.setValue(AudioDrive.Settings.getBoolean("game.visualization"));
		sight.setValue(AudioDrive.Settings.getInteger("game.sight"));
		night.setValue(AudioDrive.Settings.getBoolean("game.night"));
		sky.setValue(AudioDrive.Settings.getBoolean("game.sky"));
		difficulty.setValue(AudioDrive.Settings.getDouble("game.difficulty"));
		staticCollectableColor.setValue(AudioDrive.Settings.getBoolean("block.collectable.color.static"));
		glowingCollectables.setValue(AudioDrive.Settings.getBoolean("block.collectable.glowing"));
		staticObstacleColor.setValue(AudioDrive.Settings.getBoolean("block.obstacle.color.static"));
		glowingObstacles.setValue(AudioDrive.Settings.getBoolean("block.obstacle.glowing"));
		keyboard.setValue(AudioDrive.Settings.getDouble("input.keyboard.speed"));
		mouse.setValue(AudioDrive.Settings.getDouble("input.mouse.speed"));
		interfaceVolume.setValue(AudioDrive.Settings.getDouble("interface.volume"));
		musicVolume.setValue(AudioDrive.Settings.getDouble("music.volume"));
		soundVolume.setValue(AudioDrive.Settings.getDouble("sound.volume"));
	}
	
	@Override
	public void render() {
		glClear(GL_COLOR_BUFFER_BIT);
		background.render();
		titleText.render();
		graphicSettingsText.render();
		graphicMenu.render();
		soundSettingsText.render();
		soundMenu.render();
		inputSettingsText.render();
		inputMenu.render();
		gameSettingsText.render();
		gameMenu.render();
		saveMenu.render();
	}
	
	@Override
	public void exiting() {
		Input.removeObservers(saveMenu, graphicMenu, soundMenu, inputMenu, gameMenu);
	}
	
	@Override
	public void keyReleased(int key, char character) {
		super.keyReleased(key, character);
		Log.trace("Key '" + character + "' was realeased,");
		switch (key) {
		case Keyboard.KEY_ESCAPE:
			back();
			break;
		default:
			break;
		}
	}
	
	@Override
	public void onHover(Item item, boolean hover) {}
	
	@Override
	public void onSelect(Item item, boolean select) {
		selectAudio.play(volume);
		if (item == saveItem) {
			saveSettings();
			back();
		} else if (item == closeItem) {
			back();
		}
	}
	
	private void saveSettings() {
		AudioDrive.Settings.set("window.antialiasing", antialiasing.valueAsString());
		AudioDrive.Settings.set("window.multisampling", multisampling.valueAsString());
		AudioDrive.Settings.set("window.vsync", vSync.valueAsString());
		AudioDrive.Settings.set("graphics.glow", glow.valueAsString());
		AudioDrive.Settings.set("graphics.particles", particles.valueAsString());
		AudioDrive.Settings.set("graphics.reflections", reflections.valueAsString());
		AudioDrive.Settings.set("game.environment", environment.valueAsString());
		AudioDrive.Settings.set("game.visualization", visualization.valueAsString());
		AudioDrive.Settings.set("game.sight", sight.valueAsString());
		AudioDrive.Settings.set("game.night", night.valueAsString());
		AudioDrive.Settings.set("game.sky", sky.valueAsString());
		AudioDrive.Settings.set("game.difficulty", difficulty.valueAsString());
		AudioDrive.Settings.set("block.collectable.color.static", staticCollectableColor.valueAsString());
		AudioDrive.Settings.set("block.collectable.glowing", glowingCollectables.valueAsString());
		AudioDrive.Settings.set("block.obstacle.color.static", staticObstacleColor.valueAsString());
		AudioDrive.Settings.set("block.obstacle.glowing", glowingObstacles.valueAsString());
		AudioDrive.Settings.set("input.keyboard.speed", keyboard.valueAsString());
		AudioDrive.Settings.set("input.mouse.speed", mouse.valueAsString());
		AudioDrive.Settings.set("interface.volume", interfaceVolume.valueAsString());
		AudioDrive.Settings.set("music.volume", musicVolume.valueAsString());
		AudioDrive.Settings.set("sound.volume", soundVolume.valueAsString());
		AudioDrive.Settings.save();
		applySettings();
	}
	
	private void applySettings() {
		Window.setAntialiasingEnabled(AudioDrive.Settings.getBoolean("window.antialiasing"));
		Window.setVSyncEnabled(AudioDrive.Settings.getBoolean("window.vsync"));
		int muiltisampling = AudioDrive.Settings.getInteger("window.multisampling");
		if (muiltisampling != Window.getPixelFormat().getSamples()) Window.setMultisampling(muiltisampling);
	}
	
}
