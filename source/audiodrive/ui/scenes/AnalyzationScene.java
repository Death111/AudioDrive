package audiodrive.ui.scenes;

import static org.lwjgl.opengl.GL11.*;
import audiodrive.AudioDrive;
import audiodrive.audio.AudioAnalyzer;
import audiodrive.audio.AudioFile;
import audiodrive.model.buffer.VertexBuffer;
import audiodrive.ui.components.Camera;
import audiodrive.ui.components.Scene;
import audiodrive.ui.components.Text;
import audiodrive.ui.effects.ShaderProgram;
import audiodrive.utilities.Buffers;
import audiodrive.utilities.Log;

public class AnalyzationScene extends Scene {
	
	private Text title;
	private AudioFile file;
	private AudioAnalyzer analyzer;
	private VertexBuffer canvas;
	private ShaderProgram shader;
	private double duration;
	
	public void enter(AudioFile file) {
		this.file = file;
		super.enter();
	}
	
	@Override
	public void entering() {
		title = new Text("Analyzing audio...").setFont(AudioDrive.Font).setSize(48).setPosition(20, 20);
		canvas = new VertexBuffer(Buffers.create(0, 0, 0, getHeight(), getWidth(), getHeight(), getWidth(), 0)).step(2).mode(GL_QUADS);
		shader = new ShaderProgram("shaders/default.vs", "shaders/analyzation.fs");
		Camera.overlay(getWidth(), getHeight());
		analyzer = new AudioAnalyzer();
		new Thread(() -> {
			Log.info("analyzing audio...");
			analyzer.analyze(file);
			Log.info("analyzation complete");
		}).start();
	}
	
	@Override
	public void update(double elapsed) {
		duration += elapsed;
		if (analyzer.isDone()) Scene.get(MenuScene.class).enter(analyzer.getResults());
	}
	
	@Override
	public void render() {
		glClear(GL_COLOR_BUFFER_BIT);
		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
		shader.bind();
		shader.uniform("time").set(duration);
		shader.uniform("resolution").set((float) getWidth(), (float) getHeight());
		canvas.draw();
		shader.unbind();
		title.render();
	}
	
	@Override
	public void exiting() {
		shader = null;
		title = null;
	}
	
}
