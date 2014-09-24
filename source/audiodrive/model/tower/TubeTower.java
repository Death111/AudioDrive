package audiodrive.model.tower;

import audiodrive.Resources;
import audiodrive.model.geometry.Color;

public class TubeTower extends MusicTower {
	
	public TubeTower(int iteration) {
		this.iteration = iteration;
		scale = 2;
	}
	
	@Override
	public MusicTower intensity(double intensity) {
		this.intensity = intensity;
		return this;
	}
	
	@Override
	public void render() {
		Color tmp_Color = color.clone().itensity(intensity);
		Resources.getTubeTowerModel().placement(placement).color(tmp_Color).scale(scale).render();
		
	}
}
