package audiodrive.model.tower;

import static org.lwjgl.opengl.GL11.glRotated;
import audiodrive.model.geometry.Color;
import audiodrive.model.geometry.transform.Transformation;
import audiodrive.model.loader.ModelLoader;

public class RotationTower extends MusicTower {
	
	private double rotation = 0;
	private Transformation transformation = new Transformation() {
		@Override
		public boolean ignorable() {
			return false;
		}
		
		@Override
		public void apply() {
			glRotated(rotation, 0, 1, 0);
		}
	};
	
	public RotationTower(int iteration) {
		this.iteration = iteration;
		this.scale = 2;
		if (model != null) return;
		
		model = ModelLoader.loadSingleModel("models/musictower2/musictower2");
		model.transformations().add(transformation);
	}
	
	public void render() {
		Color tmp_Color = color.clone().itensity(intensity);
		model.placement(placement).color(tmp_Color).scale(scale).render();
	}
	
	@Override
	public RotationTower intensity(double intensity, double time) {
		this.intensity = intensity;
		// TODO thomas, how does roation work, it's silly
		rotation += intensity * 2;
		rotation = rotation % 360;
		return this;
	}
	
	public double getR() {
		return rotation;
	}
	
}
