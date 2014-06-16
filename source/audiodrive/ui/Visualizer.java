package audiodrive.ui;

import static org.lwjgl.opengl.GL11.*;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.concurrent.TimeUnit;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;

import audiodrive.audio.AudioAnalyzer;
import audiodrive.audio.AudioAnalyzer.Results;
import audiodrive.audio.AudioFile;
import audiodrive.audio.AudioPlayer;
import audiodrive.model.geometry.Vector;
import audiodrive.ui.components.Camera;
import audiodrive.ui.components.Window;
import audiodrive.ui.control.Input;
import audiodrive.utilities.Log;

public class Visualizer {
	
	/** Application title. */
	public static final String Title = "Spline";
	/** Frame rate in frames per second. */
	public static final int Framerate = 100;
	
	public static final boolean Fullscreen = false;
	
	private static long secondTimestamp;
	private static int frames;
	private static int fps;

	private static AudioAnalyzer analyzer;
	
	/** Private constructor to prevent instantiation. */
	private Visualizer() {
		throw new IllegalStateException("This class shall not be instantiated.");
	}
	
	public static void visualize(AudioFile file) {
		Log.info("analyzing...");
		analyzer = new AudioAnalyzer().analyze(file);
		AudioPlayer player = new AudioPlayer();
		try {
			if (Fullscreen) Window.setBorderless(true);
			else {
				Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
				Window.setSize(size.width - 100, 600);
			}
			Display.setResizable(true);
			Display.setTitle(Title);
			Display.create();
			Input.addObserver(observer);
			player.play(file);
			while (!Display.isCloseRequested()) {
				Visualizer.tick();
				Visualizer.render();
				Display.update();
				Display.sync(Framerate);
				Input.update();
			}
			player.stop();
			Display.destroy();
		} catch (LWJGLException exception) {
			throw new RuntimeException(exception);
		}
	}

	private static long startTime;

	private static void render() {
		if (startTime == 0) startTime = System.currentTimeMillis();

		Results left = analyzer.getResults(0);
		Results right = analyzer.getResults(1);
		
		long time = System.currentTimeMillis();
		double seconds = (time - startTime) / 1000.0;
		double spectraPerSecond = (double) analyzer.getSamples().getSampleRate() / analyzer.getSamples().getIteration();
		int spectaIndex = (int) Math.round(spectraPerSecond * seconds);
		if (spectaIndex >= left.spectra.size()) spectaIndex = 0;
		
		float[] leftSpectrum = left.spectra.get(spectaIndex);
		float[] rightSpectrum = right.spectra.get(spectaIndex);
		
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		glDisable(GL_CULL_FACE);
		glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
		
		Camera.overlay(Display.getWidth(), Display.getHeight());
		
		glBegin(GL_QUADS);
		double x = 0;
		double y = Display.getHeight() * 0.5;
		double width = (double) Display.getWidth() / leftSpectrum.length;
		for (int band = 0; band < leftSpectrum.length; band++) {
			float amplitude = leftSpectrum[band];
			new Vector().x(x).y(y).gl();
			new Vector().x(x + width).y(y).gl();
			new Vector().x(x + width).y(y + amplitude).gl();
			new Vector().x(x).y(y + amplitude).gl();
			x += width;
		}
		x = 0;
		for (int band = 0; band < rightSpectrum.length; band++) {
			float amplitude = rightSpectrum[band];
			new Vector().x(x).y(y).gl();
			new Vector().x(x + width).y(y).gl();
			new Vector().x(x + width).y(y - amplitude).gl();
			new Vector().x(x).y(y - amplitude).gl();
			x += width;
		}
		glEnd();
	}

	private static void tick() {
		long time = System.nanoTime();
		if (time - secondTimestamp >= TimeUnit.SECONDS.toNanos(1)) {
			fps = frames;
			Visualizer.update();
			frames = 0;
			secondTimestamp = time;
		}
		frames++;
	}

	public static void update() {
		Display.setTitle(Title + " : " + fps + " FPS)");
	}
	
	public static int getFramerate() {
		return fps;
	}
	
	private static Input.Observer observer = new Input.Observer() {};

}
