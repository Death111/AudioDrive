package audiodrive.ui.overlays;

import static org.lwjgl.opengl.GL11.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import audiodrive.AudioDrive;
import audiodrive.audio.AnalyzedAudio;
import audiodrive.audio.AnalyzedChannel;
import audiodrive.model.Player;
import audiodrive.model.geometry.Color;
import audiodrive.model.geometry.Vector;
import audiodrive.model.track.Block;
import audiodrive.model.track.Track;
import audiodrive.ui.components.Camera;
import audiodrive.ui.components.Overlay;
import audiodrive.ui.components.Scene;
import audiodrive.ui.components.Text;
import audiodrive.ui.components.Text.Alignment;
import audiodrive.ui.components.Window;
import audiodrive.ui.scenes.GameScene;
import audiodrive.ui.scenes.GameScene.State;
import audiodrive.utilities.Arithmetic;
import audiodrive.utilities.Format;
import audiodrive.utilities.Memory;
import audiodrive.utilities.Memory.Unit;

public class GameOverlay extends Overlay {
	
	private Map<String, Text> texts = new HashMap<>();
	
	private GameScene scene;
	private TrackOverview trackOverview;
	private Player player;
	
	private int width, height;
	private int collectables;
	
	private AnalyzedAudio audio;
	private AnalyzedChannel leftChannel;
	private AnalyzedChannel rightChannel;
	private int bands;
	private float[] leftSpectrum;
	private float[] rightSpectrum;
	private Color spectrumColor = Color.White;
	private Color spectrumFadeColor = Color.Transparent;
	
	public GameOverlay(GameScene scene) {
		this.scene = scene;
		width = scene.getWidth();
		height = scene.getHeight();
		player = scene.getPlayer();
		trackOverview = new TrackOverview(scene.getTrack());
		Color color = scene.getTrack().color().inverse();
		text("title").setText(scene.getTrack().getAudio().getName()).setColor(color).setSize(15).setPosition(width * 0.5, height - 10).setAlignment(Alignment.LowerCenter);
		text("time").setColor(color).setSize(15).setPosition(scene.getWidth() - 10, 130).setAlignment(Alignment.UpperRight);
		text("memory").setColor(color).setSize(10).setPosition(scene.getWidth() - 10, 155).setAlignment(Alignment.UpperRight);
		text("framerate").setColor(color).setSize(10).setPosition(scene.getWidth() - 10, 170).setAlignment(Alignment.UpperRight);
		text("points").setColor(color).setSize(30).setPosition(10, 10);
		text("damage").setColor(color).setSize(30).setPosition(10, 50);
		text("collected").setColor(color).setSize(20).setPosition(10, 110);
		text("collided").setColor(color).setSize(20).setPosition(10, 140);
		int infoSize = 11;
		int infoSpacing = 15;
		text("settings").setText("Display Settings").setColor(color).setSize(15).setPosition(10, scene.getHeight() - infoSpacing * 12).setAlignment(Alignment.LowerLeft);
		text("environment").setColor(color).setSize(infoSize).setPosition(10, scene.getHeight() - infoSpacing * 11).setAlignment(Alignment.LowerLeft);
		text("glow").setColor(color).setSize(infoSize).setPosition(10, scene.getHeight() - infoSpacing * 10).setAlignment(Alignment.LowerLeft);
		text("health").setColor(color).setSize(infoSize).setPosition(10, scene.getHeight() - infoSpacing * 9).setAlignment(Alignment.LowerLeft);
		text("particles").setColor(color).setSize(infoSize).setPosition(10, scene.getHeight() - infoSpacing * 8).setAlignment(Alignment.LowerLeft);
		text("reflections").setColor(color).setSize(infoSize).setPosition(10, scene.getHeight() - infoSpacing * 7).setAlignment(Alignment.LowerLeft);
		text("rings").setColor(color).setSize(infoSize).setPosition(10, scene.getHeight() - infoSpacing * 6).setAlignment(Alignment.LowerLeft);
		text("sky").setColor(color).setSize(infoSize).setPosition(10, scene.getHeight() - infoSpacing * 5).setAlignment(Alignment.LowerLeft);
		text("spectrum").setColor(color).setSize(infoSize).setPosition(10, scene.getHeight() - infoSpacing * 4).setAlignment(Alignment.LowerLeft);
		text("visualization").setColor(color).setSize(infoSize).setPosition(10, scene.getHeight() - infoSpacing * 3).setAlignment(Alignment.LowerLeft);
		text("anti-aliasing").setColor(color).setSize(infoSize).setPosition(10, scene.getHeight() - infoSpacing * 2).setAlignment(Alignment.LowerLeft);
		text("v-sync").setColor(color).setSize(infoSize).setPosition(10, scene.getHeight() - infoSpacing * 1).setAlignment(Alignment.LowerLeft);
		text("notification").setColor(color).setSize(48).setPosition(width * 0.5, height * 0.5).setAlignment(Alignment.Center).setVisible(false);
		collectables = scene.getTrack().getNumberOfCollectables();
		audio = scene.getTrack().getAudio();
		bands = audio.getBandCount();
		leftChannel = audio.getChannel(0);
		rightChannel = audio.getChannel(1);
		leftSpectrum = new float[bands];
		rightSpectrum = new float[bands];
	}
	
	public void update() {
		trackOverview.updatePlayerPosition(scene.getTrack().index());
		List<Block> passed = scene.getTrack().getBlocks().stream().filter(block -> block.iteration() <= scene.getTrack().index().integer).collect(Collectors.toList());
		int passedCollectables = (int) passed.stream().filter(Block::isCollectable).count();
		int passedObstacles = passed.size() - passedCollectables;
		final int framerate = Scene.getFramerate();
		text("framerate").setText(framerate + " FPS").setColor(Color.lerp(Color.Red, Color.Yellow, Color.Green, (framerate - 30) / 30.0));
		text("memory").setText(Memory.used().toString(Unit.GB) + " / " + Memory.allocated().toString(Unit.GB));
		text("time").setText(Format.seconds(scene.playtime(), 1) + " / " + Format.seconds(audio.getDuration(), 1));
		text("points").setText(String.format("Points: %d / %d (%.0f%%)", player.points(), collectables, 100.0 * player.points() / collectables));
		text("damage").setText(String.format("Damage: %d / %d (%d%%)", player.collided(), player.hitpoints(), player.damage()));
		text("collected").setText(
			String.format("Collected: %d / %d (%.1f%%)", player.collected(), passedCollectables, 100.0 * player.collected() / Math.max(passedCollectables, 1)));
		text("collided").setText(String.format("Collided: %d / %d (%.1f%%)", player.collided(), passedObstacles, 100.0 * player.collided() / Math.max(passedObstacles, 1)));
		boolean paused = scene.getState() == State.Paused;
		text("settings").setVisible(paused);
		text("environment").setVisible(paused).setText("Environment: " + (GameScene.environment ? "on" : "off") + " (E)");
		text("glow").setVisible(paused).setText("Glow: " + (GameScene.glow ? "on" : "off") + " (G)");
		text("health").setVisible(paused).setText("Health: " + (GameScene.health ? "on" : "off") + " (H)");
		text("particles").setVisible(paused).setText("Particles: " + (GameScene.particles ? "on" : "off") + " (P)");
		text("reflections").setVisible(paused).setText("Reflections: " + (GameScene.reflections ? "on" : "off") + " (R)");
		text("rings").setVisible(paused).setText("Rings: " + (GameScene.rings ? "on" : "off") + " (Shift + R)");
		text("sky").setVisible(paused).setText("Sky: " + (GameScene.sky ? "on" : "off") + " (S)");
		text("spectrum").setVisible(paused).setText("Spectrum: " + (GameScene.spectrum ? "on" : "off") + " (Shift + S)");
		text("visualization").setVisible(paused).setText("Visualization: " + (GameScene.visualization ? "on" : "off") + " (V)");
		text("anti-aliasing").setVisible(paused).setText("Anti-Aliasing: " + (Window.isAntialiasingEnabled() ? "on" : "off") + " (Control + A)");
		text("v-sync").setVisible(paused).setText("V-Sync: " + (Window.isVSyncEnabled() ? "on" : "off") + " (Control + V)");
		Text notification = text("notification").setVisible(scene.getState() != State.Running && scene.getState() != State.Animating);
		switch (scene.getState()) {
		case Destroyed:
			notification.setText("Destroyed").setColor(Color.TransparentRed);
			break;
		case Ended:
			notification.setText("Finish").setColor(Color.TransparentGreen);
			break;
		case Paused:
			notification.setText("Paused").setColor(Color.TransparentBlue);
			break;
		case Resuming:
			notification.setText("Resuming...").setColor(Color.TransparentBlue);
			break;
		default:
			break;
		}
		if (GameScene.visualization && GameScene.spectrum) updateSpectrum();
	}
	
	private void updateSpectrum() {
		float responsivness = 15;
		float multiplier = 3;
		float limit = 0.15f;
		spectrumColor = scene.getTrack().getColorAtIndex(scene.getTrack().index().integer);
		int iteration = Arithmetic.clamp((int) (audio.getIterationRate() * scene.playtime()), 0, audio.getIterationCount() - 1);
		for (int band = 0; band < bands; band++) {
			double left = leftChannel.clamp(leftChannel.getBands().get(band).get(iteration)) * multiplier;
			double right = rightChannel.clamp(rightChannel.getBands().get(band).get(iteration)) * multiplier;
			leftSpectrum[band] += (left - leftSpectrum[band]) * Scene.deltaTime() * responsivness;
			rightSpectrum[band] += (right - rightSpectrum[band]) * Scene.deltaTime() * responsivness;
			if (leftSpectrum[band] > limit) leftSpectrum[band] = limit;
			if (rightSpectrum[band] > limit) rightSpectrum[band] = limit;
		}
	}
	
	@Override
	public void render() {
		Camera.overlay(width, height);
		glDisable(GL_LIGHTING);
		super.render();
		if (GameScene.visualization && GameScene.peaks) drawPeaks();
		if (GameScene.visualization && GameScene.spectrum) drawSpectrum();
		if (GameScene.particles && GameScene.sky && scene.getState() != State.Paused) scene.particles().render();
		trackOverview.render();
		if (text("notification").isVisible()) drawNotificationBackground();
		texts.values().forEach(Text::render);
		glEnable(GL_LIGHTING);
	}
	
	private void drawNotificationBackground() {
		Text notification = text("notification");
		int textHeight = notification.getHeight();
		notification.getColor().gl();
		glBegin(GL_QUADS);
		glVertex2d(0, height * 0.5 - textHeight);
		glVertex2d(0, height * 0.5 + textHeight);
		glVertex2d(width, height * 0.5 + textHeight);
		glVertex2d(width, height * 0.5 - textHeight);
		glEnd();
		notification.setColor(Color.White);
	}
	
	private Text text(String name) {
		Text text = texts.get(name);
		if (text == null) {
			text = new Text().setFont(AudioDrive.Font);
			texts.put(name, text);
		}
		return text;
	}
	
	private void drawSpectrum() {
		glDisable(GL_CULL_FACE);
		
		glBegin(GL_QUADS);
		double x = 0;
		double y = height / 2;
		double width = this.width / 2;
		double height = (double) this.height / 2 / bands;
		final Vector vector = new Vector();
		for (int band = 0; band < bands; band++) {
			double amplitude = leftSpectrum[band] * width;
			spectrumColor.gl();
			vector.x(x).y(y - height).glVertex();
			vector.x(x).y(y).glVertex();
			spectrumFadeColor.gl();
			vector.x(x + amplitude).y(y).glVertex();
			vector.x(x + amplitude).y(y - height).glVertex();
			y -= height;
		}
		x = this.width;
		y = this.height / 2;
		for (int band = 0; band < bands; band++) {
			double amplitude = rightSpectrum[band] * width;
			spectrumColor.gl();
			vector.x(x).y(y - height).glVertex();
			vector.x(x).y(y).glVertex();
			spectrumFadeColor.gl();
			vector.x(x - amplitude).y(y).glVertex();
			vector.x(x - amplitude).y(y - height).glVertex();
			y -= height;
		}
		
		x = 0;
		y = this.height / 2;
		for (int band = 0; band < bands; band++) {
			double amplitude = leftSpectrum[band] * width;
			spectrumColor.gl();
			vector.x(x).y(y + height).glVertex();
			vector.x(x).y(y).glVertex();
			spectrumFadeColor.gl();
			vector.x(x + amplitude).y(y).glVertex();
			vector.x(x + amplitude).y(y + height).glVertex();
			y += height;
		}
		x = this.width;
		y = this.height / 2;
		for (int band = 0; band < bands; band++) {
			double amplitude = rightSpectrum[band] * width;
			spectrumColor.gl();
			vector.x(x).y(y + height).glVertex();
			vector.x(x).y(y).glVertex();
			spectrumFadeColor.gl();
			vector.x(x - amplitude).y(y).glVertex();
			vector.x(x - amplitude).y(y + height).glVertex();
			y += height;
		}
		glEnd();
		glEnable(GL_CULL_FACE);
	}
	
	private void drawPeaks() {
		Track track = scene.getTrack();
		AnalyzedAudio audio = track.getAudio();
		int iteration = track.index().integer;
		if (iteration >= audio.getIterationCount()) return;
		double width = scene.getWidth();
		double height = scene.getHeight();
		float mixedPeak = audio.getMix().getPeaks().getClamped(iteration);
		float leftPeak = audio.getChannel(0).getPeaks().getClamped(iteration);
		float rightPeak = audio.getChannel(1).getPeaks().getClamped(iteration);
		glDisable(GL_CULL_FACE);
		if (mixedPeak > 0) {
			double peak = mixedPeak * height / 2;
			glBegin(GL_QUADS);
			glColor4d(1, 1, 1, 1);
			glVertex2d(0, 0);
			glVertex2d(width, 0);
			glColor4d(0.5, 0.5, 1, 0);
			glVertex2d(width, peak);
			glVertex2d(0, peak);
			glEnd();
		}
		if (leftPeak > 0) {
			double peak = leftPeak * width / 2;
			glBegin(GL_QUADS);
			glColor4d(1, 1, 1, 1);
			glVertex2d(0, 0);
			glVertex2d(0, height);
			glColor4d(0.5, 0.5, 1, 0);
			glVertex2d(peak, height);
			glVertex2d(peak, 0);
			glEnd();
		}
		if (rightPeak > 0) {
			double peak = rightPeak * width / 2;
			glBegin(GL_QUADS);
			glColor4d(0.5, 0.5, 1, 0);
			glVertex2d(width - peak, 0);
			glVertex2d(width - peak, height);
			glColor4d(1, 1, 1, 1);
			glVertex2d(width, height);
			glVertex2d(width, 0);
			glEnd();
		}
	}
	
}
