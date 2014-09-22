package audiodrive.ui.overlays;

import audiodrive.ui.components.Camera;
import audiodrive.ui.components.Overlay;
import audiodrive.ui.effects.BackgroundSpectral;
import audiodrive.ui.scenes.GameScene;

public class GameBackground extends Overlay {
	
	private GameScene scene;
	private BackgroundSpectral backgroundSpectral;
	
	private int width, height;
	
	public GameBackground(GameScene scene) {
		this.scene = scene;
		width = scene.getWidth();
		height = scene.getHeight();
		backgroundSpectral = new BackgroundSpectral(scene);
	}
	
	public void update() {}
	
	@Override
	public void render() {
		Camera.overlay(width, height);
		super.render();
		if (GameScene.visualization && !GameScene.sky) backgroundSpectral.render();
		if (GameScene.particles && !GameScene.sky) scene.particleEffects().render();
	}
	
}
