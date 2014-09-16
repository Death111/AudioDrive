package audiodrive.ui.scenes;

import static org.lwjgl.opengl.GL11.*;

import java.io.File;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import audiodrive.AudioDrive;
import audiodrive.audio.AudioFile;
import audiodrive.audio.Playback;
import audiodrive.model.Player;
import audiodrive.model.geometry.Vector;
import audiodrive.model.geometry.transform.Rotation;
import audiodrive.model.geometry.transform.Translation;
import audiodrive.model.loader.ModelLoader;
import audiodrive.model.track.Track;
import audiodrive.ui.GL;
import audiodrive.ui.components.Camera;
import audiodrive.ui.components.Scene;
import audiodrive.ui.components.Window;
import audiodrive.ui.scenes.overlays.GameOverlay;
import audiodrive.utilities.Buffers;
import audiodrive.utilities.Files;
import audiodrive.utilities.Log;

public class GameScene extends Scene {
	
	public static enum State {
		Running, Paused, Ended, Destroyed
	}
	
	private State state;
	private Track track;
	private Player player;
	
	private Rotation rotation = new Rotation();
	private Translation translation = new Translation();
	
	private Playback playback;
	private GameOverlay overlay;
	
	private double time;
	
	public void enter(Track track) {
		this.track = track;
		super.enter();
	}
	
	@Override
	protected void entering() {
		Log.info("starting game...");
		state = State.Paused;
		File model = Files.find("models/player", AudioDrive.Settings.get("model") + ".obj").orElse(Files.list("models/player", ".obj", true).get(0));
		player = new Player(this).model(ModelLoader.loadSingleModel(model.getPath()));
		player.model().scale(0.05);
		overlay = new GameOverlay(this);
		playback = new Playback(track.getAudio().getFile());
		rotation.reset();
		translation.reset();
		time = 0;
		track.player(player);
		player.update(0);
		track.update(0);
		Camera.perspective(45, getWidth(), getHeight(), 0.1, 10000);
		GL.pushAttributes();
		glEnable(GL_CULL_FACE);
		glEnable(GL_NORMALIZE);
		glEnable(GL_LIGHT0);
		glLight(GL_LIGHT0, GL_DIFFUSE, Buffers.create(1f, 1f, 1f, 1f));
		final float ambientAmount = .3f;
		glLight(GL_LIGHT0, GL_AMBIENT, Buffers.create(ambientAmount, ambientAmount, ambientAmount, 1f));
		glColorMaterial(GL_FRONT, GL_AMBIENT_AND_DIFFUSE);
		glEnable(GL_COLOR_MATERIAL);
		glShadeModel(GL_SMOOTH);
		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glEnable(GL_LIGHTING);
		Mouse.setGrabbed(true);
	}
	
	@Override
	protected void update(double elapsed) {
		updateState();
		overlay.update();
		if (state != State.Running) return;
		time = playback.getTime();
		track.update(playback.getTime());
		player.update(elapsed);
	}
	
	private void updateState() {
		State oldState = state;
		if (player.damage() >= 100) state = State.Destroyed;
		else if (track.index().integer == track.lastIndex()) state = State.Ended;
		else if (!playback.isRunning()) state = State.Paused;
		else state = State.Running;
		if (state == oldState) return;
		if (state == State.Destroyed) {
			playback.stop();
			new AudioFile("sounds/Destroyed.mp3").play();
		}
	}
	
	@Override
	protected void render() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		Camera.perspective(45, getWidth(), getHeight(), 0.1, 10000);
		player.camera();
		
		translation.apply();
		// rotation.apply();
		
		track.render();
		player.render();
		
		overlay.render();
	}
	
	@Override
	protected void exiting() {
		GL.popAttributes();
		playback.stop();
		Mouse.setGrabbed(false);
	}
	
	public Track getTrack() {
		return track;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public void setState(State state) {
		this.state = state;
	}
	
	public State getState() {
		return state;
	}
	
	public double playtime() {
		return time;
	}
	
	@Override
	public void keyPressed(int key, char character) {
		Vector translation = new Vector();
		switch (key) {
		case Keyboard.KEY_NUMPAD0:
			translation.add(0, 0, -0.01);
			break;
		case Keyboard.KEY_NUMPAD2:
			translation.add(0, 0.01, 0);
			break;
		case Keyboard.KEY_NUMPAD4:
			translation.add(-0.01, 0, 0);
			break;
		case Keyboard.KEY_NUMPAD5:
			translation.add(0, 0, 0.01);
			break;
		case Keyboard.KEY_NUMPAD6:
			translation.add(0.01, 0, 0);
			break;
		case Keyboard.KEY_NUMPAD8:
			translation.add(0, -0.01, 0);
			break;
		case Keyboard.KEY_RIGHT:
			movePlayer(0.1);
			break;
		case Keyboard.KEY_LEFT:
			movePlayer(-0.1);
			break;
		default:
			break;
		}
		this.translation.vector().add(rotation.rotate(translation));
	}
	
	@Override
	public void keyReleased(int key, char character) {
		switch (key) {
		case Keyboard.KEY_PAUSE:
			playback.toggle();
			break;
		case Keyboard.KEY_ESCAPE:
			back();
			break;
		case Keyboard.KEY_HOME:
			translation.reset();
			rotation.reset();
			player.zoom(1.0);
			break;
		case Keyboard.KEY_P:
			overlay.togglePeaks();
			break;
		case Keyboard.KEY_V:
			Window.toggleVSync();
			break;
		default:
			break;
		}
	}
	
	@Override
	public void mouseMoved(int x, int y, int dx, int dy) {
		movePlayer(dx * 0.002);
	}
	
	private void movePlayer(double x) {
		if (!playback.isRunning()) return;
		double newX = player.model().translation().x() - x; // FIXME why negative?
		double maxX = track.width() / 3;
		player.model().translation().x(Math.max(-maxX, Math.min(maxX, newX)));
	}
	
	@Override
	public void mouseDragged(int button, int mouseX, int mouseY, int dx, int dy) {
		double horizontal = dx * -0.1;
		double vertical = dy * 0.1;
		switch (button) {
		case 0:
			rotation.xAdd(vertical).yAdd(horizontal);
			break;
		case 1:
			rotation.zAdd(horizontal);
			break;
		default:
			break;
		}
	}
	
	@Override
	public void mouseWheelRotated(int rotation, int x, int y) {
		player.zoom(player.zoom() + rotation * 0.001);
	}
	
}
