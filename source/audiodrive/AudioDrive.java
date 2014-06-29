package audiodrive;

import java.awt.Font;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import audiodrive.ui.components.Scene;
import audiodrive.ui.components.Text;
import audiodrive.ui.components.Window;
import audiodrive.ui.scenes.TitleScene;
import audiodrive.utilities.Log;

public class AudioDrive extends Application {
	
	public static final String Title = "AudioDrive";
	public static final Font Font = Text.getFont("Shojumaru");
	
	private static Stage stage;
	
	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void start(Stage stage) throws Exception {
		Log.info("AudioDrive");
		AudioDrive.stage = stage;
		
		Window.setBorderless(true);
		Window.setFullscreen(true);
		Window.setVSyncEnabled(true);
		Window.open(Scene.get(TitleScene.class));
		
		Platform.exit();
	}
	
	public static void exit() {
		Window.close();
	}
	
	public static Stage getStage() {
		return stage;
	}
}
