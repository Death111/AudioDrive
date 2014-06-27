package audiodrive.ui;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;

import java.nio.FloatBuffer;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

import audiodrive.ui.effects.ShaderProgram;
import audiodrive.utilities.Buffers;

public class ShaderViewer {
	
	public static void show(String vertexShaderFilename, String fragmentShaderFilename) {
		try {
			System.setProperty("org.lwjgl.opengl.Window.undecorated", "true");
			Display.setTitle("Shader Demo");
			Display.setVSyncEnabled(true);
			Display.create();
		} catch (LWJGLException exception) {
			exception.printStackTrace();
			Display.destroy();
			return;
		}
		
		ShaderProgram shader = new ShaderProgram(vertexShaderFilename, fragmentShaderFilename);
		
		int vertexBuffer = glGenBuffers();
		FloatBuffer vertices = Buffers.create(-1f, -1f, 1f, -1f, 1f, 1f, -1f, 1f);
		glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer);
		glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
		
		long timestamp = System.currentTimeMillis();
		while (!Display.isCloseRequested()) {
			if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) break;
			long time = System.currentTimeMillis();
			float seconds = (time - timestamp) / 1000f;
			shader.bind();
			shader.uniform("time").set(seconds);
			shader.uniform("resolution").set((float) Display.getWidth(), (float) Display.getHeight());
			glEnableClientState(GL_VERTEX_ARRAY);
			glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer);
			glVertexPointer(2, GL_FLOAT, 0, 0);
			glDrawArrays(GL_QUADS, 0, 4);
			shader.unbind();
			Display.update();
			Display.sync(60);
		}
		
		shader.delete();
		Display.destroy();
	}
	
}