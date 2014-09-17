package audiodrive.model.track;

import static org.lwjgl.opengl.GL11.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

import audiodrive.AudioDrive;
import audiodrive.audio.AnalyzedAudio;
import audiodrive.audio.AnalyzedChannel;
import audiodrive.audio.MinMax;
import audiodrive.audio.SpectraMinMax;
import audiodrive.model.Player;
import audiodrive.model.Ring;
import audiodrive.model.buffer.VertexBuffer;
import audiodrive.model.geometry.*;
import audiodrive.model.geometry.transform.Placement;
import audiodrive.model.geometry.transform.Rotation;
import audiodrive.model.loader.Model;
import audiodrive.model.tower.MusicTower;
import audiodrive.model.tower.RotationTower;
import audiodrive.model.tower.SpectralTower;
import audiodrive.model.tower.TubeTower;
import audiodrive.model.track.interpolation.CatmullRom;
import audiodrive.ui.GL;
import audiodrive.utilities.Arithmetic;
import audiodrive.utilities.Log;
import audiodrive.utilities.Range;

public class Track {
	
	private static final String TRACK_TEXTURE = "models/track/track.png";
	
	private AnalyzedAudio audio;
	private List<Vector> vectorinates;
	private List<Block> blocks;
	private int smoothing;
	
	private List<Block> visibleBlocks;
	private List<Ring> visibleRings;
	List<MusicTower> visibleMusicTowers;
	
	private List<Vector> spline;
	private List<Vector> splineArea;
	private List<Vertex> splineArea2;
	private double width = 3;
	private double borderHeight = 0.2;
	private double borderWidth = 0.2;
	private double flightHeight = 0.2;
	
	private int numberOfRails = 3;
	private int numberOfCollectables;
	private int numberOfObstacles;
	
	private VertexBuffer pointBuffer;
	private VertexBuffer splineBuffer;
	private VertexBuffer splineAreaBuffer;
	private VertexBuffer splineArea2Buffer;
	private VertexBuffer leftBorderBuffer;
	private VertexBuffer rightBorderBuffer;
	private CuboidStripRenderer cuboidStripRenderer;
	private Texture trackTexture;
	private List<Vertex> leftVertexList;
	private VertexBuffer leftBorderVertexBuffer;
	private List<Vertex> rightVertexList;
	private VertexBuffer rightBorderVertexBuffer;
	
	private Color relaxedColor = AudioDrive.Settings.getColor("color.relaxed");
	private Color intenseColor = AudioDrive.Settings.getColor("color.intense");
	private boolean staticCollectableColor = AudioDrive.Settings.getBoolean("color.collectable.static");
	private List<MusicTower> musicTowers;
	private List<MinMax> spectraMinMax;
	
	private Player player;
	
	private Index index;
	
	public Track(AnalyzedAudio audio, List<Vector> vectorinates, List<Block> blocks, int smoothing) {
		this.audio = audio;
		this.vectorinates = vectorinates;
		this.blocks = blocks;
		this.smoothing = smoothing;
		try {
			trackTexture = TextureLoader.getTexture("PNG", new FileInputStream(new File(TRACK_TEXTURE)));
		} catch (IOException e) {
			Log.error(e);
		}
		numberOfCollectables = (int) blocks.stream().filter(Block::isCollectable).count();
		numberOfObstacles = blocks.size() - numberOfCollectables;
		build();
		spectraMinMax = SpectraMinMax.getMinMax(audio.getMix());
	}
	
	private void build() {
		calculateSpline();
		generateTowers();
		splineArea2Buffer = new VertexBuffer(splineArea2).mode(GL_QUAD_STRIP).useColor(true).useTexture(true);
	}
	
	private void generateTowers() {
		int spacing = 300;
		musicTowers = new ArrayList<>();
		for (int iteration = spacing; iteration < audio.getIterationCount(); iteration += spacing) {
			float peak = audio.getMix().getThreshold().getClamped(iteration);
			if (peak > 0.7) musicTowers.add(new TubeTower(iteration));
			else if (peak > 0.3) musicTowers.add(new SpectralTower(iteration));
			else musicTowers.add(new RotationTower(iteration));
		}
	}
	
	private void calculateSpline() {
		spline = CatmullRom.interpolate(vectorinates, smoothing, CatmullRom.Type.Centripetal);
		splineArea = new ArrayList<>();
		splineArea2 = new ArrayList<>();
		
		if (spline == null || spline.isEmpty()) return;
		Vector last = null;
		
		double sideWidth = width / 2;
		for (int i = 0; i < spline.size() - 1; i++) {
			Vector one = spline.get(i);
			Vector two = spline.get(i + 1);
			Vector next;
			
			// Check if first
			if (last == null) {
				last = two.minus(one).cross(Vector.Y).length(sideWidth);
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
				next = n1.plus(n2).length(sideWidth / Math.cos(n1.angle(n2) * 0.5));
			} else { // Normal
				next = two.minus(one).cross(Vector.Y).length(sideWidth);
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
		
		leftVertexList = calcBorderVertexes(leftBorder);
		rightVertexList = calcBorderVertexes(rightBorder);
		
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
		
		List<Vertex> vertexList = new ArrayList<>();
		final int size = border.size();
		
		// Add front
		Vector v1_lastVector = border.get(3);
		{
			final Vector v2 = border.get(2);
			final Vector v3 = border.get(3);
			Vertex vertex = getVertex(v1_lastVector, v3, v2, false);
			vertexList.add(vertex.clone().position(border.get(0)));
			vertexList.add(vertex.clone().position(border.get(1)));
			vertexList.add(vertex.clone().position(v2));
			vertexList.add(vertex.clone().position(v3));
		}
		
		// Draw right side
		for (int i = 2; i < size; i += 3) {
			final Vector v2 = border.get(i);
			final Vector v3 = border.get(++i);
			Vertex vertex = getVertex(v1_lastVector, v3, v2, false);
			v1_lastVector = v2;
			vertexList.add(vertex.clone().position(v2));
			vertexList.add(vertex.clone().position(v3));
		}
		
		// Add back
		{
			final Vector v2 = border.get(size - 4);
			final Vector v3 = border.get(size - 3);
			Vertex vertex = getVertex(v1_lastVector, v3, v2, false);
			v1_lastVector = v2;
			vertexList.add(vertex.clone().position(border.get(size - 2)));
			vertexList.add(vertex.clone().position(border.get(size - 1)));
			vertexList.add(vertex.clone().position(v2));
			vertexList.add(vertex.clone().position(v3));
		}
		
		// Draw left side
		for (int i = size - 4; i >= 0; i -= 5) {
			final Vector v2 = border.get(i);
			final Vector v3 = border.get(++i);
			Vertex vertex = getVertex(v1_lastVector, v3, v2, true);
			v1_lastVector = v2;
			vertexList.add(vertex.clone().position(v2));
			vertexList.add(vertex.clone().position(v3));
		}
		
		// Add fix
		{
			final Vector v2 = border.get(0);
			final Vector v3 = border.get(2);
			Vertex vertex = getVertex(border.get(3), v3, v2, false);
			v1_lastVector = v2;
			vertexList.add(vertex.clone().position(v2));
			vertexList.add(vertex.clone().position(v3));
		}
		
		// Draw top side
		for (int i = 4; i < size; i += 2) {
			final Vector v2 = border.get(i);
			i += 2;
			final Vector v3 = border.get(i);
			Vertex vertex = getVertex(v1_lastVector, v3, v2, false);
			v1_lastVector = v2;
			vertexList.add(vertex.clone().position(v2));
			vertexList.add(vertex.clone().position(v3));
		}
		
		// Dont know if needed to draw bottom layer
		// Draw bottom
		for (int i = size - 3; i >= 0; i -= 6) {
			final Vector v2 = border.get(i);
			i += 2;
			final Vector v3 = border.get(i);
			Vertex vertex = getVertex(v1_lastVector, v3, v2, true);
			v1_lastVector = v2;
			vertexList.add(vertex.clone().position(v2));
			vertexList.add(vertex.clone().position(v3));
		}
		
		return vertexList;
	}
	
	private Vertex getVertex(Vector v1, final Vector v2, final Vector v3, boolean switchColor) {
		double a = (switchColor) ? v3.y() - v1.y() : v1.y() - v3.y();
		final Color color = getColor(a);
		Vertex vertex = new Vertex().color(color).normal(v2.clone().subtract(v1).cross(v3.clone().subtract(v1)));
		return vertex;
	}
	
	private Color getColor(double t) {
		return Color.Lerp(relaxedColor, intenseColor, (float) Arithmetic.scaleLinear(t, 0, 1, -.7f, .7f));
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
		
		leftVertex.color = new Color(1, 1, 1, .8);
		rightVertex.color = new Color(1, 1, 1, .8);
		
		splineArea2.add(leftVertex);
		splineArea2.add(rightVertex);
		
	}
	
	public void update(double time) {
		int preview = 250;
		int review = 10;
		index = getIndex(time);
		int minimum = Math.max(index.integer - review, 5);
		int maximum = Math.min(index.integer + preview, spline.size() - 2);
		
		int iteration = (int) (audio.getIterationRate() * time);
		if (iteration >= audio.getIterationCount()) iteration = audio.getIterationCount() - 1;
		Color currentColor = getColorAtIndex(index.integer);
		visibleBlocks = blocks.stream().filter(block -> block.iteration() > minimum && block.iteration() < maximum).collect(Collectors.toList());
		visibleBlocks.forEach(block -> {
			double position = block.iteration() - (block.iteration() - index.integer) / 2.0;
			block.placement(getPlacement(new Index((int) position, position - (int) position), true, block.rail()));
			block.placement().direction().negate(); // flip direction for logo
			if (!staticCollectableColor && block.isCollectable()) block.color(currentColor);
		});
		
		AnalyzedChannel mix = audio.getMix();
		double additionalRingScale = 0.5 * mix.getPeaks().getClamped(iteration);
		visibleRings = new ArrayList<>();
		for (int i = minimum; i < maximum; i++) {
			double baseRingScale = 5 - 3 * mix.getThreshold().getClamped(i);
			float peak = mix.getPeaks().getClamped(i);
			final Color ringColor = getColorAtIndex(i);
			Placement placement = getPlacement(new Index(i, 0.5), true, 0);
			placement.direction().negate();
			if (peak > 0) visibleRings.add(new Ring(ringColor, placement).scale(baseRingScale + additionalRingScale));
		}
		
		float[] spectrum2 = mix.getSpectrum(iteration);
		float current = spectrum2[1];
		MinMax minMax = spectraMinMax.get(1);
		double linearIntensity = Arithmetic.scaleLinear(current, 0.1, 1.0, minMax.min, minMax.max);
		double rotationSpeed = mix.getSpectralSum().getClamped(iteration) * 360;
		
		visibleMusicTowers = musicTowers
			.stream()
			.filter(musicTower -> musicTower.iteration() > index.integer - 10 && musicTower.iteration() < index.integer + 900)
			.collect(Collectors.toList());
		visibleMusicTowers.forEach(musicTower -> {
			float f = mix.getSpectrum(musicTower.iteration())[1];
			Placement a = getPlacement(new Index(musicTower.iteration(), 0), true, 0);
			if (musicTower instanceof RotationTower) {
				((RotationTower) musicTower).rotation(rotationSpeed);
				a.position().yAdd(10);
			} else {
				a.position().xAdd((((int) f) % 2 == 0) ? -50 : 50).zAdd(-50);
			}
			// a.direction(Vector.Z);
			if (!(musicTower instanceof SpectralTower)) musicTower.intensity(linearIntensity);
			musicTower.placement(a).color(currentColor);
		});
		
		int combine = 15;
		double maxIntensity = spectraMinMax.stream().mapToDouble(v -> v.max).max().orElse(0);
		double[] intensities = new double[audio.getBandCount() / combine + 1];
		for (int i = 0; i < audio.getBandCount(); i++) {
			intensities[i / combine] += Arithmetic.scaleLogarithmic(spectrum2[i], 0.0, 1.0 / combine, 0, maxIntensity);
		}
		SpectralTower.spectrum(intensities);
	}
	
	private Color getColorAtIndex(int index) {
		// TODO no hax :D
		final Color color = leftVertexList.get(index * 2).color.clone();
		return color;
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
			glEnable(GL_CULL_FACE);
		}
		
		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
		drawReflections();
		// Draw track
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
		// glDisable(GL_LIGHTING);
		glCullFace(GL_FRONT);
		splineArea2Buffer.useColor(false);
		Color.Black.gl();
		splineArea2Buffer.draw();
		splineArea2Buffer.useColor(true);
		GL.popAttributes();
		// Draw obstacles
		
		visibleMusicTowers.forEach(MusicTower::render);
		visibleBlocks.forEach(Block::render);
		visibleRings.forEach(Ring::render);
		
		// Draw borders
		leftBorderVertexBuffer.draw();
		rightBorderVertexBuffer.draw();
		
		// drawBorderNormals(leftVertexList);
		// drawBorderNormals(rightVertexList);
	}
	
	private void drawReflections() {
		glClear(GL_STENCIL_BUFFER_BIT);
		// write reflection surface to stencil buffer
		glColorMask(false, false, false, false);
		glDisable(GL_DEPTH_TEST);
		glEnable(GL_STENCIL_TEST);
		glStencilFunc(GL_ALWAYS, 1, 0xffffffff);
		glStencilOp(GL_REPLACE, GL_REPLACE, GL_REPLACE);
		splineArea2Buffer.draw();
		// TODO find better solution
		// write down-shifted track-area to depth buffer to avoid reflections of hidden objects
		glEnable(GL_DEPTH_TEST);
		glDisable(GL_STENCIL_TEST);
		glTranslated(0, -2 * flightHeight, 0);
		splineArea2Buffer.draw();
		glTranslated(0, 2 * flightHeight, 0);
		// render objects according to stencil buffer
		glColorMask(true, true, true, true);
		glEnable(GL_STENCIL_TEST);
		glStencilFunc(GL_EQUAL, 1, 0xffffffff);
		glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
		Model model = player.model();
		// draw block reflections
		int range = smoothing * 5;
		Rotation flip = new Rotation().z(180);
		Texture originalTexture = Block.Model.getTexture();
		Block.Model.setTexture(Block.Reflected);
		blocks.stream().filter(block -> block.iteration() > index.integer - range && block.iteration() < index.integer + range).forEach(block -> {
			Placement placement = block.placement();
			Placement originalPlacement = placement.clone();
			placement.position(placement.position().plus(placement.up().multiplied(-2 * flightHeight)));
			Block.Model.transformations().add(flip);
			block.render();
			Block.Model.transformations().remove(flip);
			block.placement(originalPlacement);
		});;
		Block.Model.setTexture(originalTexture);
		// draw player reflection
		Placement placement = model.placement();
		Placement originalPlacement = placement.clone();
		placement.position(placement.position().plus(placement.up().multiplied(-2 * flightHeight)));
		model.rotation().invert();
		model.transformations().add(flip);
		model.render();
		model.transformations().remove(flip);
		model.rotation().invert();
		model.placement(originalPlacement);
		glDisable(GL_STENCIL_TEST);
	}
	
	private void drawBorderNormals(List<Vertex> vertexList) {
		Color.Blue.gl();
		for (Vertex vertex : vertexList) {
			glBegin(GL_LINES);
			final Vector position = vertex.position;
			glVertex3d(position.x(), position.y(), position.z());
			final Vector normal = vertex.normal;
			final Vector add = position.clone().add(normal);
			glVertex3d(add.x(), add.y(), add.z());
			glEnd();
		}
	}
	
	public Index index() {
		return index;
	}
	
	public int lastIndex() {
		return spline.size() - 2;
	}
	
	public Index getIndex(double time) {
		double index = time * spline.size() / audio.getDuration();
		if (index >= spline.size() - 1) index = spline.size() - 2;
		int integer = (int) index;
		double fraction = index - integer;
		return new Index(integer, fraction);
	}
	
	public Placement getPlacement(double time) {
		return getPlacement(getIndex(time), false, 0);
	}
	
	private Placement getPlacement(Index index, boolean interpolated, int rail) {
		Vector current = spline.get(index.integer);
		Vector next = spline.get(index.integer + 1);
		Vector direction = next.minus(current);
		Vector up = Vector.Y;
		Vector position = current.plus(direction.multiplied(interpolated ? index.fraction : 0.5)).plus(up.multiplied(flightHeight));
		Placement placement = new Placement().position(position).direction(direction).up(up);
		if (rail != 0) placement.position().add(placement.side().multiplied(Math.signum(rail) * railWidth()));
		return placement;
	}
	
	public AnalyzedAudio getAudio() {
		return audio;
	}
	
	public List<Vector> getVectors() {
		return vectorinates;
	}
	
	public double getDuration() {
		return audio.getDuration();
	}
	
	public int getSmoothing() {
		return smoothing;
	}
	
	public List<Vector> spline() {
		return spline;
	}
	
	public List<Block> getBlocks() {
		return blocks;
	}
	
	public double width() {
		return width;
	}
	
	public double railWidth() {
		return width / numberOfRails;
	}
	
	public Track player(Player player) {
		this.player = player;
		return this;
	}
	
	public Range getRailRange(int rail) {
		double railWidth = railWidth();
		double offset = rail * railWidth;
		return new Range(offset - railWidth / 2, offset + railWidth / 2);
	}
	
	public int getNumberOfRails() {
		return numberOfRails;
	}
	
	public int getNumberOfCollectables() {
		return numberOfCollectables;
	}
	
	public int getNumberOfObstacles() {
		return numberOfObstacles;
	}
	
	public class Index {
		
		public final int integer;
		public final double fraction;
		
		public Index(int integer, double fraction) {
			if (integer > spline.size() - 2) integer = spline.size() - 2;
			this.integer = integer;
			this.fraction = fraction;
		}
		
		@Override
		public String toString() {
			return "Index [integer=" + integer + ", fraction=" + fraction + "]";
		}
		
	}
	
}
