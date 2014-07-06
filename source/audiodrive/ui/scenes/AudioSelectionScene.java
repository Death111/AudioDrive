package audiodrive.ui.scenes;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.filechooser.FileSystemView;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import audiodrive.AudioDrive;
import audiodrive.audio.AudioFile;
import audiodrive.audio.AudioPlayer;
import audiodrive.ui.components.Camera;
import audiodrive.ui.components.Scene;
import audiodrive.ui.components.Text;
import audiodrive.ui.menu.Menu;
import audiodrive.ui.menu.item.FileChooserItem;
import audiodrive.ui.menu.item.Item;
import audiodrive.ui.menu.item.ItemListener;
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
	private AudioPlayer ap;
	private AudioFile hoverAudio;
	private AudioFile selectAudio;

	@Override
	public void entering() {
		Camera.overlay(getWidth(), getHeight());

		this.titleText = new Text("Choose an AudioFile").setFont(AudioDrive.Font).setSize(48).setPosition(20, 20);

		this.rootMenu = new Menu(20, 200, 1);
		this.itemMenu = new Menu(40 + FileChooserItem.FILECHOOSER_ITEM_WIDTH, 200, 1);
		this.currentFolderText = new Text().setFont(AudioDrive.Font).setPosition(20, 150).setSize(30);
		this.selectedFileText = new Text().setFont(AudioDrive.Font).setPosition(20, 800).setSize(30);
		this.continueText = new Text().setText("Press 'N' to continue").setFont(AudioDrive.Font).setPosition(20, 1000).setSize(35);

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
			this.rootMenu.addItem(fci);
			rootMap.put(fci, file);
		}
		final String hoverPath = "/sounds/" + "hover" + ".wav";
		final String selectPath = "/sounds/" + "select" + ".wav";
		hoverAudio = loadAudioFile(hoverPath);
		selectAudio = loadAudioFile(selectPath);
		ap = new AudioPlayer();
		super.enter();
	}

	private AudioFile loadAudioFile(String audioPath) {
		URL resource = this.getClass().getResource(audioPath);
		try {
			final URI uri = resource.toURI();
			return new AudioFile(new File(uri));
		} catch (URISyntaxException e) {
			throw new RuntimeException("Could not load file '" + audioPath + "'. " + e.getMessage(), e);
		}
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
		this.itemMap.clear();
		this.itemMenu.removeAllItems();

		// Add parent
		File parentFile = fsv.getParentDirectory(rootFile);
		if (parentFile != null && !fsv.isFileSystemRoot(rootFile)) {
			FileChooserItem parentFileChooser = new FileChooserItem(PARENT_FILENAME, true, this);
			this.itemMenu.addItem(parentFileChooser);

			itemMap.put(parentFileChooser, parentFile);
		}

		// TODO filter for supported filetypes
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
		renderBackground();
		this.itemMenu.render();
		this.rootMenu.render();
		this.currentFolderText.render();
		this.selectedFileText.render();
		this.titleText.render();
		this.continueText.render();
	}

	/**
	 * renders some nice background
	 */
	private void renderBackground() {
		final int height = getHeight();
		final int width = getWidth();

		final int quadCount = 15;
		final int sizeX = width / quadCount;
		final int sizeY = height / quadCount;

		for (int x = 0; x < width; x += sizeX) {
			for (int y = 0; y < height; y += sizeY) {
				GL11.glColor4d(0, 0, 1, Math.random());
				GL11.glBegin(GL11.GL_LINE_STRIP);

				GL11.glVertex2i(x, y + sizeY);
				GL11.glVertex2i(x + sizeX, y + sizeY);
				GL11.glVertex2i(x + sizeX, y);
				GL11.glVertex2i(x, y);
				GL11.glEnd();
			}
		}

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
					this.selectedFile = rootFile;
					this.selectedFileText.setText("File selected: '" + this.selectedFile.getName() + "'");
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
		this.selectedFile = null;
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
			if (this.selectedFile != null) {
				Scene.get(AnalyzationScene.class).enter(new AudioFile(this.selectedFile));
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
			ap.play(hoverAudio);
		}
	}

	Item item = null;

	@Override
	public void onSelect(Item item, boolean select) {
		if (!select) {
			return;
		}
		ap.play(selectAudio);
		this.item = item;
	}
}
