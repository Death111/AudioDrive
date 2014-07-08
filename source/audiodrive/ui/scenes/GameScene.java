package audiodrive.ui.scenes;

import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.input.Keyboard;

import audiodrive.audio.Playback;
import audiodrive.model.Player;
import audiodrive.model.geometry.Placement;
import audiodrive.model.geometry.Rotation;
import audiodrive.model.geometry.Vector;
import audiodrive.model.loader.ModelLoader;
import audiodrive.model.track.Track;
import audiodrive.ui.components.Camera;
import audiodrive.ui.components.Scene;
import audiodrive.utilities.Buffers;
import audiodrive.utilities.Log;

public class GameScene extends Scene {
	
	private Track track;
	private Player player;
	
	private Rotation rotation = new Rotation();
	private Vector translate = new Vector();
	
	private Vector look = new Vector();
	private Vector camera = new Vector(0, 0, 0.01);
	
	private double time;
	
	private Playback playback;
	
	public void enter(Track track) {
		this.track = track;
		track.prepare();
		super.enter();
	}
	
	@Override
	protected void entering() {
		Log.info("starting game...");
		player = new Player().model(ModelLoader.loadSingleModel("models/xwing/xwing"));
		Vector current = track.spline().get(0);
		Vector next = track.spline().get(1);
		Vector position = current.plus(0, 0.0005, 0);
		Vector direction = next.minus(current);
		Vector up = Vector.Y;
		player.model().scale(0.0001).position(position).align(direction, up);
		playback = new Playback(track.getAudio().getFile());
		rotation.reset();
		translate.set(Vector.Null);
	}
	
	@Override
	protected void update(double elapsed) {
		if (!playback.isRunning()) return;
		time += elapsed;
		// time = track.getDuration() - 0.11;
		Placement placement = track.calculatePlayerPlacement(time);
		player.model().placement(placement);
	}
	
	@Override
	protected void render() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
		
		glEnable(GL_NORMALIZE);
		glEnable(GL_LIGHT0);
		glLight(GL_LIGHT0, GL_AMBIENT, Buffers.create(1f, 1f, 1f, 1f));
		glShadeModel(GL_SMOOTH);
		
		Camera.perspective(45, getWidth(), getHeight(), 0.001, 100);
		// Camera.position(camera);
		// Camera.lookAt(look);
		player.camera();
		
		// rotation.apply();
		// glTranslated(translate.x(), translate.y(), translate.z());
		
		// glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
		track.render();
		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
		glDisable(GL_CULL_FACE); // FIXME model face culling
		player.render();
	}
	
	@Override
	protected void exiting() {
		playback.stop();
		time = 0;
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
		default:
			break;
		}
		translate.add(rotation.rotate(translation));
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
		default:
			break;
		}
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
			rotation.xAdd(vertical).zAdd(horizontal);
			break;
		case 2:
			rotation.yAdd(vertical).zAdd(horizontal);
			break;
		default:
			break;
		}
	}
	
}
