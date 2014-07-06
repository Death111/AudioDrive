package audiodrive.ui.menu;

import java.util.ArrayList;
import java.util.List;

import audiodrive.ui.menu.item.MenuItem;

public class Menu {

	List<MenuItem> menuItems = new ArrayList<MenuItem>();
	int posX, posY;
	int spacing;

	/**
	 * @param menuItems
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
		for (MenuItem menuItem : this.menuItems) {
			menuItem.render();
		}
	}

	public void addMenuItem(MenuItem menuItem) {
		menuItem.setPosX(posX);
		menuItem.setPosY(posY + (this.menuItems.size() * MenuItem.MENU_ITEM_HEIGHT) + this.menuItems.size() * this.spacing);
		this.menuItems.add(menuItem);
	}

	public void mouseMoved(int x, int y) {
		// Loop through all MenuItems to check onHover
		for (MenuItem menuItem : this.menuItems) {
			final boolean leftBounds = x >= menuItem.getPosX();
			final boolean rightBounds = x < menuItem.getPosX() + menuItem.getWidth();
			final boolean topBounds = y >= menuItem.getPosY();
			final boolean bottomBounds = y < menuItem.getPosY() + menuItem.getHeight();
			final boolean inBounds = leftBounds && rightBounds && topBounds && bottomBounds;
			// Check if mouse is inbounds of item
			if (inBounds) {
				if (!menuItem.getHover()) {
					menuItem.setHover(true);
				}
			} else {
				if (menuItem.getHover()) {
					menuItem.setHover(false);
				}
			}
		}
	}

	public void mousePressed(int button, int x, int y) {
		if (button == 0) {
			for (MenuItem menuItem : this.menuItems) {
				final boolean leftBounds = x >= menuItem.getPosX();
				final boolean rightBounds = x < menuItem.getPosX() + menuItem.getWidth();
				final boolean topBounds = y >= menuItem.getPosY();
				final boolean bottomBounds = y < menuItem.getPosY() + menuItem.getHeight();
				final boolean inBounds = leftBounds && rightBounds && topBounds && bottomBounds;
				// Check if mouse is inbounds of item
				if (inBounds) {
					// Selected gets focus
					menuItem.setSelected(true);
				} else {
					// All other loose it
					menuItem.setSelected(false);
				}
			}
		}
	}

}
