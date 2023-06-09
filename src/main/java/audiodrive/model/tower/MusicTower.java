package audiodrive.model.tower;

import audiodrive.model.geometry.Color;
import audiodrive.model.geometry.transform.Placement;

public abstract class MusicTower {
	protected double scale = 2;
	protected Color color;
	protected Placement placement;
	protected int iteration;
	protected double intensity;
	
	public abstract void render();
	
	public abstract MusicTower intensity(double intensity);
	
	public MusicTower scale(double scale) {
		this.scale = scale;
		return this;
	}
	
	public int iteration() {
		return iteration;
	}
	
	public MusicTower color(Color color) {
		this.color = color;
		return this;
	}
	
	public Color color() {
		return color;
	}
	
	public MusicTower placement(Placement placement) {
		this.placement = placement;
		return this;
	}
	
	public Placement placement() {
		return placement;
	}
}
