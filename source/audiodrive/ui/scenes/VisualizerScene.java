package audiodrive.ui.scenes;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;

import java.nio.DoubleBuffer;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

import audiodrive.AudioDrive;
import audiodrive.audio.AudioAnalyzer.AnalyzedAudio;
import audiodrive.audio.AudioAnalyzer.AnalyzedChannel;
import audiodrive.audio.AudioPlayer;
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
	private AudioPlayer player;
	private double duration;
	
	private ShaderProgram shader;
	private int screenVertexBuffer;
	private double iterationRate;
	private AnalyzedChannel leftChannel;
	private AnalyzedChannel rightChannel;
	private int bands;
	
	public void enter(AnalyzedAudio audio) {
		this.audio = audio;
		super.enter();
	}
	
	@Override
	protected void entering() {
		title = new Text("Visualizing \"" + audio.file.getName() + "\"...").setFont(AudioDrive.Font).setSize(48).setPosition(10, 10);
		Log.info("visualizing audio...");
		Camera.overlay(getWidth(), getHeight());
		
		shader = new ShaderProgram("shaders/default.vs", "shaders/spectrum.fs");
		
		screenVertexBuffer = glGenBuffers();
		DoubleBuffer vertices = Buffers.create(new Vector(0, getHeight(), 0), new Vector(getWidth(), getHeight(), 0), new Vector(getWidth(), 0, 0), new Vector());
		glBindBuffer(GL_ARRAY_BUFFER, screenVertexBuffer);
		glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
		
		iterationRate = audio.getIterationRate();
		bands = Math.min(audio.getNumberOfBands(), 100);
		leftChannel = audio.channels.get(0);
		rightChannel = audio.channels.get(1);
		player = new AudioPlayer();
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
		
		int spectaIndex = (int) Math.round(iterationRate * duration);
		if (spectaIndex >= leftChannel.spectra.size()) spectaIndex = 0;
		
		float[] spectrum = audio.mixed.spectra.get(spectaIndex);
		float[] leftSpectrum = leftChannel.spectra.get(spectaIndex);
		float[] rightSpectrum = rightChannel.spectra.get(spectaIndex);
		
		if (shader != null) {
			glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
			shader.bind();
			shader.uniform("time").set(duration);
			shader.uniform("resolution").set((float) getWidth(), (float) getHeight());
			shader.uniform("position").set(-0.5);
			shader.uniform("intensity").set(1.0);
			shader.uniform("scale").set(0.01);
			shader.uniform("numberOfBands").set(bands);
			for (int i = 0; i < bands; i++) {
				shader.uniform("bands[" + i + "].amplitude").set(spectrum[i]);
				shader.uniform("bands[" + i + "].frequency").set(audio.getFrequencyOfBand(i) * 0.01);
			}
			glEnableClientState(GL_VERTEX_ARRAY);
			glBindBuffer(GL_ARRAY_BUFFER, screenVertexBuffer);
			glVertexPointer(3, GL_DOUBLE, 0, 0);
			glDrawArrays(GL_QUADS, 0, 4);
			shader.unbind();
		}
		
		glDisable(GL_CULL_FACE);
		glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
		glBegin(GL_QUADS);
		double x = 0;
		double y = getHeight() * 0.5;
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
		shader.delete();
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
		default:
			break;
		}
	}
	
}