package audiodrive;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

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
	
	public static URI get(String name) {
		try {
			URL url = ClassLoader.getSystemResource(name);
			if (url == null) throw new RuntimeException("Can't find resource \"" + name + "\".");
			return url.toURI();
		} catch (URISyntaxException exception) {
			throw new RuntimeException(exception);
		}
	}
	
	public static File getFile(String name) {
		File file = new File(name);
		if (!file.exists()) {
			file = new File(get(name));
		}
		return file;
	}
	
	public static AudioFile getAudioFile(String name) {
		return new AudioFile(getFile(name));
	}
	
	/** Static Resources */
	
	private static Model iconModel;
	private static HashMap<Icon, Texture> iconTextures;
	private static Texture particleTexture;
	private static Texture blockTexture;
	private static Texture reflectedBlockTexture;
	private static List<Model> blockModels;
	private static Model tubeTowerModel;
	private static Model rotationTowerModel;
	private static Model spectraTowerModel;
	private static Texture ringTexture;
	private static Texture ringPulseTexture;
	private static Model ringModel;
	
	public static void destroy() {
		iconModel = null;
		iconTextures = null;
		particleTexture = null;
		blockTexture = null;
		reflectedBlockTexture = null;
		blockModels = null;
		tubeTowerModel = null;
		rotationTowerModel = null;
		spectraTowerModel = null;
		ringTexture = null;
		ringPulseTexture = null;
		ringModel = null;
	}
	
	public static Model getIconModel() {
		if (iconModel == null) iconModel = ModelLoader.loadSingleModel("models/quad/quad");
		return iconModel;
	}
	
	public static HashMap<Icon, Texture> getIconTextures() {
		if (iconTextures == null) {
			iconTextures = new HashMap<Icon, Texture>(5);
			iconTextures.put(Icon.Music, ModelLoader.getTexture("textures/icon/music.png"));
			iconTextures.put(Icon.Folder, ModelLoader.getTexture("textures/icon/folder.png"));
			iconTextures.put(Icon.Normal, ModelLoader.getTexture("textures/icon/normal.png"));
			iconTextures.put(Icon.Next, ModelLoader.getTexture("textures/icon/normal.png"));
			iconTextures.put(Icon.Previous, ModelLoader.getTexture("textures/icon/normal.png"));
		}
		return iconTextures;
	}
	
	public static Texture getParticleTexture() {
		if (particleTexture == null) particleTexture = ModelLoader.getTexture("models/particles/particles.png");
		return particleTexture;
	}
	
	public static Texture getBlockTexture() {
		if (blockTexture == null) blockTexture = ModelLoader.getTexture("models/block/block_lod.png");
		return blockTexture;
	}
	
	public static List<Model> getBlockModels() {
		if (blockModels == null) blockModels = ModelLoader.loadModels("models/block/block_lod");
		return blockModels;
	}
	
	public static Texture getReflectedBlockTexture() {
		if (reflectedBlockTexture == null) reflectedBlockTexture = ModelLoader.getTexture("models/block/block-reflection.png");
		return reflectedBlockTexture;
	}
	
	public static Texture getRingTexture() {
		if (ringTexture == null) ringTexture = ModelLoader.getTexture("textures/ring/ring.png");
		return ringTexture;
	}
	
	public static Texture getRingPulseTexture() {
		if (ringPulseTexture == null) ringPulseTexture = ModelLoader.getTexture("textures/ring/ring-pulse.png");
		return ringPulseTexture;
	}
	
	public static Model getRingModel() {
		if (ringModel == null) ringModel = ModelLoader.loadSingleModel("models/quad/quad");
		return ringModel;
	}
	
	public static Model getTubeTowerModel() {
		if (tubeTowerModel == null) tubeTowerModel = ModelLoader.loadSingleModel("models/musictower1/musictower1");
		return tubeTowerModel;
	}
	
	public static Model getRotationTowerModel() {
		if (rotationTowerModel == null) rotationTowerModel = ModelLoader.loadSingleModel("models/musictower2/musictower2");
		return rotationTowerModel;
	}
	
	public static Model getSpectraTowerModel() {
		if (spectraTowerModel == null) spectraTowerModel = ModelLoader.loadSingleModel("models/musictower3/musictower3");
		return spectraTowerModel;
	}
	
}
