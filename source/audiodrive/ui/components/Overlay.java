package audiodrive.ui.components;

import static org.lwjgl.opengl.GL11.GL_QUADS;

import java.util.function.Consumer;

import audiodrive.model.buffer.VertexBuffer;
import audiodrive.ui.effects.ShaderProgram;
import audiodrive.utilities.Buffers;

public class Overlay {
	
	private int width = Window.getWidth();
	private int height = Window.getHeight();
	private VertexBuffer canvas = new VertexBuffer(Buffers.create(0, 0, 0, height, width, height, width, 0), 2).mode(GL_QUADS);
	private ShaderProgram shader;
	private Consumer<ShaderProgram> setup;
	
	public Overlay shader(ShaderProgram shader) {
		this.shader = shader;
		return this;
	}
	
	public ShaderProgram shader() {
		return shader;
	}
	
	public void render() {
		if (shader != null) {
			shader.bind();
			shader.uniform("time").set(Scene.time());
			shader.uniform("resolution").set((float) width, (float) height);
			if (setup != null) setup.accept(shader);
			canvas.draw();
			shader.unbind();
		}
	}
	
	public Overlay update(Consumer<ShaderProgram> shader) {
		setup = shader;
		return this;
	}
	
}
