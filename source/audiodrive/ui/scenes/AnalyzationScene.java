package audiodrive.ui.scenes;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;

import java.nio.DoubleBuffer;

import audiodrive.AudioDrive;
import audiodrive.audio.AudioAnalyzer;
import audiodrive.audio.AudioFile;
import audiodrive.model.geometry.Vector;
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
	private ShaderProgram shader;
	private int vertexBuffer;
	private double duration;
	
	public void enter(AudioFile file) {
		this.file = file;
		super.enter();
	}
	
	@Override
	public void entering() {
		title = new Text("Analyzing audio...").setFont(AudioDrive.Font).setSize(48).setPosition(20, 20);
		shader = new ShaderProgram("shaders/default.vs", "shaders/analyzation.fs");
		vertexBuffer = glGenBuffers();
		DoubleBuffer vertices = Buffers.create(new Vector(0, getHeight(), 0), new Vector(getWidth(), getHeight(), 0), new Vector(getWidth(), 0, 0), new Vector());
		glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer);
		glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
		Camera.overlay(getWidth(), getHeight());
		
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
		glEnableClientState(GL_VERTEX_ARRAY);
		glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer);
		glVertexPointer(3, GL_DOUBLE, 0, 0);
		glDrawArrays(GL_QUADS, 0, 4);
		shader.unbind();
		title.render();
	}
	
	@Override
	public void exiting() {
		shader.delete();
		shader = null;
		title = null;
	}
	
}
