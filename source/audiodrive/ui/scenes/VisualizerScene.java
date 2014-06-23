package audiodrive.ui.scenes;

import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

import audiodrive.AudioDrive;
import audiodrive.audio.AudioAnalyzer.AnalyzedChannel;
import audiodrive.audio.AudioAnalyzer.AnalyzedAudio;
import audiodrive.audio.AudioPlayer;
import audiodrive.model.geometry.Vector;
import audiodrive.ui.components.Camera;
import audiodrive.ui.components.Scene;
import audiodrive.ui.components.Text;
import audiodrive.utilities.Log;

public class VisualizerScene extends Scene {
	
	private Text title;
	private AnalyzedAudio audio;
	private AudioPlayer player;
	private double duration;
	
	public void enter(AnalyzedAudio audio) {
		this.audio = audio;
		player = new AudioPlayer();
		super.enter();
	}
	
	@Override
	protected void entering() {
		title = new Text("Visualizing \"" + audio.file.getName() + "\"...").setFont(AudioDrive.Font).setSize(48).setPosition(10, 10);
		Log.info("visualizing audio...");
		Camera.overlay(Display.getWidth(), Display.getHeight());
		player.play(audio.file);
	}
	
	@Override
	protected void update(double elapsed) {
		duration += elapsed;
	}
	
	@Override
	protected void render() {
		glClear(GL_COLOR_BUFFER_BIT);
		title.render();
		
		glDisable(GL_CULL_FACE);
		glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
		
		AnalyzedChannel left = audio.channels.get(0);
		AnalyzedChannel right = audio.channels.get(1);
		
		double spectraPerSecond = (double) audio.samples.getSampleRate() / audio.samples.getIteration();
		int spectaIndex = (int) Math.round(spectraPerSecond * duration);
		if (spectaIndex >= left.spectra.size()) spectaIndex = 0;
		
		float[] leftSpectrum = left.spectra.get(spectaIndex);
		float[] rightSpectrum = right.spectra.get(spectaIndex);
		
		glBegin(GL_QUADS);
		double x = 0;
		double y = Display.getHeight() * 0.5;
		double width = (double) Display.getWidth() / leftSpectrum.length;
		for (int band = 0; band < rightSpectrum.length; band++) {
			float amplitude = rightSpectrum[band];
			new Vector().x(x).y(y).gl();
			new Vector().x(x + width).y(y).gl();
			new Vector().x(x + width).y(y + amplitude).gl();
			new Vector().x(x).y(y + amplitude).gl();
			x += width;
		}
		x = 0;
		for (int band = 0; band < leftSpectrum.length; band++) {
			float amplitude = leftSpectrum[band];
			new Vector().x(x).y(y).gl();
			new Vector().x(x + width).y(y).gl();
			new Vector().x(x + width).y(y - amplitude).gl();
			new Vector().x(x).y(y - amplitude).gl();
			x += width;
		}
		glEnd();
	}
	
	@Override
	protected void exiting() {
		player.stop();
		duration = 0;
	}
	
	@Override
	public void keyReleased(int key, char character) {
		switch (key) {
		case Keyboard.KEY_ESCAPE:
			back();
			break;
		default:
			break;
		}
	}
	
}
