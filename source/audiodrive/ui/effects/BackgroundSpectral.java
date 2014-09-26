package audiodrive.ui.effects;

import static org.lwjgl.opengl.GL11.*;
import audiodrive.audio.AnalyzedAudio;
import audiodrive.model.geometry.Color;
import audiodrive.model.geometry.Vector;
import audiodrive.ui.components.Overlay;
import audiodrive.ui.scenes.GameScene;

public class BackgroundSpectral extends Overlay {
	
	private GameScene scene;
	
	private int width, height;
	final AnalyzedAudio audio;
	
	public BackgroundSpectral(GameScene scene) {
		this.scene = scene;
		audio = scene.getTrack().getAudio();
		width = scene.getWidth();
		height = scene.getHeight();
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
		
		int bandSkip = 50;
		float width = (float) this.width / (audio.getBandCount() - bandSkip);
		
		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
		glBegin(GL_QUAD_STRIP);
		// TODO mirror
		for (int band = 0; band < audio.getBandCount(); band += bandSkip) {
			final float scaleLinear = audio.getMix().getBands().get(band).getClamped(iteration);
			Color.lerp(Color.Black, Color.White, scaleLinear).itensity(.2).gl();
			new Vector().x(width * band).y(0).glVertex(); // top
			new Vector().x(width * band).y(height).glVertex(); // down
		}
		
		glEnd();
	}
	
}
