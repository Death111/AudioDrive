package audiodrive.ui;

import static org.lwjgl.opengl.GL11.*;

import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import audiodrive.model.geometry.Matrix;
import audiodrive.model.geometry.Vector;
import audiodrive.ui.components.Viewport;
import audiodrive.ui.components.Window;
import audiodrive.ui.scenes.GameScene;
import audiodrive.utilities.Arithmetic;
import audiodrive.utilities.Buffers;
import audiodrive.utilities.Matrices;
import audiodrive.utilities.Range;

public class GL {
	
	/** Private constructor to prevent instantiation. */
	private GL() {
		throw new IllegalStateException("This class shall not be instantiated.");
	}
	
	public static void pushMatrix() {
		glPushMatrix();
	}
	
	public static void popMatrix() {
		glPopMatrix();
	}
	
	public static void pushAttributes() {
		glPushAttrib(GL_ALL_ATTRIB_BITS);
	}
	
	public static void popAttributes() {
		glPopAttrib();
	}
	
	public static void rotate(double angle, Vector axis) {
		glRotated(angle, axis.x(), axis.y(), axis.z());
	}
	
	public static void translate(Vector vector) {
		glTranslated(vector.x(), vector.y(), vector.z());
	}
	
	public static Matrix projectionMatrix() {
		DoubleBuffer projection = Buffers.createDoubleBuffer(4 * 4);
		glGetDouble(GL_PROJECTION_MATRIX, projection);
		return new Matrix().set(projection).transpose();
	}
	
	public static Matrix modelviewMatrix() {
		DoubleBuffer modelview = Buffers.createDoubleBuffer(4 * 4);
		glGetDouble(GL_MODELVIEW_MATRIX, modelview);
		return new Matrix().set(modelview).transpose();
	}
	
	public static Matrix modelviewProjectionMatrix() {
		return projectionMatrix().multiplied(modelviewMatrix());
	}
	
	public static Viewport viewport() {
		IntBuffer viewport = Buffers.createIntBuffer(4 * 4);
		glGetInteger(GL_VIEWPORT, viewport);
		return new Viewport(viewport);
	}
	
	public static Range depthRange() {
		DoubleBuffer depthRange = Buffers.createDoubleBuffer(4 * 4);
		glGetDouble(GL_DEPTH_RANGE, depthRange);
		return new Range(depthRange.get(0), depthRange.get(1));
	}
	
	public static Vector screenspace(Vector vector) {
		return screenspace(vector, modelviewProjectionMatrix(), viewport(), depthRange());
	}
	
	/*
	openGL specification for screen-space
	 
	If a vertex in object coordinates is given by v(x0, y0, z0, w0) and the model-view matrix is M, then the vertex's eye coordinates are found as
	eye(xe, ye, ze, we) = M*v

	Similarly, if P is the projection matrix, then the vertex's clip coordinates are
	clip(xc, yc, zc, wc) = P*eye

	The vertex's normalized device coordinates are then
	dev(xd, yd, zd) = (xc/wc, yc/wc, zc/wc)

	The viewport transformation is determined by the viewport's width and height in pixels, px and py, respectively, and its center (ox, oy)(also in pixels)

	The vertex's window coordinates (xw, yw, zw) are given by.
	xw = (px/2)xd + ox
	yw = (py/2)yd + oy
	zw = [(f-n)/2]zd + (n+f)/2

	The factor and offset applied to zd encoded by n and f are set using void DepthRange(clampd n, clampd f)
	 */
	public static Vector screenspace(Vector vector, Matrix mvpMatrix, Viewport viewport, Range depthRange) {
		double[] clipspace = Matrices.multiply(mvpMatrix.toArray(), vector.toHomogeneous());
		double w = clipspace[3];
		double x = clipspace[0] / w;
		double y = clipspace[1] / w;
		double z = clipspace[2] / w;
		double n = depthRange.minimum;
		double f = depthRange.maximum;
		x = viewport.xCenter + x * viewport.width / 2;
		y = viewport.yCenter + y * viewport.height / 2;
		z = (n + f) / 2 + z * (f - n) / 2;
		return new Vector(x, y, z);
	}
	
	/**
	 * Returns the clamped depth buffer value for a given screen position.
	 * 
	 * @param x horizontal position on the screen
	 * @param y vertical position on the screen
	 * @return the depth buffer value with range [0,1]
	 */
	public static float depthBufferValue(int x, int y) {
		FloatBuffer buffer = Buffers.createFloatBuffer(1);
		glReadPixels(x, y, 1, 1, GL_DEPTH_COMPONENT, GL_FLOAT, buffer);
		return buffer.get(0);
	}
	
	/**
	 * Calculates the clamped depth buffer value for a given distance.
	 * 
	 * @param z distance from the eye to the object
	 * @return the depth buffer value with range [0,1]
	 */
	public static double depthClamped(double z) {
		double range = GameScene.Far - GameScene.Near;
		return depth(z) / range;
	}
	
	/**
	 * Calculates the actual depth buffer value for a given distance.
	 * 
	 * @param z distance from the eye to the object
	 * @return the depth buffer value
	 * @see http://www.sjbaker.org/steve/omniv/love_your_z_buffer.html
	 */
	public static int depth(double z) {
		z = Arithmetic.clamp(z, GameScene.Near, GameScene.Far);
		double scale = 1 << Window.getPixelFormat().getDepthBits();
		double a = GameScene.Far / (GameScene.Far - GameScene.Near);
		double b = GameScene.Far * GameScene.Near / (GameScene.Near - GameScene.Far);
		return (int) Math.round(scale * (a + b / z));
	}
	
}