package audiodrive.ui.menu.item;

public class MenuItem extends Item {
	
	public final static int MENU_ITEM_WIDTH = 350;
	public final static int MENU_ITEM_HEIGHT = 50;
	
	/**
	 * 
	 * @param text Text of the item
	 */
	public MenuItem(String text) {
		super(text, MENU_ITEM_WIDTH, MENU_ITEM_HEIGHT);
	}
	
	/**
	 * 
	 * @param text Text of the item
	 * @param itemListener An item Listener
	 */
	public MenuItem(String text, ItemListener itemListener) {
		super(text, MENU_ITEM_WIDTH, MENU_ITEM_HEIGHT);
		this.addItemListener(itemListener);
	}
	
}
