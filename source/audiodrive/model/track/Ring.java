package audiodrive.model.track;

import static org.lwjgl.opengl.GL11.glDepthMask;

import org.newdawn.slick.opengl.Texture;

import audiodrive.model.Renderable;
import audiodrive.model.geometry.Color;
import audiodrive.model.geometry.transform.Placement;
import audiodrive.model.loader.Model;
import audiodrive.model.loader.ModelLoader;
import audiodrive.utilities.Arithmetic;

public class Ring implements Renderable {
	
	public static final Texture Default = ModelLoader.getTexture("textures/ring/ring.png");
	public static final Texture Pulse = ModelLoader.getTexture("textures/ring/ring-pulse.png");
	private static final Model Model = ModelLoader.loadSingleModel("models/quad/quad").setTexture(Default);
	
	private Color color;
	private Placement placement;
	private double scale = 2;
	private double pulse;
	private int iteration;
	
	public Ring(int iteration, Color color, Placement placement) {
		this.iteration = iteration;
		this.color = color;
		this.placement = placement;
	}
	
	@Override
	public void render() {
		glDepthMask(false); // disable depth
		Model.setTexture(Default);
		Model.placement(placement).color(color).scale(scale + scale * 0.15 * pulse).render();
		if (pulse > 0) {
			Model.setTexture(Pulse);
			Model.color(color.alpha(pulse)).render();
		}
		glDepthMask(true); // enable depth
	}
	
	public int iteration() {
		return iteration;
	}
	
	public Placement placement() {
		return placement;
	}
	
	public Ring pulse(double pulse) {
		this.pulse = Arithmetic.clamp(pulse);
		return this;
	}
	
	public Ring scale(double scale) {
		this.scale = scale;
		return this;
	}
	
	public double width() {
		return scale + scale * 0.15 * pulse;
	}
	
}
