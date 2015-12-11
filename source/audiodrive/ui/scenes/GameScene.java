package audiodrive.ui.scenes;

import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import audiodrive.AudioDrive;
import audiodrive.Resources;
import audiodrive.audio.AudioResource;
import audiodrive.audio.Playback;
import audiodrive.model.Player;
import audiodrive.model.geometry.Color;
import audiodrive.model.geometry.Vector;
import audiodrive.model.geometry.transform.Rotation;
import audiodrive.model.geometry.transform.Translation;
import audiodrive.model.track.Track;
import audiodrive.ui.GL;
import audiodrive.ui.components.Camera;
import audiodrive.ui.components.Light;
import audiodrive.ui.components.Scene;
import audiodrive.ui.components.Window;
import audiodrive.ui.effects.Particles2D;
import audiodrive.ui.overlays.GameBackground;
import audiodrive.ui.overlays.GameOverlay;
import audiodrive.utilities.Arithmetic;
import audiodrive.utilities.CameraPath;
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
	public static boolean spectrum;
	public static boolean peaks;
	public static boolean hitbox;
	public static boolean health;
	
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
	private Particles2D particles2D;
	
	private CameraPath startCameraPath;
	
	private double time;
	private double keyboardSpeed;
	private double mouseSpeed;
	private double volume;
	
	public void setup() {
		track = AudioDrive.getTrack();
		colorizeCollectables = !AudioDrive.Settings.getBoolean("block.collectable.color.static");
		colorizeObstacles = !AudioDrive.Settings.getBoolean("block.obstacle.color.static");
		spectrum = visualization = AudioDrive.Settings.getBoolean("game.visualization");
		environment = AudioDrive.Settings.getBoolean("game.environment");
		reflections = AudioDrive.Settings.getBoolean("graphics.reflections");
		particles = AudioDrive.Settings.getBoolean("graphics.particles");
		glow = AudioDrive.Settings.getBoolean("graphics.glow");
		night = AudioDrive.Settings.getBoolean("game.night");
		sky = AudioDrive.Settings.getBoolean("game.sky");
		keyboardSpeed = AudioDrive.Settings.getDouble("input.keyboard.speed");
		mouseSpeed = AudioDrive.Settings.getDouble("input.mouse.speed");
		volume = AudioDrive.Settings.getDouble("sound.volume");
		health = AudioDrive.Settings.getBoolean("player.healthbar");
		particles2D = new Particles2D();
		track.build();
		player = new Player(this).model(Resources.getCurrentPlayerModel());
		player.model().scale(0.05);
		overlay = new GameOverlay(this);
		background = new GameBackground(this);
		playback = new Playback(track.getAudio().getResource()).setVolume(AudioDrive.Settings.getDouble("music.volume"));
	}
	
	@Override
	protected void entering() {
		Log.info("Starting game...");
		Log.debug("Playing \"%s\"...", track.getAudio().getName());
		translation.reset();
		rotation = 0;
		time = 0;
		track.player(player);
		track.update(0);
		player.update(0);
		Camera.perspective(45, getWidth(), getHeight(), Near, Far);
		GL.pushAttributes();
		Light.enable();
		Light.Zero.diffuse(Color.White).ambient(Color.White.intensity(0.15)).on();
		Light.One.direction(Vector.Y.negated()).diffuse(Color.White.intensity(0.5)).on();
		glEnable(GL_CULL_FACE);
		glEnable(GL_NORMALIZE);
		glColorMaterial(GL_FRONT_AND_BACK, GL_AMBIENT_AND_DIFFUSE);
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
		Light.Zero.position(player.model().position());
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
				Log.info("Game started.");
			}
			return;
		}
		if (state == State.Resuming && rotation == 0) {
			state = State.Running;
			playback.resume();
			Log.info("Game resumed.");
			return;
		}
		if (state != State.Running) return;
		if (player.damage() >= 100) {
			state = State.Destroyed;
			playback.stop();
			rotate = true;
			new AudioResource("sounds/Destroyed.mp3").play(volume);
			Log.info("Destroyed. Game ended.");
			return;
		}
		if (track.index().integer == track.lastIndex()) {
			state = State.Ended;
			rotate = true;
			Log.info("Game ended.");
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
		
		if (state == State.Animating) {
			Camera.perspective(45, getWidth(), getHeight(), Near, Far);
			startCameraPath.camera();
		} else {
			player.camera();
		}
		
		translation.apply();
		
		Vector position = player.model().position().plus(player.model().translation().vector());
		Translation translation = new Translation().set(position);
		translation.apply();
		GL.rotate(rotation, player.model().up());
		translation.invert().apply();
		
		track.render();
		player.render();
		if (health) player.renderHealth();
		if (glow) track.glow().render();
		
		overlay.render();
	}
	
	@Override
	protected void exiting() {
		GL.popAttributes();
		Mouse.setGrabbed(false);
		playback.stop();
		playback = null;
		startCameraPath = null;
		particles2D = null;
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
	
	public Particles2D particles() {
		return particles2D;
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
		case Keyboard.KEY_END:
			rotate = false;
			break;
		default:
			break;
		}
	}
	
	public void pause() {
		if (state == State.Running || state == State.Resuming) {
			playback.pause();
			state = State.Paused;
			Log.info("Game paused.");
		} else if (state == State.Paused) {
			state = State.Resuming;
			Log.info("Game resuming...");
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
			if (state == State.Animating) {
				if (startCameraPath.isSkippable()) startCameraPath.skip();
			} else if (state == State.Running) pause();
			else {
				if (state != State.Ended && state != State.Destroyed) Log.info("Game canceled.");
				Scene.get(MenuScene.class).enter();
			}
			break;
		case Keyboard.KEY_HOME:
			translation.reset();
			player.zoom(1.0);
			break;
		case Keyboard.KEY_A:
			if (Keyboard.isKeyDown(Keyboard.KEY_RCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) Window.toggleAntialiasing();
			break;
		case Keyboard.KEY_V:
			if (Keyboard.isKeyDown(Keyboard.KEY_RCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) Window.toggleVSync();
			else visualization = !visualization;
			break;
		case Keyboard.KEY_D:
			if (Keyboard.isKeyDown(Keyboard.KEY_RCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) { // debugging
				if (state == State.Animating) startCameraPath.pause(!startCameraPath.isPaused());
				hitbox = !hitbox;
			}
			break;
		case Keyboard.KEY_E:
			environment = !environment;
			break;
		case Keyboard.KEY_G:
			glow = !glow;
			break;
		case Keyboard.KEY_H:
			health = !health;
			break;
		case Keyboard.KEY_P:
			if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) peaks = !peaks;
			else particles = !particles;
			break;
		case Keyboard.KEY_R:
			reflections = !reflections;
			break;
		case Keyboard.KEY_S:
			if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) spectrum = !spectrum;
			else sky = !sky;
			break;
		case Keyboard.KEY_LEFT:
			if (state != State.Running) {
				rotationDirection = 1;
				rotate = !Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);
			}
			break;
		case Keyboard.KEY_RIGHT:
			if (state != State.Running) {
				rotationDirection = -1;
				rotate = !Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);
			}
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
