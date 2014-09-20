package audiodrive.model.buffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureImpl;

import audiodrive.model.Renderable;

public class Framebuffer {
	
	public final int width, height;
	public final int framebufferID;
	public final int depthbufferID;
	public final int textureID;
	public final Texture texture;
	
	private Runnable depthpass;
	private Runnable renderpass;
	
	public Framebuffer(int width, int height) {
		// create a buffers and texture
		this.width = width;
		this.height = height;
		framebufferID = glGenFramebuffers();
		depthbufferID = glGenRenderbuffers();
		textureID = glGenTextures();
		
		// switch to the new framebuffer
		glBindFramebuffer(GL_FRAMEBUFFER, framebufferID);
		
		// initialize color texture
		glBindTexture(GL_TEXTURE_2D, textureID);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_INT, (ByteBuffer) null);
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, textureID, 0);
		
		// initialize depth render buffer
		glBindRenderbuffer(GL_RENDERBUFFER, depthbufferID);
		glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, width, height);
		glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, depthbufferID);
		
		// switch back to default framebuffer
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
		glBindRenderbuffer(GL_RENDERBUFFER, 0);
		glBindTexture(GL_TEXTURE_2D, 0);
		
		texture = new TextureImpl("Framebuffer-generated", GL_TEXTURE_2D, textureID);
	}
	
	@Override
	protected void finalize() throws Throwable {
		glDeleteFramebuffers(framebufferID);
		glDeleteRenderbuffers(depthbufferID);
		glDeleteTextures(textureID);
	};
	
	public Framebuffer depthpass(Runnable depthpass) {
		this.depthpass = depthpass;
		return this;
	}
	
	public Framebuffer renderpass(Runnable renderpass) {
		this.renderpass = renderpass;
		return this;
	}
	
	/**
	 * Render to framebuffer
	 * 
	 * @param renderables objects to render
	 */
	protected Framebuffer render(List<? extends Renderable> renderables) {
		glBindFramebuffer(GL_FRAMEBUFFER, framebufferID);
		
		glEnable(GL_TEXTURE_2D);
		glViewport(0, 0, width, height);
		glClearColor(0, 0, 0, 0);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		if (depthpass != null) {
			glColorMask(false, false, false, false);
			depthpass.run();
			glColorMask(true, true, true, true);
		}
		if (renderpass != null) {
			renderpass.run();
		}
		renderables.forEach(Renderable::render);
		
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
		return this;
	}
	
	/**
	 * Render to framebuffer
	 * 
	 * @param renderables objects to render
	 */
	protected Framebuffer render(Renderable... renderables) {
		return render(Arrays.asList(renderables));
	}
	
	public Framebuffer render() {
		return render(Collections.emptyList());
	}
	
	public int width() {
		return width;
	}
	
	public int height() {
		return height;
	}
	
	public Texture texture() {
		return texture;
	}
	
}
