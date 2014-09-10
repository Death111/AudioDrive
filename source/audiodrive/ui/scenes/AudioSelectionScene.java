package audiodrive.ui.scenes;

import static org.lwjgl.opengl.GL11.*;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.filechooser.FileSystemView;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

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
import audiodrive.ui.menu.item.MenuItem;
import audiodrive.utilities.Buffers;
import audiodrive.utilities.Log;

/**
 * 
 * @author Death
 *
 */
public class AudioSelectionScene extends Scene implements ItemListener {

	private static final String PARENT_FILENAME = "..";
	private Menu itemMenu;
	private Menu rootMenu;
	private Menu nextMenu;

	private MenuItem continueMenuItem;
	private Map<FileChooserItem, File> itemMap = new HashMap<FileChooserItem, File>();
	private Map<FileChooserItem, File> rootMap = new HashMap<FileChooserItem, File>();
	FileSystemView fsv = FileSystemView.getFileSystemView();

	private File selectedFile = null;
	private Text currentFolderText;
	private Text selectedFileText;
	private Text titleText;
	private AudioFile hoverAudio;
	private AudioFile selectAudio;
	private VertexBuffer canvas;
	private ShaderProgram shader;
	private List<String> supportedFileExtensionList = Arrays.asList("mp3", "wav");
	// Item which was selected
	private Item item = null;
	
	@Override
	public void entering() {
		Camera.overlay(getWidth(), getHeight());
		canvas = new VertexBuffer(Buffers.create(0, 0, 0, getHeight(), getWidth(), getHeight(), getWidth(), 0), 2).mode(GL_QUADS);
		shader = new ShaderProgram("shaders/default.vs", "shaders/title.fs");

		titleText = new Text("Choose an AudioFile").setFont(AudioDrive.Font).setSize(48).setPosition(20, 20);

		int textHeight = 50;

		rootMenu = new Menu(20, 200, FileChooserItem.FILECHOOSER_ITEM_WIDTH + 1, Display.getHeight() - 500, 1);
		itemMenu = new Menu(30 + FileChooserItem.FILECHOOSER_ITEM_WIDTH, 200, Display.getWidth() - FileChooserItem.FILECHOOSER_ITEM_WIDTH - 50, Display.getHeight() - 200 - 3
				* textHeight, 1);
		nextMenu = new Menu(20, itemMenu.getHeight() + 200 + 50, 400, Display.getHeight() - itemMenu.getHeight() + 200 + 50, 1);

		continueMenuItem = new MenuItem("Continue", this);
		continueMenuItem.setDisabled(true);
		nextMenu.addItem(continueMenuItem);

		currentFolderText = new Text().setFont(AudioDrive.Font).setPosition(20, 150).setSize(30);
		selectedFileText = new Text().setFont(AudioDrive.Font).setPosition(20, itemMenu.getHeight() + 200).setSize(30);

		// Setup start node
		updateItemExplorer(new File(AudioDrive.Settings.get("directory")));

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
		AudioDrive.Settings.set("directory", rootFile.getPath());
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
			final String fileName = file.getName();
			final String extension = getFileExtension(fileName);
			// Check if file is supported
			if (directory || supportedFileExtensionList.contains(extension)) {
				FileChooserItem fci = new FileChooserItem(fileName, directory, this);
				itemMenu.addItem(fci);
				itemMap.put(fci, file);
			}
		}

	}

	@Override
	public void render() {
		glClear(GL_COLOR_BUFFER_BIT);
		shader.bind();
		shader.uniform("time").set(time());
		shader.uniform("resolution").set((float) getWidth(), (float) getHeight());
		canvas.draw();
		shader.unbind();

		if (item != null) {
			checkItemExplorer(itemMap);
			checkItemExplorer(rootMap);
		}
		item = null;
		rootMenu.render();
		itemMenu.render();
		nextMenu.render();
		currentFolderText.render();
		selectedFileText.render();
		titleText.render();
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
				if (!fileChooserItem.isDirectory()) {
					Log.debug("An item was selected: '" + rootFile.getName() + "'.");
					selectedFile = rootFile;
					selectedFileText.setText("File selected: '" + selectedFile.getName() + "'");
					continueMenuItem.setDisabled(false);
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
		nextMenu.mouseMoved(x, y);
	}

	@Override
	public void mouseButtonReleased(int button, int x, int y) {
		// yCoordinates start in left bottom corner, instead left top
		y = getHeight() - y;

		itemMenu.mousePressed(button, x, y);
		rootMenu.mousePressed(button, x, y);
		nextMenu.mousePressed(button, x, y);
	}

	@Override
	public void keyReleased(int key, char character) {
		Log.trace("Key '" + character + "' was realeased,");
		switch (key) {
		case Keyboard.KEY_ESCAPE:
			back();
			break;
		case Keyboard.KEY_RETURN:
			onSelect(continueMenuItem, true);
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

		if (item == continueMenuItem) {
			if (selectedFile != null) {
				Scene.get(AnalyzationScene.class).enter(new AudioFile(selectedFile));
			} else {
				Log.warning("No file selected.");
			}
		} else {
			this.item = item;
		}
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
