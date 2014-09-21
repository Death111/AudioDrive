package audiodrive.ui.scenes;

import static org.lwjgl.opengl.GL11.*;

import java.util.Arrays;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

import audiodrive.AudioDrive;
import audiodrive.audio.AudioFile;
import audiodrive.ui.components.Camera;
import audiodrive.ui.components.Overlay;
import audiodrive.ui.components.Scene;
import audiodrive.ui.components.Text;
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
	
	private static final List<Boolean> booleanValues = Arrays.asList(true, false);
	private static final List<Integer> superSamplingValues = Arrays.asList(0, 2, 4, 8, 16);
	private static final List<Double> difficultyValues = Arrays.asList(.1, .2, .3, .4, .5, .6, .7, .8, .9, 1.);
	private static final List<Double> controlValues = Arrays.asList(.5, .6, .7, .8, .9, 1., 1.2, 1.4, 1.6, 1.8, 2.0);
	private static final List<Double> volumeValues = Arrays.asList(0., .1, .2, .3, .4, .5, .6, .7, .8, .9, 1.);
	
	private Menu graphicMenu;
	private Menu volumeMenu;
	private Menu inputMenu;
	private SettingsItem<Boolean> antialiasing;
	private SettingsItem<Integer> multisampling;
	private SettingsItem<Boolean> vSync;
	private SettingsItem<Boolean> glow;
	private SettingsItem<Boolean> particles;
	private SettingsItem<Boolean> reflections;
	private SettingsItem<Boolean> environment;
	private SettingsItem<Boolean> visualization;
	private SettingsItem<Boolean> staticCollectableColor;
	private SettingsItem<Boolean> glowingCollectables;
	private SettingsItem<Boolean> staticObstacleColor;
	private SettingsItem<Boolean> glowingObstacles;
	private SettingsItem<Double> difficulty;
	private SettingsItem<Double> keyboard;
	private SettingsItem<Double> mouse;
	private SettingsItem<Double> interfaceVolume;
	private SettingsItem<Double> audioVolume;
	private SettingsItem<Double> soundVolume;
	
	private Menu saveMenu;
	private MenuItem saveItem;
	private MenuItem closeItem;
	
	private Text titleText;
	private Text graphicSettingsText;
	private Text soundSettingsText;
	private Text inputSettingsText;
	private Overlay background;
	private AudioFile selectAudio;
	
	private double volume;
	
	@Override
	public void entering() {
		titleText = new Text("Settings").setFont(AudioDrive.Font).setSize(48).setPosition(20, 20);
		
		int height = Display.getHeight() * 2 / 3;
		int y = (Display.getHeight() - height) / 2;
		final int spacing = 50;
		int itemHeight = spacing;
		int width = (Display.getWidth() - 3 * spacing) / 2;
		graphicMenu = new Menu(spacing, y, width + 1, height, 1);
		graphicSettingsText = new Text("Graphic").setFont(AudioDrive.Font).setSize(48).setPosition(spacing, y - itemHeight * 1.5);
		volumeMenu = new Menu(spacing + spacing + width, y, width + 1, height / 3, 1);
		soundSettingsText = new Text("Sound").setFont(AudioDrive.Font).setSize(48).setPosition(spacing + spacing + width, y - itemHeight * 1.5);
		inputMenu = new Menu(spacing + spacing + width, y + height / 3 + (int) (spacing * 1.5), width + 1, height / 3, 1);
		inputSettingsText = new Text("Input").setFont(AudioDrive.Font).setSize(48).setPosition(spacing + spacing + width, y + height / 3);
		
		saveMenu = new Menu(20, Display.getHeight() - MenuItem.MENU_ITEM_HEIGHT - 20, Display.getWidth() - spacing, MenuItem.MENU_ITEM_HEIGHT + 1, 1);
		
		saveItem = new MenuItem("Save", this);
		closeItem = new MenuItem("Return", this);
		
		saveMenu.addItem(saveItem);
		saveMenu.addItem(closeItem);
		
		antialiasing = new SettingsItem<Boolean>("Anti-Aliasing", booleanValues, width, itemHeight);
		multisampling = new SettingsItem<Integer>("Multisampling", superSamplingValues, width, itemHeight);
		vSync = new SettingsItem<Boolean>("V-Sync", booleanValues, width, itemHeight);
		glow = new SettingsItem<Boolean>("Glow", booleanValues, width, itemHeight);
		particles = new SettingsItem<Boolean>("Particles", booleanValues, width, itemHeight);
		reflections = new SettingsItem<Boolean>("Reflections", booleanValues, width, itemHeight);
		environment = new SettingsItem<Boolean>("Environment", booleanValues, width, itemHeight);
		visualization = new SettingsItem<Boolean>("Visualization", booleanValues, width, itemHeight);
		staticCollectableColor = new SettingsItem<Boolean>("Static Collectable Color", booleanValues, width, itemHeight);
		glowingCollectables = new SettingsItem<Boolean>("Glowing Collectables", booleanValues, width, itemHeight);
		staticObstacleColor = new SettingsItem<Boolean>("Static Obstacle Color", booleanValues, width, itemHeight);
		glowingObstacles = new SettingsItem<Boolean>("Glowing Obstacles", booleanValues, width, itemHeight);
		difficulty = new SettingsItem<Double>("Difficulty", difficultyValues, width, itemHeight);
		keyboard = new SettingsItem<Double>("Keyboard Sensitivity", controlValues, width, itemHeight);
		mouse = new SettingsItem<Double>("Mouse Sensitivity", controlValues, width, itemHeight);
		interfaceVolume = new SettingsItem<Double>("Interface Volume", volumeValues, width, itemHeight);
		audioVolume = new SettingsItem<Double>("Audio Volume", volumeValues, width, itemHeight);
		soundVolume = new SettingsItem<Double>("Sound Volume", volumeValues, width, itemHeight);
		
		graphicMenu.addItem(antialiasing);
		graphicMenu.addItem(multisampling);
		graphicMenu.addItem(vSync);
		graphicMenu.addItem(glow);
		graphicMenu.addItem(particles);
		graphicMenu.addItem(reflections);
		graphicMenu.addItem(environment);
		graphicMenu.addItem(visualization);
		graphicMenu.addItem(staticCollectableColor);
		graphicMenu.addItem(glowingCollectables);
		graphicMenu.addItem(staticObstacleColor);
		graphicMenu.addItem(glowingObstacles);
		graphicMenu.addItem(difficulty);
		inputMenu.addItem(keyboard);
		inputMenu.addItem(mouse);
		volumeMenu.addItem(interfaceVolume);
		volumeMenu.addItem(audioVolume);
		volumeMenu.addItem(soundVolume);
		
		selectAudio = new AudioFile("sounds/Select.mp3");
		volume = AudioDrive.Settings.getDouble("interface.volume");
		
		background = new Overlay().shader(new ShaderProgram("shaders/default.vs", "shaders/title.fs"));
		updateSettings();
		Camera.overlay(getWidth(), getHeight());
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
		difficulty.setValue(AudioDrive.Settings.getDouble("game.difficulty"));
		staticCollectableColor.setValue(AudioDrive.Settings.getBoolean("block.collectable.color.static"));
		glowingCollectables.setValue(AudioDrive.Settings.getBoolean("block.collectable.glowing"));
		staticObstacleColor.setValue(AudioDrive.Settings.getBoolean("block.obstacle.color.static"));
		glowingObstacles.setValue(AudioDrive.Settings.getBoolean("block.obstacle.glowing"));
		keyboard.setValue(AudioDrive.Settings.getDouble("input.keyboard.speed"));
		mouse.setValue(AudioDrive.Settings.getDouble("input.mouse.speed"));
		interfaceVolume.setValue(AudioDrive.Settings.getDouble("interface.volume"));
		audioVolume.setValue(AudioDrive.Settings.getDouble("music.volume"));
		soundVolume.setValue(AudioDrive.Settings.getDouble("sound.volume"));
	}
	
	@Override
	public void render() {
		glClear(GL_COLOR_BUFFER_BIT);
		background.render();
		titleText.render();
		graphicSettingsText.render();
		soundSettingsText.render();
		inputSettingsText.render();
		graphicMenu.render();
		inputMenu.render();
		volumeMenu.render();
		saveMenu.render();
	}
	
	@Override
	public void exiting() {}
	
	@Override
	public void mouseMoved(int x, int y, int dx, int dy) {
		// yCoordinates start in left bottom corner, instead left top
		y = getHeight() - y;
		graphicMenu.mouseMoved(x, y);
		volumeMenu.mouseMoved(x, y);
		inputMenu.mouseMoved(x, y);
		saveMenu.mouseMoved(x, y);
	}
	
	@Override
	public void mouseButtonReleased(int button, int x, int y) {
		// yCoordinates start in left bottom corner, instead left top
		y = getHeight() - y;
		graphicMenu.mousePressed(button, x, y);
		volumeMenu.mousePressed(button, x, y);
		inputMenu.mousePressed(button, x, y);
		saveMenu.mousePressed(button, x, y);
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
		AudioDrive.Settings.set("game.difficulty", difficulty.valueAsString());
		AudioDrive.Settings.set("block.collectable.color.static", staticCollectableColor.valueAsString());
		AudioDrive.Settings.set("block.collectable.glowing", glowingCollectables.valueAsString());
		AudioDrive.Settings.set("block.obstacle.color.static", staticObstacleColor.valueAsString());
		AudioDrive.Settings.set("block.obstacle.glowing", glowingObstacles.valueAsString());
		AudioDrive.Settings.set("input.keyboard.speed", keyboard.valueAsString());
		AudioDrive.Settings.set("input.mouse.speed", mouse.valueAsString());
		AudioDrive.Settings.set("interface.volume", interfaceVolume.valueAsString());
		AudioDrive.Settings.set("music.volume", audioVolume.valueAsString());
		AudioDrive.Settings.set("sound.volume", soundVolume.valueAsString());
		AudioDrive.Settings.save();
		applySettings();
	}
	
	private void applySettings() {
		// not yet possible, since entering the game scene would cause a crash
		// int muiltisampling = AudioDrive.Settings.getInteger("window.multisampling");
		// if (muiltisampling != Window.getPixelFormat().getSamples()) Window.setMultisampling(muiltisampling);
	}
	
}
