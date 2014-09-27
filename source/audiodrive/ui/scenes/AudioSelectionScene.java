package audiodrive.ui.scenes;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;

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
import audiodrive.audio.AudioResource;
import audiodrive.audio.AudioInfo;
import audiodrive.model.geometry.Color;
import audiodrive.ui.components.Camera;
import audiodrive.ui.components.Overlay;
import audiodrive.ui.components.Scene;
import audiodrive.ui.components.Text;
import audiodrive.ui.components.Text.Alignment;
import audiodrive.ui.effects.ShaderProgram;
import audiodrive.ui.menu.Menu;
import audiodrive.ui.menu.item.FileChooserItem;
import audiodrive.ui.menu.item.Item;
import audiodrive.ui.menu.item.ItemListener;
import audiodrive.ui.menu.item.MenuItem;
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
	private FileSystemView fsv = FileSystemView.getFileSystemView();
	
	private Text currentFolderText;
	private Text selectedFileText;
	private Text selectedFileInfoText;
	private Text titleText;
	private Overlay background;
	
	private AudioResource hoverAudio;
	private AudioResource selectAudio;
	private List<String> supportedFileExtensionList = Arrays.asList("mp3", "wav");
	
	private File selectedFile = null;
	private Item selectedItem = null;
	
	private boolean silentHovering = true;
	private double volume;
	
	@Override
	public void entering() {
		volume = AudioDrive.Settings.getDouble("interface.volume");
		
		Camera.overlay(getWidth(), getHeight());
		background = new Overlay().shader(new ShaderProgram("shaders/default.vs", "shaders/title.fs"));
		
		titleText = new Text("Select an AudioFile").setFont(AudioDrive.Font).setSize(48).setPosition(20, 20);
		currentFolderText = new Text().setFont(AudioDrive.Font).setPosition(20, 125).setSize(22).setAlignment(Alignment.Left);
		selectedFileText = new Text().setFont(AudioDrive.Font).setPosition(20, Display.getHeight() - 125).setSize(22).setAlignment(Alignment.Left);
		selectedFileInfoText = new Text().setFont(AudioDrive.Font).setPosition(20, Display.getHeight() - 100).setSize(22).setAlignment(Alignment.Left);
		
		rootMenu = new Menu(20, 180, 151, Display.getHeight() - 180, 1);
		itemMenu = new Menu(30 + 151, 180, Display.getWidth() - 151 - 50, Display.getHeight() - 2 * 180 + currentFolderText.getHeight(), 1);
		nextMenu = new Menu(20, Display.getHeight() - MenuItem.MENU_ITEM_HEIGHT - 20, Display.getWidth() - 50, MenuItem.MENU_ITEM_HEIGHT + 1, 1);
		
		continueMenuItem = new MenuItem("Continue", this);
		continueMenuItem.colorMapping().put(Item.State.Normal, new Item.Colors(Color.Green, Color.White, Color.TransparentGreen));
		continueMenuItem.setFilled(true);
		continueMenuItem.setDisabled(true);
		nextMenu.addItem(continueMenuItem);
		
		// Setup start node
		updateItemExplorer(new File(AudioDrive.Settings.get("music.directory")));
		
		// Adding default item do list
		final File defaultFile = new File("music");
		if (defaultFile.exists()) {
			FileChooserItem defaultFCI = new FileChooserItem("default", true, this, 150, FileChooserItem.FILECHOOSER_ITEM_HEIGHT);
			rootMenu.addItem(defaultFCI);
			rootMap.put(defaultFCI, defaultFile);
		}
		
		// Adding 'My Music' to list
		final File myMusicFile = new File(System.getProperty("user.home") + "\\music");
		if (myMusicFile.exists()) {
			FileChooserItem myMusicFCI = new FileChooserItem("My Music", true, this, 150, FileChooserItem.FILECHOOSER_ITEM_HEIGHT);
			rootMenu.addItem(myMusicFCI);
			rootMap.put(myMusicFCI, myMusicFile);
		}
		// Adding all roots to list
		for (File file : File.listRoots()) {
			final boolean directory = file.isDirectory();
			if (!directory) {
				// Root directory is empty (e.g. not used usb)
				continue;
			}
			String fileName = file.toString();
			FileChooserItem fci = new FileChooserItem(fileName, directory, this, 150, FileChooserItem.FILECHOOSER_ITEM_HEIGHT);
			rootMenu.addItem(fci);
			rootMap.put(fci, file);
		}
		
		hoverAudio = new AudioResource("sounds/Hover.mp3");
		selectAudio = new AudioResource("sounds/Select.mp3");
	}
	
	/**
	 * Updates the explorer with the given file as root node
	 * 
	 * @param rootFile The root file of the tree to be displayed
	 */
	private void updateItemExplorer(File rootFile) {
		currentFolderText.setText("Current folder: " + rootFile.getAbsolutePath());
		AudioDrive.Settings.set("music.directory", rootFile.getPath());
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
		background.render();
		if (selectedItem != null) {
			checkItemExplorer(itemMap);
			checkItemExplorer(rootMap);
		}
		selectedItem = null;
		rootMenu.render();
		itemMenu.render();
		nextMenu.render();
		currentFolderText.render();
		selectedFileText.render();
		selectedFileInfoText.render();
		titleText.render();
	}
	
	private void checkItemExplorer(Map<FileChooserItem, File> map) {
		final Set<FileChooserItem> keySet = map.keySet();
		boolean update = false;
		File rootFile = null;
		
		for (FileChooserItem fileChooserItem : keySet) {
			
			if (selectedItem == fileChooserItem) {
				final File file = map.get(fileChooserItem);
				update = true;
				rootFile = file;
				if (!fileChooserItem.isDirectory()) {
					Log.debug("An item was selected: '" + rootFile.getName() + "'.");
					selectedFile = rootFile;
					// Try to get some informations about selected file
					AudioInfo audioInfo = new AudioInfo(rootFile);
					String duration = "Duration: " + ((audioInfo.duration.isEmpty()) ? "N/A" : audioInfo.duration);
					selectedFileText.setText("File selected: '" + selectedFile.getName() + "'");
					selectedFileInfoText.setText(duration);
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
		background = null;
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
		super.keyReleased(key, character);
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
		if (hover && !silentHovering) {
			hoverAudio.play(volume);
		}
	}
	
	@Override
	public void onSelect(Item item, boolean select) {
		if (!select) {
			return;
		}
		selectAudio.play(volume);
		
		if (item == continueMenuItem) {
			if (selectedFile != null) {
				Scene.get(AnalyzationScene.class).enter(new AudioResource(selectedFile));
			} else {
				Log.warning("No file selected.");
			}
		} else {
			selectedItem = item;
		}
	}
	
	/**
	 * Returns the file extension of the given filename
	 * 
	 * @param fileName fileName do get extension from
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
