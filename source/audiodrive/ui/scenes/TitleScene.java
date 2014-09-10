package audiodrive.ui.scenes;

import static org.lwjgl.opengl.GL11.*;
import audiodrive.AudioDrive;
import audiodrive.model.buffer.VertexBuffer;
import audiodrive.ui.components.Camera;
import audiodrive.ui.components.Scene;
import audiodrive.ui.components.Text;
import audiodrive.ui.effects.ShaderProgram;
import audiodrive.utilities.Buffers;

public class TitleScene extends Scene {
	
	private Text title;
	private VertexBuffer canvas;
	private ShaderProgram shader;
	
	@Override
	public void entering() {
		title = new Text(AudioDrive.Title).setFont(AudioDrive.Font).setSize(48).setCentered(getWidth() / 2, getHeight() / 2);
		canvas = new VertexBuffer(Buffers.create(0, 0, 0, getHeight(), getWidth(), getHeight(), getWidth(), 0), 2).mode(GL_QUADS);
		shader = new ShaderProgram("shaders/default.vs", "shaders/title.fs");
		Camera.overlay(getWidth(), getHeight());
	}
	
	@Override
	public void update(double elapsed) {
		if (time() >= 2.0) Scene.get(MenuScene.class).enter();
	}
	
	@Override
	public void render() {
		glClear(GL_COLOR_BUFFER_BIT);
		shader.bind();
		shader.uniform("time").set(time());
		shader.uniform("resolution").set((float) getWidth(), (float) getHeight());
		canvas.draw();
		shader.unbind();
		title.render();
	}
	
	@Override
	public void exiting() {
		canvas = null;
		shader = null;
		title = null;
	}
	
}
