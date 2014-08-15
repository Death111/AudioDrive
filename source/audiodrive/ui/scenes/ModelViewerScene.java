package audiodrive.ui.scenes;

import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.input.Keyboard;

import audiodrive.model.geometry.Rotation;
import audiodrive.model.geometry.Vector;
import audiodrive.model.loader.Model;
import audiodrive.model.loader.ModelLoader;
import audiodrive.ui.components.Camera;
import audiodrive.ui.components.Scene;
import audiodrive.ui.components.Window;
import audiodrive.utilities.Buffers;

public class ModelViewerScene extends Scene {
	
	private Rotation rotation = new Rotation();
	private Vector translate = new Vector();
	private Vector look = new Vector();
	private Vector camera = new Vector(0, 0, 2.5);
	private Model model;
	private boolean bended = true;
	
	@Override
	protected void entering() {
		model = ModelLoader.loadSingleModel("models/xwing/xwing").scale(0.1);
	}
	
	@Override
	protected void render() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
		// glDisable(GL_CULL_FACE);
		glEnable(GL_NORMALIZE);
		glEnable(GL_LIGHT0);
		glLight(GL_LIGHT0, GL_DIFFUSE, Buffers.create(1f, 1f, 1f, 1f));
		glEnable(GL_COLOR_MATERIAL);
		glColorMaterial(GL_FRONT, GL_DIFFUSE);
		glShadeModel(GL_SMOOTH);
		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		
		Camera.perspective(45, getWidth(), getHeight(), 0.001, 1000);
		Camera.position(camera);
		Camera.lookAt(look);
		
		glTranslated(translate.x(), translate.y(), translate.z());
		rotation.apply();
		
		drawCoordinateSystem(3);
		
		// disable color and depth updates
		glDisable(GL_DEPTH_TEST);
		glColorMask(false, false, false, false);
		// draw "1" into the stencil buffer
		glEnable(GL_STENCIL_TEST);
		glClearStencil(0);
		glStencilOp(GL_REPLACE, GL_REPLACE, GL_REPLACE);
		glStencilFunc(GL_ALWAYS, 1, 0xffffffff);
		// now drawing the floor just tags the floor pixels as stencil value 1
		drawSurface();
		// re-enable color and depth updates
		glColorMask(true, true, true, true);
		glEnable(GL_DEPTH_TEST);
		// now, only render where stencil is set to 1.
		glStencilFunc(GL_EQUAL, 1, 0xffffffff);
		glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
		
		// draw reflection on plane
		glEnable(GL_LIGHTING);
		glPushMatrix();
		glRotated(180, 0, 0, 1);
		glTranslated(0, 0.5, 0);
		glLight(GL_LIGHT0, GL_POSITION, Buffers.create(0f, 1f, 0f, 0f));
		glColor4d(1, 1, 1, 1);
		model.render();
		glPopMatrix();
		glDisable(GL_STENCIL_TEST);
		
		glLight(GL_LIGHT0, GL_POSITION, Buffers.create(0f, 1f, 0f, 0f));
		
		glColor4d(1, 1, 1, 0.5);
		drawSurface();
		
		// draw model
		glColor4d(1, 1, 1, 1);
		model.render();
		glDisable(GL_LIGHTING);
	}
	
	private void drawSurface() {
		double y = -0.25;
		if (bended) {
			Vector risingA = new Vector(-1, 2 * y, 1);
			Vector risingB = new Vector(1, 2 * y, 1);
			Vector risingC = new Vector(1, y, 0);
			Vector risingD = new Vector(-1, y, 0);
			Vector risingNormal = risingB.minus(risingA).cross(risingD.minus(risingA)).normalize();
			glBegin(GL_QUADS);
			risingNormal.glNormal();
			risingA.glVertex();
			risingB.glVertex();
			risingC.glVertex();
			risingD.glVertex();
			Vector fallingA = new Vector(-1, y, 0);
			Vector fallingB = new Vector(1, y, 0);
			Vector fallingC = new Vector(1, 2 * y, -1);
			Vector fallingD = new Vector(-1, 2 * y, -1);
			Vector fallingNormal = fallingB.minus(fallingA).cross(fallingD.minus(fallingA)).normalize();
			glBegin(GL_QUADS);
			risingNormal.glNormal();
			risingA.glVertex();
			risingB.glVertex();
			risingC.glVertex();
			risingD.glVertex();
			glBegin(GL_QUADS);
			fallingNormal.glNormal();
			fallingA.glVertex();
			fallingB.glVertex();
			fallingC.glVertex();
			fallingD.glVertex();
			glEnd();
			glColor4d(1, 1, 1, 1);
			Vector risingCenter = risingA.plus(risingC).divided(2);
			Vector fallingCenter = fallingA.plus(fallingC).divided(2);
			glBegin(GL_LINES);
			risingCenter.glVertex();
			risingCenter.plus(risingNormal).glVertex();
			fallingCenter.glVertex();
			fallingCenter.plus(fallingNormal).glVertex();
			glEnd();
		} else {
			glBegin(GL_QUADS);
			glNormal3d(0, 1, 0);
			glVertex3d(-1, y, 1);
			glVertex3d(1, y, 1);
			glVertex3d(1, y, -1);
			glVertex3d(-1, y, -1);
			glEnd();
		}
	}
	
	private void drawCoordinateSystem(int length) {
		glBegin(GL_LINES);
		glColor4d(length, 0, 0, length);
		glVertex3d(length, 0, 0);
		glVertex3d(-length, 0, 0);
		glColor4d(0, length, 0, length);
		glVertex3d(0, length, 0);
		glVertex3d(0, -length, 0);
		glColor4d(0, 0, length, length);
		glVertex3d(0, 0, length);
		glVertex3d(0, 0, -length);
		glEnd();
	}
	
	@Override
	public void keyPressed(int key, char character) {
		switch (key) {
		case Keyboard.KEY_NUMPAD0:
			translate.add(0, 0, -0.01);
			break;
		case Keyboard.KEY_NUMPAD2:
			translate.add(0, 0.01, 0);
			break;
		case Keyboard.KEY_NUMPAD4:
			translate.add(0.01, 0, 0);
			break;
		case Keyboard.KEY_NUMPAD5:
			translate.add(0, 0, 0.01);
			break;
		case Keyboard.KEY_NUMPAD6:
			translate.add(-0.01, 0, 0);
			break;
		case Keyboard.KEY_NUMPAD8:
			translate.add(0, -0.01, 0);
			break;
		case Keyboard.KEY_ADD:
			camera.add(look.minus(camera).length(0.1));
			break;
		case Keyboard.KEY_SUBTRACT:
			camera.add(camera.minus(look).length(0.1));
			break;
		
		default:
			break;
		}
	}
	
	@Override
	public void keyReleased(int key, char character) {
		switch (key) {
		case Keyboard.KEY_HOME:
			rotation.reset();
			translate.set(Vector.Null);
			break;
		case Keyboard.KEY_ESCAPE:
			back();
			break;
		case Keyboard.KEY_V:
			Window.toggleVSync();
			break;
		default:
			break;
		}
	}
	
	@Override
	public void mouseDragged(int button, int mouseX, int mouseY, int dx, int dy) {
		double horizontal = dx * -0.1;
		double vertical = dy * 0.1;
		switch (button) {
		case 0:
			rotation.xAdd(vertical).yAdd(horizontal);
			break;
		case 1:
			rotation.zAdd(horizontal);
			break;
		default:
			break;
		}
	}
	
}
