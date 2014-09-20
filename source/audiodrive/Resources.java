package audiodrive;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;

import org.newdawn.slick.opengl.Texture;

import audiodrive.audio.AudioFile;
import audiodrive.model.loader.Model;
import audiodrive.model.loader.ModelLoader;
import audiodrive.ui.menu.item.Item.Icon;

public class Resources {
	
	/** Private constructor to prevent instantiation. */
	private Resources() {
		throw new IllegalStateException("This class shall not be instantiated.");
	}
	
	public static URI get(String name, boolean fatal) {
		try {
			URL url = ClassLoader.getSystemResource(name);
			if (url == null && fatal) throw new RuntimeException("Can't find resource \"" + name + "\".");
			if (url == null) return null;
			return url.toURI();
		} catch (URISyntaxException exception) {
			throw new RuntimeException(exception);
		}
	}
	
	public static File getFile(String name) {
		File file = new File(name);
		if (!file.exists()) {
			file = new File(get(name, true));
		}
		return file;
	}
	
	public static File tryGetFile(String name) {
		File file = new File(name);
		if (!file.exists()) {
			final URI uri = get(name, false);
			if (uri == null) return null;
			file = new File(uri);
		}
		return file;
	}
	
	public static AudioFile getAudioFile(String name) {
		return new AudioFile(getFile(name));
	}
	
	/** Static Resources */
	
	private static class Cache {
		
		private static Model iconModel;
		private static HashMap<Icon, Texture> iconTextures;
		
		static {
			reload();
		}
		
		private static void reload() {
			iconModel = ModelLoader.loadSingleModel("models/quad/quad");
			iconTextures = new HashMap<Icon, Texture>(5);
			iconTextures.put(Icon.Music, ModelLoader.getTexture("textures/icon/music.png"));
			iconTextures.put(Icon.Folder, ModelLoader.getTexture("textures/icon/folder.png"));
			iconTextures.put(Icon.Normal, ModelLoader.getTexture("textures/icon/normal.png"));
			iconTextures.put(Icon.Next, ModelLoader.getTexture("textures/icon/normal.png"));
			iconTextures.put(Icon.Previous, ModelLoader.getTexture("textures/icon/normal.png"));
		}
		
	}
	
	public static void reload() {
		Cache.reload();
	}
	
	public static Model getIconModel() {
		return Cache.iconModel;
	}
	
	public static HashMap<Icon, Texture> getIconTextures() {
		return Cache.iconTextures;
	}
	
}
