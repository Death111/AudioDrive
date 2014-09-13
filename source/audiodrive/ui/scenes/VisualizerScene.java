package audiodrive.ui.scenes;

import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.input.Keyboard;

import audiodrive.AudioDrive;
import audiodrive.audio.AnalyzedAudio;
import audiodrive.audio.AnalyzedChannel;
import audiodrive.audio.Playback;
import audiodrive.model.buffer.VertexBuffer;
import audiodrive.model.geometry.Vector;
import audiodrive.ui.components.Camera;
import audiodrive.ui.components.Scene;
import audiodrive.ui.components.Text;
import audiodrive.ui.effects.ShaderProgram;
import audiodrive.utilities.Buffers;
import audiodrive.utilities.Log;

public class VisualizerScene extends Scene {
	
	private Text title;
	private AnalyzedAudio audio;
	private double duration;
	
	private VertexBuffer canvas;
	private ShaderProgram shader;
	private AnalyzedChannel leftChannel;
	private AnalyzedChannel rightChannel;
	private Playback playback;
	
	private int iteration;
	private int bands;
	
	private boolean showMixedPeaks = true;
	private boolean showRightPeaks = true;
	private boolean showLeftPeaks = true;
	private boolean showFrequencies = true;
	
	public void enter(AnalyzedAudio audio) {
		this.audio = audio;
		super.enter();
	}
	
	@Override
	protected void entering() {
		Log.info("visualizing audio...");
		title = new Text("Visualizing \"" + audio.getFile().getName() + "\"").setFont(AudioDrive.Font).setSize(32).setPosition(10, 10);
		canvas = new VertexBuffer(Buffers.create(0, 0, 0, getHeight(), getWidth(), getHeight(), getWidth(), 0), 2).mode(GL_QUADS);
		shader = new ShaderProgram("shaders/default.vs", "shaders/spectrum.fs");
		
		bands = Math.min(audio.getBandCount(), 100);
		leftChannel = audio.getChannel(0);
		rightChannel = audio.getChannel(1);
		playback = new Playback(audio.getFile()).start();
		
		Camera.overlay(getWidth(), getHeight());
		glDisable(GL_CULL_FACE);
	}
	
	@Override
	protected void update(double elapsed) {
		duration += elapsed;
		iteration = (int) (audio.getIterationRate() * duration);
		if (iteration >= audio.getSpectraCount()) iteration = audio.getSpectraCount() - 1;
	}
	
	@Override
	protected void render() {
		glClear(GL_COLOR_BUFFER_BIT);
		title.render();
		drawFrequencies();
		drawPeaks();
		drawSpectrum();
	}
	
	private void drawSpectrum() {
		glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
		float[] leftSpectrum = leftChannel.getSpectrum(iteration);
		float[] rightSpectrum = rightChannel.getSpectrum(iteration);
		glBegin(GL_QUADS);
		double x = 0;
		double y = getHeight() * 0.5;
		double width = (double) getWidth() / audio.getBandCount();
		for (int band = 0; band < rightSpectrum.length; band++) {
			float amplitude = rightSpectrum[band];
			new Vector().x(x).y(y).glVertex();
			new Vector().x(x + width).y(y).glVertex();
			new Vector().x(x + width).y(y + amplitude).glVertex();
			new Vector().x(x).y(y + amplitude).glVertex();
			x += width;
		}
		x = 0;
		for (int band = 0; band < leftSpectrum.length; band++) {
			float amplitude = leftSpectrum[band];
			new Vector().x(x).y(y).glVertex();
			new Vector().x(x + width).y(y).glVertex();
			new Vector().x(x + width).y(y - amplitude).glVertex();
			new Vector().x(x).y(y - amplitude).glVertex();
			x += width;
		}
		glEnd();
	}
	
	private void drawPeaks() {
		if (iteration >= audio.getMix().getPeaks().size()) return;
		double y = getHeight() * 0.5;
		double width = getWidth();
		double height = getHeight() * 0.5;
		float mixedPeak = audio.getMix().getPeaks().get(iteration);
		float leftPeak = leftChannel.getPeaks().get(iteration);
		float rightPeak = rightChannel.getPeaks().get(iteration);
		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
		if (showMixedPeaks && mixedPeak > 0) {
			double peak = audio.getMix().getPeaks().clamp(mixedPeak);
			peak *= width;
			glBegin(GL_TRIANGLES);
			glColor4d(1, 1, 1, 1);
			glVertex2d(0, y);
			glColor4d(0.5, 0.5, 1, 0);
			glVertex2d(peak, y);
			glVertex2d(0, 0);
			glColor4d(1, 1, 1, 1);
			glVertex2d(0, y);
			glColor4d(0.5, 0.5, 1, 0);
			glVertex2d(peak, y);
			glVertex2d(0, getHeight());
			glEnd();
		}
		if (showLeftPeaks && leftPeak > 0) {
			double peak = leftChannel.getPeaks().clamp(leftPeak);
			peak *= height;
			glBegin(GL_QUADS);
			glColor4d(1, 1, 1, 1);
			glVertex2d(0, y);
			glVertex2d(width, y);
			glColor4d(0.5, 0.5, 1, 0);
			glVertex2d(width, y + peak);
			glVertex2d(0, y + peak);
			glEnd();
		}
		if (showRightPeaks && rightPeak > 0) {
			double peak = rightChannel.getPeaks().clamp(rightPeak);
			peak *= height;
			glBegin(GL_QUADS);
			glColor4d(1, 1, 1, 1);
			glVertex2d(0, y);
			glVertex2d(width, y);
			glColor4d(0.5, 0.5, 1, 0);
			glVertex2d(width, y - peak);
			glVertex2d(0, y - peak);
			glEnd();
		}
	}
	
	private void drawFrequencies() {
		if (!showFrequencies || shader == null) return;
		float[] spectrum = audio.getMix().getSpectrum(iteration);
		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
		shader.bind();
		shader.uniform("color").set(0.5, 0.5, 1.0);
		shader.uniform("resolution").set((float) getWidth(), (float) getHeight());
		shader.uniform("time").set(duration);
		shader.uniform("position").set(-0.5);
		shader.uniform("intensity").set(1.0);
		shader.uniform("scale").set(0.01);
		shader.uniform("numberOfBands").set(bands);
		for (int i = 0; i < bands; i++) {
			shader.uniform("bands[" + i + "].amplitude").set(spectrum[i]);
			shader.uniform("bands[" + i + "].frequency").set(audio.getFrequencyOfBand(i) * 0.01);
		}
		canvas.draw();
		shader.unbind();
	}
	
	@Override
	protected void exiting() {
		playback.stop();
		shader = null;
		title = null;
		duration = 0;
	}
	
	@Override
	public void keyReleased(int key, char character) {
		switch (key) {
		case Keyboard.KEY_ESCAPE:
			back();
			break;
		case Keyboard.KEY_UP:
			showMixedPeaks = !showMixedPeaks;
			break;
		case Keyboard.KEY_LEFT:
			showLeftPeaks = !showLeftPeaks;
			break;
		case Keyboard.KEY_RIGHT:
			showRightPeaks = !showRightPeaks;
			break;
		case Keyboard.KEY_DOWN:
			showFrequencies = !showFrequencies;
			break;
		default:
			break;
		}
	}
	
}