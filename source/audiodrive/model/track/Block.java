package audiodrive.model.track;

import java.util.concurrent.atomic.AtomicLong;

import org.newdawn.slick.opengl.Texture;

import audiodrive.AudioDrive;
import audiodrive.model.geometry.Color;
import audiodrive.model.geometry.transform.Placement;
import audiodrive.model.loader.Model;
import audiodrive.model.loader.ModelLoader;

public class Block {
	
	public static final Color CollectableColor = AudioDrive.Settings.getColor("collectableColor");
	public static final Color ObstacleColor = AudioDrive.Settings.getColor("obstacleColor");
	public static final Model Model = ModelLoader.loadSingleModel("models/obstacle/obstacle").scale(0.1);
	public static final Texture Reflected = ModelLoader.getTexture("models/obstacle/obstacle-reflection.png");
	
	private static AtomicLong ID = new AtomicLong();
	
	private long id;
	private Placement placement = new Placement();
	private Color color;
	private boolean collectable;
	private boolean destroyed;
	private int iteration;
	private int rail;
	
	public Block(boolean collectable, int iteration, int rail) {
		id = ID.getAndIncrement();
		this.collectable = collectable;
		this.iteration = iteration;
		this.rail = rail;
		color = collectable ? CollectableColor : ObstacleColor;
	}
	
	public void render() {
		if (destroyed) return;
		Model.placement().set(placement);
		Model.color(color);
		Model.render();
	}
	
	public Block placement(Placement placement) {
		this.placement = placement;
		return this;
	}
	
	public Placement placement() {
		return placement;
	}
	
	public Block color(Color color) {
		this.color = color;
		return this;
	}
	
	public Color color() {
		return color;
	}
	
	public boolean isCollectable() {
		return collectable;
	}
	
	public boolean isDestroyed() {
		return destroyed;
	}
	
	public long id() {
		return id;
	}
	
	public int iteration() {
		return iteration;
	}
	
	public int rail() {
		return rail;
	}
	
	public void destroy() {
		destroyed = true;
	}
	
	@Override
	public String toString() {
		return "Block " + id;
	}
	
}
