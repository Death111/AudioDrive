package audiodrive;

import java.awt.Font;

import audiodrive.audio.AnalyzedAudio;
import audiodrive.audio.AudioResource;
import audiodrive.audio.Playback;
import audiodrive.model.loader.Model;
import audiodrive.model.track.Track;
import audiodrive.ui.components.Scene;
import audiodrive.ui.components.Text;
import audiodrive.ui.components.Window;
import audiodrive.ui.scenes.TitleScene;
import audiodrive.utilities.Log;
import audiodrive.utilities.Natives;
import audiodrive.utilities.SlickLog;
import audiodrive.utilities.Versioning;

public class AudioDrive {
	
	public static enum Action {
		None, Play, Visualize
	}
	
	public static final String Title = "AudioDrive";
	public static final String Version = Versioning.getVersion();
	public static final String Creators = "Nico Mutter and Thomas Würstle";
	public static final Settings Settings = new Settings("audiodrive.properties");
	public static final Playback MenuSound = new Playback(new AudioResource("sounds/Menu.mp3"));
	public static final Font Font = Text.getFont("Shojumaru");
	
	private static AudioResource selectedAudio;
	private static AnalyzedAudio analyzedAudio;
	private static Track track;
	private static Model playerModel;
	private static Action action = Action.None;
	
	public static void main(String[] args) {
		Log.info("AudioDrive");
		Log.info("Version: " + Version);
		Log.info("Creators: " + Creators);
		SlickLog.bind();
		Settings.load();
		Natives.load();
		Window.useSecondaryMonitor(Settings.getBoolean("window.useSecondaryMonitor"));
		Window.setBorderless(true);
		Window.setFullscreen(true);
		Window.setFramerate(Settings.getInteger("window.framerate"));
		Window.setAntialiasingEnabled(Settings.getBoolean("window.antialiasing"));
		Window.setVSyncEnabled(Settings.getBoolean("window.vsync"));
		Window.open(Scene.get(TitleScene.class));
	}
	
	public static void exit() {
		Scene.destroy();
		Window.close();
		Settings.save();
		Log.info("Ended.");
	}
	
	public static void setAction(Action action) {
		AudioDrive.action = action;
	}
	
	public static void setSelectedAudio(AudioResource selectedAudio) {
		AudioDrive.selectedAudio = selectedAudio;
	}
	
	public static void setAnalyzedAudio(AnalyzedAudio analyzedAudio) {
		AudioDrive.analyzedAudio = analyzedAudio;
	}
	
	public static void setTrack(Track track) {
		AudioDrive.track = track;
	}
	
	public static void setPlayerModel(Model playerModel) {
		AudioDrive.playerModel = playerModel;
	}
	
	public static Action getAction() {
		return action;
	}
	
	public static AudioResource getSelectedAudio() {
		return selectedAudio;
	}
	
	public static AnalyzedAudio getAnalyzedAudio() {
		return analyzedAudio;
	}
	
	public static Track getTrack() {
		return track;
	}
	
	public static Model getPlayerModel() {
		return playerModel;
	}
	
}
