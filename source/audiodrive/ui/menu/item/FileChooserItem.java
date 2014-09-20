package audiodrive.ui.menu.item;

import audiodrive.model.geometry.Color;

public class FileChooserItem extends Item {
	
	public final static int FILECHOOSER_ITEM_WIDTH = 350;
	public final static int FILECHOOSER_ITEM_HEIGHT = 25;
	
	private Colors directoryColors = new Colors(Color.Cyan, Color.White, Color.TransparentCyan);
	private boolean isDirectory;
	
	/**
	 * 
	 * @param text Text of the item
	 * @param itemListener
	 * @param isDirectory
	 */
	public FileChooserItem(String text, boolean isDirectory, ItemListener itemListener, int width, int heigth) {
		super(text, width, heigth);
		setIcon(isDirectory ? Icon.Folder : Icon.Music);
		this.isDirectory = isDirectory;
		this.addItemListener(itemListener);
	}
	
	/**
	 * 
	 * @param text Text of the item
	 * @param itemListener
	 * @param isDirectory
	 */
	public FileChooserItem(String text, boolean isDirectory, ItemListener itemListener) {
		this(text, isDirectory, itemListener, FILECHOOSER_ITEM_WIDTH, FILECHOOSER_ITEM_HEIGHT);
	}
	
	/**
	 * 
	 * @param text Text of the item
	 * @param itemListener An item Listener
	 */
	public FileChooserItem(String text, ItemListener itemListener) {
		super(text, FILECHOOSER_ITEM_WIDTH, FILECHOOSER_ITEM_HEIGHT);
		this.addItemListener(itemListener);
	}
	
	@Override
	public Colors getColors() {
		if (isDirectory && getState() == State.Normal) return directoryColors;
		return super.getColors();
	}
	
	/**
	 * @return the isDirectory
	 */
	public final boolean isDirectory() {
		return isDirectory;
	}
	
}
