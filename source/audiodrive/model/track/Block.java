package audiodrive.model.track;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.newdawn.slick.opengl.Texture;

import audiodrive.AudioDrive;
import audiodrive.model.geometry.Color;
import audiodrive.model.geometry.transform.Placement;
import audiodrive.model.loader.Model;
import audiodrive.model.loader.ModelLoader;

public class Block {
	
	public static final Color CollectableColor = AudioDrive.Settings.getColor("color.collectable");
	public static final Color ObstacleColor = AudioDrive.Settings.getColor("color.obstacle");
	private static final List<Model> Models = ModelLoader.loadModels("models/obstacle/obstacle_lod");
	public static final Texture Texture = ModelLoader.getTexture("models/obstacle/obstacle_lod.png");
	public static final Texture Reflected = ModelLoader.getTexture("models/obstacle/obstacle-reflection.png");
	
	private static AtomicLong ID = new AtomicLong();
	
	private long id;
	private Placement placement = new Placement();
	private Color color;
	private boolean collectable;
	private boolean destroyed;
	private int iteration;
	private int rail;
	private Model Model = Models.get(0);
	
	public Block(boolean collectable, int iteration, int rail) {
		id = ID.getAndIncrement();
		this.collectable = collectable;
		this.iteration = iteration;
		this.rail = rail;
		color = collectable ? CollectableColor : ObstacleColor;
	}
	
	public void update(int currentIteration) {
		final int distanceToPlayer = Math.abs(iteration - currentIteration);
		final int lodRange = 10;
		int lodIndex = Models.size() - 1 - distanceToPlayer / lodRange;
		lodIndex = Math.max(0, lodIndex);
		Model = Models.get(lodIndex);
	}
	
	public void render() {
		if (destroyed) return;
		Model.scale(0.1);
		Model.placement().set(placement);
		Model.color(color);
		Model.render();
	}
	
	public Model model() {
		return Model;
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
