package audiodrive.ui.menu.item;

import org.lwjgl.opengl.GL11;

import audiodrive.model.geometry.Color;

public class FileChooserItem extends Item {

	private float[] color = { 1, 0, 0, .5f };

	public final static int FILECHOOSER_ITEM_WIDTH = 350;
	public final static int FILECHOOSER_ITEM_HEIGHT = 25;

	private boolean isDirectory;

	/**
	 * 
	 * @param text
	 *            Text of the item
	 * @param itemListener
	 * @param isDirectory
	 */
	public FileChooserItem(String text, boolean isDirectory, ItemListener itemListener) {
		super(text, FILECHOOSER_ITEM_WIDTH, FILECHOOSER_ITEM_HEIGHT);
		this.isDirectory = isDirectory;
		this.addItemListener(itemListener);
	}

	/**
	 * 
	 * @param text
	 *            Text of the item
	 * @param itemListener
	 *            An item Listener
	 */
	public FileChooserItem(String text, ItemListener itemListener) {
		super(text, FILECHOOSER_ITEM_WIDTH, FILECHOOSER_ITEM_HEIGHT);
		this.addItemListener(itemListener);
	}

	@Override
	public void render() {
		final Color itemColor;

		if (selected) {
			itemColor = Color.YELLOW().a(0.5);
		} else if (hover) {
			itemColor = Color.GREEN().a(0.5);
		} else if (isDirectory) {
			itemColor = Color.TUERKIS().a(0.5);
		} else {
			itemColor = Color.RED().a(0.5);
		}

		GL11.glColor4d(itemColor.r, itemColor.g, itemColor.b, itemColor.a);
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glVertex2f(posX + 0f, posY + 0f);

		GL11.glVertex2f(posX + 0f, posY + FILECHOOSER_ITEM_HEIGHT);
		GL11.glVertex2f(posX + FILECHOOSER_ITEM_WIDTH, posY + FILECHOOSER_ITEM_HEIGHT);
		GL11.glVertex2f(posX + FILECHOOSER_ITEM_WIDTH, posY + 0f);
		GL11.glEnd();

		text.setPosition(posX, posY).render();
	}

	/**
	 * @return the isDirectory
	 */
	public final boolean isDirectory() {
		return isDirectory;
	}
}
