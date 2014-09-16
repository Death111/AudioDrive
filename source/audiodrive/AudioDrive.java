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
		Window.setBorderless(true);
		Window.setFullscreen(true);
		if (Settings.getBoolean("useSecondaryMonitor")) Window.useSecondaryMonitor();
		Window.setFramerate(Settings.getInteger("framerate"));
		Window.setVSyncEnabled(Settings.getBoolean("vsync"));
		Window.open(Scene.get(TitleScene.class));
	}
	
	public static void exit() {
		Window.close();
		Settings.save();
		Log.info("Ended.");
	}
	
}
