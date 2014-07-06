package audiodrive.ui.menu.item;

import java.util.ArrayList;
import java.util.List;

import audiodrive.AudioDrive;
import audiodrive.ui.components.Text;
import audiodrive.utilities.Log;

public abstract class Item {

	boolean hover = false;
	boolean selected = false;
	int posX;
	int posY;
	Text text;

	protected List<ItemListener> itemListeners = new ArrayList<ItemListener>();

	public Item(String itemText, int width, int height) {
		this.posX = 0;
		this.posY = 0;

		// TODO center text
		int size = 30;
		text = new Text(itemText).setFont(AudioDrive.Font).setPosition(posX, posY).setSize(size);

		if (text.getHeight() > height && text.getWidth() > width) {
			Log.warning("Text '" + itemText + "' is bigger than menuItem");
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

	public void setHover(boolean hover) {
		itemListeners.forEach(itemListener -> itemListener.onHover(this, hover));
		this.hover = hover;
	}

	public void setSelected(boolean selected) {
		Log.info(this.text.getText());
		itemListeners.forEach(itemListener -> itemListener.onSelect(this, selected));
		this.selected = selected;
	}
}
