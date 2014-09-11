package audiodrive.model.track;

import static org.lwjgl.opengl.GL11.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

import audiodrive.AudioDrive;
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
import audiodrive.ui.GL;
import audiodrive.utilities.Arithmetic;
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
	private double width = 1.5;
	private double borderHeight = 0.2;
	private double borderWidth = 0.2;
	private double flightHeight = 0.15;
	
	private VertexBuffer pointBuffer;
	private VertexBuffer splineBuffer;
	private VertexBuffer splineAreaBuffer;
	private VertexBuffer splineArea2Buffer;
	private VertexBuffer leftBorderBuffer;
	private VertexBuffer rightBorderBuffer;
	private CuboidStripRenderer cuboidStripRenderer;
	private Texture trackTexture;
	
	private Model obstacleModel = ModelLoader.loadSingleModel("models/obstacle/obstacle");
	private List<Placement> obstacles = new ArrayList<Placement>();
	private VertexBuffer leftBorderVertexBuffer;
	private VertexBuffer rightBorderVertexBuffer;
	
	private Color risingBorderColor = AudioDrive.Settings.getColor("risingBorderColor");
	private Color fallingBorderColor = AudioDrive.Settings.getColor("fallingBorderColor");
	
	public Track(AnalyzedAudio audio, List<Vector> vectors, double duration, int smoothing) {
		this.audio = audio;
		this.vectors = vectors;
		this.duration = duration;
		this.smoothing = smoothing;
		obstacleModel.scale(0.1);
		try {
			trackTexture = TextureLoader.getTexture("PNG", new FileInputStream(new File(TRACK_TEXTURE)));
		} catch (IOException e) {
			Log.error(e);
		}
		build();
	}
	
	private void build() {
		calculateSpline();
		splineArea2Buffer = new VertexBuffer(splineArea2).mode(GL_QUAD_STRIP).useColor(true).useTexture(true);
	}
	
	private void calculateSpline() {
		spline = CatmullRom.interpolate(vectors, 15, CatmullRom.Type.Centripetal);
		splineArea = new ArrayList<>();
		splineArea2 = new ArrayList<>();
		
		if (spline == null || spline.isEmpty()) return;
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
		
		List<Vertex> leftVertexList = calcBorderVertexes(leftBorder);
		List<Vertex> rightVertexList = calcBorderVertexes(rightBorder);
		
		leftBorderVertexBuffer = new VertexBuffer(leftVertexList).mode(GL_QUAD_STRIP).useColor(true);
		rightBorderVertexBuffer = new VertexBuffer(rightVertexList).mode(GL_QUAD_STRIP).useColor(true);
		
	}
	
	/**
	 * Create border as VBO with colors
	 * 
	 * @param border
	 * @return
	 */
	private List<Vertex> calcBorderVertexes(List<Vector> border) {
		
		// TODO calculate normals appropriate
		
		List<Vertex> vertexList = new ArrayList<>();
		final int size = border.size();
		// Add front
		float lastY = 0;
		{
			float a = lastY - (float) border.get(0).y();
			final Color color = getColor(a);
			lastY = (float) border.get(0).y();
			vertexList.add(new Vertex().position(border.get(0)).color(color).normal(Vector.Y));
			vertexList.add(new Vertex().position(border.get(1)).color(color).normal(Vector.Y));
			vertexList.add(new Vertex().position(border.get(2)).color(color).normal(Vector.Y));
			vertexList.add(new Vertex().position(border.get(3)).color(color).normal(Vector.Y));
		}
		
		// Draw right side
		for (int i = 2; i < size; i += 3) {
			float a = lastY - (float) border.get(i).y();
			final Color color = getColor(a);
			lastY = (float) border.get(i).y();
			vertexList.add(new Vertex().position(border.get(i)).color(color).normal(Vector.Y));
			i++;
			vertexList.add(new Vertex().position(border.get(i)).color(color).normal(Vector.Y));
		}
		
		// Add back
		{
			float a = lastY - (float) border.get(size - 1).y();
			final Color color = getColor(a);
			lastY = (float) border.get(size - 1).y();
			vertexList.add(new Vertex().position(border.get(size - 1)).color(color).normal(Vector.Y));
			vertexList.add(new Vertex().position(border.get(size - 2)).color(color).normal(Vector.Y));
			vertexList.add(new Vertex().position(border.get(size - 3)).color(color).normal(Vector.Y));
			vertexList.add(new Vertex().position(border.get(size - 4)).color(color).normal(Vector.Y));
		}
		
		// Draw left side
		// TODO culling
		for (int i = size - 3; i > 0; i -= 3) {
			float a = (float) border.get(i).y() - lastY;
			final Color color = getColor(a);
			lastY = (float) border.get(i).y();
			vertexList.add(new Vertex().position(border.get(i)).color(color).normal(Vector.Y));
			i--;
			vertexList.add(new Vertex().position(border.get(i)).color(color).normal(Vector.Y));
		}
		
		// Draw top side
		// TODO culling
		for (int i = 2; i < size; i += 6) {
			float a = lastY - (float) border.get(i).y();
			final Color color = getColor(a);
			lastY = (float) border.get(i).y();
			vertexList.add(new Vertex().position(border.get(i)).color(color).normal(Vector.Y));
			i -= 2;
			vertexList.add(new Vertex().position(border.get(i)).color(color).normal(Vector.Y));
		}
		
		// Dont know if needed to draw bottom layer
		// // Add back
		// leftVertexList.add(new Vertex().position(leftBorder.get(size -
		// 2)).color(Color.GREEN()).normal(Vector.Y));
		// leftVertexList.add(new Vertex().position(leftBorder.get(size -
		// 4)).color(Color.GREEN()).normal(Vector.Y));
		// leftVertexList.add(new Vertex().position(leftBorder.get(size -
		// 3)).color(Color.GREEN()).normal(Vector.Y));
		// leftVertexList.add(new Vertex().position(leftBorder.get(size -
		// 1)).color(Color.GREEN()).normal(Vector.Y));
		
		// Draw bottom
		// for (int i = size - 3; i < 0; i -= 4) {
		// leftVertexList.add(new
		// Vertex().position(leftBorder.get(i)).color(Color.WHITE()).normal(Vector.Y));
		// i += 2;
		// leftVertexList.add(new
		// Vertex().position(leftBorder.get(i)).color(Color.WHITE()).normal(Vector.Y));
		// }
		
		return vertexList;
	}
	
	private Color getColor(float index) {
		return Color.Lerp(risingBorderColor, fallingBorderColor, (float) Arithmetic.linearScale(index, 0, 1, .7f, -.7f));
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
	
	public void update(double time) {
		int forecast = 100;
		double threshold = 75;
		obstacles.clear();
		Index index = getIndex(time);
		int minimum = Math.max(index.integer, 5);
		int maximum = Math.min(index.integer + forecast, spline.size() - 2);
		for (int i = minimum; i < maximum; i++) {
			boolean left = audio.getChannel(0).getPeaks().get(i) > threshold;
			boolean right = audio.getChannel(1).getPeaks().get(i) > threshold;
			if (left && right) {
				obstacles.add(getPlacement(new Index(i, 0), true, 0));
			} else if (left || right) {
				obstacles.add(getPlacement(new Index(i, 0), true, left ? -1 : 1));
			}
		}
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
		}
		
		glEnable(GL_CULL_FACE);
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
			GL.pushAttributes();
			glDisable(GL_CULL_FACE);
			glDisable(GL_LIGHTING);
			splineArea2Buffer.useColor(false);
			glColor4d(0, 0, 0, 1);
			splineArea2Buffer.draw();
			splineArea2Buffer.useColor(true);
			GL.popAttributes();
		}
		for (Placement placement : obstacles) {
			obstacleModel.placement(placement);
			obstacleModel.render();
		}
		glDisable(GL_CULL_FACE);
		leftBorderVertexBuffer.draw();
		rightBorderVertexBuffer.draw();
	}
	
	public Index getIndex(double time) {
		double index = time * spline.size() / duration;
		if (index >= spline.size() - 1) index = spline.size() - 2;
		int integer = (int) index;
		double fraction = index - integer;
		return new Index(integer, fraction);
	}
	
	public Placement getPlacement(double time) {
		return getPlacement(getIndex(time), false, 0);
	}
	
	private Placement getPlacement(Index index, boolean interpolated, int side) {
		Vector current = spline.get(index.integer);
		Vector next = spline.get(index.integer + 1);
		Vector direction = next.minus(current);
		Vector up = Vector.Y;
		Vector position = current.plus(direction.multiplied(interpolated ? index.fraction : 0.5)).plus(up.multiplied(flightHeight));
		Placement placement = new Placement().position(position).direction(direction).up(up);
		if (side != 0) placement.position().add(placement.side().multiplied(Math.signum(side) * width * 2 / 3));
		return placement;
	}
	
	public List<ReflectionPlane> getReflectionPlanes(double time) {
		List<ReflectionPlane> planes = new ArrayList<>();
		Index index = getIndex(time);
		planes.add(getPlane(index.integer));
		ReflectionPlane plane = getPlane(index.integer - 1);
		if (plane != null) planes.add(plane);
		// if (index.fraction < 0.5) Get.optional(getPlane(index.integer -
		// 1)).ifPresent(planes::add);
		// else Get.optional(getPlane(index.integer +
		// 1)).ifPresent(planes::add);
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
