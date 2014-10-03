package audiodrive.ui.menu;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;

import audiodrive.ui.components.Window;
import audiodrive.ui.control.Input;
import audiodrive.ui.menu.item.FileChooserItem;
import audiodrive.ui.menu.item.Item;
import audiodrive.ui.menu.item.ItemListener;

public class Menu implements ItemListener, Input.Observer {
	
	private List<Item> items = new ArrayList<Item>();
	private List<Item> visibleItems = new ArrayList<Item>();
	private Item nextPage = new FileChooserItem(">> Next page", this);
	private Item previousPage = new FileChooserItem("<< Previous page", this);
	private int posX, posY, width, height;
	private int spacing;
	
	private int oldPage = -1;
	private int page = 0;
	
	/**
	 * @param items
	 * @param posX
	 * @param posY
	 * @param spacing
	 */
	public Menu(int posX, int posY, int width, int height, int spacing) {
		super();
		this.posX = posX;
		this.posY = posY;
		this.width = width;
		this.height = height;
		this.spacing = spacing;
	}
	
	public void render() {
		
		// Check if visible items needs to be recalculated
		if (oldPage != page) {
			oldPage = page;
			calculateVisibleItems();
		}
		
		for (Item item : visibleItems) {
			item.render();
		}
	}
	
	/**
	 * Calculates the current visible items and adds them to visibleitems with appropriate position
	 */
	private void calculateVisibleItems() {
		
		visibleItems.clear();
		
		int listSize = items.size();
		final Item item = items.get(0);
		
		final int ROWS = height / (item.getHeight() + spacing);
		final int COLUMNS = width / (item.getWidth() + spacing);
		
		// Check if "previous page" button has to be added
		if (page > 0) {
			int a = visibleItems.size() % (ROWS * COLUMNS);
			int currentColumn = a / ROWS;
			int currentRow = a % ROWS;
			final int x = posX + currentColumn * (item.getWidth() + spacing);
			final int y = posY + currentRow * (item.getHeight() + spacing);
			previousPage.setX(x);
			previousPage.setY(y);
			previousPage.setSelected(false, x, y);
			visibleItems.add(previousPage);
		}
		
		for (int i = page * ROWS * COLUMNS - page * 2; i < listSize; i++) {
			Item currentItem = items.get(i);
			int a = visibleItems.size() % (ROWS * COLUMNS);
			int currentColumn = a / ROWS;
			int currentRow = a % ROWS;
			final int x = posX + currentColumn * (item.getWidth() + spacing);
			final int y = posY + currentRow * (item.getHeight() + spacing);
			
			// Test if limit was reached and items are following
			if (a == ROWS * COLUMNS - 1 && i < listSize - 1) {
				// Add a nextPage button
				nextPage.setX(x);
				nextPage.setY(y);
				nextPage.setSelected(false, x, y);
				visibleItems.add(nextPage);
				return;
			}
			
			currentItem.setX(x);
			currentItem.setY(y);
			
			visibleItems.add(currentItem);
		}
	}
	
	/**
	 * Adds an item to the menu
	 * 
	 * @param item item to add
	 */
	public void addItem(Item item) {
		// Check if at least one menu item fits into menu
		// TODO could cause a infinite "next page" loop
		if (width <= item.getWidth() || height <= item.getHeight()) {
			throw new RuntimeException("No space for menu item");
		}
		
		item.reset();
		items.add(item);
		// reset old page
		oldPage = -1;
	}
	
	public void removeAllItems() {
		page = 0;
		oldPage = -1;
		items = new ArrayList<Item>();
	}
	
	@Override
	public void mouseMoved(int x, int y, int dx, int dy) {
		// yCoordinates start in left bottom corner, instead left top
		y = Window.getHeight() - y;
		
		// Loop through all MenuItems to check onHover
		for (Item item : visibleItems) {
			if (item.isDisabled()) continue;
			setHovering(x, y, item);
		}
	}
	
	@Override
	public void mouseButtonPressed(int button, int x, int y) {
		// yCoordinates start in left bottom corner, instead left top
		y = Window.getHeight() - y;
		
		if (button == 0) {
			for (Item item : visibleItems) {
				if (item.isDisabled()) continue;
				setSelected(x, y, item);
			}
		}
	}
	
	@Override
	public void mouseWheelRotated(int rotation, int x, int y) {
		visibleItems.stream().filter(Item::isHovering).forEach(rotation > 0 ? Item::increment : Item::decrement);
	}
	
	@Override
	public void keyPressed(int key, char character) {
		switch (key) {
		case Keyboard.KEY_UP:
		case Keyboard.KEY_RIGHT:
		case Keyboard.KEY_ADD:
			visibleItems.stream().filter(Item::isHovering).forEach(Item::increment);
			break;
		case Keyboard.KEY_DOWN:
		case Keyboard.KEY_LEFT:
		case Keyboard.KEY_SUBTRACT:
			visibleItems.stream().filter(Item::isHovering).forEach(Item::decrement);
			break;
		default:
			break;
		}
	}
	
	/**
	 * Set hovering if mouse is in bounds.
	 */
	private void setHovering(int x, int y, Item item) {
		if (item.isSelected()) return;
		boolean hovering = inBounds(item, x, y);
		item.setHovering(hovering, x, y);
	}
	
	/**
	 * Set selected if mouse is in bounds.
	 */
	private void setSelected(int x, int y, Item item) {
		final boolean selected = inBounds(item, x, y);
		item.setSelected(selected, x, y);
	}
	
	private boolean inBounds(Item item, int x, int y) {
		final boolean leftBounds = x >= item.getX();
		final boolean rightBounds = x < item.getX() + item.getWidth();
		final boolean topBounds = y >= item.getY();
		final boolean bottomBounds = y < item.getY() + item.getHeight();
		return (leftBounds && rightBounds && topBounds && bottomBounds);
	}
	
	@Override
	public void onHover(Item item, boolean hover) {}
	
	@Override
	public void onSelect(Item item, boolean select) {
		if (!select) return;
		if (item == nextPage) {
			page++;
		} else if (item == previousPage) {
			page--;
		}
	}
	
	/**
	 * @return the width
	 */
	public final int getWidth() {
		return width;
	}
	
	/**
	 * @return the height
	 */
	public final int getHeight() {
		return height;
	}
	
}
