package audiodrive.ui.scenes.overlays;

import audiodrive.AudioDrive;
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
	private Text framerate;
	
	public GameOverlay(GameScene scene) {
		this.scene = scene;
		trackOverview = new TrackOverview(scene.track());
		framerate = new Text().setFont(AudioDrive.Font).setSize(10).setPosition(scene.getWidth() - 10, 125).setAlignment(Alignment.UpperRight);
	}
	
	public void update(double time) {
		trackOverview.updatePlayerPosition(scene.track().getIndex(time));
	}
	
	@Override
	public void render() {
		framerate.setText(Scene.getFramerate() + " FPS");
		Camera.overlay(scene.getWidth(), scene.getHeight());
		super.render();
		trackOverview.render();
		framerate.render();
	}
	
}
