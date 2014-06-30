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
	private double width = 0.003;
	
	private VertexBuffer pointBuffer;
	private VertexBuffer splineBuffer;
	private VertexBuffer splineAreaBuffer;
	
	public Track(AnalyzedAudio audio, List<Vector> vectors, double duration, int smoothing) {
		this.audio = audio;
		this.vectors = vectors;
		this.duration = duration;
		calculateSpline();
		pointBuffer = new VertexBuffer(vectors);
		splineBuffer = new VertexBuffer(spline).mode(GL_LINE_STRIP);
		splineAreaBuffer = new VertexBuffer(splineArea).mode(GL_QUAD_STRIP);
	}
	
	@Override
	protected void finalize() throws Throwable {
		if (pointBuffer != null) pointBuffer.delete();
		if (splineBuffer != null) splineBuffer.delete();
		if (splineAreaBuffer != null) splineAreaBuffer.delete();
	};
	
	private void calculateSpline() {
		spline = CatmullRom.interpolate(vectors, 15, CatmullRom.Type.Centripetal);
		splineArea = new ArrayList<>();
		if (spline == null || spline.isEmpty()) return;
		Vector sideOne = null;
		for (int i = 0; i < spline.size() - 1; i++) {
			Vector one = spline.get(i);
			Vector two = spline.get(i + 1);
			Vector sideTwo;
			if (sideOne == null) {
				sideOne = two.minus(one).cross(Vector.Y).length(width);
				splineArea.add(one.plus(sideOne));
				splineArea.add(one.plus(sideOne.negated()));
			}
			if (i < spline.size() - 2) {
				Vector three = spline.get(i + 2);
				Vector n1 = two.minus(one).cross(Vector.Y).normalize();
				Vector n2 = three.minus(two).cross(Vector.Y).normalize();
				sideTwo = n1.plus(n2).length(width / Math.cos(n1.angle(n2) * 0.5));
			} else {
				sideTwo = two.minus(one).cross(Vector.Y).length(width);
			}
			splineArea.add(two.plus(sideTwo));
			splineArea.add(two.plus(sideTwo.negated()));
			sideOne = sideTwo;
		}
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
