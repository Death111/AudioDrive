package audiodrive.ui.scenes;

import static org.lwjgl.opengl.GL11.*;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;

import audiodrive.AudioDrive;
import audiodrive.Resources;
import audiodrive.audio.AnalyzedAudio;
import audiodrive.audio.AudioResource;
import audiodrive.audio.Playback;
import audiodrive.model.geometry.Vector;
import audiodrive.model.geometry.transform.Rotation;
import audiodrive.model.loader.Model;
import audiodrive.model.loader.ModelLoader;
import audiodrive.model.track.Track;
import audiodrive.model.track.TrackGenerator;
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
public class MenuScene extends Scene implements ItemListener {
	
	private static Playback playback = new Playback(new AudioResource("sounds/Menu.mp3")).setLooping(true);
	
	private Text title;
	private Text version;
	private Text credits;
	private Menu menu;
	private Menu audioSelectionMenu;
	private Menu modelSelectionMenu;
	private SettingsItem<Integer> modelValue;
	private MenuItem visualizeMenuItem;
	private MenuItem playMenuItem;
	private MenuItem selectAudioMenuItem;
	private MenuItem selectModelMenuItem;
	private MenuItem settingsMenuItem;
	private MenuItem exitMenuItem;
	private Overlay background;
	
	private AnalyzedAudio audio;
	private AudioResource hoverAudio;
	private AudioResource selectAudio;
	
	private boolean silentHovering = true;
	private double volume;
	
	private Model playerModel;
	
	private Rotation rotation = new Rotation();
	private Text audioText;
	private Text selectedAudioText;
	List<String> models;
	
	public void enter(AnalyzedAudio audio) {
		this.audio = audio;
		hierarchy().clear();
		super.enter();
	}
	
	@Override
	public void entering() {
		volume = AudioDrive.Settings.getDouble("interface.volume");
		playback.setVolume(AudioDrive.Settings.getDouble("music.volume"));
		if (!playback.isRunning()) playback.setLooping(true).start();
		Log.trace("Entering MenueScene");
		
		title = new Text("AudioDrive").setFont(AudioDrive.Font).setSize(48).setPosition(100, 80);
		audioText = new Text("Selected Audio").setFont(AudioDrive.Font).setSize(32).setPosition(600, 200);
		selectedAudioText = new Text().setFont(AudioDrive.Font).setSize(22).setPosition(600, 260);
		version = new Text("Version " + AudioDrive.Version).setFont(AudioDrive.Font).setSize(10).setPosition(10, getHeight() - 10).setAlignment(Alignment.LowerLeft);
		credits = new Text("Made by " + AudioDrive.Creators).setFont(AudioDrive.Font).setSize(10).setPosition(getWidth() - 10, getHeight() - 10).setAlignment(Alignment.LowerRight);
		
		menu = new Menu(100, 200, 400, 600, 25);
		playMenuItem = new MenuItem("Play", this);
		menu.addItem(playMenuItem);
		visualizeMenuItem = new MenuItem("Visualize", this);
		menu.addItem(visualizeMenuItem);
		selectModelMenuItem = new MenuItem("Select Model", this);
		// menu.addItem(selectModelMenuItem);
		settingsMenuItem = new MenuItem("Settings", this);
		menu.addItem(settingsMenuItem);
		exitMenuItem = new MenuItem("Exit", this);
		menu.addItem(exitMenuItem);
		
		hoverAudio = new AudioResource("sounds/Hover.mp3");
		selectAudio = new AudioResource("sounds/Select.mp3");
		
		background = new Overlay().shader(new ShaderProgram("shaders/default.vs", "shaders/title.fs"));
		
		audioSelectionMenu = new Menu(958, 200, 600 + 1, 51, 0);
		selectAudioMenuItem = new MenuItem("(Change)", this);
		selectedAudioText.setText(((audio != null) ? "\"" + audio.getName() + "\"" : "None selected"));
		selectAudioMenuItem.setIcon(null);
		audioSelectionMenu.addItem(selectAudioMenuItem);
		
		models = Resources.getAvailablePlayerModelPaths();
		playerModel = Resources.getCurrentPlayerModel();
		modelSelectionMenu = new Menu(600, 400, 600 + 1, 51, 0);
		List<Integer> modelIndexe = new ArrayList<>();
		int currentModel = 1;
		for (int i = 0; i < models.size(); i++) {
			modelIndexe.add(i + 1);
			if (models.get(i).contains(AudioDrive.Settings.get("player.model"))) currentModel = i + 1;
		}
		modelValue = new SettingsItem<Integer>("Selected Model", modelIndexe, 600, 50, this);
		modelValue.setValue(currentModel);
		modelSelectionMenu.addItem(modelValue);
		Input.addObservers(menu, audioSelectionMenu, modelSelectionMenu);
	}
	
	@Override
	public void render() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		Camera.overlay(getWidth(), getHeight());
		background.render();
		title.render();
		audioText.render();
		version.render();
		credits.render();
		menu.render();
		audioSelectionMenu.render();
		selectedAudioText.render();
		modelSelectionMenu.render();
		
		Camera.perspective(45, getWidth(), getHeight(), .1, 100);
		Camera.position(new Vector(.3, .2, -1));
		Camera.lookAt(new Vector(0, 0, 1));
		rotation.apply();
		// TODO maybe add reflection
		double factor = getHeight() / 1080.0;
		playerModel.position().y(-0.15 * (1.1 - factor));
		playerModel.scale(0.05 * factor).render();
	}
	
	@Override
	public void update(double time) {
		rotation.yAdd(10 * time);
	}
	
	@Override
	public void exiting() {
		if (!Window.isRecreating() && getEntering() == null) playback.stop();
		Input.removeObservers(menu, audioSelectionMenu, modelSelectionMenu);
		title = null;
		version = null;
		credits = null;
		background = null;
		menu = null;
		audioSelectionMenu = null;
		modelSelectionMenu = null;
		visualizeMenuItem = null;
		playMenuItem = null;
		selectAudioMenuItem = null;
		selectModelMenuItem = null;
		settingsMenuItem = null;
		exitMenuItem = null;
		hoverAudio = null;
		selectAudio = null;
		System.gc();
	}
	
	public AnalyzedAudio getAudio() {
		return audio;
	}
	
	@Override
	public void keyReleased(int key, char character) {
		super.keyReleased(key, character);
		Log.trace("Key '" + character + "' was realeased,");
		switch (key) {
		case Keyboard.KEY_ESCAPE:
			exit();
			break;
		case Keyboard.KEY_V:
			onSelect(visualizeMenuItem, true);
			break;
		case Keyboard.KEY_RETURN:
		case Keyboard.KEY_P:
			onSelect(playMenuItem, true);
			break;
		case Keyboard.KEY_A:
			onSelect(selectAudioMenuItem, true);
			break;
		case Keyboard.KEY_M:
			onSelect(selectModelMenuItem, true);
			break;
		default:
			break;
		}
	}
	
	enum MouseButton {
		LEFT, RIGHT, MIDDLE;
	}
	
	@Override
	public void onHover(Item item, boolean hover) {
		if (hover && !silentHovering) {
			hoverAudio.play(volume);
		}
	}
	
	@Override
	public void onSelect(Item item, boolean select) {
		if (!select) {
			return;
		}
		selectAudio.play(volume);
		if (item == visualizeMenuItem) {
			if (audio == null) {
				Scene.get(AudioSelectionScene.class).enter();
				return;
			}
			playback.stop();
			Scene.get(VisualizerScene.class).enter(audio);
			return;
		}
		if (item == playMenuItem) {
			if (audio == null) {
				Scene.get(AudioSelectionScene.class).enter();
				return;
			}
			Track track = TrackGenerator.generate(audio);
			playback.setLooping(false);
			Scene.get(GameScene.class).enter(track);
			return;
		}
		if (item == selectAudioMenuItem) {
			Scene.get(AudioSelectionScene.class).enter();
			return;
		}
		if (item == selectModelMenuItem) {
			Scene.get(ModelSelectionScene.class).enter();
			return;
		}
		if (item == exitMenuItem) {
			exit();
			return;
		}
		if (item == settingsMenuItem) {
			Scene.get(SettingsScene.class).enter();
		}
		if (item == modelValue) {
			final String modelPath = models.get(modelValue.getValue() - 1);
			playerModel = ModelLoader.loadModel(modelPath);
			AudioDrive.Settings.set("player.model", playerModel.getName());
		}
	}
	
}
