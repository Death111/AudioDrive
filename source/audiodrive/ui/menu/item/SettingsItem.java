package audiodrive.ui.menu.item;

import static org.lwjgl.opengl.GL11.*;

import java.util.List;

import audiodrive.model.geometry.Color;
import audiodrive.model.geometry.Vector;
import audiodrive.ui.components.Text;

public class SettingsItem<T> extends Item {
	
	int currentValueIndex = 0;
	String name;
	List<T> values;
	
	private static int length = 300;
	
	public SettingsItem(String name, List<T> values, int width, int height) {
		super(name, width, height);
		this.name = name;
		this.values = values;
	}
	
	@Override
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
		
		text.setText(name).setColor(color).setPosition(x, y).render();
		
		int xOffset = width - length;
		iconModel.position(new Vector(x + xOffset, y, 0)).position().xAdd(iconWidth / 2).yAdd(iconWidth / 2);
		iconModel.scale(-1 * iconWidth / 2).color(color).setTexture(iconTextures.get(Icon.Previous)).render();
		
		Text valueText = new Text(valueAsString());
		valueText.setSize(text.getSize());
		xOffset += length / 2 - valueText.getWidth() / 2;
		valueText.setColor(color).setPosition(x + xOffset, y).render();
		xOffset = width - iconWidth;
		iconModel.position(new Vector(x + xOffset, y, 0)).position().xAdd(iconWidth / 2).yAdd(iconWidth / 2);
		iconModel.scale(iconWidth / 2).color(color).setTexture(iconTextures.get(Icon.Next)).render();
	}
	
	public void setSelected(boolean selected, int x, int y) {
		if (!selected) return;
		
		if (inBounds(this.x + width - length, this.y, x, y)) {
			currentValueIndex--;
			if (currentValueIndex < 0) {
				currentValueIndex = values.size() - 1;
			}
		} else if (inBounds(this.x + width - iconWidth, this.y, x, y)) {
			currentValueIndex++;
			if (currentValueIndex > values.size() - 1) {
				currentValueIndex = 0;
			}
		}
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
