package audiodrive.ui.scenes;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;

import org.lwjgl.input.Keyboard;

import audiodrive.AudioDrive;
import audiodrive.audio.AnalyzedAudio;
import audiodrive.audio.AudioResource;
import audiodrive.audio.Playback;
import audiodrive.model.track.Track;
import audiodrive.model.track.TrackGenerator;
import audiodrive.ui.components.Camera;
import audiodrive.ui.components.Overlay;
import audiodrive.ui.components.Scene;
import audiodrive.ui.components.Text;
import audiodrive.ui.components.Window;
import audiodrive.ui.effects.ShaderProgram;
import audiodrive.ui.menu.Menu;
import audiodrive.ui.menu.item.Item;
import audiodrive.ui.menu.item.ItemListener;
import audiodrive.ui.menu.item.MenuItem;
import audiodrive.utilities.Log;

/**
 * 
 * @author Death
 *
 */
public class MenuScene extends Scene implements ItemListener {
	
	private static Playback playback = new Playback(new AudioResource("sounds/Menu.mp3")).setLooping(true);
	
	private Text title;
	private Menu menu;
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
		
		menu = new Menu(100, 200, 400, 600, 25);
		visualizeMenuItem = new MenuItem("Visualize", this);
		menu.addItem(visualizeMenuItem);
		playMenuItem = new MenuItem("Play", this);
		menu.addItem(playMenuItem);
		selectAudioMenuItem = new MenuItem("Particles", this);
		menu.addItem(selectAudioMenuItem);
		selectModelMenuItem = new MenuItem("Select Model", this);
		menu.addItem(selectModelMenuItem);
		settingsMenuItem = new MenuItem("Settings", this);
		menu.addItem(settingsMenuItem);
		exitMenuItem = new MenuItem("Exit", this);
		menu.addItem(exitMenuItem);
		
		hoverAudio = new AudioResource("sounds/Hover.mp3");
		selectAudio = new AudioResource("sounds/Select.mp3");
		
		background = new Overlay().shader(new ShaderProgram("shaders/default.vs", "shaders/title.fs"));
		Camera.overlay(getWidth(), getHeight());
	}
	
	@Override
	public void render() {
		glClear(GL_COLOR_BUFFER_BIT);
		background.render();
		title.render();
		menu.render();
	}
	
	@Override
	public void exiting() {
		if (!Window.isRecreating() && getEntering() == null) playback.stop();
		title = null;
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
	}
	
	@Override
	public void mouseButtonReleased(int button, int x, int y) {
		// yCoordinates start in left bottom corner, instead left top
		y = getHeight() - y;
		
		menu.mousePressed(button, x, y);
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
			Scene.get(ParticleScene.class).enter();
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
	}
	
}
