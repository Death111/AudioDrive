package audiodrive.ui.scenes;

import static org.lwjgl.opengl.GL11.*;

import java.util.concurrent.atomic.AtomicBoolean;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.SharedDrawable;

import audiodrive.AudioDrive;
import audiodrive.model.track.TrackGenerator;
import audiodrive.ui.components.Camera;
import audiodrive.ui.components.Overlay;
import audiodrive.ui.components.Scene;
import audiodrive.ui.components.Text;
import audiodrive.ui.effects.ShaderProgram;
import audiodrive.utilities.Stopwatch;

public class GenerationScene extends Scene {
	
	private AtomicBoolean done = new AtomicBoolean();
	
	private SharedDrawable drawable;
	private Thread thread;
	
	@Override
	public void entering() {
		try {
			drawable = new SharedDrawable(Display.getDrawable());
		} catch (LWJGLException exception) {
			throw new RuntimeException(exception);
		}
		thread = new Thread(() -> {
			Stopwatch stopwatch = new Stopwatch().start();
			try {
				drawable.makeCurrent();
				Text title = new Text("Generating track...").setFont(AudioDrive.Font).setSize(48).setPosition(20, 20);
				Overlay background = new Overlay().shader(new ShaderProgram("shaders/Default.vs", "shaders/Generation.fs")).update(shader -> {
					shader.uniform("time").set(stopwatch.getTotalSeconds());
				});
				Camera.reset();
				Camera.overlay(getWidth(), getHeight());
				while (!done.get()) {
					glClear(GL_COLOR_BUFFER_BIT);
					glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
					background.render();
					title.render();
					Display.update();
				}
				drawable.releaseContext();
			} catch (LWJGLException exception) {
				exception.printStackTrace();
			}
		});
		thread.setName("Generation Thread");
		thread.start();
	}
	
	@Override
	public void update(double elapsed) {
		AudioDrive.setTrack(TrackGenerator.generate(AudioDrive.getAnalyzedAudio()));
		Scene.get(GameScene.class).setup();
		done.set(true);
		try {
			thread.join();
		} catch (InterruptedException exception) {
			throw new RuntimeException(exception);
		}
		Scene.get(GameScene.class).enter();
	}
	
	@Override
	public void render() {}
	
	@Override
	public void exiting() {
		done.set(false);
		drawable = null;
		thread = null;
	}
	
}
