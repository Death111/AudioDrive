package audiodrive.ui.scenes;

import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.input.Keyboard;

import audiodrive.AudioDrive;
import audiodrive.audio.AnalyzedAudio;
import audiodrive.audio.AnalyzedChannel;
import audiodrive.audio.Playback;
import audiodrive.model.buffer.VertexBuffer;
import audiodrive.model.geometry.Color;
import audiodrive.model.geometry.Vector;
import audiodrive.ui.components.Camera;
import audiodrive.ui.components.Scene;
import audiodrive.ui.components.Text;
import audiodrive.ui.components.Text.Alignment;
import audiodrive.ui.effects.ShaderProgram;
import audiodrive.utilities.Arithmetic;
import audiodrive.utilities.Buffers;
import audiodrive.utilities.Log;

public class VisualizerScene extends Scene {
	
	private enum Mode {
		Normal, Direct, Logaritmic, Clamped
	}
	
	private Text title;
	private Text info;
	private AnalyzedAudio audio;
	private double duration;
	
	private VertexBuffer canvas;
	private ShaderProgram shader;
	private AnalyzedChannel leftChannel;
	private AnalyzedChannel rightChannel;
	private float[] leftSpectrum;
	private float[] rightSpectrum;
	private float[] newLeftSpectrum;
	private float[] newRightSpectrum;
	private Playback playback;
	
	private Color color = Color.White;
	private Color fade = new Color(0.5, 0.5, 1, 0);
	
	private int iteration;
	private int bands;
	private int freqencies;
	
	private boolean showMixedPeaks = false;
	private boolean showRightPeaks = false;
	private boolean showLeftPeaks = false;
	private boolean showFrequencies = true;
	
	private Mode mode = Mode.Normal;
	private boolean filled = true;
	private boolean smooth = false;
	private double speed = 15;
	private double scale = 1;
	private int combine = 0;
	
	public void enter(AnalyzedAudio audio) {
		this.audio = audio;
		super.enter();
	}
	
	@Override
	protected void entering() {
		Log.info("visualizing audio...");
		title = new Text("Visualizing \"" + audio.getResource().getName() + "\"").setFont(AudioDrive.Font).setSize(32).setPosition(20, 20);
		info = new Text().setFont(AudioDrive.Font).setSize(10).setPosition(20, 70).setAlignment(Alignment.UpperLeft);
		canvas = new VertexBuffer(Buffers.create(0, 0, 0, getHeight(), getWidth(), getHeight(), getWidth(), 0), 2).mode(GL_QUADS);
		shader = new ShaderProgram("shaders/default.vs", "shaders/spectrum.fs");
		
		freqencies = Math.min(audio.getBandCount(), 100);
		leftChannel = audio.getChannel(0);
		rightChannel = audio.getChannel(1);
		leftSpectrum = new float[audio.getBandCount()];
		rightSpectrum = new float[audio.getBandCount()];
		newLeftSpectrum = new float[audio.getBandCount()];
		newRightSpectrum = new float[audio.getBandCount()];
		playback = new Playback(audio.getResource()).start();
		
		Camera.overlay(getWidth(), getHeight());
		glDisable(GL_CULL_FACE);
	}
	
	@Override
	protected void update(double elapsed) {
		duration += elapsed;
		iteration = (int) (audio.getIterationRate() * duration);
		if (iteration >= audio.getIterationCount()) iteration = audio.getIterationCount() - 1;
		
		bands = audio.getBandCount();
		switch (mode) {
		case Normal:
			info.setText("Normal Mode");
			for (int band = 0; band < audio.getBandCount(); band++) {
				newLeftSpectrum[band] = leftChannel.clamp(leftChannel.getBands().get(band).get(iteration)) * 2;
				newRightSpectrum[band] = rightChannel.clamp(leftChannel.getBands().get(band).get(iteration)) * 2;
			}
			break;
		case Direct:
			info.setText("Direct Mode");
			System.arraycopy(leftChannel.getSpectrum(iteration), 0, leftSpectrum, 0, bands);
			System.arraycopy(rightChannel.getSpectrum(iteration), 0, rightSpectrum, 0, bands);
			break;
		case Clamped:
			info.setText("Clamped Mode");
			for (int band = 0; band < audio.getBandCount(); band++) {
				newLeftSpectrum[band] = leftChannel.getBands().get(band).getClamped(iteration);
				newRightSpectrum[band] = rightChannel.getBands().get(band).getClamped(iteration);
			}
			break;
		case Logaritmic:
			info.setText("Logaritmic Mode");
			for (int band = 0; band < audio.getBandCount(); band++) {
				newLeftSpectrum[band] = (float) Arithmetic.scaleLogarithmic(leftChannel.getBands().get(band).get(iteration), 0, 1, 0, leftChannel.getMaximum());
				newRightSpectrum[band] = (float) Arithmetic.scaleLogarithmic(rightChannel.getBands().get(band).get(iteration), 0, 1, 0, leftChannel.getMaximum());
			}
			break;
		}
		
		info.setText(info.getText() + String.format(" (scale %.1f)", scale));
		
		if (combine > 1) {
			info.setText(info.getText() + " (combined " + combine + " bands)");
			bands = Math.min(bands / combine + 1, bands);
			float[] left = newLeftSpectrum;
			float[] right = newRightSpectrum;
			newLeftSpectrum = new float[newLeftSpectrum.length];
			newRightSpectrum = new float[newRightSpectrum.length];
			for (int i = 0; i < audio.getBandCount(); i++) {
				newLeftSpectrum[i / combine] += left[i] / combine;
				newRightSpectrum[i / combine] += right[i] / combine;
			}
		}
		
		if (smooth) {
			info.setText(info.getText() + " (speed " + speed + ") (smoothed)");
			for (int band = 0; band < bands; band++) {
				float leftDelta = newLeftSpectrum[band] - leftSpectrum[band];
				float rightDelta = newRightSpectrum[band] - rightSpectrum[band];
				leftSpectrum[band] += Math.signum(leftDelta) * Arithmetic.smooth(0, 1, leftChannel.clamp(Math.abs(leftDelta))) * 10000;
				rightSpectrum[band] += Math.signum(rightDelta) * Arithmetic.smooth(0, 1, rightChannel.clamp(Math.abs(rightDelta))) * 10000;
				leftSpectrum[band] += leftDelta * Scene.deltaTime() * speed * 0.25;
				rightSpectrum[band] += rightDelta * Scene.deltaTime() * speed * 0.25;
			}
		} else if (speed > 0) {
			info.setText(info.getText() + String.format(" (speed %.1f)", speed));
			for (int band = 0; band < bands; band++) {
				leftSpectrum[band] += (newLeftSpectrum[band] - leftSpectrum[band]) * Scene.deltaTime() * speed;
				rightSpectrum[band] += (newRightSpectrum[band] - rightSpectrum[band]) * Scene.deltaTime() * speed;
			}
		} else {
			info.setText(info.getText() + " (direct speed)");
			System.arraycopy(newLeftSpectrum, 0, leftSpectrum, 0, bands);
			System.arraycopy(newRightSpectrum, 0, rightSpectrum, 0, bands);
		}
	}
	
	@Override
	protected void render() {
		glClear(GL_COLOR_BUFFER_BIT);
		title.render();
		info.render();
		drawFrequencies();
		drawPeaks();
		drawSpectrum();
	}
	
	private void drawSpectrum() {
		if (!filled) glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
		glBegin(GL_QUADS);
		double x = 0;
		double y = getHeight() / 2;
		double width = (double) getWidth() / bands;
		double height = getHeight() / 2 * scale;
		for (int band = 0; band < bands; band++) {
			double amplitude = rightSpectrum[band] * height;
			color.gl();
			new Vector().x(x).y(y).glVertex();
			new Vector().x(x + width).y(y).glVertex();
			if (filled) fade.gl();
			new Vector().x(x + width).y(y + amplitude).glVertex();
			new Vector().x(x).y(y + amplitude).glVertex();
			x += width;
		}
		x = 0;
		for (int band = 0; band < bands; band++) {
			double amplitude = leftSpectrum[band] * height;
			color.gl();
			new Vector().x(x).y(y).glVertex();
			new Vector().x(x + width).y(y).glVertex();
			if (filled) fade.gl();
			new Vector().x(x + width).y(y - amplitude).glVertex();
			new Vector().x(x).y(y - amplitude).glVertex();
			x += width;
		}
		glEnd();
	}
	
	private void drawPeaks() {
		if (iteration >= audio.getMix().getPeaks().size()) return;
		Color color = Color.White;
		Color fade = new Color(0.5, 0.5, 1, 0);
		double y = getHeight() / 2;
		double width = getWidth();
		double height = getHeight() / 2;
		float mixedPeak = audio.getMix().getPeaks().get(iteration);
		float leftPeak = leftChannel.getPeaks().get(iteration);
		float rightPeak = rightChannel.getPeaks().get(iteration);
		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
		if (showMixedPeaks && mixedPeak > 0) {
			double peak = audio.getMix().getPeaks().clamp(mixedPeak);
			peak *= width;
			glBegin(GL_TRIANGLES);
			color.gl();
			glVertex2d(0, y);
			fade.gl();
			glVertex2d(peak, y);
			glVertex2d(0, 0);
			color.gl();
			glVertex2d(0, y);
			fade.gl();
			glVertex2d(peak, y);
			glVertex2d(0, getHeight());
			glEnd();
		}
		if (showLeftPeaks && leftPeak > 0) {
			double peak = leftChannel.getPeaks().clamp(leftPeak);
			peak *= height;
			glBegin(GL_QUADS);
			color.gl();
			glVertex2d(0, y);
			glVertex2d(width, y);
			fade.gl();
			glVertex2d(width, y + peak);
			glVertex2d(0, y + peak);
			glEnd();
		}
		if (showRightPeaks && rightPeak > 0) {
			double peak = rightChannel.getPeaks().clamp(rightPeak);
			peak *= height;
			glBegin(GL_QUADS);
			color.gl();
			glVertex2d(0, y);
			glVertex2d(width, y);
			fade.gl();
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
		shader.uniform("numberOfBands").set(freqencies);
		for (int i = 0; i < freqencies; i++) {
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
		super.keyReleased(key, character);
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
		case Keyboard.KEY_SPACE:
			showLeftPeaks = false;
			showMixedPeaks = false;
			showRightPeaks = false;
			showFrequencies = false;
			break;
		case Keyboard.KEY_N:
			mode = Mode.Normal;
			smooth = false;
			scale = 1.0;
			speed = 15;
			combine = 0;
			break;
		case Keyboard.KEY_C:
			if (mode == Mode.Clamped) mode = Mode.Normal;
			else mode = Mode.Clamped;
			break;
		case Keyboard.KEY_D:
			if (mode == Mode.Direct) mode = Mode.Normal;
			else mode = Mode.Direct;
			break;
		case Keyboard.KEY_L:
			if (mode == Mode.Logaritmic) mode = Mode.Normal;
			else mode = Mode.Logaritmic;
			break;
		case Keyboard.KEY_S:
			smooth = !smooth;
			break;
		case Keyboard.KEY_F:
			filled = !filled;
			break;
		case Keyboard.KEY_MULTIPLY:
			combine = Arithmetic.clamp(combine - 1, 1, 50);
			break;
		case Keyboard.KEY_DIVIDE:
			combine = Arithmetic.clamp(combine + 1, 1, 50);
			break;
		default:
			break;
		}
	}
	
	@Override
	public void keyPressed(int key, char character) {
		switch (key) {
		case Keyboard.KEY_ADD:
			speed = Arithmetic.clamp(speed + 0.1, 0, 50);
			break;
		case Keyboard.KEY_SUBTRACT:
			speed = Arithmetic.clamp(speed - 0.1, 0, 50);
			break;
		default:
			break;
		}
	}
	
	@Override
	public void mouseWheelRotated(int rotation, int x, int y) {
		scale = Arithmetic.clamp(scale + Math.signum(rotation) * 0.1, 0.1, 10);
	}
	
}