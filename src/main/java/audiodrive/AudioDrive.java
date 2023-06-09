package audiodrive;

import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.stream.Collectors;

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
import audiodrive.utilities.Versioning;

public class AudioDrive {
	
	public static enum Action {
		None, Play, Visualize
	}
	
	public static final String Title = "AudioDrive";
	public static final String Version = Versioning.getVersion();
	public static final String Creators = "Nico Mutter and Thomas Würstle";
	public static final Settings Settings = new Settings("audiodrive.properties");
	public static final Playback MenuSound = new Playback(new AudioResource("sounds/Menu.wav"));
	public static final Font Font = Text.getFont("Shojumaru/Shojumaru-Regular");
	
	private static AudioResource selectedAudio;
	private static AnalyzedAudio analyzedAudio;
	private static Track track;
	private static Model playerModel;
	private static Action action = Action.None;
	
	public static void main(String[] args) throws IOException {
		String domain = new File(AudioDrive.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getName();
		// if started from a jar file ...
		if (domain.toLowerCase().endsWith(".jar") && args.length == 0) {
			// re-launch the application with the desired garbage collector
			Runtime.getRuntime().exec(new String[]{"java", "-XX:+UseConcMarkSweepGC", "-jar", domain, "x"});
			return;
		}
		start();
	}
	
	public static void start() {
		Log.info("AudioDrive");
		Log.info("Version: " + Version);
		Log.info("Creators: " + Creators);
		Log.debug("Garbage Collector: " + ManagementFactory.getGarbageCollectorMXBeans().stream().map(GarbageCollectorMXBean::getName).collect(Collectors.joining(", ")));
		
		Settings.load();
		Natives.load();
		Window.setTitle(AudioDrive.Title);
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
