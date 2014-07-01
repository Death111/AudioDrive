package audiodrive.model.track;

import static org.lwjgl.opengl.GL11.*;

import java.util.ArrayList;
import java.util.List;

import audiodrive.audio.AnalyzedAudio;
import audiodrive.model.buffer.VertexBuffer;
import audiodrive.model.geometry.CuboidStripRenderer;
import audiodrive.model.geometry.Placement;
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
	private double borderHeight = 0.0005;
	private double borderWidth = 0.0003;
	private double flightHeight = 0.001;
	
	private VertexBuffer pointBuffer;
	private VertexBuffer splineBuffer;
	private VertexBuffer splineAreaBuffer;
	private VertexBuffer rightBorderBuffer;
	private VertexBuffer leftBorderBuffer;
	private CuboidStripRenderer cuboidStripRenderer;
	
	public Track(AnalyzedAudio audio, List<Vector> vectors, double duration, int smoothing) {
		this.audio = audio;
		this.vectors = vectors;
		this.duration = duration;
		this.smoothing = smoothing;
	}
	
	public void prepare() {
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
				splineArea.add(one.plus(last.negated()));
				splineArea.add(one.plus(last));
			}
			if (i < spline.size() - 2) {
				Vector three = spline.get(i + 2);
				Vector n1 = two.minus(one).cross(Vector.Y).normalize();
				Vector n2 = three.minus(two).cross(Vector.Y).normalize();
				next = n1.plus(n2).length(width / Math.cos(n1.angle(n2) * 0.5));
			} else {
				next = two.minus(one).cross(Vector.Y).length(width);
			}
			splineArea.add(two.plus(next.negated()));
			splineArea.add(two.plus(next));
			last = next;
		}
		List<Vector> rightBorder = new ArrayList<>();
		List<Vector> leftBorder = new ArrayList<>();
		Vector height = new Vector().y(borderHeight);
		for (int i = 0; i < splineArea.size() - 2; i += 2) {
			Vector right = splineArea.get(i);
			Vector left = splineArea.get(i + 1);
			Vector width = right.minus(left).length(borderWidth);
			Vector upper = right.plus(height);
			Vector lower = right.minus(height);
			rightBorder.add(upper);
			rightBorder.add(lower);
			rightBorder.add(upper.plus(width));
			rightBorder.add(lower.plus(width));
			upper = left.plus(height);
			lower = left.minus(height);
			leftBorder.add(upper);
			leftBorder.add(lower);
			leftBorder.add(upper.minus(width));
			leftBorder.add(lower.minus(width));
		}
		cuboidStripRenderer = new CuboidStripRenderer(spline.size());
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
		glColor4d(0.0, 0.5, 0.5, 0.5);
		glDisable(GL_CULL_FACE);
		splineAreaBuffer.draw();
		glEnable(GL_CULL_FACE);
		cuboidStripRenderer.render(rightBorderBuffer);
		cuboidStripRenderer.render(leftBorderBuffer);
	}
	
	public Placement calculatePlayerPlacement(double time) {
		double calculatedIndex = time * spline.size() / duration;
		if (calculatedIndex >= spline.size() - 1) calculatedIndex = spline.size() - 2;
		int index = (int) calculatedIndex;
		double fraction = calculatedIndex - index;
		Vector current = spline.get(index);
		Vector next = spline.get(index + 1);
		Vector direction = next.minus(current);
		Vector normal = Vector.Y;
		// TODO interpolating the position causes bucking
		// Vector position = current.plus(direction.multiplied(fraction)).plus(normal.multiplied(flightHeight));
		Vector position = current.plus(normal.multiplied(flightHeight));
		return new Placement().position(position).direction(direction).normal(normal);
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
