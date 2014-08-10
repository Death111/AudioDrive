package audiodrive.ui.scenes;

import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.input.Keyboard;

import audiodrive.audio.AnalyzedAudio;
import audiodrive.audio.AudioFile;
import audiodrive.model.buffer.VertexBuffer;
import audiodrive.model.track.Track;
import audiodrive.model.track.TrackGenerator;
import audiodrive.ui.components.Camera;
import audiodrive.ui.components.Scene;
import audiodrive.ui.effects.ShaderProgram;
import audiodrive.ui.menu.Menu;
import audiodrive.ui.menu.item.Item;
import audiodrive.ui.menu.item.ItemListener;
import audiodrive.ui.menu.item.MenuItem;
import audiodrive.utilities.Buffers;
import audiodrive.utilities.Log;

/**
 * 
 * @author Death
 *
 */
public class MenuScene extends Scene implements ItemListener {
	
	Menu menu;
	private MenuItem visualizeMenuItem;
	private MenuItem playMenuItem;
	private MenuItem selectAudioMenuItem;
	private MenuItem selectModelMenuItem;
	private MenuItem exitMenuItem;
	
	private AnalyzedAudio audio;
	private AudioFile hoverAudio;
	private AudioFile selectAudio;
	
	private VertexBuffer canvas;
	private ShaderProgram shader;
	private double duration;
	
	public void enter(AnalyzedAudio audio) {
		this.audio = audio;
		super.enter();
	}
	
	@Override
	public void entering() {
		Log.trace("Entering MenueScene");
		// Change to Ortho
		Camera.overlay(getWidth(), getHeight());
		canvas = new VertexBuffer(Buffers.create(0, 0, 0, getHeight(), getWidth(), getHeight(), getWidth(), 0), 2).mode(GL_QUADS);
		shader = new ShaderProgram("shaders/default.vs", "shaders/title.fs");
		
		menu = new Menu(100, 200, 25);
		visualizeMenuItem = new MenuItem("Visualize", this);
		menu.addItem(visualizeMenuItem);
		playMenuItem = new MenuItem("Play", this);
		menu.addItem(playMenuItem);
		selectAudioMenuItem = new MenuItem("Select Audio", this);
		menu.addItem(selectAudioMenuItem);
		selectModelMenuItem = new MenuItem("Select Model", this);
		menu.addItem(selectModelMenuItem);
		exitMenuItem = new MenuItem("Exit", this);
		menu.addItem(exitMenuItem);
		
		hoverAudio = new AudioFile("sounds/hover.wav");
		selectAudio = new AudioFile("sounds/select.wav");
	}
	
	@Override
	public void update(double elapsed) {
		duration += elapsed;
	}
	
	@Override
	public void render() {
		glClear(GL_COLOR_BUFFER_BIT);
		shader.bind();
		shader.uniform("time").set(duration);
		shader.uniform("resolution").set((float) getWidth(), (float) getHeight());
		canvas.draw();
		shader.unbind();
		menu.render();
	}
	
	@Override
	public void exiting() {
		Log.info("exiting");
		canvas = null;
		shader = null;
	}
	
	@Override
	public void keyReleased(int key, char character) {
		Log.trace("Key '" + character + "' was realeased,");
		switch (key) {
		
		case Keyboard.KEY_ESCAPE:
			exit();
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
		if (hover) {
			hoverAudio.play();
		}
	}
	
	@Override
	public void onSelect(Item item, boolean select) {
		if (!select) {
			return;
		}
		selectAudio.play();
		if (item == visualizeMenuItem) {
			Scene.get(VisualizerScene.class).enter(audio);
			return;
		}
		if (item == playMenuItem) {
			TrackGenerator trackGenerator = new TrackGenerator();
			Track track = trackGenerator.generate(audio, 25);
			Scene.get(GameScene.class).enter(track);
			return;
		}
		if (item == selectAudioMenuItem) {
			Scene.get(AudioSelectionScene.class).enter();
			return;
		}
		if (item == selectModelMenuItem) {
			Scene.get(ModelViewerScene.class).enter();
			return;
		}
		if (item == exitMenuItem) {
			exit();
			return;
		}
	}
}
