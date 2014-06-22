package audiodrive.ui.components;

import static org.lwjgl.opengl.GL11.*;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.opengl.TextureImpl;
import org.newdawn.slick.util.ResourceLoader;

public class Text {
	
	public static final Font DefaultFont = new Font(Font.SANS_SERIF, Font.PLAIN, 15);
	public static final Map<Font, TrueTypeFont> fonts = new HashMap<>();
	
	private String text;
	private Color color = Color.white;
	private Font font;
	private double x, y;
	private boolean centered;
	
	public Text() {
		setFont(DefaultFont);
	}
	
	public Text(String text) {
		this();
		this.text = text;
	}
	
	public Text setText(String text) {
		this.text = text;
		return this;
	}
	
	public String getText() {
		return text;
	}
	
	public Text setColor(Color color) {
		this.color = color;
		return this;
	}
	
	public Color getColor() {
		return color;
	}
	
	public Text setFont(String name) {
		setFont(getFont(name));
		return this;
	}
	
	public Text setFont(Font font) {
		this.font = font;
		if (!fonts.containsKey(font)) fonts.put(font, new TrueTypeFont(font, true));
		TextureImpl.bindNone();
		return this;
	}
	
	public Font getFont() {
		return font;
	}
	
	public Text setSize(int size) {
		setFont(font.deriveFont((float) size));
		return this;
	}
	
	public Text setStyle(int style) {
		setFont(font.deriveFont(style));
		return this;
	}
	
	public int getWidth() {
		if (text == null) return 0;
		return fonts.get(font).getWidth(text);
	}
	
	public int getHeight() {
		return fonts.get(font).getHeight();
	}
	
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}
	
	public Text setX(double x) {
		this.x = x;
		return this;
	}
	
	public Text setY(double y) {
		this.y = y;
		return this;
	}
	
	public Text setPosition(double x, double y) {
		this.x = x;
		this.y = y;
		return this;
	}
	
	public Text setCentered(double x, double y) {
		this.x = x;
		this.y = y;
		centered = true;
		return this;
	}
	
	public Text setCentered(boolean centered) {
		this.centered = centered;
		return this;
	}
	
	public void render() {
		if (text == null) return;
		glPolygonMode(GL_FRONT, GL_FILL);
		float x = (float) (centered ? getX() - getWidth() / 2 : getX());
		float y = (float) (centered ? getY() - getHeight() / 2 : getY());
		fonts.get(font).drawString(x, y, text, new org.newdawn.slick.Color(color.getRGB()));
		TextureImpl.bindNone();
	}
	
	@Override
	public String toString() {
		return text;
	}
	
	public static Font getFont(String name) {
		if (getAvailableFonts().contains(name)) {
			return new Font(name, DefaultFont.getStyle(), DefaultFont.getSize());
		} else {
			try {
				InputStream inputStream = ResourceLoader.getResourceAsStream("fonts/" + name + ".ttf");
				Font font = Font.createFont(Font.TRUETYPE_FONT, inputStream).deriveFont(DefaultFont.getSize2D());
				GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
				return font;
			} catch (FontFormatException | IOException exception) {
				throw new RuntimeException(exception);
			}
		}
	}
	
	public static List<String> getAvailableFonts() {
		return Arrays.asList(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
	}
	
}
