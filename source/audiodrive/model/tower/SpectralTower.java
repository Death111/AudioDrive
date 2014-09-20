package audiodrive.model.tower;

import audiodrive.model.geometry.Vector;
import audiodrive.model.loader.Model;
import audiodrive.model.loader.ModelLoader;
import audiodrive.ui.components.Scene;

public class SpectralTower extends MusicTower {
	
	private static final Model model = ModelLoader.loadSingleModel("models/musictower3/musictower3");
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
		Vector up = model.placement().up();
		model.placement().set(placement);
		for (int i = 0; i < scales.length; i++) {
			double targetScale = spectrum[i] * Radius;
			double delta = targetScale - scales[i];
			scales[i] += delta * Scene.deltaTime() * Speed;
			double scale = scales[i];
			model.scaling().x(scale);
			model.scaling().z(scale);
			model.placement().position().add(up.multiplied(Spacing));
			model.color(color.itensity(spectrum[i] * intensity)).render();
		}
	}
	
}