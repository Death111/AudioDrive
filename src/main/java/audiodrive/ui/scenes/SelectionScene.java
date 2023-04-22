package audiodrive.ui.scenes;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import javax.swing.filechooser.FileSystemView;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

import audiodrive.AudioDrive;
import audiodrive.audio.AnalyzedAudio;
import audiodrive.audio.AudioInfo;
import audiodrive.audio.AudioResource;
import audiodrive.model.geometry.Color;
import audiodrive.ui.components.Camera;
import audiodrive.ui.components.Overlay;
import audiodrive.ui.components.Scene;
import audiodrive.ui.components.Text;
import audiodrive.ui.components.Text.Alignment;
import audiodrive.ui.control.Input;
import audiodrive.ui.effects.ShaderProgram;
import audiodrive.ui.menu.Menu;
import audiodrive.ui.menu.item.FileChooserItem;
import audiodrive.ui.menu.item.Item;
import audiodrive.ui.menu.item.ItemListener;
import audiodrive.ui.menu.item.MenuItem;
import audiodrive.utilities.Log;
import audiodrive.utilities.Sort;

/**
 * 
 * @author Death
 *
 */
public class SelectionScene extends Scene implements ItemListener {
	
	private static final String PARENT_FILENAME = "..";
	
	private Menu rootMenu;
	private Menu itemMenu;
	private Menu continueMenu;
	private MenuItem continueMenuItem;
	private Map<FileChooserItem, File> rootMap;
	private Map<FileChooserItem, File> itemMap;
	private FileSystemView fsv = FileSystemView.getFileSystemView();
	
	private Text currentFolderText;
	private Text selectedFileText;
	private Text selectedFileInfoText;
	private Text titleText;
	private Overlay background;
	
	private AudioResource hoverAudio;
	private AudioResource selectAudio;
	private List<String> supportedFileExtensionList = Arrays.asList("mp3", "wav");
	
	private File rootFile = new File(AudioDrive.Settings.get("music.directory"));
	private File selectedFile = null;
	private Item selectedItem = null;
	
	private boolean silentHovering = true;
	private double volume;
	
	@Override
	public void entering() {
		volume = AudioDrive.Settings.getDouble("interface.volume");
		
		Camera.overlay(getWidth(), getHeight());
		background = new Overlay().shader(new ShaderProgram("shaders/Default.vs", "shaders/Title.fs"));
		
		titleText = new Text("Select an AudioFile").setFont(AudioDrive.Font).setSize(48).setPosition(20, 20);
		currentFolderText = new Text().setFont(AudioDrive.Font).setPosition(20, 125).setSize(22).setAlignment(Alignment.Left);
		selectedFileText = new Text().setFont(AudioDrive.Font).setPosition(20, Display.getHeight() - 125).setSize(22).setAlignment(Alignment.Left);
		selectedFileInfoText = new Text().setFont(AudioDrive.Font).setPosition(20, Display.getHeight() - 100).setSize(22).setAlignment(Alignment.Left);
		
		rootMenu = new Menu(20, 180, 151, Display.getHeight() - 180, 1);
		itemMenu = new Menu(30 + 151, 180, Display.getWidth() - 151 - 50, Display.getHeight() - 2 * 180 + currentFolderText.getHeight(), 1);
		continueMenu = new Menu(20, Display.getHeight() - MenuItem.MENU_ITEM_HEIGHT - 20, Display.getWidth() - 50, MenuItem.MENU_ITEM_HEIGHT + 1, 1);
		
		continueMenuItem = new MenuItem("Continue", this);
		continueMenuItem.colorMapping().put(Item.State.Normal, new Item.Colors(Color.Green, Color.White, Color.TransparentGreen));
		continueMenuItem.setFilled(true);
		continueMenuItem.setDisabled(true);
		continueMenu.addItem(continueMenuItem);
		
		rootMap = new HashMap<FileChooserItem, File>();
		itemMap = new HashMap<FileChooserItem, File>();
		
		// Setup start node
		if (!rootFile.exists()) {
			rootFile = new File("music");
		}
		updateItemExplorer(rootFile);
		if (selectedFile != null) {
			Optional<FileChooserItem> item = itemMap.entrySet().stream().filter(entry -> entry.getValue().equals(selectedFile)).findAny().map(Entry::getKey);
			if (item.isPresent()) {
				FileChooserItem selected = item.get();
				selected.setSelected(true, selected.getX(), selected.getY());
			}
		}
		
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
		Input.addObservers(rootMenu, itemMenu, continueMenu);
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
		
		Arrays.stream(listFiles).sorted(Sort.comparingIsDirectory()).forEach(file -> {
			final boolean directory = file.isDirectory();
			final String fileName = file.getName();
			final String extension = getFileExtension(fileName);
			// Check if file is supported
			if (directory || supportedFileExtensionList.contains(extension)) {
				FileChooserItem fci = new FileChooserItem(fileName, directory, this);
				itemMenu.addItem(fci);
				itemMap.put(fci, file);
			}
		});
	}
	
	@Override
	public void render() {
		glClear(GL_COLOR_BUFFER_BIT);
		background.render();
		rootMenu.render();
		itemMenu.render();
		continueMenu.render();
		currentFolderText.render();
		selectedFileText.render();
		selectedFileInfoText.render();
		titleText.render();
	}
	
	private void checkItemExplorer(Map<FileChooserItem, File> map) {
		final Set<FileChooserItem> keySet = map.keySet();
		boolean update = false;
		
		for (FileChooserItem fileChooserItem : keySet) {
			
			if (selectedItem == fileChooserItem) {
				final File file = map.get(fileChooserItem);
				if (!fileChooserItem.isDirectory()) {
					Log.debug("An item was selected: '" + rootFile.getName() + "'.");
					selectedFile = rootFile;
					selectedFileText.setText("File selected: '" + selectedFile.getName() + "'");
					
					// Try to get some informations about selected file
					AudioInfo audioInfo = new AudioInfo(file);
					String duration = "Duration: " + audioInfo.duration;
					selectedFileInfoText.setText(duration); // Only set duration due to little space
					
					continueMenuItem.setDisabled(false);
					setSelected(file);
					update = false;
				} else {
					rootFile = file;
					update = true;
				}
			}
		}
		
		if (update) {
			updateItemExplorer(rootFile);
		}
	}
	
	private void setSelected(File file) {
		selectedFile = file;
		if (file == null) {
			continueMenuItem.setDisabled(true);
			selectedFileText.setText(null);
			return;
		}
		Log.debug("An item was selected: '" + file.getName() + "'.");
		selectedFileText.setText("File selected: '" + selectedFile.getName() + "'");
		continueMenuItem.setDisabled(false);
	}
	
	@Override
	public void exiting() {
		Input.removeObservers(rootMenu, itemMenu, continueMenu);
		rootMap = null;
		itemMap = null;
		rootMenu = null;
		itemMenu = null;
		continueMenu = null;
		titleText = null;
		currentFolderText = null;
		selectedFileText = null;
		continueMenuItem = null;
		background = null;
		selectedItem = null;
	}
	
	@Override
	public void keyReleased(int key, char character) {
		super.keyReleased(key, character);
		switch (key) {
		case Keyboard.KEY_ESCAPE:
			Scene.get(MenuScene.class).enter();
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
				AudioResource audio = new AudioResource(selectedFile);
				AnalyzedAudio analyzedAudio = AudioDrive.getAnalyzedAudio();
				if (analyzedAudio == null || !analyzedAudio.getResource().equals(audio)) {
					AudioDrive.setSelectedAudio(audio);
					Scene.get(AnalyzationScene.class).enter();
				} else {
					switch (AudioDrive.getAction()) {
					case None:
						Scene.get(MenuScene.class).enter();
						break;
					case Play:
						Scene.get(GenerationScene.class).enter();
						break;
					case Visualize:
						Scene.get(VisualizationScene.class).enter();
						break;
					}
				}
			} else {
				Log.warning("No file selected.");
			}
		} else {
			selectedItem = item;
			checkItemExplorer(itemMap);
			checkItemExplorer(rootMap);
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
