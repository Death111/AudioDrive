package audiodrive.ui.menu.item;

import static org.lwjgl.opengl.GL11.*;

import java.util.List;

import audiodrive.Resources;
import audiodrive.model.geometry.Color;
import audiodrive.model.geometry.Vector;
import audiodrive.ui.components.Text;
import audiodrive.utilities.Arithmetic;

public class SettingsItem<T> extends Item {
	
	int currentValueIndex = 0;
	String name;
	List<T> values;
	
	private int length = 300;
	
	public SettingsItem(String name, List<T> values, int width, int height) {
		super(name, width, height);
		this.name = name;
		this.values = values;
		length = (width / 3) < length ? width / 3 : length;
		recalculate(name, width - length + iconWidth);
	}
	
	public SettingsItem(String name, List<T> values, int width, int height, ItemListener itemListener) {
		this(name, values, width, height);
		this.addItemListener(itemListener);
	}
	
	@Override
	public void render() {
		Colors normalColors = getColors(State.Normal);
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
		final Color textColor = box ? normalColors.foreground : normalColors.text;
		Color settingsColor = box ? colors.foreground : colors.text;
		
		// Setting name
		text.setColor(textColor).setPosition(x, y).render();
		
		int xOffset = width - length;
		// Previous arrow
		Resources.getIconModel().position(new Vector(x + xOffset, y, 0)).position().xAdd(iconWidth / 2).yAdd(iconWidth / 2);
		Resources.getIconModel().scale(-1 * iconWidth / 2).color(settingsColor).setTexture(Resources.getIconTextures().get(Icon.Previous)).render();
		
		// Setting value
		Text valueText = new Text(valueAsString());
		valueText.setSize(text.getSize());
		xOffset += length / 2 - valueText.getWidth() / 2;
		valueText.setColor(settingsColor).setPosition(x + xOffset, y).render();
		
		// Next arrow
		xOffset = width - iconWidth;
		Resources.getIconModel().position(new Vector(x + xOffset, y, 0)).position().xAdd(iconWidth / 2).yAdd(iconWidth / 2);
		Resources.getIconModel().scale(iconWidth / 2).color(settingsColor).setTexture(Resources.getIconTextures().get(Icon.Next)).render();
	}
	
	@Override
	public void setHovering(boolean hovering, int x, int y) {
		super.setHovering(hovering && x > this.x + width - length, x, y);
	}
	
	@Override
	public void setSelected(boolean selected, int x, int y) {
		if (!selected) return;
		
		if (values.size() == 2) {
			currentValueIndex = Math.abs(currentValueIndex - 1);
			this.fireStateChange(State.Selected, true);
		} else if (inBounds(this.x + width - length, this.y, x, y)) {
			decrement();
		} else if (inBounds(this.x + width - iconWidth, this.y, x, y)) {
			increment();
		}
	}
	
	@Override
	public void increment() {
		int index = Arithmetic.clamp(currentValueIndex + 1, 0, values.size() - 1);
		if (index == currentValueIndex) return;
		currentValueIndex = index;
		this.fireStateChange(State.Selected, true);
	}
	
	@Override
	public void decrement() {
		int index = Arithmetic.clamp(currentValueIndex - 1, 0, values.size() - 1);
		if (index == currentValueIndex) return;
		currentValueIndex = index;
		this.fireStateChange(State.Selected, true);
	}
	
	private boolean inBounds(int itemX, int itemY, int x, int y) {
		final boolean leftBounds = x >= itemX;
		final boolean rightBounds = x < itemX + iconWidth;
		final boolean topBounds = y >= itemY;
		final boolean bottomBounds = y < itemY + iconWidth;
		return (leftBounds && rightBounds && topBounds && bottomBounds);
	}
	
	public T getValue() {
		return values.get(currentValueIndex);
	}
	
	public void setValue(T value) {
		for (int i = 0; i < values.size(); i++)
			if (values.get(i).equals(value)) currentValueIndex = i;
	}
	
	public String valueAsString() {
		return getValue().toString();
	}
}
