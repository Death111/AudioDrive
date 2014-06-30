package audiodrive.model.track;

import static org.lwjgl.opengl.GL11.*;

import java.util.ArrayList;
import java.util.List;

import audiodrive.audio.AnalyzedAudio;
import audiodrive.model.buffer.VertexBuffer;
import audiodrive.model.geometry.Vector;
import audiodrive.model.track.interpolation.CatmullRom;

public class Track {
	
	private AnalyzedAudio audio;
	private List<Vector> vectors;
	private double duration;
	private int smoothing;
	private List<Vector> spline;
	private List<Vector> splineArea;
	private double borderHeight = 0.0005;
	private double borderWidth = 0.00001;
	private double width = 0.003;
	
	private VertexBuffer pointBuffer;
	private VertexBuffer splineBuffer;
	private VertexBuffer splineAreaBuffer;
	private VertexBuffer rightBorderBuffer;
	private VertexBuffer leftBorderBuffer;
	
	public Track(AnalyzedAudio audio, List<Vector> vectors, double duration, int smoothing) {
		this.audio = audio;
		this.vectors = vectors;
		this.duration = duration;
		calculateSpline();
		pointBuffer = new VertexBuffer(vectors);
		splineBuffer = new VertexBuffer(spline).mode(GL_LINE_STRIP);
		splineAreaBuffer = new VertexBuffer(splineArea).mode(GL_QUAD_STRIP);
	}
	
	private void calculateSpline() {
		spline = CatmullRom.interpolate(vectors, 15, CatmullRom.Type.Centripetal);
		splineArea = new ArrayList<>();
		if (spline == null || spline.isEmpty()) return;
		Vector last = null;
		for (int i = 0; i < spline.size() - 1; i++) {
			Vector one = spline.get(i);
			Vector two = spline.get(i + 1);
			Vector next;
			if (last == null) {
				last = two.minus(one).cross(Vector.Y).length(width);
				splineArea.add(one.plus(last));
				splineArea.add(one.plus(last.negated()));
			}
			if (i < spline.size() - 2) {
				Vector three = spline.get(i + 2);
				Vector n1 = two.minus(one).cross(Vector.Y).normalize();
				Vector n2 = three.minus(two).cross(Vector.Y).normalize();
				next = n1.plus(n2).length(width / Math.cos(n1.angle(n2) * 0.5));
			} else {
				next = two.minus(one).cross(Vector.Y).length(width);
			}
			splineArea.add(two.plus(next));
			splineArea.add(two.plus(next.negated()));
			last = next;
		}
		List<Vector> rightBorder = new ArrayList<>();
		List<Vector> leftBorder = new ArrayList<>();
		Vector height = new Vector().y(borderHeight);
		for (int i = 0; i < splineArea.size() - 2; i += 2) {
			Vector right = splineArea.get(i);
			Vector left = splineArea.get(i + 1);
			Vector width = left.minus(right).length(borderWidth);
			rightBorder.add(right.plus(height));
			rightBorder.add(right.minus(height));
			leftBorder.add(left.plus(height));
			leftBorder.add(left.minus(height));
		}
		rightBorderBuffer = new VertexBuffer(rightBorder).mode(GL_QUAD_STRIP);
		leftBorderBuffer = new VertexBuffer(leftBorder).mode(GL_QUAD_STRIP);
	}
	
	public void render() {
		glColor4d(1, 1, 1, 1);
		if (pointBuffer != null) {
			glPointSize(5f);
			pointBuffer.draw();
		}
		if (splineBuffer != null) {
			splineBuffer.draw();
		}
		if (splineAreaBuffer != null) {
			glColor4d(0.0, 0.5, 0.5, 0.5);
			splineAreaBuffer.draw();
		}
		if (rightBorderBuffer != null) {
			glColor4d(0.0, 1.0, 1.0, 0.5);
			rightBorderBuffer.draw();
		}
		if (leftBorderBuffer != null) {
			glColor4d(1.0, 1.0, 0.0, 0.5);
			leftBorderBuffer.draw();
		}
	}
	
	public AnalyzedAudio getAudio() {
		return audio;
	}
	
	public List<Vector> getVectors() {
		return vectors;
	}
	
	public double getDuration() {
		return duration;
	}
	
	public int getSmoothing() {
		return smoothing;
	}
	
	public List<Vector> spline() {
		return spline;
	}
	
}
