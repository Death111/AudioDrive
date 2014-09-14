package audiodrive.ui.scenes.overlays;

import static org.lwjgl.opengl.GL11.*;

import java.util.HashMap;
import java.util.Map;

import audiodrive.AudioDrive;
import audiodrive.audio.AnalyzedAudio;
import audiodrive.model.Player;
import audiodrive.ui.TrackOverview;
import audiodrive.ui.components.Camera;
import audiodrive.ui.components.Overlay;
import audiodrive.ui.components.Scene;
import audiodrive.ui.components.Text;
import audiodrive.ui.components.Text.Alignment;
import audiodrive.ui.scenes.GameScene;

public class GameOverlay extends Overlay {
	
	private GameScene scene;
	private TrackOverview trackOverview;
	private Map<String, Text> texts = new HashMap<>();
	
	private boolean showPeaks;
	private int iteration;
	private int collectables;
	private int obstacles;
	private Player player;
	
	public GameOverlay(GameScene scene) {
		this.scene = scene;
		player = scene.getPlayer();
		trackOverview = new TrackOverview(scene.getTrack());
		text("title").setText(scene.getTrack().getAudio().getName()).setSize(15).setPosition(scene.getWidth() / 2, scene.getHeight() - 10).setAlignment(Alignment.LowerCenter);
		text("framerate").setSize(10).setPosition(scene.getWidth() - 10, 125).setAlignment(Alignment.UpperRight);
		text("points").setSize(30).setPosition(10, 10);
		text("damage").setSize(30).setPosition(10, 50);
		text("collected").setSize(20).setPosition(10, 110);
		text("collided").setSize(20).setPosition(10, 140);
		collectables = scene.getTrack().getNumberOfCollectables();
		obstacles = scene.getTrack().getNumberOfObstacles();
	}
	
	public void update(double time) {
		trackOverview.updatePlayerPosition(scene.getTrack().getIndex(time));
		text("points").setText(String.format("Points: %d / %d (%.0f%%)", player.points(), collectables, 100.0 * player.points() / collectables));
		text("damage").setText("Damage: " + player.collided() + " / " + player.hitpoints() + " (" + player.damage() + "%)");
		text("collected").setText(String.format("Collected: %.1f%%", 100.0 * player.collected() / collectables));
		text("collided").setText(String.format("Collided: %.1f%%", 100.0 * player.collided() / obstacles));
		iteration = scene.getTrack().getIndex(time).integer;
	}
	
	@Override
	public void render() {
		texts.get("framerate").setText(Scene.getFramerate() + " FPS");
		Camera.overlay(scene.getWidth(), scene.getHeight());
		super.render();
		trackOverview.render();
		texts.values().forEach(Text::render);
		if (showPeaks) drawPeaks();
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
		AnalyzedAudio audio = scene.getTrack().getAudio();
		if (iteration >= audio.getIterationCount()) return;
		double width = scene.getWidth();
		double height = scene.getHeight();
		float mixedPeak = audio.getMix().getPeaks().getClamped(iteration);
		float leftPeak = audio.getChannel(0).getPeaks().getClamped(iteration);
		float rightPeak = audio.getChannel(1).getPeaks().getClamped(iteration);
		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
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
