package audiodrive.model.track;

import static org.lwjgl.opengl.GL11.*;

import java.util.ArrayList;
import java.util.List;

import audiodrive.audio.AnalyzedAudio;
import audiodrive.model.buffer.VertexBuffer;
import audiodrive.model.geometry.CuboidStripRenderer;
import audiodrive.model.geometry.ReflectionPlane;
import audiodrive.model.geometry.Vector;
import audiodrive.model.geometry.transform.Placement;
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
	private double flightHeight = 0.0003;
	
	private VertexBuffer pointBuffer;
	private VertexBuffer splineBuffer;
	private VertexBuffer splineAreaBuffer;
	private VertexBuffer leftBorderBuffer;
	private VertexBuffer rightBorderBuffer;
	private CuboidStripRenderer cuboidStripRenderer;
	
	public Track(AnalyzedAudio audio, List<Vector> vectors, double duration, int smoothing) {
		this.audio = audio;
		this.vectors = vectors;
		this.duration = duration;
		this.smoothing = smoothing;
	}
	
	public void prepare() {
		calculateSpline();
		// pointBuffer = new VertexBuffer(vectors);
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
		List<Vector> leftBorder = new ArrayList<>();
		List<Vector> rightBorder = new ArrayList<>();
		Vector height = new Vector().y(borderHeight);
		for (int i = 0; i < splineArea.size(); i += 2) {
			Vector right = splineArea.get(i);
			Vector left = splineArea.get(i + 1);
			Vector width = right.minus(left).length(borderWidth);
			Vector upper = right.plus(height);
			Vector lower = right.minus(height);
			leftBorder.add(upper.plus(width));
			leftBorder.add(lower.plus(width));
			leftBorder.add(upper);
			leftBorder.add(lower);
			upper = left.plus(height);
			lower = left.minus(height);
			rightBorder.add(upper);
			rightBorder.add(lower);
			rightBorder.add(upper.minus(width));
			rightBorder.add(lower.minus(width));
		}
		cuboidStripRenderer = new CuboidStripRenderer(spline.size() - 1);
		leftBorderBuffer = new VertexBuffer(leftBorder).mode(GL_QUAD_STRIP);
		rightBorderBuffer = new VertexBuffer(rightBorder).mode(GL_QUAD_STRIP);
	}
	
	public void render() {
		glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
		glColor4d(1, 1, 1, 1);
		if (pointBuffer != null) {
			glPointSize(5f);
			pointBuffer.draw();
		}
		if (splineBuffer != null) {
			splineBuffer.draw();
		}
		glColor4d(1, 1, 1, 0.5);
		glLineWidth(2.0f);
		glDisable(GL_CULL_FACE);
		splineAreaBuffer.draw();
		glEnable(GL_CULL_FACE);
		cuboidStripRenderer.render(leftBorderBuffer);
		cuboidStripRenderer.render(rightBorderBuffer);
		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
		glColor4d(0.1, 0.1, 0.1, 0.25);
		splineAreaBuffer.draw();
		cuboidStripRenderer.render(leftBorderBuffer);
		cuboidStripRenderer.render(rightBorderBuffer);
	}
	
	public Index getIndex(double time) {
		double index = time * spline.size() / duration;
		if (index >= spline.size() - 1) index = spline.size() - 2;
		int integer = (int) index;
		double fraction = index - integer;
		return new Index(integer, fraction);
	}
	
	public Placement getPlacement(double time) {
		Index index = getIndex(time);
		Vector current = spline.get(index.integer);
		Vector next = spline.get(index.integer + 1);
		Vector direction = next.minus(current);
		Vector up = Vector.Y;
		// TODO interpolating the position causes bucking
		// Vector position = current.plus(direction.multiplied(index.fraction)).plus(up.multiplied(flightHeight));
		Vector position = current.plus(up.multiplied(flightHeight));
		return new Placement().position(position).direction(direction).up(up);
	}
	
	public List<ReflectionPlane> getReflectionPlanes(double time) {
		List<ReflectionPlane> planes = new ArrayList<>();
		Index index = getIndex(time);
		planes.add(getPlane(index.integer));
		ReflectionPlane plane = getPlane(index.integer - 1);
		if (plane != null) planes.add(plane);
		// if (index.fraction < 0.5) Get.optional(getPlane(index.integer - 1)).ifPresent(planes::add);
		// else Get.optional(getPlane(index.integer + 1)).ifPresent(planes::add);
		return planes;
	}
	
	private ReflectionPlane getPlane(int index) {
		if (index < 0 || index * 2 > splineArea.size() - 4) return null;
		Vector a = splineArea.get(index * 2);
		Vector b = splineArea.get(index * 2 + 1);
		Vector c = splineArea.get(index * 2 + 3);
		Vector d = splineArea.get(index * 2 + 2);
		return new ReflectionPlane(a, b, c, d);
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
	
	public static class Index {
		
		final int integer;
		final double fraction;
		
		public Index(int integer, double fraction) {
			this.integer = integer;
			this.fraction = fraction;
		}
		
		@Override
		public String toString() {
			return "Index [integer=" + integer + ", fraction=" + fraction + "]";
		}
		
	}
	
}
