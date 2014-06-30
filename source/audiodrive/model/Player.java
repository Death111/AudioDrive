package audiodrive.model;

import static org.lwjgl.opengl.GL11.*;
import audiodrive.model.loader.Model;

public class Player {
	
	private Model model;
	
	public void render() {
		glEnable(GL_LIGHTING);
		model.render();
		glDisable(GL_LIGHTING);
	}
	
	public Player model(Model model) {
		this.model = model;
		return this;
	}
	
	public Model model() {
		return model;
	}
	
}
