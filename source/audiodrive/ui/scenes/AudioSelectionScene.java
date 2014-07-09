package audiodrive.ui.scenes;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.glClear;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.filechooser.FileSystemView;

import org.lwjgl.input.Keyboard;

import audiodrive.AudioDrive;
import audiodrive.audio.AudioFile;
import audiodrive.model.buffer.VertexBuffer;
import audiodrive.ui.components.Camera;
import audiodrive.ui.components.Scene;
import audiodrive.ui.components.Text;
import audiodrive.ui.effects.ShaderProgram;
import audiodrive.ui.menu.Menu;
import audiodrive.ui.menu.item.FileChooserItem;
import audiodrive.ui.menu.item.Item;
import audiodrive.ui.menu.item.ItemListener;
import audiodrive.utilities.Buffers;
import audiodrive.utilities.Log;

/**
 * 
 * @author Death
 *
 */
public class AudioSelectionScene extends Scene implements ItemListener {

	// TODO Add button to confirm selection

	private static final String PARENT_FILENAME = "..";
	private Menu itemMenu;
	private Menu rootMenu;

	private Map<FileChooserItem, File> itemMap = new HashMap<FileChooserItem, File>();
	private Map<FileChooserItem, File> rootMap = new HashMap<FileChooserItem, File>();
	FileSystemView fsv = FileSystemView.getFileSystemView();

	private File selectedFile = null;
	private Text currentFolderText;
	private Text selectedFileText;
	private Text titleText;
	private Text continueText;
	private AudioFile hoverAudio;
	private AudioFile selectAudio;
	private VertexBuffer canvas;
	private ShaderProgram shader;
	private double duration;
	private List<String> supportedFileExtensionList = Arrays.asList("mp3", "wav");
	// Item which was selected
	private Item item = null;

	public void enter(double duration) {
		this.duration = duration;
		super.enter();
	}

	@Override
	public void entering() {
		Camera.overlay(getWidth(), getHeight());
		canvas = new VertexBuffer(Buffers.create(0, 0, 0, getHeight(), getWidth(), getHeight(), getWidth(), 0)).step(2).mode(GL_QUADS);
		shader = new ShaderProgram("shaders/default.vs", "shaders/title.fs");

		titleText = new Text("Choose an AudioFile").setFont(AudioDrive.Font).setSize(48).setPosition(20, 20);

		rootMenu = new Menu(20, 200, 1);
		itemMenu = new Menu(40 + FileChooserItem.FILECHOOSER_ITEM_WIDTH, 200, 1);
		currentFolderText = new Text().setFont(AudioDrive.Font).setPosition(20, 150).setSize(30);
		selectedFileText = new Text().setFont(AudioDrive.Font).setPosition(20, 800).setSize(30);
		continueText = new Text().setText("Press 'N' to continue").setFont(AudioDrive.Font).setPosition(20, 1000).setSize(35);

		// Setup start node
		File rootFile = new File("./music/");
		updateItemExplorer(rootFile);

		for (File file : File.listRoots()) {
			final boolean directory = file.isDirectory();
			if (!directory) {
				// Root directory is empty (e.g. not used usb)
				continue;
			}
			String fileName = file.toString();
			FileChooserItem fci = new FileChooserItem(fileName, directory, this);
			rootMenu.addItem(fci);
			rootMap.put(fci, file);
		}
		hoverAudio = new AudioFile("sounds/hover.wav");
		selectAudio = new AudioFile("sounds/select.wav");
	}

	/**
	 * Updates the explorer with the given file as root node
	 * 
	 * @param rootFile
	 *            The root file of the tree to be displayed
	 */
	private void updateItemExplorer(File rootFile) {
		currentFolderText.setText("Current folder: " + rootFile.getAbsolutePath());
		final File[] listFiles = rootFile.listFiles();
		itemMap.clear();
		itemMenu.removeAllItems();
		// Add parent
		File parentFile = fsv.getParentDirectory(rootFile);
		if (parentFile != null && !fsv.isFileSystemRoot(rootFile)) {
			FileChooserItem parentFileChooser = new FileChooserItem(PARENT_FILENAME, true, this);
			itemMenu.addItem(parentFileChooser);

			itemMap.put(parentFileChooser, parentFile);
		}
		// Check if there are files available
		if (listFiles == null) {
			return;
		}

		for (File file : listFiles) {
			final boolean directory = file.isDirectory();

			String fileName = file.getName();
			String extension = getFileExtension(fileName);
			// Check if file is supported
			if (directory || supportedFileExtensionList.contains(extension)) {
				FileChooserItem fci = new FileChooserItem(fileName, directory, this);
				itemMenu.addItem(fci);
				itemMap.put(fci, file);
			}
		}

	}

	@Override
	public void update(double elapsed) {
		duration += elapsed;
	}

	@Override
	public void render() {
		glClear(GL_COLOR_BUFFER_BIT);
		shader.bind();
		shader.uniform("time").set(duration);
		shader.uniform("resolution").set((float) getWidth(), (float) getHeight());
		canvas.draw();
		shader.unbind();

		if (item != null) {
			checkItemExplorer(itemMap);
			checkItemExplorer(rootMap);
		}
		item = null;
		itemMenu.render();
		rootMenu.render();
		currentFolderText.render();
		selectedFileText.render();
		titleText.render();
		continueText.render();
	}

	private void checkItemExplorer(Map<FileChooserItem, File> map) {
		final Set<FileChooserItem> keySet = map.keySet();
		boolean update = false;
		File rootFile = null;

		for (FileChooserItem fileChooserItem : keySet) {

			if (item == fileChooserItem) {
				final File file = map.get(fileChooserItem);
				update = true;
				rootFile = file;
				if (!rootFile.isDirectory()) {
					Log.debug("An item was selected: '" + rootFile.getName() + "'.");
					selectedFile = rootFile;
					selectedFileText.setText("File selected: '" + selectedFile.getName() + "'");
					update = false;
				}
			}
		}

		if (update) {
			updateItemExplorer(rootFile);
		}
	}

	@Override
	public void exiting() {
		canvas = null;
		shader = null;
		selectedFile = null;
	}

	@Override
	public void mouseMoved(int x, int y, int dx, int dy) {
		// yCoordinates start in left bottom corner, instead left top
		y = getHeight() - y;

		itemMenu.mouseMoved(x, y);
		rootMenu.mouseMoved(x, y);
	}

	@Override
	public void mouseButtonReleased(int button, int x, int y) {
		// yCoordinates start in left bottom corner, instead left top
		y = getHeight() - y;

		itemMenu.mousePressed(button, x, y);
		rootMenu.mousePressed(button, x, y);
	}

	@Override
	public void keyReleased(int key, char character) {
		Log.trace("Key '" + character + "' was realeased,");
		switch (key) {

		case Keyboard.KEY_ESCAPE:
			exit();
			break;
		case Keyboard.KEY_N:
			if (selectedFile != null) {
				Scene.get(AnalyzationScene.class).enter(new AudioFile(selectedFile));
			} else {
				Log.warning("No file selected.");
			}

			break;
		default:
			break;
		}
	}

	@Override
	public void onHover(Item item, boolean hover) {
		if (hover) {
			hoverAudio.play();
		}
	}

	@Override
	public void onSelect(Item item, boolean select) {
		if (!select) {
			return;
		}
		selectAudio.play();
		this.item = item;
	}

	/**
	 * Returns the file extension of the given filename
	 * 
	 * @param fileName
	 *            fileName do get extension from
	 * @return file extension
	 */
	private String getFileExtension(String fileName) {
		String extension = "";

		int i = fileName.lastIndexOf('.');
		if (i > 0) {
			extension = fileName.substring(i + 1);
		}

		return extension;
	}
}
