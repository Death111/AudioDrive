package audiodrive.model.geometry;

import static org.lwjgl.opengl.GL11.*;
import audiodrive.model.geometry.transform.Transformation;
import audiodrive.model.geometry.transform.Translation;
import audiodrive.model.hierarchy.Node;
import audiodrive.utilities.Buffers;

public class ReflectionPlane {
	
	private Vector a, b, c, d, center, normal;
	private boolean renderNormal = false;
	
	public ReflectionPlane(Vector a, Vector b, Vector c, Vector d) {
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
		center = a.plus(c).divided(2);
		normal = b.minus(a).cross(d.minus(a)).normalize();
	}
	
	public ReflectionPlane renderNormal(boolean renderNormal) {
		this.renderNormal = renderNormal;
		return this;
	}
	
	public Vector a() {
		return a;
	}
	
	public Vector b() {
		return b;
	}
	
	public Vector c() {
		return c;
	}
	
	public Vector d() {
		return d;
	}
	
	public Vector normal() {
		return normal;
	}
	
	public void reflect(Node node) {
		glClear(GL_STENCIL_BUFFER_BIT);
		
		// disable color and depth updates
		glDisable(GL_DEPTH_TEST);
		glColorMask(false, false, false, false);
		// draw "1" into the stencil buffer
		glEnable(GL_STENCIL_TEST);
		glStencilOp(GL_REPLACE, GL_REPLACE, GL_REPLACE);
		glStencilFunc(GL_ALWAYS, 1, 0xffffffff);
		// tags the plane's pixels as stencil value 1
		sendGeometry();
		// re-enable color and depth updates
		glColorMask(true, true, true, true);
		glEnable(GL_DEPTH_TEST);
		// now, only render where stencil is set to 1
		glStencilFunc(GL_EQUAL, 1, 0xffffffff);
		glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
		// draw reflection on plane
		glPushMatrix();
		node.placement().apply();
		glLight(GL_LIGHT0, GL_POSITION, Buffers.create(0f, -1f, 0f, 0f));
		glTranslated(0, -2 * distance(node), 0);
		new Translation().set(node.translation()).y(-node.translation().y()).apply();
		node.rotation().inverted().apply();
		glRotated(angle(node), 1, 0, 0);
		glRotated(180, 0, 0, 1);
		node.scaling().apply();
		node.transformations().stream().forEach(Transformation::apply);
		glColor4d(1, 1, 1, 1);
		node.draw();
		glPopMatrix();
		glDisable(GL_STENCIL_TEST);
		glLight(GL_LIGHT0, GL_POSITION, Buffers.create(0f, 1f, 0f, 0f));
	}
	
	public double angle(Node node) {
		return node.placement().up().degrees(normal) * Math.signum(node.placement().direction().dot(normal));
	}
	
	public double distance(Node node) {
		return node.placement().position().minus(center).dot(normal);
	}
	
	public void render() {
		glColor4d(1, 1, 1, 0.5);
		sendGeometry();
		if (!renderNormal) return;
		glColor4d(1, 1, 1, 1);
		glBegin(GL_LINES);
		center.glVertex();
		center.plus(normal).glVertex();
		glEnd();
	}
	
	public void sendGeometry() {
		glBegin(GL_QUADS);
		normal.glNormal();
		a.glVertex();
		b.glVertex();
		c.glVertex();
		d.glVertex();
		glEnd();
	}
	
	@Override
	public String toString() {
		return "ReflectionPlane [a=" + a + ", b=" + b + ", c=" + c + ", d=" + d + ", normal=" + normal + "]";
	}
	
}
