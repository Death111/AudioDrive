package audiodrive.ui.scenes.overlays;

import audiodrive.ui.components.Camera;
import audiodrive.ui.components.Overlay;
import audiodrive.ui.effects.ParticleEffects;
import audiodrive.ui.scenes.GameScene;

public class BackgroundOverlay extends Overlay {
	
	private GameScene scene;
	private ParticleEffects specialEffects;
	
	private int width, height;
	
	public BackgroundOverlay(GameScene scene) {
		this.scene = scene;
		width = scene.getWidth();
		height = scene.getHeight();
		specialEffects = new ParticleEffects();
	}
	
	public void update() {}
	
	@Override
	public void render() {
		Camera.overlay(width, height);
		super.render();
		specialEffects.render();
	}
}
