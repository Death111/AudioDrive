package audiodrive.ui.scenes;

import static org.lwjgl.opengl.GL11.*;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import audiodrive.audio.Playback;
import audiodrive.model.Player;
import audiodrive.model.geometry.ReflectionPlane;
import audiodrive.model.geometry.Vector;
import audiodrive.model.geometry.transform.Placement;
import audiodrive.model.geometry.transform.Rotation;
import audiodrive.model.geometry.transform.Translation;
import audiodrive.model.loader.ModelLoader;
import audiodrive.model.track.Track;
import audiodrive.ui.GL;
import audiodrive.ui.TrackOverview;
import audiodrive.ui.components.Camera;
import audiodrive.ui.components.Scene;
import audiodrive.utilities.Buffers;
import audiodrive.utilities.Log;

public class GameScene extends Scene {
	
	private Track track;
	private Player player;
	private List<ReflectionPlane> reflectionPlanes = new ArrayList<>(2);
	
	private Rotation rotation = new Rotation();
	private Translation translation = new Translation();
	
	private Vector look = new Vector();
	private Vector camera = new Vector(0, 0, 0.01);
	
	private double time;
	
	private Playback playback;
	private TrackOverview trackOverview;
	
	public void enter(Track track) {
		this.track = track;
		track.prepare();
		super.enter();
	}
	
	@Override
	protected void entering() {
		Log.info("starting game...");
		trackOverview = new TrackOverview(track);
		player = new Player().model(ModelLoader.loadSingleModel("models/xwing/xwing"));
		Vector current = track.spline().get(0);
		Vector next = track.spline().get(1);
		Vector position = current.plus(0, 0.0005, 0);
		Vector direction = next.minus(current);
		Vector up = Vector.Y;
		player.model().scale(0.0001).position(position).align(direction, up);
		playback = new Playback(track.getAudio().getFile());
		rotation.reset();
		translation.reset();
		updatePlacement();
		Camera.perspective(45, getWidth(), getHeight(), 0.001, 100);
		GL.pushAttributes();
		glEnable(GL_CULL_FACE);
		glEnable(GL_NORMALIZE);
		glEnable(GL_LIGHT0);
		glLight(GL_LIGHT0, GL_DIFFUSE, Buffers.create(1f, 1f, 1f, 1f));
		glEnable(GL_COLOR_MATERIAL);
		glColorMaterial(GL_FRONT, GL_DIFFUSE);
		glShadeModel(GL_SMOOTH);
		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glEnable(GL_LIGHTING);
		Mouse.setGrabbed(true);
	}
	
	@Override
	protected void update(double elapsed) {
		if (!playback.isRunning()) return;
		time += elapsed;
		// time = track.getDuration() - 0.11;
		player.update(elapsed);
		updatePlacement();
	}
	
	private void updatePlacement() {
		Placement placement = track.getPlacement(time);
		player.model().placement(placement);
		reflectionPlanes.clear();
		reflectionPlanes.addAll(track.getReflectionPlanes(time));
		trackOverview.updatePlayerPosition(track.getIndex(time));
	}
	
	@Override
	protected void render() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		Camera.overlay(getWidth(), getHeight());
		trackOverview.render();
		
		Camera.perspective(45, getWidth(), getHeight(), 0.001, 100);
		player.camera();
		
		translation.apply();
		// rotation.apply();
		
		reflectionPlanes.stream().forEach(plane -> plane.reflect(player.model()));
		track.render();
		player.render();
	}
	
	@Override
	protected void exiting() {
		GL.popAttributes();
		playback.stop();
		time = 0;
		Mouse.setGrabbed(false);
	}
	
	@Override
	public void keyPressed(int key, char character) {
		Vector translation = new Vector();
		switch (key) {
		case Keyboard.KEY_NUMPAD0:
			translation.add(0, 0, -0.0001);
			break;
		case Keyboard.KEY_NUMPAD2:
			translation.add(0, 0.0001, 0);
			break;
		case Keyboard.KEY_NUMPAD4:
			translation.add(0.0001, 0, 0);
			break;
		case Keyboard.KEY_NUMPAD5:
			translation.add(0, 0, 0.0001);
			break;
		case Keyboard.KEY_NUMPAD6:
			translation.add(-0.0001, 0, 0);
			break;
		case Keyboard.KEY_NUMPAD8:
			translation.add(0, -0.0001, 0);
			break;
		case Keyboard.KEY_RIGHT:
			movePlayer(0.0001);
			break;
		case Keyboard.KEY_LEFT:
			movePlayer(-0.0001);
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
			break;
		default:
			break;
		}
	}
	
	@Override
	public void mouseMoved(int x, int y, int dx, int dy) {
		if (!playback.isRunning()) return;
		movePlayer(dx * 0.000005);
	}
	
	private void movePlayer(double x) {
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
	
}
