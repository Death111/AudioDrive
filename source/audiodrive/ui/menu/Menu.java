package audiodrive.ui.menu;

import java.util.ArrayList;
import java.util.List;

import audiodrive.ui.menu.item.Item;
import audiodrive.ui.menu.item.ItemListener;

public class Menu implements ItemListener {

	List<Item> items = new ArrayList<Item>();
	int posX, posY;
	int spacing;

	/**
	 * @param items
	 * @param posX
	 * @param posY
	 * @param spacing
	 */
	public Menu(int posX, int posY, int spacing) {
		super();
		this.posX = posX;
		this.posY = posY;
		this.spacing = spacing;
	}

	public void render() {
		for (Item item : this.items) {
			item.render();
		}
	}

	/**
	 * Adds an item to the menu
	 * 
	 * @param item
	 *            item to add
	 */
	public void addItem(Item item) {
		// TODO add pagination if area is out of bounds

		int listSize = this.items.size();
		final int COLUMN_SIZE = 20;
		final int column = listSize / COLUMN_SIZE;

		item.setPosX(posX + column * item.getWidth() + column * (this.spacing * 2));
		listSize -= COLUMN_SIZE * column;
		final int itemHeight = listSize * item.getHeight();
		final int spacingHeight = listSize * this.spacing;
		item.setPosY(posY + itemHeight + spacingHeight);

		this.items.add(item);
	}

	public void removeAllItems() {
		this.items = new ArrayList<Item>();
	}

	public void mouseMoved(int x, int y) {
		// Loop through all MenuItems to check onHover
		for (Item item : this.items) {
			if (item.isDisabled())
				continue;
			final boolean leftBounds = x >= item.getPosX();
			final boolean rightBounds = x < item.getPosX() + item.getWidth();
			final boolean topBounds = y >= item.getPosY();
			final boolean bottomBounds = y < item.getPosY() + item.getHeight();
			final boolean inBounds = leftBounds && rightBounds && topBounds && bottomBounds;
			// Check if mouse is inbounds of item
			if (inBounds) {
				if (!item.getHover()) {
					item.setHover(true);
				}
			} else {
				if (item.getHover()) {
					item.setHover(false);
				}
			}
		}
	}

	public void mousePressed(int button, int x, int y) {
		if (button == 0) {
			for (Item item : this.items) {
				if (item.isDisabled())
					continue;
				final boolean leftBounds = x >= item.getPosX();
				final boolean rightBounds = x < item.getPosX() + item.getWidth();
				final boolean topBounds = y >= item.getPosY();
				final boolean bottomBounds = y < item.getPosY() + item.getHeight();
				final boolean inBounds = leftBounds && rightBounds && topBounds && bottomBounds;
				// Check if mouse is inbounds of item
				if (inBounds) {
					// Selected gets focus
					item.setSelected(true);
				} else {
					// All other loose it
					item.setSelected(false);
				}
			}
		}
	}

	@Override
	public void onHover(Item item, boolean hover) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSelect(Item item, boolean select) {
		// TODO Auto-generated method stub

	}

}
