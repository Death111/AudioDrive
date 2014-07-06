package audiodrive.ui.menu;

import java.util.ArrayList;
import java.util.List;

import audiodrive.ui.menu.item.Item;

public class Menu {

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

	public void addItem(Item item) {
		item.setPosX(posX);
		item.setPosY(posY + (this.items.size() * item.getHeight()) + this.items.size() * this.spacing);
		this.items.add(item);
	}

	public void mouseMoved(int x, int y) {
		// Loop through all MenuItems to check onHover
		for (Item item : this.items) {
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

}
