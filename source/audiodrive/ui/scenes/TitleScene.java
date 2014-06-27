package audiodrive.ui.scenes;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;

import java.nio.DoubleBuffer;

import audiodrive.AudioDrive;
import audiodrive.model.geometry.Vector;
import audiodrive.ui.components.Camera;
import audiodrive.ui.components.Scene;
import audiodrive.ui.components.Text;
import audiodrive.ui.effects.ShaderProgram;
import audiodrive.utilities.Buffers;

public class TitleScene extends Scene {
	
	private Text title;
	private double duration;
	private ShaderProgram shader;
	private int vertexBuffer;
	
	@Override
	public void entering() {
		title = new Text(AudioDrive.Title).setFont(AudioDrive.Font).setSize(48).setCentered(getWidth() / 2, getHeight() / 2);
		shader = new ShaderProgram("shaders/default.vs", "shaders/title.fs");
		vertexBuffer = glGenBuffers();
		DoubleBuffer vertices = Buffers.create(new Vector(0, getHeight(), 0), new Vector(getWidth(), getHeight(), 0), new Vector(getWidth(), 0, 0), new Vector());
		glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer);
		glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
		Camera.overlay(getWidth(), getHeight());
	}
	
	@Override
	public void update(double elapsed) {
		duration += elapsed;
		if (duration >= 2.0) Scene.get(AudioSelectionScene.class).enter();
	}
	
	@Override
	public void render() {
		glClear(GL_COLOR_BUFFER_BIT);
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
