package audiodrive.ui.overlays;

import audiodrive.AudioDrive;
import audiodrive.ui.components.Camera;
import audiodrive.ui.components.Overlay;
import audiodrive.ui.effects.BackgroundSpectral;
import audiodrive.ui.effects.ParticleEffects;
import audiodrive.ui.scenes.GameScene;

public class GameBackground extends Overlay {
	
	private GameScene scene;
	private ParticleEffects specialEffects;
	private BackgroundSpectral backgroundSpectral;
	
	private int width, height;
	private boolean visualization = AudioDrive.Settings.getBoolean("game.visualization");
	private boolean particles = AudioDrive.Settings.getBoolean("graphics.particles");
	
	public GameBackground(GameScene scene) {
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
		if (visualization) backgroundSpectral.render();
		if (particles) specialEffects.render();
	}
	
}
