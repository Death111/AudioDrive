package audiodrive.ui.scenes;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import audiodrive.audio.AnalyzedAudio;
import audiodrive.audio.AudioFile;
import audiodrive.audio.AudioPlayer;
import audiodrive.model.track.Track;
import audiodrive.model.track.TrackGenerator;
import audiodrive.ui.components.Camera;
import audiodrive.ui.components.Scene;
import audiodrive.ui.menu.Menu;
import audiodrive.ui.menu.item.Item;
import audiodrive.ui.menu.item.ItemListener;
import audiodrive.ui.menu.item.MenuItem;
import audiodrive.utilities.Log;

public class NicosMenuScene extends Scene implements ItemListener {

	Menu menu;
	private MenuItem visualizeMenuItem;
	private MenuItem playMenuItem;
	private MenuItem selectAudioMenuItem;
	private MenuItem selectModelMenuItem;
	private MenuItem exitMenuItem;

	private AnalyzedAudio audio;
	private AudioPlayer ap;
	private AudioFile hoverAudio;
	private AudioFile selectAudio;

	public void enter(AnalyzedAudio audio) {
		this.audio = audio;
		super.enter();
	}

	@Override
	public void entering() {
		Log.trace("Entering NicosMenueScene");
		// Change to Ortho
		Camera.overlay(getWidth(), getHeight());

		this.menu = new Menu(100, 200, 25);
		visualizeMenuItem = new MenuItem("Visualize", this);
		menu.addMenuItem(visualizeMenuItem);
		playMenuItem = new MenuItem("Play", this);
		menu.addMenuItem(playMenuItem);
		selectAudioMenuItem = new MenuItem("Select Audio", this);
		menu.addMenuItem(selectAudioMenuItem);
		selectModelMenuItem = new MenuItem("Select Model", this);
		menu.addMenuItem(selectModelMenuItem);
		exitMenuItem = new MenuItem("Exit", this);
		menu.addMenuItem(exitMenuItem);

		final String hoverPath = "/sounds/" + "hover" + ".wav";
		final String selectPath = "/sounds/" + "select" + ".wav";
		hoverAudio = loadAudioFile(hoverPath);
		selectAudio = loadAudioFile(selectPath);
		ap = new AudioPlayer();
		super.enter();
	}

	private AudioFile loadAudioFile(String audioPath) {
		URL resource = this.getClass().getResource(audioPath);
		try {
			final URI uri = resource.toURI();
			return new AudioFile(new File(uri));
		} catch (URISyntaxException e) {
			throw new RuntimeException("Could not load file '" + audioPath + "'. " + e.getMessage(), e);
		}
	}

	@Override
	public void update(double elapsed) {

	}

	@Override
	public void render() {
		glClear(GL_COLOR_BUFFER_BIT);
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glLoadIdentity();

		renderBackground();
		menu.render();
	}

	/**
	 * renders some nice background
	 */
	private void renderBackground() {
		final int height = getHeight();
		final int width = getWidth();

		final int quadCount = 15;
		final int sizeX = width / quadCount;
		final int sizeY = height / quadCount;
		GL11.glBegin(GL11.GL_QUADS);

		for (int x = 0; x < width; x += sizeX) {
			for (int y = 0; y < height; y += sizeY) {
				GL11.glColor4d(0, 0, 1, Math.random());

				GL11.glVertex2i(x, y);
				GL11.glVertex2i(x + sizeX, y);
				GL11.glVertex2i(x + sizeX, y + sizeY);
				GL11.glVertex2i(x, y + sizeY);
			}
		}

		GL11.glEnd();
	}

	@Override
	public void exiting() {
		Log.info("exiting");
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

	int lastX = 0;
	int lastY = 0;

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
			ap.play(hoverAudio);
		}
	}

	@Override
	public void onSelect(Item item, boolean select) {
		if (!select) {
			return;
		}
		ap.play(selectAudio);
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
