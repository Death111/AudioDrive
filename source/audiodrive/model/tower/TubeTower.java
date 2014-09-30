package audiodrive.model.tower;

import audiodrive.Resources;
import audiodrive.model.loader.Model;

public class TubeTower extends MusicTower {
	
	private Model model = Resources.getTubeTowerModel();
	
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
		model.placement(placement).color(color.intensity(intensity)).scale(scale).render();
		
	}
}
