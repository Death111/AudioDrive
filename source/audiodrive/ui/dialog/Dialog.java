package audiodrive.ui.dialog;

import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.opengl.Display;

import audiodrive.model.geometry.Color;
import audiodrive.ui.components.Text;
import audiodrive.ui.components.Text.Alignment;
import audiodrive.ui.control.Input;
import audiodrive.ui.menu.Menu;
import audiodrive.ui.menu.item.Item;
import audiodrive.ui.menu.item.Item.Icon;
import audiodrive.ui.menu.item.ItemListener;
import audiodrive.ui.menu.item.MenuItem;

public class Dialog implements ItemListener {
	
	public static enum DialogType {
		CONFIRM, YES_NO
	}
	
	public static enum DialogAnswer {
		CONFIRM, YES, NO
	}
	
	final Item yesItem = new MenuItem("Yes", this).setIcon(Icon.Yes);
	final Item noItem = new MenuItem("No", this).setIcon(Icon.No);
	final Item confirmItem = new MenuItem("Ok", this).setIcon(Icon.Confirm);
	
	DialogType type;
	
	int width, height;
	private Text text;
	private Menu menu;
	
	int dialogWidth;
	int dialogHeight;
	private int centerX;
	private int centerY;
	
	public DialogAnswer answer = null;
	
	public Dialog(String text, DialogType type) {
		width = Display.getWidth();
		height = Display.getHeight();
		
		centerX = width / 2;
		centerY = height / 2;
		this.text = new Text(text).setSize(47).setAlignment(Alignment.Center);
		this.type = type;
		dialogWidth = Math.max(MenuItem.MENU_ITEM_WIDTH * 2, this.text.getWidth() + 50);
		final int menuItemHeight = 51;
		dialogHeight = this.text.getHeight() + 100 + menuItemHeight;
		
		this.text.setPosition(centerX, centerY);
		
		menu = new Menu(centerX - dialogWidth / 2, centerY + dialogHeight / 2 - menuItemHeight, dialogWidth, menuItemHeight, 0);
		
		switch (type) {
		case CONFIRM:
			menu.addItem(confirmItem);
			break;
		case YES_NO:
			menu.addItem(yesItem);
			menu.addItem(noItem);
			break;
		}
	}
	
	public void reset() {
		answer = null;
		confirmItem.setSelected(false, 0, 0);
		yesItem.setSelected(false, 0, 0);
		noItem.setSelected(false, 0, 0);
	}
	
	public Dialog activate() {
		Input.addObservers(menu);
		return this;
	}
	
	public void render() {
		drawBackgroundOverlay();
		drawDialog();
	}
	
	private void drawDialog() {
		drawQuad(centerX - dialogWidth / 2, centerY - 100, dialogWidth, 200, GL_LINE, Color.White);
		text.render();
		menu.render();
	}
	
	private void drawBackgroundOverlay() {
		drawQuad(0, 0, width, height, GL_FILL, Color.Black.alpha(.7));
	}
	
	private void drawQuad(float posX, float posY, float width, float height, int style, Color color) {
		color.gl();
		glPolygonMode(GL_FRONT, style);
		glBegin(GL_QUADS);
		{
			glVertex2f(posX, posY);
			glVertex2f(posX, posY + height);
			glVertex2f(posX + width, posY + height);
			glVertex2f(posX + width, posY);
		}
		glEnd();
	}
	
	@Override
	public void onHover(Item item, boolean hover) {
		
	}
	
	@Override
	public void onSelect(Item item, boolean select) {
		if (!select) return;
		
		if (item == confirmItem) {
			answer = DialogAnswer.CONFIRM;
		} else if (item == noItem) {
			answer = DialogAnswer.NO;
		} else if (item == yesItem) {
			answer = DialogAnswer.YES;
		}
		
		Input.removeObservers(menu);
	}
	
	public void cancel() {
		onSelect(noItem, true);
	}
	
	public void confirm() {
		onSelect((type == DialogType.CONFIRM) ? confirmItem : yesItem, true);
	}
}
