package audiodrive.ui.scenes;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.filechooser.FileSystemView;

import org.lwjgl.input.Keyboard;

import audiodrive.ui.components.Camera;
import audiodrive.ui.components.Scene;
import audiodrive.ui.menu.Menu;
import audiodrive.ui.menu.item.FileChooserItem;
import audiodrive.ui.menu.item.Item;
import audiodrive.ui.menu.item.ItemListener;
import audiodrive.utilities.Log;

public class NicosAudioSelectionScene extends Scene implements ItemListener {

	private static final String PARENT_FILENAME = "..";
	private Menu itemMenu;
	private Menu rootMenu;

	private Map<FileChooserItem, File> itemMap = new HashMap<FileChooserItem, File>();
	private Map<FileChooserItem, File> rootMap = new HashMap<FileChooserItem, File>();
	FileSystemView fsv = FileSystemView.getFileSystemView();

	@Override
	public void entering() {
		Camera.overlay(getWidth(), getHeight());
		this.rootMenu = new Menu(100, 100, 1);
		this.itemMenu = new Menu(500, 100, 1);
		File rootFile = new File("./music/");
		updateItemExplorer(rootFile);

		for (File file : File.listRoots()) {
			final boolean directory = file.isDirectory();
			if (!directory) {
				continue;
			}
			String fileName = file.toString();
			FileChooserItem fci = new FileChooserItem(fileName, directory, this);
			this.rootMenu.addItem(fci);
			rootMap.put(fci, file);
		}

	}

	/**
	 * Updates the exlporer with the given file as root node
	 * 
	 * @param rootFile
	 *            The root file of the tree to be displayed
	 */
	private void updateItemExplorer(File rootFile) {

		final File[] listFiles = rootFile.listFiles();
		this.itemMap.clear();
		this.itemMenu.removeAllItems();

		// Add parent
		File parentFile = fsv.getParentDirectory(rootFile);
		if (parentFile != null && !fsv.isFileSystemRoot(rootFile)) {
			FileChooserItem parentFileChooser = new FileChooserItem(PARENT_FILENAME, true, this);
			this.itemMenu.addItem(parentFileChooser);

			itemMap.put(parentFileChooser, parentFile);
		}

		for (File file : listFiles) {
			final boolean directory = file.isDirectory();
			String fileName = file.getName();
			FileChooserItem fci = new FileChooserItem(fileName, directory, this);
			this.itemMenu.addItem(fci);
			itemMap.put(fci, file);
		}

	}

	@Override
	public void update(double elapsed) {

	}

	@Override
	public void render() {
		glClear(GL_COLOR_BUFFER_BIT);
		if (this.item != null) {
			checkItemExplorer(this.itemMap);
			checkItemExplorer(this.rootMap);
		}
		this.item = null;
		this.itemMenu.render();
		this.rootMenu.render();
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
		default:
			break;
		}
	}

	@Override
	public void onHover(Item item, boolean hover) {
		// TODO Auto-generated method stub

	}

	Item item = null;

	@Override
	public void onSelect(Item item, boolean select) {
		if (!select) {
			return;
		}
		this.item = item;
	}
}
