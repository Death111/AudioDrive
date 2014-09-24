package audiodrive;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

import audiodrive.model.loader.Model;
import audiodrive.model.loader.ModelLoader;
import audiodrive.ui.menu.item.Item.Icon;
import audiodrive.utilities.Files;
import audiodrive.utilities.Get;

public class Resources {
	
	/** Private constructor to prevent instantiation. */
	private Resources() {
		throw new IllegalStateException("This class shall not be instantiated.");
	}
	
	public static InputStream stream(String name) {
		return ClassLoader.getSystemResourceAsStream(name);
	}
	
	public static Optional<URL> getOptional(String name) {
		return getOptional(new File(name));
	}
	
	public static URL get(String name) {
		return getOptional(name).orElseThrow(() -> new RuntimeException("Couldn't find resource \"" + name + "\"."));
	}
	
	public static Optional<URL> getOptional(File file) {
		try {
			if (!file.exists()) throw new FileNotFoundException();
			return Get.optional(file.toURI().toURL());
		} catch (FileNotFoundException | MalformedURLException exception) {
			return Get.optional(ClassLoader.getSystemResource(file.toString().replace('\\', '/')));
		}
	}
	
	public static URL get(File file) {
		return getOptional(file).orElseThrow(() -> new RuntimeException("Couldn't find resource \"" + file.getName() + "\"."));
	}
	
	public static Texture getTexture(URL url) {
		try {
			return TextureLoader.getTexture(getType(url.toString()), url.openStream());
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}
	
	public static Texture getTexture(String name) {
		try {
			return TextureLoader.getTexture(getType(name), get(name).openStream());
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}
	
	public static String getName(String resource) {
		int start = resource.lastIndexOf("/") + 1;
		int end = Math.max(resource.lastIndexOf("."), 0);
		return resource.substring(start, end);
	}
	
	public static String getType(String resource) {
		int dot = resource.lastIndexOf(".");
		if (dot < 0 || dot >= resource.length() - 1) return null;
		return resource.substring(dot + 1);
	}
	
	public static String getClasspath() {
		return System.getProperty("java.class.path").split(";")[0];
	}
	
	public static String getPath(URL url) {
		try {
			return URLDecoder.decode(url.toString(), "UTF-8");
		} catch (UnsupportedEncodingException exception) {
			return url.toString();
		}
	}
	
	public static Optional<String> find(String name) {
		return list().stream().filter(string -> string.endsWith(name)).findFirst();
	}
	
	public static Optional<String> find(String directory, String name) {
		return list(directory).stream().filter(string -> string.endsWith(name)).findFirst();
	}
	
	public static List<String> list(String... paths) {
		List<String> classpathEntries = new ArrayList<String>();
		String[] classpaths = System.getProperty("java.class.path").split(";");
		for (String classpath : classpaths) {
			try (JarFile jarFile = new JarFile(classpath)) { // assume it's a jar
				Enumeration<JarEntry> entries = jarFile.entries();
				while (entries.hasMoreElements()) {
					ZipEntry entry = entries.nextElement();
					String string = entry.toString();
					if (string.endsWith("/") || string.endsWith(".class")) continue;
					if (paths.length == 0 || Arrays.stream(paths).anyMatch(path -> string.startsWith(path))) {
						classpathEntries.add(string);
					}
				}
			} catch (IOException exception) { // no jar, assume it's an eclipse project
				int index = classpath.length() + 1;
				Files
					.list(classpath, true)
					.stream()
					.map(File::toString)
					.filter(string -> !string.endsWith(".class"))
					.map(string -> string.substring(index))
					.filter(string -> paths.length == 0 || Arrays.stream(paths).anyMatch(path -> string.replace('\\', '/').startsWith(path)))
					.forEach(classpathEntries::add);
			}
		}
		return classpathEntries;
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
		if (iconModel == null) iconModel = ModelLoader.loadModel("models/quad/quad");
		return iconModel;
	}
	
	public static HashMap<Icon, Texture> getIconTextures() {
		if (iconTextures == null) {
			iconTextures = new HashMap<Icon, Texture>(5);
			iconTextures.put(Icon.Music, getTexture("textures/icon/music.png"));
			iconTextures.put(Icon.Folder, getTexture("textures/icon/folder.png"));
			iconTextures.put(Icon.Normal, getTexture("textures/icon/normal.png"));
			iconTextures.put(Icon.Next, getTexture("textures/icon/normal.png"));
			iconTextures.put(Icon.Previous, getTexture("textures/icon/normal.png"));
		}
		return iconTextures;
	}
	
	public static Texture getParticleTexture() {
		if (particleTexture == null) particleTexture = getTexture("models/particles/particles.png");
		return particleTexture;
	}
	
	public static Texture getBlockTexture() {
		if (blockTexture == null) blockTexture = getTexture("models/block/block_lod.png");
		return blockTexture;
	}
	
	public static List<Model> getBlockModels() {
		if (blockModels == null) blockModels = ModelLoader.loadModels("models/block/block_lod");
		return blockModels;
	}
	
	public static Texture getReflectedBlockTexture() {
		if (reflectedBlockTexture == null) reflectedBlockTexture = getTexture("models/block/block-reflection.png");
		return reflectedBlockTexture;
	}
	
	public static Texture getRingTexture() {
		if (ringTexture == null) ringTexture = getTexture("textures/ring/ring.png");
		return ringTexture;
	}
	
	public static Texture getRingPulseTexture() {
		if (ringPulseTexture == null) ringPulseTexture = getTexture("textures/ring/ring-pulse.png");
		return ringPulseTexture;
	}
	
	public static Model getRingModel() {
		if (ringModel == null) ringModel = ModelLoader.loadModel("models/quad/quad");
		return ringModel;
	}
	
	public static Model getTubeTowerModel() {
		if (tubeTowerModel == null) tubeTowerModel = ModelLoader.loadModel("models/musictower1/musictower1");
		return tubeTowerModel;
	}
	
	public static Model getRotationTowerModel() {
		if (rotationTowerModel == null) rotationTowerModel = ModelLoader.loadModel("models/musictower2/musictower2");
		return rotationTowerModel;
	}
	
	public static Model getSpectraTowerModel() {
		if (spectraTowerModel == null) spectraTowerModel = ModelLoader.loadModel("models/musictower3/musictower3");
		return spectraTowerModel;
	}
	
}
