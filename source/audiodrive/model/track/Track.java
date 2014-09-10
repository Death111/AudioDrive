package audiodrive.model.track;

import static org.lwjgl.opengl.GL11.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

import audiodrive.audio.AnalyzedAudio;
import audiodrive.model.buffer.VertexBuffer;
import audiodrive.model.geometry.Color;
import audiodrive.model.geometry.CuboidStripRenderer;
import audiodrive.model.geometry.ReflectionPlane;
import audiodrive.model.geometry.TextureCoordinate;
import audiodrive.model.geometry.Vector;
import audiodrive.model.geometry.Vertex;
import audiodrive.model.geometry.transform.Placement;
import audiodrive.model.loader.Model;
import audiodrive.model.loader.ModelLoader;
import audiodrive.model.track.interpolation.CatmullRom;
import audiodrive.utilities.Log;

public class Track {

	private static final String TRACK_TEXTURE = "models/track/track.png";
	private AnalyzedAudio audio;
	private List<Vector> vectors;
	private double duration;
	private int smoothing;
	private List<Vector> spline;
	private List<Vector> splineArea;
	private List<Vertex> splineArea2;
	private double width = 0.003;
	private double borderHeight = 0.0005;
	private double borderWidth = 0.0003;
	private double flightHeight = 0.0003;

	private VertexBuffer pointBuffer;
	private VertexBuffer splineBuffer;
	private VertexBuffer splineAreaBuffer;
	private Texture trackTexture;
	private VertexBuffer splineArea2Buffer;
	private VertexBuffer leftBorderBuffer;
	private VertexBuffer rightBorderBuffer;
	private CuboidStripRenderer cuboidStripRenderer;

	private Model obstacleModel = ModelLoader.loadSingleModel("models/obstacle/obstacle");
	private List<Placement> obstacles = new ArrayList<Placement>();

	public Track(AnalyzedAudio audio, List<Vector> vectors, double duration, int smoothing) {
		this.audio = audio;
		this.vectors = vectors;
		this.duration = duration;
		this.smoothing = smoothing;
	}

	public void prepare() {
		calculateSpline();
		obstacleModel.scale(0.0002);
		calculateObstacles();
		// pointBuffer = new VertexBuffer(vectors);
		// splineBuffer = new VertexBuffer(spline).mode(GL_LINE_STRIP);
		// splineAreaBuffer = new VertexBuffer(splineArea).mode(GL_QUAD_STRIP);

		try {
			trackTexture = TextureLoader.getTexture("PNG", new FileInputStream(new File(TRACK_TEXTURE)));
		} catch (IOException e) {
			Log.error(e);
		}
		splineArea2Buffer = new VertexBuffer(splineArea2, true, true).mode(GL_QUAD_STRIP);
	}

	private void calculateObstacles() {

		// TODO use audio track for generation instead of every 10 units
		// TODO add collision detection
		for (int i = 0; i < splineArea.size() - 1; i += 10) {
			final Vector left = splineArea.get(i);
			final Vector right = splineArea.get(i + 1);
			Vector nextLeft = new Vector(0, 0, 0);
			if (splineArea.size() > i + 2) {
				nextLeft = splineArea.get(i + 2);
			}

			final Vector horizontal = left.minus(right).normalize();

			double mult = ((width * 2) / 4);
			double rail = (double) (i % 3) + 1;

			final Vector position = left.minus(horizontal.multiplied(rail * mult)).plus(0, flightHeight, 0);
			Vector direction = nextLeft.minus(left);
			obstacles.add(new Placement().position(position).direction(direction).up(Vector.Y));
		}

		Log.debug("created '" + obstacles.size() + "' obstacles");
	}

	private void calculateSpline() {
		spline = CatmullRom.interpolate(vectors, 15, CatmullRom.Type.Centripetal);
		splineArea = new ArrayList<>();
		splineArea2 = new ArrayList<>();

		if (spline == null || spline.isEmpty())
			return;
		Vector last = null;

		for (int i = 0; i < spline.size() - 1; i++) {
			Vector one = spline.get(i);
			Vector two = spline.get(i + 1);
			Vector next;

			// Check if first
			if (last == null) {
				last = two.minus(one).cross(Vector.Y).length(width);
				final Vector leftPosition = one.plus(last.negated());
				final Vector rightPosition = one.plus(last);

				splineArea.add(leftPosition);
				splineArea.add(rightPosition);

				calculateVertex(leftPosition, rightPosition);
			}
			// Check if last
			if (i < spline.size() - 2) {
				Vector three = spline.get(i + 2);
				Vector n1 = two.minus(one).cross(Vector.Y).normalize();
				Vector n2 = three.minus(two).cross(Vector.Y).normalize();
				next = n1.plus(n2).length(width / Math.cos(n1.angle(n2) * 0.5));
			} else { // Normal
				next = two.minus(one).cross(Vector.Y).length(width);
			}

			// Add points
			final Vector left = two.plus(next.negated());
			final Vector right = two.plus(next);

			splineArea.add(left);
			splineArea.add(right);

			calculateVertex(left, right);
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

	private int textureIndex = 0;
	private int sub = 1;

	private void calculateVertex(final Vector leftPosition, final Vector rightPosition) {
		// Create Vertex
		final Vertex leftVertex = new Vertex();
		final Vertex rightVertex = new Vertex();

		// Set position
		leftVertex.position = leftPosition;
		rightVertex.position = rightPosition;

		// Calculate normals
		final Vector leftNormal = rightPosition.cross(leftPosition);
		final Vector rightNormal = rightPosition.cross(leftPosition);
		leftVertex.normal = leftNormal;
		rightVertex.normal = rightNormal;

		// Calculate Texture coordinates
		final TextureCoordinate rightTexture;
		final TextureCoordinate leftTexture;

		float offset = textureIndex % 5 * 0.25f;
		if (textureIndex == 4) {
			sub = -1;
		} else if (textureIndex == 0) {
			sub = 1;
		}

		leftTexture = new TextureCoordinate(0, offset);
		rightTexture = new TextureCoordinate(0.25, offset);

		textureIndex += sub;

		// Set texture coordinates
		leftVertex.textureCoordinate = leftTexture;
		rightVertex.textureCoordinate = rightTexture;

		leftVertex.color = new Color(1, 1, 1, .5);
		rightVertex.color = new Color(1, 1, 1, .5);

		splineArea2.add(leftVertex);
		splineArea2.add(rightVertex);
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

		if (splineAreaBuffer != null) {
			glColor4d(1, 1, 1, 0.5);
			glLineWidth(2.0f);
			glDisable(GL_CULL_FACE);
			splineAreaBuffer.draw();
			splineAreaBuffer.draw();
		}

		glEnable(GL_CULL_FACE);
		cuboidStripRenderer.render(leftBorderBuffer);
		cuboidStripRenderer.render(rightBorderBuffer);
		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
		// Draw track
		{
			if (trackTexture != null) {
				glEnable(GL_TEXTURE_2D);
				glBindTexture(GL_TEXTURE_2D, trackTexture.getTextureID());
			}
			splineArea2Buffer.draw();
			if (trackTexture != null) {
				glBindTexture(GL_TEXTURE_2D, 0);
				glDisable(GL_TEXTURE_2D);
			}
		}
		glColor4d(.1, .1, .1, .25);
		cuboidStripRenderer.render(leftBorderBuffer);
		cuboidStripRenderer.render(rightBorderBuffer);

		// Draw obstacles
		// TODO limit obstacles to be drawn
		for (Placement placement : obstacles) {
			obstacleModel.position(placement.position()).direction(placement.direction());
			obstacleModel.render();
		}

	}

	public Index getIndex(double time) {
		double index = time * spline.size() / duration;
		if (index >= spline.size() - 1)
			index = spline.size() - 2;
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
		// Vector position =
		// current.plus(direction.multiplied(index.fraction)).plus(up.multiplied(flightHeight));
		Vector position = current.plus(direction.multiplied(0.5)).plus(up.multiplied(flightHeight));
		return new Placement().position(position).direction(direction).up(up);
	}

	public List<ReflectionPlane> getReflectionPlanes(double time) {
		List<ReflectionPlane> planes = new ArrayList<>();
		Index index = getIndex(time);
		planes.add(getPlane(index.integer));
		ReflectionPlane plane = getPlane(index.integer - 1);
		if (plane != null)
			planes.add(plane);
		// if (index.fraction < 0.5) Get.optional(getPlane(index.integer -
		// 1)).ifPresent(planes::add);
		// else Get.optional(getPlane(index.integer +
		// 1)).ifPresent(planes::add);
		return planes;
	}

	private ReflectionPlane getPlane(int index) {
		if (index < 0 || index * 2 > splineArea.size() - 4)
			return null;
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

	public double width() {
		return width * 2;
	}

	public static class Index {

		public final int integer;
		public final double fraction;

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
