package audiodrive.model;

import audiodrive.model.geometry.Vector;
import audiodrive.model.loader.Model;
import audiodrive.ui.components.Camera;
import audiodrive.utilities.Arithmetic;

public class Player {
	
	private Model model;
	private double lookDistance = 2.5;
	private double eyeHeight = 1.0;
	private double eyeDistance = 2.0;
	private double lookShifting = 0.3;
	private double eyeShifting = 0.5;
	
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
	
	private double time;
	
	public void update(double elapsed) {
		time += elapsed;
		double newX = model.translation().x();
		double moved = newX - oldX;
		oldX = newX;
		boolean tilting = tilt(elapsed, moved);
		jump(elapsed, tilting ? true : jumpUpwards);
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
			tiltTime = time;
		} else if (tiltProgress != 0.5) { // reset
			if (time - tiltTime < 0.1) return true; // delay
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
	
	public Player model(Model model) {
		this.model = model;
		return this;
	}
	
	public Model model() {
		return model;
	}
	
	public void camera() {
		double slope = model().up().angle(Vector.Y) * Math.signum(model.direction().dot(Vector.Y));
		double height = eyeHeight + eyeHeight * slope;
		double distance = eyeDistance + eyeDistance * 0.6 * slope;
		Vector eyePosition = eyeShifting == 0.0 ? model().position() : model().position().plus(model().translation().vector().multiplied(eyeShifting));
		Vector lookPosition = lookShifting == 0.0 ? model().position() : model().position().plus(model().translation().vector().multiplied(lookShifting));
		Camera.position(eyePosition.plus(model().direction().multiplied(-distance)).plus(model().up().multiplied(height)));
		Camera.lookAt(lookPosition.plus(model().direction().multiplied(lookDistance)));
	}
}
