package audiodrive.model;

import audiodrive.model.loader.Model;
import audiodrive.ui.components.Camera;

public class Player {
	
	private Model model;
	private double lookDistance = 0.005;
	private double eyeHeight = 0.002;
	private double eyeDistance = 0.005;
	
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
		Camera.position(model().position().plus(model().direction().multiplied(-eyeDistance)).plus(model().up().multiplied(eyeHeight)));
		Camera.lookAt(model().position().plus(model().direction().multiplied(lookDistance)));
	}
}
