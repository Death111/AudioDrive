package audiodrive.ui.effects;

import static org.lwjgl.opengl.GL11.*;
import audiodrive.model.buffer.Framebuffer;
import audiodrive.ui.components.Camera;
import audiodrive.ui.components.Window;

public class Glow extends Framebuffer {
	
	public Glow() {
		super(Window.getWidth(), Window.getHeight());
	}
	
	@Override
	public Glow depthpass(Runnable depthpass) {
		super.depthpass(depthpass);
		return this;
	}
	
	@Override
	public Glow renderpass(Runnable renderpass) {
		super.renderpass(renderpass);
		return this;
	}
	
	@Override
	public Glow render() {
		super.render();
		Camera.overlay(Window.getWidth(), Window.getHeight());
		int times = 10;
		double shift = 0.002;
		double alpha = 0.1;
		double offset = 0.0;
		double alphainc = alpha / times;
		glViewport(0, 0, Window.getWidth(), Window.getHeight());
		glEnable(GL_TEXTURE_2D);
		glEnable(GL_BLEND);
		texture().bind();
		glBlendFunc(GL_SRC_ALPHA, GL_ONE);
		// TODO make texture transparent...
		// glDisable(GL_DEPTH_TEST);
		// glDepthMask(false);
		// glEnable(GL_ALPHA_TEST);
		// glAlphaFunc(GL_GREATER, 0.1f);
		glBegin(GL_QUADS);
		for (int i = 0; i < times; i++) {
			glColor4d(1.0, 1.0, 1.0, alpha);
			glTexCoord2d(0 + offset, 1 - offset);
			glVertex2d(0, 0);
			glTexCoord2d(0 + offset, 0 + offset);
			glVertex2d(0, Window.getHeight());
			glTexCoord2d(1 - offset, 0 + offset);
			glVertex2d(Window.getWidth(), Window.getHeight());
			glTexCoord2d(1 - offset, 1 - offset);
			glVertex2d(Window.getWidth(), 0);
			offset += shift;
			alpha = alpha - alphainc;
		}
		glEnd();
		glBindTexture(GL_TEXTURE_2D, 0);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		return this;
	}
	
	/**
	 * Increases the shininess of bright elements on the screen.
	 */
	public static void overlay() {
		Camera.overlay(Window.getWidth(), Window.getHeight());
		glBlendFunc(GL_DST_COLOR, GL_ONE_MINUS_SRC_ALPHA);
		glBegin(GL_QUADS);
		glColor4d(1.0, 1.0, 1.0, 0.5);
		glTexCoord2d(0, 1);
		glVertex2d(0, 0);
		glTexCoord2d(0, 0);
		glVertex2d(0, Window.getHeight());
		glTexCoord2d(1, 0);
		glVertex2d(Window.getWidth(), Window.getHeight());
		glTexCoord2d(1, 1);
		glVertex2d(Window.getWidth(), 0);
		glEnd();
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
	}
	
}
