package audiodrive.ui.scenes;

import static org.lwjgl.opengl.GL11.*;

import java.util.Arrays;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

import audiodrive.AudioDrive;
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
	
	private final Menu settingsMenu;
	private final Menu saveMenu;
	
	private final List<Boolean> booleanValues = Arrays.asList(true, false);
	
	private final List<Integer> superSamplingValues = Arrays.asList(2, 4, 8, 16);
	
	private final List<Double> difficultyValues = Arrays.asList(.1, .2, .3, .4, .5, .6, .7, .8, .9, 1.);
	private final List<Double> keyboardValues = Arrays.asList(.1, .2, .3, .4, .5, .6, .7, .8, .9, 1.);
	private final List<Double> mouseValues = Arrays.asList(.1, .2, .3, .4, .5, .6, .7, .8, .9, 1.);
	private final List<Double> interfaceVolumeValues = Arrays.asList(.1, .2, .3, .4, .5, .6, .7, .8, .9, 1.);
	private final List<Double> audioVolumeValues = Arrays.asList(.1, .2, .3, .4, .5, .6, .7, .8, .9, 1.);
	private final List<Double> soundVolumeValues = Arrays.asList(.1, .2, .3, .4, .5, .6, .7, .8, .9, 1.);
	
	private final SettingsItem<Boolean> antiAliasing;
	private final SettingsItem<Integer> superSampling;
	private final SettingsItem<Boolean> vSync;
	private final SettingsItem<Boolean> staticCollectableColor;
	private final SettingsItem<Boolean> glowingCollectables;
	private final SettingsItem<Boolean> staticObstacleColor;
	private final SettingsItem<Boolean> glowingObstacles;
	private final SettingsItem<Double> difficulty;
	private final SettingsItem<Double> keyboard;
	private final SettingsItem<Double> mouse;
	private final SettingsItem<Double> interfaceVolume;
	private final SettingsItem<Double> audioVolume;
	private final SettingsItem<Double> soundVolume;
	
	private final MenuItem saveItem;
	private final MenuItem closeItem;
	private final Text titleText;
	private Overlay background;
	
	public SettingsScene() {
		titleText = new Text("Settings").setFont(AudioDrive.Font).setSize(48).setPosition(20, 20);
		
		int width = 1000;
		settingsMenu = new Menu(20, 180, width + 1, Display.getHeight() - 180, 1);
		saveMenu = new Menu(20, Display.getHeight() - MenuItem.MENU_ITEM_HEIGHT - 20, Display.getWidth() - 50, MenuItem.MENU_ITEM_HEIGHT + 1, 1);
		
		saveItem = new MenuItem("Save", this);
		closeItem = new MenuItem("Return", this);
		
		saveMenu.addItem(saveItem);
		saveMenu.addItem(closeItem);
		
		antiAliasing = new SettingsItem<Boolean>("Anti Aliasing", booleanValues, width, 50);
		superSampling = new SettingsItem<Integer>("Multi Sampling", superSamplingValues, width, 50);
		vSync = new SettingsItem<Boolean>("VSync", booleanValues, width, 50);
		staticCollectableColor = new SettingsItem<Boolean>("Static Collectable Color", booleanValues, width, 50);
		glowingCollectables = new SettingsItem<Boolean>("Glowing Collectables", booleanValues, width, 50);
		staticObstacleColor = new SettingsItem<Boolean>("Static Obstacle Color", booleanValues, width, 50);
		glowingObstacles = new SettingsItem<Boolean>("Glowing Obstacles", booleanValues, width, 50);
		difficulty = new SettingsItem<Double>("Difficulty", difficultyValues, width, 50);
		keyboard = new SettingsItem<Double>("Keyboard", keyboardValues, width, 50);
		mouse = new SettingsItem<Double>("Mouse", mouseValues, width, 50);
		interfaceVolume = new SettingsItem<Double>("Interface Volume", interfaceVolumeValues, width, 50);
		audioVolume = new SettingsItem<Double>("Audio Volume", audioVolumeValues, width, 50);
		soundVolume = new SettingsItem<Double>("Sound Volume", soundVolumeValues, width, 50);
		
		settingsMenu.addItem(antiAliasing);
		settingsMenu.addItem(superSampling);
		settingsMenu.addItem(vSync);
		settingsMenu.addItem(staticCollectableColor);
		settingsMenu.addItem(glowingCollectables);
		settingsMenu.addItem(staticObstacleColor);
		settingsMenu.addItem(glowingObstacles);
		settingsMenu.addItem(difficulty);
		settingsMenu.addItem(keyboard);
		settingsMenu.addItem(mouse);
		settingsMenu.addItem(interfaceVolume);
		settingsMenu.addItem(audioVolume);
		settingsMenu.addItem(soundVolume);
		
		background = new Overlay().shader(new ShaderProgram("shaders/default.vs", "shaders/title.fs"));
	}
	
	@Override
	public void entering() {
		updateSettings();
		Camera.overlay(getWidth(), getHeight());
	}
	
	private void updateSettings() {
		AudioDrive.Settings.load();
		antiAliasing.setValue(AudioDrive.Settings.getBoolean("window.antialiasing"));
		superSampling.setValue(AudioDrive.Settings.getInteger("window.supersampling"));
		vSync.setValue(AudioDrive.Settings.getBoolean("window.vsync"));
		staticCollectableColor.setValue(AudioDrive.Settings.getBoolean("block.collectable.color.static"));
		glowingCollectables.setValue(AudioDrive.Settings.getBoolean("block.collectable.glowing"));
		staticObstacleColor.setValue(AudioDrive.Settings.getBoolean("block.obstacle.color.static"));
		glowingObstacles.setValue(AudioDrive.Settings.getBoolean("block.obstacle.glowing"));
		difficulty.setValue(AudioDrive.Settings.getDouble("game.difficulty"));
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
		settingsMenu.render();
		saveMenu.render();
	}
	
	@Override
	public void exiting() {}
	
	@Override
	public void mouseMoved(int x, int y, int dx, int dy) {
		// yCoordinates start in left bottom corner, instead left top
		y = getHeight() - y;
		settingsMenu.mouseMoved(x, y);
		saveMenu.mouseMoved(x, y);
	}
	
	@Override
	public void mouseButtonReleased(int button, int x, int y) {
		// yCoordinates start in left bottom corner, instead left top
		y = getHeight() - y;
		settingsMenu.mousePressed(button, x, y);
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
		if (item == saveItem) {
			saveSettings();
		} else if (item == closeItem) {
			back();
		}
	}
	
	private void saveSettings() {
		AudioDrive.Settings.set("color.collectable.static", staticObstacleColor.valueAsString());
		AudioDrive.Settings.set("game.difficulty", difficulty.valueAsString());
		AudioDrive.Settings.set("input.mouse.speed", mouse.valueAsString());
		AudioDrive.Settings.set("input.keyboard.speed", keyboard.valueAsString());
		AudioDrive.Settings.set("interface.volume", interfaceVolume.valueAsString());
		AudioDrive.Settings.set("music.volume", audioVolume.valueAsString());
		AudioDrive.Settings.set("sound.volume", soundVolume.valueAsString());
		AudioDrive.Settings.set("window.antialiasing", antiAliasing.valueAsString());
		AudioDrive.Settings.set("window.supersampling", superSampling.valueAsString());
		AudioDrive.Settings.set("window.vsync", vSync.valueAsString());
		AudioDrive.Settings.save();
	}
}
