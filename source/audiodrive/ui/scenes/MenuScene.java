package audiodrive.ui.scenes;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.lwjgl.input.Keyboard;

import audiodrive.AudioDrive;
import audiodrive.Resources;
import audiodrive.audio.AnalyzedAudio;
import audiodrive.audio.AudioResource;
import audiodrive.audio.AudioInfo;
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
	private List<Text> audioInformationText = new ArrayList<>();
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
		version = new Text("Version " + AudioDrive.Version).setFont(AudioDrive.Font).setSize(10).setPosition(10, getHeight() - 10).setAlignment(Alignment.LowerLeft);
		credits = new Text("Made by " + AudioDrive.Creators).setFont(AudioDrive.Font).setSize(10).setPosition(getWidth() - 10, getHeight() - 10).setAlignment(Alignment.LowerRight);
		
		menu = new Menu(100, 200, 400, 600, 25);
		playMenuItem = new MenuItem("Play", this);
		menu.addItem(playMenuItem);
		visualizeMenuItem = new MenuItem("Visualize", this);
		menu.addItem(visualizeMenuItem);
		selectAudioMenuItem = new MenuItem("Select Audio", this);
		menu.addItem(selectAudioMenuItem);
		selectModelMenuItem = new MenuItem("Select Model", this);
		// menu.addItem(selectModelMenuItem);
		settingsMenuItem = new MenuItem("Settings", this);
		menu.addItem(settingsMenuItem);
		exitMenuItem = new MenuItem("Exit", this);
		menu.addItem(exitMenuItem);
		
		hoverAudio = new AudioResource("sounds/Hover.mp3");
		selectAudio = new AudioResource("sounds/Select.mp3");
		
		background = new Overlay().shader(new ShaderProgram("shaders/default.vs", "shaders/title.fs"));
		
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
		
		// Test if audio was selected
		if (audio == null) {
			audioInformationText.add(new Text("Non selected").setFont(AudioDrive.Font).setSize(24).setPosition(600, 250));
			return;
		}
		// Try to parse tags
		try {
			final AudioInfo audioInfo = new AudioInfo(audio.getFile().getPath());
			int offset = 25; // Spacing between text
			final List<String> infos = audioInfo.getInfos();
			audioInformationText.clear();
			IntStream.range(0, infos.size()).forEach(idx -> {
				audioInformationText.add(new Text(infos.get(idx)).setFont(AudioDrive.Font).setSize(24).setPosition(600, 250 + idx * offset));
			});
			
		} catch (Exception e) {
			// Something went wrong i.e. no tags available use file name
			Log.warning("Error while parsing tags: " + e);
			audioInformationText.clear();
			audioInformationText.add(new Text(audio.getName()).setFont(AudioDrive.Font).setSize(24).setPosition(600, 250));
		}
	}
	
	@Override
	public void render() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		Camera.overlay(getWidth(), getHeight());
		background.render();
		title.render();
		audioText.render();
		audioInformationText.stream().forEach(Text::render);
		
		version.render();
		credits.render();
		menu.render();
		modelSelectionMenu.render();
		
		Camera.perspective(45, getWidth(), getHeight(), .1, 100);
		Camera.position(new Vector(.3, .2, -1));
		Camera.lookAt(new Vector(0, 0, 1));
		rotation.apply();
		// TODO maybe add reflection
		playerModel.scale(0.05).render();
	}
	
	private double lastTime = System.currentTimeMillis() / 1000;
	
	@Override
	public void update(double time) {
		rotation.yAdd(10 * time);
		rotation.y(rotation.y() % 360);
	}
	
	@Override
	public void exiting() {
		if (!Window.isRecreating() && getEntering() == null) playback.stop();
		title = null;
		version = null;
		credits = null;
		background = null;
		menu = null;
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
	
	@Override
	public void mouseMoved(int x, int y, int dx, int dy) {
		// yCoordinates start in left bottom corner, instead left top
		y = getHeight() - y;
		
		menu.mouseMoved(x, y);
		modelSelectionMenu.mouseMoved(x, y);
	}
	
	@Override
	public void mouseButtonReleased(int button, int x, int y) {
		// yCoordinates start in left bottom corner, instead left top
		y = getHeight() - y;
		
		menu.mousePressed(button, x, y);
		modelSelectionMenu.mousePressed(button, x, y);
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
