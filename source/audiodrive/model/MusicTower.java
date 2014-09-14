package audiodrive.model;

import audiodrive.model.geometry.Color;
import audiodrive.model.geometry.transform.Placement;
import audiodrive.model.loader.Model;
import audiodrive.model.loader.ModelLoader;

public class MusicTower {

	private double scale = 2;
	private static Model model;
	private Color color;
	private Placement placement;
	private int iteration;

	public MusicTower(int iteration) {
		this.iteration = iteration;

		if (model != null)
			return;

		model = ModelLoader.loadSingleModel("models/musictower1/musictower1");
	}

	public void render() {
		model.placement(placement).color(color).scale(scale).render();
	}

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
