package audiodrive.ui.menu.item;

import static org.lwjgl.opengl.GL11.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import audiodrive.AudioDrive;
import audiodrive.Resources;
import audiodrive.model.geometry.Color;
import audiodrive.model.geometry.Vector;
import audiodrive.ui.components.Text;
import audiodrive.utilities.Log;

public abstract class Item {
	
	public static enum State {
		Normal, Hovering, Selected, Disabled
	}
	
	public static enum Icon {
		Music, Folder, Normal, Next, Previous
	}
	
	protected static final Map<State, Colors> DefaultColors = new HashMap<>();
	static {
		DefaultColors.put(State.Normal, new Colors(Color.White, Color.White, Color.TransparentBlack));
		DefaultColors.put(State.Hovering, new Colors(Color.Blue, Color.White, Color.TransparentBlue));
		DefaultColors.put(State.Selected, new Colors(Color.Green, Color.White, Color.TransparentGreen));
		DefaultColors.put(State.Disabled, new Colors(Color.Gray, Color.White, Color.TransparentGray));
	}
	
	private State state = State.Normal;
	protected Text text;
	protected int x;
	protected int y;
	protected int width;
	protected int height;
	protected boolean filled = true;
	protected boolean box = AudioDrive.Settings.getBoolean("interface.useBoxes");
	protected List<ItemListener> itemListeners = new ArrayList<ItemListener>();
	protected Map<State, Colors> colorMapping;
	
	protected Icon icon = Icon.Normal;
	protected int iconWidth;
	
	public Item(String itemText, int width, int height) {
		x = 0;
		y = 0;
		this.width = width;
		this.height = height;
		iconWidth = height;
		// TODO calculate size by width and height
		recalculate(itemText, width);
	}
	
	protected void recalculate(String itemText, int availWidth) {
		int size = 30;
		if (availWidth > height) {
			size = (int) (height / 1.5);
		} else {
			size = availWidth / 10;
		}
		
		text = new Text(itemText).setFont(AudioDrive.Font).setPosition(x, y).setSize(size);
		
		// Check if text is to big
		final int availableTextWidth = availWidth - iconWidth;
		final int width2 = text.getWidth();
		if (text.getHeight() > height || width2 > availableTextWidth) {
			Log.trace("Text '" + itemText + "' is bigger than menuItem. Trimming it");
			boolean trimmed = false;
			while (text.getWidth() > availableTextWidth) {
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
	
	public void render() {
		Colors colors = getColors();
		if (box) {
			colors.background.gl();
			glPolygonMode(GL_FRONT, filled ? GL_FILL : GL_LINE);
			glBegin(GL_QUADS);
			glVertex2f(x, y);
			glVertex2f(x, y + height);
			glVertex2f(x + width, y + height);
			glVertex2f(x + width, y);
			glEnd();
		}
		final Color color = box ? colors.foreground : colors.text;
		text.setColor(color).setPosition(x + iconWidth, y).render();
		if (icon != null) {
			Resources.getIconModel().position(new Vector(x, y, 0)).position().xAdd(iconWidth / 2).yAdd(iconWidth / 2);
			Resources.getIconModel().scale(iconWidth / 2).color(color).setTexture(Resources.getIconTextures().get(icon)).render();
		}
	}
	
	public void addItemListener(ItemListener itemListener) {
		itemListeners.add(itemListener);
	}
	
	public void setState(State state) {
		if (state == this.state) return;
		fireStateChange(this.state, false);
		fireStateChange(state, true);
		this.state = state;
	}
	
	private void fireStateChange(State state, boolean activated) {
		switch (state) {
		case Hovering:
			itemListeners.forEach(itemListener -> itemListener.onHover(this, activated));
			return;
		case Selected:
			itemListeners.forEach(itemListener -> itemListener.onSelect(this, activated));
			return;
		default:
			break;
		}
	}
	
	protected Item setIcon(Icon icon) {
		this.icon = icon;
		return this;
	}
	
	public void setBox(boolean box) {
		this.box = box;
	}
	
	public void setFilled(boolean filled) {
		this.filled = filled;
	}
	
	public void setY(int y) {
		this.y = y;
	}
	
	public void setX(int x) {
		this.x = x;
	}
	
	public final int getX() {
		return x;
	}
	
	public final int getY() {
		return y;
	}
	
	public final int getWidth() {
		return width;
	}
	
	public final int getHeight() {
		return height;
	}
	
	public final String getText() {
		return text.getText();
	}
	
	public void setHovering(boolean hover) {
		setState(hover ? State.Hovering : State.Normal);
	}
	
	public void setSelected(boolean selected) {
		setState(selected ? State.Selected : State.Normal);
	}
	
	public void setDisabled(boolean disabled) {
		setState(disabled ? State.Disabled : State.Normal);
	}
	
	public boolean isHovering() {
		return state == State.Hovering;
	}
	
	public boolean isSelected() {
		return state == State.Selected;
	}
	
	public boolean isDisabled() {
		return state == State.Disabled;
	}
	
	public State getState() {
		return state;
	}
	
	public Map<State, Colors> colorMapping() {
		if (colorMapping == null) colorMapping = new HashMap<>(DefaultColors);
		return colorMapping;
	}
	
	public Colors getColors() {
		return colorMapping != null ? colorMapping.get(state) : DefaultColors.get(state);
	}
	
	public static class Colors {
		/** text color if box is disabled */
		public final Color text;
		/** foreground/text color if box is enabled */
		public final Color foreground;
		/** background/border color if box is enabled */
		public final Color background;
		
		public Colors(Color text, Color foreground, Color background) {
			this.text = text != null ? text : Color.White;
			this.foreground = foreground != null ? foreground : Color.Transparent;
			this.background = background != null ? background : Color.Transparent;
		}
	}
	
}
