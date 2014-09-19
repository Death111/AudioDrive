package audiodrive.ui;

import static org.lwjgl.opengl.GL11.*;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.Display;

import audiodrive.model.geometry.Color;
import audiodrive.model.geometry.Vector;
import audiodrive.model.track.Track;
import audiodrive.model.track.Track.Index;
import audiodrive.utilities.Arithmetic;
import audiodrive.utilities.Log;

/**
 * This class generates a height-map of a track and displays it
 * 
 * TODO use vbo
 * 
 * @author Death
 *
 */
public class TrackOverview {
	private List<Double> yCoordinates = new ArrayList<Double>();
	private List<Double> rawCoordinates;
	private int width = 200, height = 100;
	private int posX = Display.getWidth() - width - 20, posY = 20;
	// holds current playerIndex of original list
	private int playerIndex;

	public TrackOverview(Track track) {
		final List<Vector> vectors = track.spline();

		rawCoordinates = new ArrayList<Double>();
		List<Double> scaledCoordinates = new ArrayList<Double>();
		double maxValue = 0;
		double minValue = 0;

		for (Vector vector : vectors) {

			final double value = vector.y();

			if (value > maxValue) {
				maxValue = value;
			} else if (value < minValue) {
				minValue = value;
			}

			rawCoordinates.add(value);
		}

		// Scale values to range 0 - height
		for (Double currentValue : rawCoordinates) {
			double value = Arithmetic.scaleLinear(currentValue, 0, height, minValue, maxValue);
			scaledCoordinates.add(value);
		}

		// Compress values in width to 0 - width
		int size = rawCoordinates.size();

		// TODO fix to work with short songs (where size is < width)
		int compressFactor = (size > width) ? size / width : 1;

		for (int i = 0; i < size / compressFactor; i++) {
			double average = 0;
			for (int j = i * compressFactor; j < (i + 1) * compressFactor; j++) {
				if (scaledCoordinates.size() <= j) {
					Log.debug("att" + j);
					continue;
				}
				average += scaledCoordinates.get(j);
			}
			average /= compressFactor;
			yCoordinates.add(average);
		}

		Log.debug("size: " + yCoordinates.size());
	}

	public void render() {
		// Draw track
		Color.White.gl();
		glBegin(GL_LINE_STRIP);
		{
			for (int i = 0; i < yCoordinates.size(); i++) {
				Double value = yCoordinates.get(i);
				glVertex2d(posX + i, posY + height - value);
			}
		}
		glEnd();

		// Draw player position on screen
		Color.White.clone().alpha(.9).gl();
		// glBegin(GL_LINES);
		// {
		// final int index = indexToCompressedIndex(playerIndex);
		// glVertex2d(posX + index, posY + height);
		// glVertex2d(posX + index, posY);
		// }
		// glEnd();

		glPointSize(6);
		glBegin(GL_POINTS);
		{
			final int index = indexToCompressedIndex(playerIndex);
			glVertex2d(posX + index, posY + height - yCoordinates.get(index));
		}
		glEnd();
	}

	/**
	 * Returns appropriate index in compressed data
	 * 
	 * @param index
	 * @return
	 */
	private int indexToCompressedIndex(int index) {
		int size = rawCoordinates.size();

		// TODO wont work with short songs
		int compressFactor = (size > width) ? size / width : 1;

		for (int i = 0; i < size / compressFactor; i++) {
			for (int j = i * compressFactor; j < (i + 1) * compressFactor; j++) {
				if (index == j) {
					return i;
				}
			}
		}

		// Couldnt get valid index; returning last valid
		return yCoordinates.size() - 1;
	}

	public void updatePlayerPosition(Index index) {
		playerIndex = index.integer;
	}
}
