package audiodrive.model.track;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import audiodrive.AudioDrive;
import audiodrive.Resources;
import audiodrive.model.Renderable;
import audiodrive.model.geometry.Color;
import audiodrive.model.geometry.transform.Placement;
import audiodrive.model.loader.Model;

public class Block implements Renderable {
	
	private static AtomicLong ID = new AtomicLong();
	
	private List<Model> models = Resources.getBlockModels();
	
	private long id;
	private Placement placement = new Placement();
	private Color color;
	private boolean collectable;
	private boolean destroyed;
	private boolean glowing;
	private int iteration;
	private int rail;
	private Model model;
	
	public Block(boolean collectable, int iteration, int rail) {
		id = ID.getAndIncrement();
		this.collectable = collectable;
		this.iteration = iteration;
		this.rail = rail;
		color = AudioDrive.Settings.getColor(collectable ? "block.collectable.color" : "block.obstacle.color");
		glowing = AudioDrive.Settings.getBoolean(collectable ? "block.collectable.glowing" : "block.obstacle.glowing");
		Resources.getBlockTexture();
		Resources.getReflectedBlockTexture();
	}
	
	public void update(int currentIteration) {
		final int distanceToPlayer = Math.abs(iteration - currentIteration);
		final int lodRange = 10;
		int lodIndex = models.size() - 1 - distanceToPlayer / lodRange;
		lodIndex = Math.max(0, lodIndex);
		model = models.get(lodIndex);
	}
	
	@Override
	public void render() {
		if (destroyed) return;
		model.scale(0.1);
		model.color(color);
		model.placement().set(placement);
		model.render();
	}
	
	public Model model() {
		return model;
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
	
	public boolean isGlowing() {
		return glowing;
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
	
	public double width() {
		return 0.8;
	}
	
	@Override
	public String toString() {
		return "Block " + id;
	}
	
}
