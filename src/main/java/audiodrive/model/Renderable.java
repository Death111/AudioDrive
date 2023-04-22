package audiodrive.model;

public interface Renderable {
	
	default void update(double time) {}
	
	void render();
	
}
