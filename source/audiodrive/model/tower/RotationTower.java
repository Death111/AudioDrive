package audiodrive.model.tower;

import audiodrive.Resources;
import audiodrive.model.geometry.Color;
import audiodrive.model.loader.Model;
import audiodrive.ui.components.Scene;

public class RotationTower extends MusicTower {
	
	private double rotation;
	
	public RotationTower(int iteration) {
		this.iteration = iteration;
		scale = 2;
	}
	
	@Override
	public void render() {
		Color tmp_Color = color.clone().itensity(intensity);
		Model model = Resources.getRotationTowerModel();
		model.rotation().yAdd(rotation * Scene.deltaTime());
		model.placement(placement).color(tmp_Color).scale(scale).render();
	}
	
	@Override
	public RotationTower intensity(double intensity) {
		this.intensity = intensity;
		return this;
	}
	
	public RotationTower rotation(double rotation) {
		this.rotation = rotation;
		return this;
	}
	
	public double getR() {
		return rotation;
	}
	
}
