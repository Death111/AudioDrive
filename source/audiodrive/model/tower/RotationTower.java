package audiodrive.model.tower;

import audiodrive.model.geometry.Color;
import audiodrive.model.loader.ModelLoader;

public class RotationTower extends MusicTower {

	private double rotation = 0;

	public RotationTower(int iteration) {
		this.iteration = iteration;
		this.scale = 2;
		if (model != null)
			return;

		model = ModelLoader.loadSingleModel("models/musictower2/musictower2");
	}

	public void render() {
		Color tmp_Color = color.clone().itensity(intensity);
		model.placement(placement).color(tmp_Color).scale(scale).render();
	}

	@Override
	public RotationTower intensity(double intensity) {
		this.intensity = intensity;
		// TODO thomas, how does roation work, it's silly
		rotation += intensity * 0.2;
		this.model.rotation().y(rotation);
		return this;
	}

}
