package audiodrive.model.track;

import static org.lwjgl.opengl.GL11.glDepthMask;

import org.newdawn.slick.opengl.Texture;

import audiodrive.Resources;
import audiodrive.model.Renderable;
import audiodrive.model.geometry.Color;
import audiodrive.model.geometry.transform.Placement;
import audiodrive.model.loader.Model;
import audiodrive.utilities.Arithmetic;

public class Ring implements Renderable {
	
	private Model model = Resources.getRingModel();
	private Texture normalTexture = Resources.getRingTexture();
	private Texture pulseTexture = Resources.getRingPulseTexture();
	
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
		model.setTexture(normalTexture);
		model.placement(placement).color(color).scale(scale + scale * 0.15 * pulse).render();
		if (pulse > 0) {
			model.setTexture(pulseTexture);
			model.color(color.alpha(pulse)).render();
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
	
	public Color color() {
		return this.color;
	}
	
	public Ring color(Color color) {
		this.color = color;
		return this;
	}
	
}
