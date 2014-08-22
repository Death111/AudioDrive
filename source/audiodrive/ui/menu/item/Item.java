package audiodrive.ui.menu.item;

import java.util.ArrayList;
import java.util.List;

import audiodrive.AudioDrive;
import audiodrive.ui.components.Text;
import audiodrive.utilities.Log;

public abstract class Item {

	boolean hover = false;
	boolean selected = false;
	boolean disabled = false;
	int posX;
	int posY;
	Text text;

	protected List<ItemListener> itemListeners = new ArrayList<ItemListener>();
	private int width;
	private int height;

	public Item(String itemText, int width, int height) {
		this.posX = 0;
		this.posY = 0;
		this.width = width;
		this.height = height;

		// TODO calculate size by width and height
		int size = 30;
		if (width > height) {
			size = (int) (height / 1.5);
		} else {
			size = width / 10;
		}
		text = new Text(itemText).setFont(AudioDrive.Font).setPosition(posX, posY).setSize(size);

		// Check if text is to big
		if (text.getHeight() > height || text.getWidth() > width) {
			Log.warning("Text '" + itemText + "' is bigger than menuItem. Trimming it");
			boolean trimmed = false;
			while (text.getWidth() > width) {
				String trimmedText = text.getText();
				trimmed = true;
				text.setText(trimmedText.substring(0, trimmedText.length() - 1));
			}
			// Add dots for trimmed items
			if (trimmed) {
				String trimmedText = text.getText();
				text.setText(trimmedText.substring(0, trimmedText.length() - 3) + "...");
			}
		}
	}

	public abstract void render();

	public void addItemListener(ItemListener itemListener) {
		itemListeners.add(itemListener);
	}

	public final boolean getHover() {
		return hover;
	}

	public final boolean getSelected() {
		return selected;
	}

	/**
	 * @return the posX
	 */
	public final int getPosX() {
		return posX;
	}

	/**
	 * @return the posY
	 */
	public final int getPosY() {
		return posY;
	}

	/**
	 * @return the width
	 */
	public final int getWidth() {
		return this.width;
	}

	/**
	 * @return the height
	 */
	public final int getHeight() {
		return this.height;
	}

	public final String getText() {
		return this.text.getText();
	}

	public void setPosY(int y) {
		this.posY = y;
	}

	public void setPosX(int posX) {
		this.posX = posX;
	}

	public void setHover(boolean hover) {
		itemListeners.forEach(itemListener -> itemListener.onHover(this, hover));
		this.hover = hover;
	}

	public void setSelected(boolean selected) {
		itemListeners.forEach(itemListener -> itemListener.onSelect(this, selected));
		this.selected = selected;
	}

	/**
	 * @return the disabled
	 */
	public final boolean isDisabled() {
		return disabled;
	}

	/**
	 * @param disabled
	 *            the disabled to set
	 */
	public final void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}
}
