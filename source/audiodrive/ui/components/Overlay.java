package audiodrive.ui.components;

import static org.lwjgl.opengl.GL11.GL_QUADS;
import audiodrive.model.buffer.VertexBuffer;
import audiodrive.ui.effects.ShaderProgram;
import audiodrive.utilities.Buffers;

public class Overlay {
	
	public static final int Width = Window.getWidth();
	public static final int Height = Window.getHeight();
	public static final VertexBuffer Canvas = new VertexBuffer(Buffers.create(0, 0, 0, Height, Width, Height, Width, 0), 2).mode(GL_QUADS);
	
	private ShaderProgram shader;
	
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
			setup(shader);
			Canvas.draw();
			shader.unbind();
		}
	}
	
	protected void setup(ShaderProgram shader) {
		shader.uniform("time").set(Scene.time());
		shader.uniform("resolution").set((float) Width, (float) Height);
	}
	
}
