package audiodrive.model.track;

import static org.lwjgl.opengl.GL11.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.newdawn.slick.opengl.Texture;

import audiodrive.AudioDrive;
import audiodrive.Resources;
import audiodrive.audio.AnalyzedAudio;
import audiodrive.audio.AnalyzedChannel;
import audiodrive.model.Player;
import audiodrive.model.Renderable;
import audiodrive.model.buffer.VertexBuffer;
import audiodrive.model.geometry.Color;
import audiodrive.model.geometry.CuboidStripRenderer;
import audiodrive.model.geometry.Matrix;
import audiodrive.model.geometry.TextureCoordinate;
import audiodrive.model.geometry.Vector;
import audiodrive.model.geometry.Vertex;
import audiodrive.model.geometry.transform.Placement;
import audiodrive.model.geometry.transform.Rotation;
import audiodrive.model.loader.Model;
import audiodrive.model.loader.ModelLoader;
import audiodrive.model.tower.MusicTower;
import audiodrive.model.tower.RotationTower;
import audiodrive.model.tower.SpectralTower;
import audiodrive.model.tower.TubeTower;
import audiodrive.ui.GL;
import audiodrive.ui.components.Viewport;
import audiodrive.ui.effects.Glow;
import audiodrive.ui.effects.Particles3D;
import audiodrive.ui.scenes.GameScene;
import audiodrive.utilities.Arithmetic;
import audiodrive.utilities.Range;

public class Track implements Renderable {
	
	private final AnalyzedAudio audio;
	private final List<Vector> spline;
	private final List<Block> blocks;
	private final int smoothing;
	
	private final int numberOfRails = 3;
	private final int numberOfCollectables;
	private final int numberOfObstacles;
	private final double indexRate;
	
	private List<Block> visibleBlocks;
	private List<Ring> visibleRings;
	private List<MusicTower> visibleMusicTowers;
	private List<MusicTower> musicTowers;
	
	private List<Vector> splineArea;
	private List<Vertex> splineArea2;
	private double width = 3;
	private double borderHeight = 0.2;
	private double borderWidth = 0.2;
	private double flightHeight = 0.2;
	
	private VertexBuffer pointBuffer;
	private VertexBuffer splineBuffer;
	private VertexBuffer splineAreaBuffer;
	private VertexBuffer splineArea2Buffer;
	private VertexBuffer leftBorderBuffer;
	private VertexBuffer rightBorderBuffer;
	private CuboidStripRenderer cuboidStripRenderer;
	
	private List<Vertex> leftVertexList;
	private VertexBuffer leftBorderVertexBuffer;
	private List<Vertex> rightVertexList;
	private VertexBuffer rightBorderVertexBuffer;
	
	private Texture trackTexture;
	private Color trackColor;
	private Color relaxedColor;
	private Color averageColor;
	private Color intenseColor;
	
	private int sight;
	
	private Glow glow;
	private Model skybox;
	private Particles3D particles;
	
	private Player player;
	private Index index;
	
	public Track(AnalyzedAudio audio, List<Vector> spline, List<Block> blocks, int smoothing) {
		this.audio = audio;
		this.spline = spline;
		this.blocks = blocks;
		this.smoothing = smoothing;
		numberOfCollectables = (int) blocks.stream().filter(Block::isCollectable).count();
		numberOfObstacles = blocks.size() - numberOfCollectables;
		indexRate = spline.size() / audio.getDuration();
	}
	
	public void build() {
		sight = AudioDrive.Settings.getInteger("game.sight");
		trackColor = GameScene.night ? Color.Black : Color.White;
		trackTexture = Resources.getTexture(GameScene.night ? "textures/track/track-black.png" : "textures/track/track-white.png");
		relaxedColor = AudioDrive.Settings.getColor("color.relaxed");
		averageColor = AudioDrive.Settings.getColor("color.average");
		intenseColor = AudioDrive.Settings.getColor("color.intense");
		particles = new Particles3D();
		glow = new Glow().depthpass(() -> {
			splineArea2Buffer.draw();
			leftBorderVertexBuffer.draw();
			rightBorderVertexBuffer.draw();
			visibleBlocks.stream().filter(block -> !block.isGlowing()).forEach(Block::render);
			if (!player.isGlowing()) player.render();
		}).renderpass(() -> {
			visibleBlocks.stream().filter(Block::isGlowing).forEach(Block::render);
			visibleMusicTowers.stream().forEach(MusicTower::render);
			visibleRings.stream().forEach(Ring::render);
			particles.render();
			if (player.isGlowing()) player.render();
		});
		skybox = ModelLoader.loadModel("models/skybox/skybox").scale(GameScene.Far / 4);
		skybox.setTexture(Resources.getTexture(GameScene.night ? "models/skybox/night.png" : "models/skybox/day.png"));
		generateTrack();
		generateTowers();
	}
	
	private void generateTowers() {
		musicTowers = new ArrayList<>();
		if (!GameScene.environment) return;
		int spacing = 300;
		for (int iteration = spacing; iteration < spline.size(); iteration += spacing) {
			float peak = audio.getMix().getThreshold().getClamped(iteration);
			if (peak > 0.7) musicTowers.add(new TubeTower(iteration));
			else if (peak > 0.3) musicTowers.add(new SpectralTower(iteration));
			else musicTowers.add(new RotationTower(iteration));
		}
	}
	
	private void generateTrack() {
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
		splineArea2Buffer = new VertexBuffer(splineArea2).mode(GL_QUAD_STRIP).useColor(true).useTexture(true);
		
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
		return Color.lerp(relaxedColor, averageColor, intenseColor, Arithmetic.scaleLinear(t, 0, 1, -0.5f, 0.5f));
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
		rightTexture = new TextureCoordinate(1, offset);
		
		textureIndex += sub;
		
		// Set texture coordinates
		leftVertex.textureCoordinate = leftTexture;
		rightVertex.textureCoordinate = rightTexture;
		
		leftVertex.color = Color.White;
		rightVertex.color = Color.White;
		
		splineArea2.add(leftVertex);
		splineArea2.add(rightVertex);
	}
	
	@Override
	public void update(double time) {
		int preview = sight;
		int review = sight / 2;
		index = getIndex(time);
		if (GameScene.sky) skybox.placement().position().set(player.model().position());
		int minimum = Math.max(index.integer - review, 0);
		int maximum = Math.min(index.integer + preview, lastIndex());
		
		int iteration = (int) (audio.getIterationRate() * time);
		if (iteration >= audio.getIterationCount()) iteration = audio.getIterationCount() - 1;
		Color borderColor = getColorAtIndex(index.integer);
		Color inverseBorderColor = borderColor.inverse();
		visibleBlocks = blocks.stream().filter(block -> block.iteration() > minimum && block.iteration() < maximum).collect(Collectors.toList());
		visibleBlocks.forEach(block -> {
			double position = block.iteration() - (block.iteration() - index.integer) / 2.0;
			block.placement(getPlacement(new Index((int) position, position - (int) position), true, block.rail()));
			block.update(index.integer);
			if (GameScene.colorizeCollectables && GameScene.colorizeObstacles) {
				block.color(block.isCollectable() ? inverseBorderColor : borderColor);
			} else if (block.isCollectable()) {
				if (GameScene.colorizeCollectables) block.color(borderColor);
				else if (!GameScene.night && block.color().equals(Color.White)) block.color(Color.Gray);
			} else {
				if (GameScene.colorizeObstacles) block.color(borderColor);
			}
		});
		
		AnalyzedChannel mix = audio.getMix();
		visibleRings = new ArrayList<>();
		double pulse = GameScene.visualization ? Arithmetic.smooth(0, 2, mix.getPeaks().getClamped(iteration)) : 0;
		for (int i = minimum; i < maximum; i++) {
			if (mix.getPeaks().getClamped(i) == 0) continue;
			double ringScale = 5 - 3 * mix.getThreshold().getClamped(i);
			Color ringColor = getColorAtIndex(i);
			Placement placement = getPlacement(new Index(i, 0.5), true, 0);
			visibleRings.add(new Ring(i, ringColor, placement).scale(ringScale).pulse(pulse));
		}
		
		double linearIntensity = mix.getBands().get(1).getClamped(iteration);
		double rotationSpeed = mix.getSpectralSum().getClamped(iteration) * 180;
		
		if (GameScene.environment) {
			visibleMusicTowers = musicTowers
				.stream()
				.filter(musicTower -> musicTower.iteration() > index.integer - review && musicTower.iteration() < index.integer + preview * 5)
				.collect(Collectors.toList());
			visibleMusicTowers.forEach(musicTower -> {
				float f = mix.getSpectrum(musicTower.iteration())[1];
				Placement a = getPlacement(new Index(musicTower.iteration(), 0), true, 0);
				if (musicTower instanceof RotationTower) {
					((RotationTower) musicTower).rotation(rotationSpeed);
					a.position().yAdd(15);
				} else {
					a.position().xAdd((((int) f) % 2 == 0) ? -50 : 50).zAdd(-50);
				}
				if (musicTower instanceof TubeTower) musicTower.intensity(.1 + linearIntensity);
				else if (musicTower instanceof RotationTower) musicTower.intensity(Math.min(linearIntensity + 0.5, 1));
				musicTower.placement(a).color(borderColor);
			});
		} else {
			visibleMusicTowers = Collections.emptyList();
		}
		
		int combine = 15;
		double maxIntensity = mix.getMaximum();
		double[] intensities = new double[audio.getBandCount() / combine + 1];
		for (int i = 0; i < audio.getBandCount(); i++) {
			intensities[i / combine] += Arithmetic.scaleLogarithmic(mix.getBands().get(i).get(iteration), 0.0, 1.0 / combine, 0, maxIntensity);
		}
		SpectralTower.spectrum(intensities);
		
		if (GameScene.particles) {
			if (linearIntensity > .5) {
				final int xOffset = (int) (20 + Math.random() * 10);
				particles.createParticles(getPlacement(time + 2).position().yAdd(-5).xAdd((Math.random() < .5) ? xOffset : -xOffset), borderColor, linearIntensity);
			}
		}
		particles.update(time);
	}
	
	public Color getColorAtIndex(int index) {
		// TODO no hax :D
		final Color color = leftVertexList.get(index * 2).color.clone();
		return color;
	}
	
	@Override
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
		
		if (GameScene.reflections) drawReflections();
		
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
		// Draw track back-side
		glCullFace(GL_FRONT);
		splineArea2Buffer.useColor(false);
		trackColor.gl();
		splineArea2Buffer.draw();
		splineArea2Buffer.useColor(true);
		glCullFace(GL_BACK);
		
		visibleMusicTowers.forEach(MusicTower::render);
		visibleBlocks.forEach(Block::render);
		
		// Draw borders
		leftBorderVertexBuffer.draw();
		rightBorderVertexBuffer.draw();
		
		glDisable(GL_CULL_FACE);
		glDisable(GL_LIGHTING);
		if (GameScene.sky) skybox.render();
		visibleRings.forEach(Ring::render);
		glEnable(GL_LIGHTING);
		glEnable(GL_CULL_FACE);
		particles.render();
		
		// drawBorderNormals(leftVertexList);
		// drawBorderNormals(rightVertexList);
	}
	
	private void drawReflections() {
		glClear(GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
		glColorMask(false, false, false, false);
		
		// write track-back-side to depth buffer
		glCullFace(GL_FRONT);
		splineArea2Buffer.draw();
		glCullFace(GL_BACK);
		
		// write reflection surface to stencil and depth buffer
		glEnable(GL_STENCIL_TEST);
		glStencilFunc(GL_ALWAYS, 1, 0xffffffff);
		glStencilOp(GL_REPLACE, GL_KEEP, GL_REPLACE);
		splineArea2Buffer.draw();
		
		// filter objects depending on visibility and depth buffer
		int range = sight / 2;
		Matrix mvpMatrix = GL.modelviewProjectionMatrix();
		Viewport viewport = GL.viewport();
		Range depthRange = GL.depthRange();
		List<Block> blocks = visibleBlocks.stream().filter(block -> block.iteration() > index.integer - range && block.iteration() < index.integer + range).filter(block -> {
			if (Math.abs(block.iteration() - index.integer) < indexRate * 0.15) return true; // render reflection of close blocks always
			Vector side = block.placement().side().multiplied(block.width());
			Vector leftScreenspaceVector = GL.screenspace(block.placement().position().plus(side), mvpMatrix, viewport, depthRange);
			Vector rightScreenspaceVector = GL.screenspace(block.placement().position().plus(side.negated()), mvpMatrix, viewport, depthRange);
			return isVisible(viewport, leftScreenspaceVector, rightScreenspaceVector);
		}).collect(Collectors.toList());
		List<Ring> rings = visibleRings.stream().filter(ring -> ring.iteration() > index.integer - range && ring.iteration() < index.integer + range).filter(ring -> {
			if (Math.abs(ring.iteration() - index.integer) < indexRate * 0.15) return true; // render reflection of close rings always
			Vector side = ring.placement().side().multiplied(ring.width());
			Vector leftScreenspaceVector = GL.screenspace(ring.placement().position().plus(side), mvpMatrix, viewport, depthRange);
			Vector rightScreenspaceVector = GL.screenspace(ring.placement().position().plus(side.negated()), mvpMatrix, viewport, depthRange);
			return isVisible(viewport, leftScreenspaceVector, rightScreenspaceVector);
		}).collect(Collectors.toList());
		
		// render objects according to stencil buffer
		glClear(GL_DEPTH_BUFFER_BIT);
		glColorMask(true, true, true, true);
		glStencilFunc(GL_EQUAL, 1, 0xffffffff);
		glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
		
		// create rotation to flip objects horizontally
		Rotation flip = new Rotation().z(180);
		
		// draw ring reflections
		glDisable(GL_CULL_FACE);
		rings.forEach(ring -> {
			double distance = Math.abs(ring.iteration() - index.integer);
			double alpha = Arithmetic.scaleLinear(distance, 1, 0, 0, range);
			final Color color = ring.color();
			ring.color(color.alpha(alpha));
			ring.render();
			ring.color(color);
		});
		glEnable(GL_CULL_FACE);
		
		// draw block reflections
		blocks.forEach(block -> {
			Placement placement = block.placement();
			Placement originalPlacement = placement.clone();
			placement.position(placement.position().plus(placement.up().multiplied(-2 * flightHeight)));
			double distance = Math.abs(block.iteration() - index.integer);
			double alpha = Arithmetic.scaleLinear(distance, 1, 0, 0, range);
			final Color color = block.color();
			block.color(color.alpha(alpha));
			block.model().setTexture(Resources.getReflectedBlockTexture());
			block.model().transformations().add(flip);
			block.render();
			block.model().transformations().remove(flip);
			block.placement(originalPlacement);
			block.model().setTexture(Resources.getBlockTexture());
			block.color(color);
		});;
		
		// draw player reflection
		Model model = player.model();
		Placement placement = model.placement();
		Placement originalPlacement = placement.clone();
		placement.position(placement.position().plus(placement.up().multiplied(-2 * (flightHeight + model.translation().y()))));
		model.rotation().invert();
		player.inclination().invert();
		model.transformations().add(flip);
		player.render();
		model.transformations().remove(flip);
		player.inclination().invert();
		model.rotation().invert();
		model.placement(originalPlacement);
		
		glDisable(GL_STENCIL_TEST);
	}
	
	// TODO improve dat performance thief
	private boolean isVisible(Viewport viewport, Vector leftScreenspaceVector, Vector rightScreenspaceVector) {
		boolean leftVisible = viewport.contains(leftScreenspaceVector);
		boolean rightVisible = viewport.contains(rightScreenspaceVector);
		if (!leftVisible && !rightVisible) return false;
		if (leftVisible && rightVisible) {
			double depthLeft = GL.depthBufferValue((int) leftScreenspaceVector.x(), (int) leftScreenspaceVector.y());
			double depthRight = GL.depthBufferValue((int) rightScreenspaceVector.x(), (int) rightScreenspaceVector.y());
			return leftScreenspaceVector.z() < depthLeft && rightScreenspaceVector.z() < depthRight;
		} else if (leftVisible) {
			double depthLeft = GL.depthBufferValue((int) leftScreenspaceVector.x(), (int) leftScreenspaceVector.y());
			return leftScreenspaceVector.z() < depthLeft;
			
		} else {
			double depthRight = GL.depthBufferValue((int) rightScreenspaceVector.x(), (int) rightScreenspaceVector.y());
			return rightScreenspaceVector.z() < depthRight;
		}
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
	
	public double indexRate() {
		return indexRate;
	}
	
	public Index getIndex(double time) {
		double index = time * indexRate();
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
	
	public Glow glow() {
		return glow;
	}
	
	public Color color() {
		return trackColor;
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
