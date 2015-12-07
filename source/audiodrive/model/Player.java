package audiodrive.model;

import static org.lwjgl.opengl.GL11.*;
import audiodrive.AudioDrive;
import audiodrive.audio.AudioResource;
import audiodrive.model.geometry.Color;
import audiodrive.model.geometry.Vector;
import audiodrive.model.geometry.transform.Rotation;
import audiodrive.model.loader.Model;
import audiodrive.model.track.Block;
import audiodrive.model.track.Track;
import audiodrive.ui.components.Camera;
import audiodrive.ui.components.Scene;
import audiodrive.ui.scenes.GameScene;
import audiodrive.ui.scenes.GameScene.State;
import audiodrive.utilities.Arithmetic;
import audiodrive.utilities.Log;
import audiodrive.utilities.Range;

public class Player implements Renderable {
	
	private static final AudioResource CollectSound = new AudioResource("sounds/Collect.mp3");
	private static final AudioResource CollideSound = new AudioResource("sounds/Collide.mp3");
	private static final Color CollisionColor = new Color(0.8, 0.2, 0.2, 1);
	
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
	private double eyeShifting = 0.3;
	
	private double zoom = 1.0;
	private double zoomTarget = 1.0;
	private double zoomMinimum = 1.0;
	private double zoomMaximum = 5.0;
	private double zoomSpeed = 1.0;
	
	private double jumpHeight = 0.05;
	private double jumpRate = 1.5;
	private double jumpProgress = 0.0;
	private boolean jumpUpwards = true;
	
	private double tiltAngle = 20.0;
	private double tiltRate = 1.0;
	private double tiltProgress = 0.5;
	private double oldX = 0.0;
	
	private int hitboxStart;
	private int hitboxEnd;
	private double hitboxSide;
	
	private double glowing;
	private boolean hit;
	private boolean destroyed;
	
	private double volume = AudioDrive.Settings.getDouble("sound.volume");
	
	private Rotation inclination = new Rotation();
	
	public Player(GameScene scene) {
		this.scene = scene;
		track = scene.getTrack();
		double difficulty = Arithmetic.clamp(AudioDrive.Settings.getDouble("game.difficulty"));
		hitpoints = Math.max(1, (int) (track.getNumberOfObstacles() * (1 - difficulty)));
		Log.debug("track \"" + track.getAudio().getResource().getName() + "\"");
		Log.debug("difficulty " + difficulty);
		Log.debug("collectables " + track.getNumberOfCollectables());
		Log.debug("obstacles " + track.getNumberOfObstacles());
		Log.debug("hitpoints " + hitpoints);
	}
	
	public void update() {
		if (damage >= 100) destroyed = true;
		else hit = !inclination.isNull();
		zoom(true);
	}
	
	@Override
	public void update(double elapsed) {
		model.placement(track.getPlacement(scene.playtime()));
		if (glowing > 0) {
			glowing -= elapsed;
			if (glowing < 0) glowing = 0;
		}
		double newX = model.translation().x();
		double moved = newX - oldX;
		oldX = newX;
		boolean tilting = tilt(elapsed, moved);
		boolean collision = checkCollisions(elapsed);
		jump(elapsed, tilting || collision ? true : jumpUpwards);
	}
	
	public void move(double x) {
		if (scene.getState() != State.Running) return;
		double newX = model.translation().x() - x; // FIXME why negative?
		double maxX = track.width() / 3;
		model.translation().x(Math.max(-maxX, Math.min(maxX, newX)));
	}
	
	private boolean zoom(boolean smooth) {
		if (zoom == zoomTarget) {
			return false;
		}
		double delta;
		if (smooth) {
			double fraction = Arithmetic.scaleLinear(Math.abs(zoomTarget - zoom), 0.1, 1, 0, zoomMaximum - zoomMinimum);
			delta = Arithmetic.smooth(0, Scene.deltaTime() * 10 * zoomSpeed, fraction);
		} else {
			delta = Scene.deltaTime() * 2.0 * zoomSpeed;
		}
		if (zoomTarget - zoom > 0) {
			zoom = Math.min(zoomTarget, zoom + delta);
		} else {
			zoom = Math.max(zoomTarget, zoom - delta);
		}
		return true;
	}
	
	private boolean tilt(double elapsed, double moved) {
		double delta = Math.abs(moved);
		if (delta > 0) {
			double rate = tiltRate;
			if (moved > 0) { // right
				if (tiltProgress < 0.5) rate *= 2.0;
				tiltProgress += rate * delta;
				if (tiltProgress > 1.0) tiltProgress = 1.0;
			} else { // left
				if (tiltProgress > 0.5) rate *= 2.0;
				tiltProgress -= rate * delta;
				if (tiltProgress < 0.0) tiltProgress = 0.0;
			}
			// tiltTime = scene.playtime();
		} else if (tiltProgress != 0.5) { // reset
			// if (scene.playtime() - tiltTime < 0.1) return true; // delay
			double sign = -Math.signum(tiltProgress - 0.5);
			tiltProgress += sign * elapsed * tiltRate;
			if (sign < 0 && tiltProgress < 0.5 || sign > 0 && tiltProgress > 0.5) tiltProgress = 0.5;
		} else {
			return false;
		}
		model.rotation().z(Arithmetic.smooth(tiltAngle, -tiltAngle, tiltProgress));
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
	
	private boolean checkCollisions(double elapsed) {
		double tiltFraction = 0.5 - Math.abs(0.5 - tiltProgress);
		hitboxStart = track.index().integer - (int) (track.indexRate() * elapsed);
		hitboxEnd = track.index().integer + (int) Math.ceil(track.index().fraction);
		hitboxSide = track.railWidth() * Arithmetic.smooth(0.01, 0.5, tiltFraction);
		long collisions = track
			.getBlocks()
			.stream()
			.filter(block -> !block.isDestroyed() && (block.iteration() >= hitboxStart && block.iteration() <= hitboxEnd))
			.filter(this::interact)
			.filter(block -> !block.isCollectable())
			.count();
		boolean collision = collisions > 0;
		if (collision) {
			double x = Math.max(-30, (inclination.x() - 5));
			inclination.x(x);
		} else {
			double x = Math.min(0, (inclination.x() + 15 * elapsed));
			inclination.x(x);
		}
		return collision;
	}
	
	@Override
	public void render() {
		if (!destroyed) {
			model.wireframe(false).color(hit ? CollisionColor : Color.White);
			model.render();
		}
		if (destroyed | hit) {
			model.wireframe(true).color(Color.TransparentRed);
			model.render();
		}
		if (GameScene.hitbox) renderHitbox();
	}
	
	private void renderHitbox() {
		Vector side = model.placement().side().multiplied(hitboxSide);
		Vector start = track.spline().get(Math.max(0, hitboxStart)).plus(model.translation().vector());
		Vector end = track.spline().get(Math.min(track.spline().size() - 1, hitboxEnd)).plus(model.translation().vector());
		Vector up = model.up().multiplied(0.2);
		start.add(up);
		end.add(up);
		Color.Red.gl();
		glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
		glLineWidth(3);
		glBegin(GL_QUADS);
		start.plus(side.negated()).glVertex();
		start.plus(side).glVertex();
		end.plus(side).glVertex();
		end.plus(side.negated()).glVertex();
		glEnd();
		glLineWidth(1);
		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
	}
	
	public boolean interact(Block block) {
		double x = -model.translation().x();
		Range playerWidth = new Range(x - hitboxSide, x + hitboxSide);
		Range railWidth = track.getRailRange(block.rail());
		boolean intersects = playerWidth.intersects(railWidth);
		if (intersects) collide(block);
		return intersects;
	}
	
	private void collide(Block block) {
		if (block.isCollectable()) {
			CollectSound.play(volume);
			collected++;
			Log.trace("collected %1s", block);
			if (GameScene.particles) scene.particles().createParticles(block);
			glowing = 0.25;
		} else {
			CollideSound.play(volume);
			collided++;
			Log.trace("collided with %1s", block);
		}
		damage = Math.min(100, (int) Math.round(100.0 * collided / hitpoints));
		points = (int) Math.round(collected * (1 - damage * 0.01));
		block.destroy();
	}
	
	public Player model(Model model) {
		if (this.model != null) model.transformations().remove(inclination);
		model.transformations().add(inclination);
		this.model = model;
		return this;
	}
	
	public Model model() {
		return model;
	}
	
	public Rotation inclination() {
		return inclination;
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
	
	public Player zoomIn(double delta) {
		zoomTo(zoomTarget - delta);
		return this;
	}
	
	public Player zoomOut(double delta) {
		zoomTo(zoomTarget + delta);
		return this;
	}
	
	public Player zoomTo(double zoom) {
		zoomTarget = Arithmetic.clamp(zoom, zoomMinimum, zoomMaximum);
		return this;
	}
	
	public Player zoom(double zoom) {
		this.zoom = zoomTarget = Arithmetic.clamp(zoom, zoomMinimum, zoomMaximum);
		return this;
	}
	
	public double zoom() {
		return zoom;
	}
	
	public boolean isGlowing() {
		return glowing > 0;
	}
	
	public void camera() {
		double slope = model().up().angle(Vector.Y) * Math.signum(model.direction().dot(Vector.Y));
		double height = eyeHeight + eyeHeight * slope;
		double distance = eyeDistance + eyeDistance * 0.6 * slope;
		height *= zoom;
		distance *= zoom;
		double fieldOfView = Arithmetic.smooth(45.0, 60.0 - 30.0 * slope, scene.playtime());
		Vector eyePosition = model().position().plus(model().translation().vector().multiplied(eyeShifting));
		Vector lookPosition = model().position().plus(model().translation().vector().multiplied(lookShifting));
		Camera.perspective(fieldOfView, scene.getWidth(), scene.getHeight(), GameScene.Near, GameScene.Far);
		Camera.position(eyePosition.plus(model().direction().multiplied(-distance)).plus(model().up().multiplied(height)));
		Camera.lookAt(lookPosition.plus(model().direction().multiplied(lookDistance)));
	}
}
