package audiodrive.ui.components;

import java.nio.IntBuffer;

import audiodrive.model.geometry.Vector;

public class Viewport {
	
	public final int x, y, width, height, xCenter, yCenter;
	
	public Viewport(IntBuffer buffer) {
		x = buffer.get(0);
		y = buffer.get(1);
		width = buffer.get(2);
		height = buffer.get(3);
		xCenter = x + width / 2;
		yCenter = y + height / 2;
	}
	
	public boolean contains(int x, int y) {
		return x >= this.x && x <= width && y >= this.y && y <= height;
	}
	
	public boolean contains(Vector vector) {
		return contains((int) vector.x(), (int) vector.y());
	}
	
}
