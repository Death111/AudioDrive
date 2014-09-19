package audiodrive.ui.overlays;

import audiodrive.ui.components.Camera;
import audiodrive.ui.components.Overlay;
import audiodrive.ui.effects.BackgroundSpectral;
import audiodrive.ui.effects.ParticleEffects;
import audiodrive.ui.scenes.GameScene;

public class BackgroundOverlay extends Overlay {
	
	private GameScene scene;
	private ParticleEffects specialEffects;
	BackgroundSpectral backgroundSpectral;
	
	private int width, height;
	
	public BackgroundOverlay(GameScene scene) {
		this.scene = scene;
		width = scene.getWidth();
		height = scene.getHeight();
		specialEffects = new ParticleEffects();
		backgroundSpectral = new BackgroundSpectral(scene);
	}
	
	public void update() {}
	
	@Override
	public void render() {
		Camera.overlay(width, height);
		super.render();
		backgroundSpectral.render();
		specialEffects.render();
	}
}
