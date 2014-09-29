package audiodrive.ui.overlays;

import audiodrive.ui.components.Camera;
import audiodrive.ui.components.Overlay;
import audiodrive.ui.scenes.GameScene;

public class GameBackground extends Overlay {
	
	private GameScene scene;
	
	private int width, height;
	
	public GameBackground(GameScene scene) {
		this.scene = scene;
		width = scene.getWidth();
		height = scene.getHeight();
	}
	
	public void update() {}
	
	@Override
	public void render() {
		Camera.overlay(width, height);
		super.render();
		if (GameScene.particles && !GameScene.sky) scene.particleEffects().render();
	}
	
}
