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
import audiodrive.ui.effects.ParticleEffects;
import audiodrive.ui.overlays.GameBackground;
import audiodrive.ui.overlays.GameOverlay;
import audiodrive.utilities.Arithmetic;
import audiodrive.utilities.Buffers;
import audiodrive.utilities.CameraPath;
import audiodrive.utilities.Files;
import audiodrive.utilities.Log;

public class GameScene extends Scene {
	
	public static final double Near = 0.1;
	public static final double Far = 100000;
	
	public static boolean colorizeCollectables;
	public static boolean colorizeObstacles;
	public static boolean visualization;
	public static boolean environment;
	public static boolean reflections;
	public static boolean particles;
	public static boolean glow;
	public static boolean night;
	public static boolean sky;
	public static boolean peaks;
	
	public static enum State {
		Animating, Running, Paused, Resuming, Ended, Destroyed
	}
	
	private State state;
	private Track track;
	private Player player;
	
	private Translation translation = new Translation();
	private double rotation = 0;
	private int rotationDirection = 1;
	private boolean rotate = true;
	
	private Playback playback;
	private GameOverlay overlay;
	private GameBackground background;
	private ParticleEffects particleEffects;
	
	private CameraPath startCameraPath;
	
	private double time;
	private double keyboardSpeed;
	private double mouseSpeed;
	private double volume;
	
	public GameScene() {
		draggable = false;
	}
	
	public void enter(Track track) {
		this.track = track;
		super.enter();
	}
	
	@Override
	protected void entering() {
		Log.info("Starting game...");
		colorizeCollectables = !AudioDrive.Settings.getBoolean("block.collectable.color.static");
		colorizeObstacles = !AudioDrive.Settings.getBoolean("block.obstacle.color.static");
		visualization = AudioDrive.Settings.getBoolean("game.visualization");
		environment = AudioDrive.Settings.getBoolean("game.environment");
		reflections = AudioDrive.Settings.getBoolean("graphics.reflections");
		particles = AudioDrive.Settings.getBoolean("graphics.particles");
		glow = AudioDrive.Settings.getBoolean("graphics.glow");
		night = AudioDrive.Settings.getBoolean("game.night");
		sky = AudioDrive.Settings.getBoolean("game.sky");
		keyboardSpeed = AudioDrive.Settings.getDouble("input.keyboard.speed");
		mouseSpeed = AudioDrive.Settings.getDouble("input.mouse.speed");
		volume = AudioDrive.Settings.getDouble("sound.volume");
		particleEffects = new ParticleEffects();
		track.build();
		File model = Files.find("models/player", AudioDrive.Settings.get("player.model") + ".obj").orElse(Files.list("models/player", ".obj", true).get(0));
		player = new Player(this).model(ModelLoader.loadSingleModel(model.getPath()));
		player.model().scale(0.05);
		overlay = new GameOverlay(this);
		background = new GameBackground(this);
		playback = new Playback(track.getAudio().getFile()).setVolume(AudioDrive.Settings.getDouble("music.volume"));
		translation.reset();
		rotation = 0;
		time = 0;
		track.player(player);
		track.update(0);
		player.update(0);
		Camera.perspective(45, getWidth(), getHeight(), Near, Far);
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
		player.camera();
		startCameraPath = new CameraPath("camera/start.camera", false);
		startCameraPath.setOffsets(Camera.position(), Camera.at());
		state = State.Animating;
	}
	
	@Override
	protected void update(double elapsed) {
		checkState();
		updateRotation(elapsed);
		overlay.update();
		background.update();
		player.update();
		if (state != State.Running) return;
		time = playback.getTime();
		track.update(playtime());
		player.update(elapsed);
	}
	
	private void checkState() {
		if (state == State.Animating) {
			if (startCameraPath.isFinished()) {
				state = State.Running;
				playback.start();
			}
			return;
		}
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
	
	private void updateRotation(double elapsed) {
		if (rotateable()) {
			if (rotate) rotation += rotationDirection * 15 * elapsed;
		} else if (state == State.Resuming) {
			rotation = Rotation.unify180(rotation);
			rotation -= Arithmetic.smooth(10 * rotation, 1, Math.abs(rotation) / 360) * elapsed;
			rotation = Arithmetic.significance(rotation, 0.1);
		}
	}
	
	@Override
	protected void render() {
		if (night) glClearColor(0, 0, 0, 1);
		else glClearColor(1, 1, 1, 1);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		background.render();
		
		Camera.perspective(45, getWidth(), getHeight(), Near, Far);
		if (state == State.Animating) startCameraPath.camera();
		else player.camera();
		
		translation.apply();
		
		Vector position = player.model().position().plus(player.model().translation().vector());
		Translation translation = new Translation().set(position);
		translation.apply();
		GL.rotate(rotation, player.model().up());
		translation.invert().apply();
		
		track.render();
		player.render();
		if (glow) track.glow().render();
		
		overlay.render();
	}
	
	@Override
	protected void exiting() {
		GL.popAttributes();
		playback.stop();
		Mouse.setGrabbed(false);
		overlay = null;
		background = null;
		track = null;
		player = null;
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
	
	public boolean rotateable() {
		return state == State.Paused || state == State.Destroyed || state == State.Ended;
	}
	
	public ParticleEffects particleEffects() {
		return particleEffects;
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
			if (rotateable()) rotation -= 90 * deltaTime();
			else player.move(8 * keyboardSpeed * Scene.deltaTime());
			break;
		case Keyboard.KEY_LEFT:
			if (rotateable()) rotation += 90 * deltaTime();
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
		case Keyboard.KEY_RETURN:
			if (state == State.Animating) {
				if (!startCameraPath.isFinished()) startCameraPath.skip();
				break;
			}
		case Keyboard.KEY_SPACE:
		case Keyboard.KEY_PAUSE:
			if (state != State.Animating) pause();
			break;
		case Keyboard.KEY_ESCAPE:
			if (state == State.Running) pause();
			else if (state == State.Animating && !startCameraPath.isFinished()) startCameraPath.skip();
			else back();
			break;
		case Keyboard.KEY_HOME:
			translation.reset();
			player.zoom(1.0);
			break;
		case Keyboard.KEY_A:
			Window.toggleAntialiasing();
			break;
		case Keyboard.KEY_V:
			Window.toggleVSync();
			break;
		case Keyboard.KEY_D:
			peaks = !peaks;
			break;
		case Keyboard.KEY_G:
			glow = !glow;
			break;
		case Keyboard.KEY_P:
			particles = !particles;
			break;
		case Keyboard.KEY_R:
			reflections = !reflections;
			break;
		case Keyboard.KEY_S:
			sky = !sky;
			break;
		case Keyboard.KEY_LEFT:
			if (state == State.Paused) {
				rotationDirection = 1;
				rotate = true;
			}
			break;
		case Keyboard.KEY_RIGHT:
			if (state == State.Paused) {
				rotationDirection = -1;
				rotate = true;
			}
			break;
		case Keyboard.KEY_END:
			rotate = false;
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
