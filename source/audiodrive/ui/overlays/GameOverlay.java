package audiodrive.ui.overlays;

import static org.lwjgl.opengl.GL11.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import audiodrive.AudioDrive;
import audiodrive.audio.AnalyzedAudio;
import audiodrive.model.Player;
import audiodrive.model.geometry.Color;
import audiodrive.model.track.Block;
import audiodrive.model.track.Track;
import audiodrive.ui.components.Camera;
import audiodrive.ui.components.Overlay;
import audiodrive.ui.components.Scene;
import audiodrive.ui.components.Text;
import audiodrive.ui.components.Text.Alignment;
import audiodrive.ui.scenes.GameScene;
import audiodrive.ui.scenes.GameScene.State;
import audiodrive.utilities.Format;
import audiodrive.utilities.Memory;

public class GameOverlay extends Overlay {
	
	private Map<String, Text> texts = new HashMap<>();
	
	private GameScene scene;
	private TrackOverview trackOverview;
	private Player player;
	
	private int width, height;
	private int collectables;
	
	public GameOverlay(GameScene scene) {
		this.scene = scene;
		width = scene.getWidth();
		height = scene.getHeight();
		player = scene.getPlayer();
		trackOverview = new TrackOverview(scene.getTrack());
		Color color = scene.getTrack().color().inverse();
		text("title").setColor(color).setText(scene.getTrack().getAudio().getName()).setSize(15).setPosition(width * 0.5, height - 10).setAlignment(Alignment.LowerCenter);
		text("framerate").setColor(color).setSize(10).setPosition(scene.getWidth() - 10, 125).setAlignment(Alignment.UpperRight);
		text("memory").setColor(color).setSize(10).setPosition(scene.getWidth() - 10, 140).setAlignment(Alignment.UpperRight);
		text("time").setColor(color).setSize(10).setPosition(scene.getWidth() - 10, 155).setAlignment(Alignment.UpperRight);
		text("points").setColor(color).setSize(30).setPosition(10, 10);
		text("damage").setColor(color).setSize(30).setPosition(10, 50);
		text("collected").setColor(color).setSize(20).setPosition(10, 110);
		text("collided").setColor(color).setSize(20).setPosition(10, 140);
		text("notification").setColor(color).setText("Paused").setSize(48).setPosition(width * 0.5, height * 0.5).setAlignment(Alignment.Center).setVisible(false);
		collectables = scene.getTrack().getNumberOfCollectables();
	}
	
	public void update() {
		trackOverview.updatePlayerPosition(scene.getTrack().index());
		List<Block> passed = scene.getTrack().getBlocks().stream().filter(block -> block.iteration() <= scene.getTrack().index().integer).collect(Collectors.toList());
		int passedCollectables = (int) passed.stream().filter(Block::isCollectable).count();
		int passedObstacles = passed.size() - passedCollectables;
		text("time").setText(Format.seconds(scene.playtime(), 1));
		text("points").setText(String.format("Points: %d / %d (%.0f%%)", player.points(), collectables, 100.0 * player.points() / collectables));
		text("damage").setText(String.format("Damage: %d / %d (%d%%)", player.collided(), player.hitpoints(), player.damage()));
		text("collected").setText(
			String.format("Collected: %d / %d (%.1f%%)", player.collected(), passedCollectables, 100.0 * player.collected() / Math.max(passedCollectables, 1)));
		text("collided").setText(String.format("Collided: %d / %d (%.1f%%)", player.collided(), passedObstacles, 100.0 * player.collided() / Math.max(passedObstacles, 1)));
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
	}
	
	@Override
	public void render() {
		texts.get("framerate").setText(Scene.getFramerate() + " FPS");
		text("memory").setText(Memory.used() + "/" + Memory.allocated());
		Camera.overlay(width, height);
		super.render();
		if (GameScene.peaks) drawPeaks();
		if (GameScene.particles && GameScene.sky && scene.getState() != State.Paused) scene.particleEffects().render();
		trackOverview.render();
		if (text("notification").isVisible()) drawNotificationBackground();
		texts.values().forEach(Text::render);
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
