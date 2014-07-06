package audiodrive.ui.menu.item;

import org.lwjgl.opengl.GL11;

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
		// TODO make color settings better..
		if (hover) {
			color[0] = 0; // red
			color[1] = 1; // green
			color[2] = 0; // blue
			color[3] = .7f; // alpha
		} else {
			color[0] = 1; // red
			color[1] = 0; // green
			color[2] = 0; // blue
			color[3] = 0.5f; // alpha
		}
		if (selected) {
			color[0] = 1; // red
			color[1] = 1; // green
			color[2] = 0; // blue
			color[3] = 0.5f; // alpha
		}

		GL11.glColor4f(color[0], color[1], color[2], color[3]);
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glVertex2f(posX + 0f, posY + 0f);

		GL11.glVertex2f(posX + 0f, posY + FILECHOOSER_ITEM_HEIGHT);
		GL11.glVertex2f(posX + FILECHOOSER_ITEM_WIDTH, posY + FILECHOOSER_ITEM_HEIGHT);
		GL11.glVertex2f(posX + FILECHOOSER_ITEM_WIDTH, posY + 0f);
		GL11.glEnd();

		text.setPosition(posX, posY).render();
	}
}
