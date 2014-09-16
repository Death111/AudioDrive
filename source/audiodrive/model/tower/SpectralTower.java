package audiodrive.model.tower;

import audiodrive.model.loader.Model;
import audiodrive.model.loader.ModelLoader;
import audiodrive.ui.components.Scene;

public class SpectralTower extends MusicTower {
	
	private static final Model Model = ModelLoader.loadSingleModel("models/musictower3/musictower3");
	private static final double Radius = 2.0;
	private static final double Spacing = 0.5;
	private static final double Speed = 20;
	private static boolean mirrored = true;
	
	private static double spectrum[];
	private static double scales[];
	
	public static void spectrum(double... intensities) {
		if (mirrored) {
			spectrum = new double[intensities.length * 2];
			if (scales == null || scales.length != spectrum.length) {
				scales = new double[spectrum.length];
			}
			for (int i = 0; i < intensities.length; i++) {
				spectrum[intensities.length + i] = spectrum[intensities.length - 1 - i] = intensities[i];
			}
		} else {
			spectrum = intensities;
			if (scales == null || scales.length != spectrum.length) {
				scales = new double[spectrum.length];
			}
		}
	}
	
	public SpectralTower(int iteration) {
		this.iteration = iteration;
		intensity = 1.0;
	}
	
	@Override
	public MusicTower intensity(double intensity) {
		this.intensity = intensity;
		return this;
	}
	
	@Override
	public void render() {
		Model.placement().set(placement);
		for (int i = 0; i < scales.length; i++) {
			double targetScale = spectrum[i] * Radius;
			double delta = targetScale - scales[i];
			scales[i] += delta * Scene.deltaTime() * Speed;
			double scale = scales[i];
			Model.scaling().x(scale);
			Model.scaling().z(scale);
			Model.placement().position().yAdd(Spacing);
			Model.color(color.itensity(spectrum[i] * intensity)).render();
		}
	}
	
}