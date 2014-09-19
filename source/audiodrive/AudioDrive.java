package audiodrive;

import java.awt.Font;

import audiodrive.ui.components.Scene;
import audiodrive.ui.components.Text;
import audiodrive.ui.components.Window;
import audiodrive.ui.scenes.TitleScene;
import audiodrive.utilities.Log;

public class AudioDrive {
	
	public static final String Title = "AudioDrive";
	public static final Settings Settings = new Settings("audiodrive.properties");
	public static final Font Font = Text.getFont("Shojumaru");
	
	public static void main(String[] args) {
		Log.info("AudioDrive");
		Settings.load();
		Window.useSecondaryMonitor(Settings.getBoolean("window.useSecondaryMonitor"));
		Window.setBorderless(true);
		Window.setFullscreen(true);
		Window.setFramerate(Settings.getInteger("window.framerate"));
		Window.setAntialiasingEnabled(Settings.getBoolean("window.antialiasing"));
		Window.setVSyncEnabled(Settings.getBoolean("window.vsync"));
		Window.open(Scene.get(TitleScene.class));
	}
	
	public static void exit() {
		Window.close();
		Settings.save();
		Log.info("Ended.");
	}
	
}
