package audiodrive.ui.components;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_MULTISAMPLE;

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

import audiodrive.model.geometry.Color;

public class Text {
	
	public static final Font DefaultFont = new Font(Font.SANS_SERIF, Font.PLAIN, 15);
	private static final Map<Font, TrueTypeFont> fonts = new HashMap<>();
	
	public static enum Alignment {
		UpperLeft, UpperCenter, UpperRight, Left, Center, Right, LowerLeft, LowerCenter, LowerRight
	}
	
	public static void destroy() {
		fonts.clear();
	}
	
	private String text;
	private Color color = Color.White;
	private Font font;
	private double x, y;
	private Alignment alignment = Alignment.UpperLeft;
	private boolean visible = true;
	private int size;
	
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
		return this;
	}
	
	public Font getFont() {
		return font;
	}
	
	public Text setSize(int size) {
		this.size = size;
		setFont(font.deriveFont((float) size));
		return this;
	}
	
	public int getSize() {
		return this.size;
	}
	
	public Text setStyle(int style) {
		setFont(font.deriveFont(style));
		return this;
	}
	
	public Text setAlignment(Alignment alignment) {
		this.alignment = alignment;
		return this;
	}
	
	public Alignment getAlignment() {
		return alignment;
	}
	
	public int getWidth() {
		if (text == null) return 0;
		return getTrueTypeFont(font).getWidth(text);
	}
	
	public int getHeight() {
		return getTrueTypeFont(font).getHeight();
	}
	
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}
	
	public double getAlignedX() {
		switch (alignment) {
		case UpperLeft:
		case Left:
		case LowerLeft:
			return getX();
		case UpperCenter:
		case Center:
		case LowerCenter:
			return getX() - getWidth() / 2;
		case UpperRight:
		case Right:
		case LowerRight:
			return getX() - getWidth();
		}
		return 0;
	}
	
	public double getAlignedY() {
		switch (alignment) {
		case UpperLeft:
		case UpperCenter:
		case UpperRight:
			return getY();
		case Left:
		case Center:
		case Right:
			return getY() - getHeight() / 2;
		case LowerLeft:
		case LowerCenter:
		case LowerRight:
			return getY() - getHeight();
		}
		return 0;
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
	
	public Text setVisible(boolean visible) {
		this.visible = visible;
		return this;
	}
	
	public boolean isVisible() {
		return visible;
	}
	
	public void render() {
		if (!visible || text == null) return;
		glPolygonMode(GL_FRONT, GL_FILL);
		boolean antialiasing = Window.isAntialiasingEnabled();
		if (antialiasing) glDisable(GL_MULTISAMPLE);
		getTrueTypeFont(font).drawString((float) getAlignedX(), (float) getAlignedY(), text, converted(color));
		if (antialiasing) glEnable(GL_MULTISAMPLE);
		TextureImpl.bindNone();
	}
	
	private org.newdawn.slick.Color converted(Color color) {
		float[] rgba = color.toFloats();
		return new org.newdawn.slick.Color(rgba[0], rgba[1], rgba[2], rgba[3]);
	}
	
	@Override
	public String toString() {
		return text;
	}
	
	public static TrueTypeFont getTrueTypeFont(Font font) {
		TrueTypeFont trueTypeFont = fonts.get(font);
		if (trueTypeFont == null) {
			trueTypeFont = new TrueTypeFont(font, true);
			fonts.put(font, trueTypeFont);
		}
		return trueTypeFont;
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
