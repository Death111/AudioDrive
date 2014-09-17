package audiodrive.ui.scenes.overlays;

import static org.lwjgl.opengl.GL11.*;

import java.util.HashMap;
import java.util.Map;

import audiodrive.AudioDrive;
import audiodrive.audio.AnalyzedAudio;
import audiodrive.model.Player;
import audiodrive.model.geometry.Color;
import audiodrive.model.track.Track;
import audiodrive.ui.TrackOverview;
import audiodrive.ui.components.Camera;
import audiodrive.ui.components.Overlay;
import audiodrive.ui.components.Scene;
import audiodrive.ui.components.Text;
import audiodrive.ui.components.Text.Alignment;
import audiodrive.ui.effects.ParticleEffects;
import audiodrive.ui.scenes.GameScene;
import audiodrive.ui.scenes.GameScene.State;
import audiodrive.utilities.Format;

public class GameOverlay extends Overlay {
	
	private Map<String, Text> texts = new HashMap<>();
	
	private GameScene scene;
	private TrackOverview trackOverview;
	private ParticleEffects specialEffects;
	private Player player;
	
	private int width, height;
	private boolean showPeaks;
	private int collectables;
	private int obstacles;
	
	public GameOverlay(GameScene scene) {
		this.scene = scene;
		width = scene.getWidth();
		height = scene.getHeight();
		player = scene.getPlayer();
		specialEffects = new ParticleEffects();
		trackOverview = new TrackOverview(scene.getTrack());
		text("title").setText(scene.getTrack().getAudio().getName()).setSize(15).setPosition(width * 0.5, height - 10).setAlignment(Alignment.LowerCenter);
		text("framerate").setSize(10).setPosition(scene.getWidth() - 10, 125).setAlignment(Alignment.UpperRight);
		text("time").setSize(10).setPosition(scene.getWidth() - 10, 140).setAlignment(Alignment.UpperRight);
		text("points").setSize(30).setPosition(10, 10);
		text("damage").setSize(30).setPosition(10, 50);
		text("collected").setSize(20).setPosition(10, 110);
		text("collided").setSize(20).setPosition(10, 140);
		text("notification").setText("Paused").setSize(48).setPosition(width * 0.5, height * 0.5).setAlignment(Alignment.Center).setVisible(false);
		collectables = scene.getTrack().getNumberOfCollectables();
		obstacles = scene.getTrack().getNumberOfObstacles();
	}
	
	public void update() {
		trackOverview.updatePlayerPosition(scene.getTrack().index());
		// specialEffects.visible(scene.getState() != State.Paused);
		text("time").setText(Format.seconds(scene.playtime()));
		text("points").setText(String.format("Points: %d / %d (%.0f%%)", player.points(), collectables, 100.0 * player.points() / collectables));
		text("damage").setText("Damage: " + player.collided() + " / " + player.hitpoints() + " (" + player.damage() + "%)");
		text("collected").setText(String.format("Collected: %.1f%%", 100.0 * player.collected() / collectables));
		text("collided").setText(String.format("Collided: %.1f%%", 100.0 * player.collided() / obstacles));
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
		Camera.overlay(width, height);
		super.render();
		trackOverview.render();
		specialEffects.render();
		
		if (text("notification").isVisible()) drawNotificationBackground();
		texts.values().forEach(Text::render);
		if (showPeaks) drawPeaks();
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
	
	public void togglePeaks() {
		showPeaks = !showPeaks;
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
