package audiodrive.model.tower;

import audiodrive.model.geometry.Color;
import audiodrive.model.loader.ModelLoader;

public class TubeTower extends MusicTower {

	public TubeTower(int iteration) {
		this.iteration = iteration;
		this.scale = 2;

		if (model != null)
			return;

		model = ModelLoader.loadSingleModel("models/musictower1/musictower1");
	}

	@Override
	public MusicTower intensity(double intensity) {
		this.intensity = intensity;
		return this;
	}

	@Override
	public void render() {
		Color tmp_Color = color.clone().itensity(intensity);
		model.placement(placement).color(tmp_Color).scale(scale).render();

	}
}
