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
import audiodrive.utilities.Arithmetic;
import audiodrive.utilities.Buffers;
import audiodrive.utilities.Files;
import audiodrive.utilities.Log;

public class GameScene extends Scene {
	
	public static enum State {
		Running, Paused, Resuming, Ended, Destroyed
	}
	
	private State state;
	private Track track;
	private Player player;
	
	private Translation translation = new Translation();
	private double rotation = 0;
	
	private Playback playback;
	private GameOverlay overlay;
	
	private double time;
	private double keyboardSpeed;
	private double mouseSpeed;
	private double volume;
	
	public void enter(Track track) {
		this.track = track;
		super.enter();
	}
	
	@Override
	protected void entering() {
		Log.info("starting game...");
		keyboardSpeed = AudioDrive.Settings.getDouble("input.keyboard.speed");
		mouseSpeed = AudioDrive.Settings.getDouble("input.mouse.speed");
		volume = AudioDrive.Settings.getDouble("sound.volume");
		state = State.Paused;
		File model = Files.find("models/player", AudioDrive.Settings.get("player.model") + ".obj").orElse(Files.list("models/player", ".obj", true).get(0));
		player = new Player(this).model(ModelLoader.loadSingleModel(model.getPath()));
		player.model().scale(0.05);
		overlay = new GameOverlay(this);
		playback = new Playback(track.getAudio().getFile()).setVolume(AudioDrive.Settings.getDouble("music.volume"));
		translation.reset();
		rotation = 0;
		time = 0;
		track.player(player);
		track.update(0);
		player.update(0);
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
		updateRotation(elapsed);
		checkState();
		overlay.update();
		player.update();
		if (state != State.Running) return;
		time = playback.getTime();
		track.update(playtime());
		player.update(elapsed);
	}
	
	private void updateRotation(double elapsed) {
		if (state == State.Paused || state == State.Ended) {
			rotation += 15 * elapsed;
		} else if (state == State.Resuming) {
			rotation = Rotation.unify180(rotation);
			rotation -= Arithmetic.smooth(15 * rotation, 1, Math.abs(rotation) / 360) * elapsed;
			rotation = Arithmetic.significance(rotation, 0.1);
		}
	}
	
	private void checkState() {
		if (state == State.Resuming && rotation == 0) {
			state = State.Running;
			if (playback.isPaused()) playback.resume();
			else playback.start();
			return;
		}
		if (state != State.Running) return;
		if (player.damage() >= 100) {
			state = State.Destroyed;
			playback.stop();
			new AudioFile("sounds/Destroyed.mp3").play(volume);
			return;
		}
		if (track.index().integer == track.lastIndex()) {
			state = State.Ended;
			return;
		}
	}
	
	@Override
	protected void render() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		Camera.perspective(45, getWidth(), getHeight(), 0.1, 10000);
		player.camera();
		
		translation.apply();
		
		Vector position = player.model().position().plus(player.model().translation().vector());
		Translation translation = new Translation().set(position);
		translation.apply();
		GL.rotate(rotation, player.model().up());
		translation.invert().apply();
		
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
			if (state == State.Paused) rotation -= 90 * deltaTime();
			else player.move(8 * keyboardSpeed * Scene.deltaTime());
			break;
		case Keyboard.KEY_LEFT:
			if (state == State.Paused) rotation += 90 * deltaTime();
			else player.move(-8 * keyboardSpeed * Scene.deltaTime());
			break;
		case Keyboard.KEY_UP:
			player.zoomIn(10.0 * Scene.deltaTime());
			break;
		case Keyboard.KEY_DOWN:
			player.zoomOut(10.0 * Scene.deltaTime());
			break;
		default:
			break;
		}
	}
	
	public void pause() {
		if (state == State.Running || state == State.Resuming) {
			playback.pause();
			state = State.Paused;
		} else if (state == State.Paused) {
			state = State.Resuming;
		}
	}
	
	@Override
	public void keyReleased(int key, char character) {
		switch (key) {
		case Keyboard.KEY_PAUSE:
			pause();
			break;
		case Keyboard.KEY_ESCAPE:
			if (state == State.Running) pause();
			else back();
			break;
		case Keyboard.KEY_HOME:
			translation.reset();
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
		player.move(dx * 0.002 * mouseSpeed);
	}
	
	@Override
	public void mouseDragged(int button, int x, int y, int dx, int dy) {
		player.move(dx * 0.002 * mouseSpeed);
	}
	
	@Override
	public void mouseWheelRotated(int rotation, int x, int y) {
		player.zoomOut(rotation * 0.01);
	}
	
}
