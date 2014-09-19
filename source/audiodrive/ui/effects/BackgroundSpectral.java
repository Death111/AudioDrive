package audiodrive.ui.effects;

import static org.lwjgl.opengl.GL11.*;

import java.util.List;

import audiodrive.audio.AnalyzedAudio;
import audiodrive.audio.MinMax;
import audiodrive.audio.SpectraMinMax;
import audiodrive.model.geometry.Color;
import audiodrive.model.geometry.Vector;
import audiodrive.ui.components.Overlay;
import audiodrive.ui.scenes.GameScene;
import audiodrive.utilities.Arithmetic;

public class BackgroundSpectral extends Overlay {
	
	private GameScene scene;
	
	private int width, height;
	final AnalyzedAudio audio;
	
	private List<MinMax> minMax;
	
	public BackgroundSpectral(GameScene scene) {
		this.scene = scene;
		audio = scene.getTrack().getAudio();
		width = scene.getWidth();
		height = scene.getHeight();
		
		minMax = SpectraMinMax.getMinMax(audio.getMix());
	}
	
	public void update() {
		
	}
	
	@Override
	public void render() {
		super.render();
		drawSpectrum();
	}
	
	private void drawSpectrum() {
		int iteration = scene.getTrack().index().integer;
		if (iteration >= audio.getIterationCount()) return;
		final float[] spectrum = audio.getMix().getSpectrum(iteration);
		
		int bandSkip = (int) 50;
		final int bandCount = spectrum.length;
		float width = (float) this.width / (bandCount - bandSkip);
		
		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
		glBegin(GL_QUAD_STRIP);
		// TODO mirror
		for (int band = 0; band < bandCount; band += bandSkip) {
			final float f = spectrum[band];
			final MinMax minMax2 = minMax.get(band);
			final float scaleLinear = (float) Arithmetic.scaleLinear(f, 0, 1, minMax2.min, minMax2.max);
			Color.Lerp(Color.Black, Color.White, scaleLinear).itensity(.2).gl();
			new Vector().x(width * band).y(0).glVertex(); // top
			new Vector().x(width * band).y(height).glVertex(); // down
		}
		
		glEnd();
	}
	
}
