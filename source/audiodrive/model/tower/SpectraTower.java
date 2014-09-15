package audiodrive.model.tower;

import audiodrive.model.geometry.Color;
import audiodrive.model.geometry.Vector;
import audiodrive.model.geometry.transform.Scaling;
import audiodrive.model.loader.ModelLoader;
import audiodrive.ui.components.Scene;
import audiodrive.utilities.Arithmetic;

public class SpectraTower extends MusicTower {
	
	private double scales[];
	private double a;
	
	public SpectraTower(int iteration) {
		this.iteration = iteration;
		scale = 2;
		
		if (model != null) return;
		
		model = ModelLoader.loadSingleModel("models/musictower3/musictower3");
	}
	
	@Override
	public MusicTower intensity(double intensity) {
		this.intensity = intensity;
		
		double elapsedTime = Scene.deltaTime();
		a += elapsedTime;
		
		for (int i = 0; i < scales.length; i++) {
			final double newScale = Arithmetic.smooth(scales[i], scale * intensity, 1);
			scales[i] = newScale;
		}
		
		if (a > 1) a = 0;
		return this;
	}
	
	double intensities[];
	
	public MusicTower intensity(double... intensity) {
		intensities = intensity;
		this.intensity = intensity[0];
		scales = new double[intensity.length];
		for (int i = 0; i < scales.length; i++) {
			final double newScale = Arithmetic.smooth(scales[i], scale * intensity[i], 1);
			scales[i] = newScale;
		}
		
		return this;
	}
	
	@Override
	public void render() {
		final Vector originalPosition = placement.position();
		final Scaling scaling = model.scaling();
		double origXScale = scaling.x();
		double origZScale = scaling.z();
		final double offset = .75;
		
		for (int i = 0; i < scales.length; i++) {
			Color tmp_Color = color.clone().itensity(intensities[i]);
			scaling.x(scales[i]);
			scaling.z(scales[i]);
			model.placement(placement).color(tmp_Color).render();
			placement.position(originalPosition.clone().yAdd(offset));
		}
		
		placement.position(originalPosition);
		scaling.x(origXScale);
		scaling.z(origZScale);
	}
}
