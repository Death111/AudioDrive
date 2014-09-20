package audiodrive.ui.scenes;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;

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
	
	private final List<Boolean> anitaliasingValues = Arrays.asList(true, false);
	private final List<Integer> superSamplingValues = Arrays.asList(2, 4, 6, 8, 16);
	private final List<Boolean> vSyncValues = Arrays.asList(true, false);
	
	private final List<Boolean> staticObstacleValues = Arrays.asList(true, false);
	
	private final List<Double> difficultyValues = Arrays.asList(.1, .2, .3, .4, .5, .6, .7, .8, .9, 1.);
	private final List<Double> keyboardValues = Arrays.asList(.1, .2, .3, .4, .5, .6, .7, .8, .9, 1.);
	private final List<Double> mouseValues = Arrays.asList(.1, .2, .3, .4, .5, .6, .7, .8, .9, 1.);
	private final List<Double> interfaceVolumeValues = Arrays.asList(.1, .2, .3, .4, .5, .6, .7, .8, .9, 1.);
	private final List<Double> audioVolumeValues = Arrays.asList(.1, .2, .3, .4, .5, .6, .7, .8, .9, 1.);
	private final List<Double> soundVolumeValues = Arrays.asList(.1, .2, .3, .4, .5, .6, .7, .8, .9, 1.);
	
	private final SettingsItem<Boolean> antiAliasing;
	private final SettingsItem<Integer> superSampling;
	private final SettingsItem<Boolean> vSync;
	private final SettingsItem<Boolean> staticObstacleColor;
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
		
		settingsMenu = new Menu(20, 180, 801, Display.getHeight() - 180, 1);
		saveMenu = new Menu(20, Display.getHeight() - MenuItem.MENU_ITEM_HEIGHT - 20, Display.getWidth() - 50, MenuItem.MENU_ITEM_HEIGHT + 1, 1);
		
		saveItem = new MenuItem("Save", this);
		closeItem = new MenuItem("Return", this);
		
		saveMenu.addItem(saveItem);
		saveMenu.addItem(closeItem);
		
		antiAliasing = new SettingsItem<Boolean>("Anti Aliasing", anitaliasingValues, 800, 50);
		superSampling = new SettingsItem<Integer>("Multi Sampling", superSamplingValues, 800, 50);
		vSync = new SettingsItem<Boolean>("VSync", vSyncValues, 800, 50);
		staticObstacleColor = new SettingsItem<Boolean>("Static obstacle Color", staticObstacleValues, 800, 50);
		difficulty = new SettingsItem<Double>("Difficulty", difficultyValues, 800, 50);
		keyboard = new SettingsItem<Double>("Keyboard", keyboardValues, 800, 50);
		mouse = new SettingsItem<Double>("Mouse", mouseValues, 800, 50);
		interfaceVolume = new SettingsItem<Double>("Interface Volume", interfaceVolumeValues, 800, 50);
		audioVolume = new SettingsItem<Double>("Audio Volume", audioVolumeValues, 800, 50);
		soundVolume = new SettingsItem<Double>("Sound Volume", soundVolumeValues, 800, 50);
		
		settingsMenu.addItem(antiAliasing);
		settingsMenu.addItem(superSampling);
		settingsMenu.addItem(vSync);
		settingsMenu.addItem(staticObstacleColor);
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
		staticObstacleColor.setValue(AudioDrive.Settings.getBoolean("color.collectable.static"));
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
