package audiodrive.model;

import audiodrive.AudioDrive;
import audiodrive.audio.AudioFile;
import audiodrive.model.geometry.Vector;
import audiodrive.model.loader.Model;
import audiodrive.model.track.Block;
import audiodrive.model.track.Track;
import audiodrive.ui.components.Camera;
import audiodrive.ui.effects.ParticleEffects;
import audiodrive.ui.scenes.GameScene;
import audiodrive.utilities.Arithmetic;
import audiodrive.utilities.Log;

public class Player {
	
	private static final AudioFile CollectSound = new AudioFile("sounds/Collect.mp3");
	private static final AudioFile CollideSound = new AudioFile("sounds/Collide.mp3");
	
	private GameScene scene;
	private Model model;
	private Track track;
	
	private int hitpoints;
	private int collected;
	private int collided;
	private int points;
	private int damage;
	
	private double lookDistance = 2.5;
	private double eyeHeight = 1.0;
	private double eyeDistance = 2.0;
	private double lookShifting = 0.3;
	private double eyeShifting = 0.5;
	private double zoom = 1.0;
	
	private double jumpHeight = 0.05;
	private double jumpRate = 1.5;
	private double jumpProgress = 0.0;
	private boolean jumpUpwards = true;
	
	private double tiltThreshold = 0.001;
	private double tiltAngle = 25.0;
	private double tiltRate = 2.0;
	private double tiltProgress = 0.5;
	private double oldX = 0.0;
	private double tiltTime;
	
	public Player(GameScene scene) {
		this.scene = scene;
		track = scene.getTrack();
		double difficulty = Arithmetic.clamp(AudioDrive.Settings.getDouble("difficulty"));
		hitpoints = Math.max(1, (int) (track.getNumberOfObstacles() * (1 - difficulty)));
		Log.debug("track \"" + track.getAudio().getFile().getName() + "\"");
		Log.debug("difficulty " + difficulty);
		Log.debug("collectables " + track.getNumberOfCollectables());
		Log.debug("obstacles " + track.getNumberOfObstacles());
		Log.debug("hitpoints " + hitpoints);
	}
	
	public void update(double elapsed) {
		model.placement(track.getPlacement(scene.playtime()));
		double newX = model.translation().x();
		double moved = newX - oldX;
		oldX = newX;
		boolean tilting = tilt(elapsed, moved);
		jump(elapsed, tilting ? true : jumpUpwards);
		checkCollisions();
	}
	
	private void checkCollisions() {
		int iteration = track.getIndex(scene.playtime()).integer;
		track.getBlocks().stream().filter(block -> !block.isDestroyed() && block.iteration() == iteration).forEach(this::interact);
	}
	
	private boolean tilt(double elapsed, double moved) {
		double delta = Math.abs(moved);
		boolean tilt = delta > tiltThreshold;
		if (tilt) {
			double rate = tiltRate;
			if (moved > 0) { // right
				if (tiltProgress < 0.5) rate *= 2.0;
				tiltProgress += elapsed * rate;
				if (tiltProgress > 1.0) tiltProgress = 1.0;
			} else { // left
				if (tiltProgress > 0.5) rate *= 2.0;
				tiltProgress -= elapsed * rate;
				if (tiltProgress < 0.0) tiltProgress = 0.0;
			}
			tiltTime = scene.playtime();
		} else if (tiltProgress != 0.5) { // reset
			if (scene.playtime() - tiltTime < 0.1) return true; // delay
			double sign = -Math.signum(tiltProgress - 0.5);
			tiltProgress += sign * elapsed * tiltRate * 0.5;
			if (sign < 0 && tiltProgress < 0.5 || sign > 0 && tiltProgress > 0.5) tiltProgress = 0.5;
		} else {
			return false;
		}
		model.rotation().z(Arithmetic.smooth(-tiltAngle, tiltAngle, tiltProgress));
		return true;
	}
	
	private boolean jump(double elapsed, boolean upwards) {
		if (upwards) { // up
			if (jumpProgress == 1.0) return false;
			jumpProgress += elapsed * jumpRate;
			if (jumpProgress >= 1.0) {
				jumpUpwards = false;
				jumpProgress = 1.0;
			}
		} else { // down
			if (jumpProgress == 0.0) return false;
			jumpProgress -= elapsed * jumpRate;
			if (jumpProgress <= 0.0) {
				jumpUpwards = true;
				jumpProgress = 0.0;
			}
		}
		model.translation().y(Arithmetic.smooth(0, jumpHeight, jumpProgress));
		return true;
	}
	
	public void render() {
		model.render();
	}
	
	public void interact(Block block) {
		double x = -model.translation().x(); // FIXME still don't know why it's negative
		if (track.getRailRange(block.rail()).contains(x)) collide(block);
	}
	
	private void collide(Block block) {
		if (block.isCollectable()) {
			CollectSound.play();
			collected++;
			Log.trace("collected %1s", block);
			ParticleEffects.createParticles(block.color(), scene.playtime());
		} else {
			CollideSound.play();
			collided++;
			Log.trace("collided with %1s", block);
		}
		damage = Math.min(100, (int) Math.round(100.0 * collided / hitpoints));
		points = (int) Math.round(collected * (1 - damage * 0.01));
		block.destroy();
	}
	
	public Player model(Model model) {
		this.model = model;
		return this;
	}
	
	public Model model() {
		return model;
	}
	
	public int hitpoints() {
		return hitpoints;
	}
	
	public int collected() {
		return collected;
	}
	
	public int collided() {
		return collided;
	}
	
	public int points() {
		return points;
	}
	
	public int damage() {
		return damage;
	}
	
	public Player zoom(double zoom) {
		this.zoom = Arithmetic.clamp(zoom, 1.0, 3.0);
		return this;
	}
	
	public double zoom() {
		return zoom;
	}
	
	public void camera() {
		double slope = model().up().angle(Vector.Y) * Math.signum(model.direction().dot(Vector.Y));
		double height = eyeHeight + eyeHeight * slope;
		double distance = eyeDistance + eyeDistance * 0.6 * slope;
		height *= zoom;
		distance *= zoom;
		Vector eyePosition = eyeShifting == 0.0 ? model().position() : model().position().plus(model().translation().vector().multiplied(eyeShifting));
		Vector lookPosition = lookShifting == 0.0 ? model().position() : model().position().plus(model().translation().vector().multiplied(lookShifting));
		Camera.position(eyePosition.plus(model().direction().multiplied(-distance)).plus(model().up().multiplied(height)));
		Camera.lookAt(lookPosition.plus(model().direction().multiplied(lookDistance)));
	}
}
