package audiodrive.utilities;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import audiodrive.Resources;
import audiodrive.model.geometry.Matrix;
import audiodrive.model.geometry.Vector;
import audiodrive.ui.components.Camera;

public class CameraPath {
	
	private double unitsPerSecond;
	private int sourceWidth;
	private int sourceHeight;
	private int sourcePixelAspectRatio;
	private int compPixelAspectRatio;
	
	private int frameCount;
	private List<Vector> positions = null;
	private List<Vector> pointOfInterests = null;
	private List<Vector> orientations = null;
	
	private Vector offsetLookAt;
	private Vector offsetPosition;
	
	double start = Double.NaN;
	
	/**
	 * Creates a camera path from the given filename
	 * 
	 * @param filePath Path to camera file
	 * @param startsWithFirst if true will subtract all coordinates with the first vector, otherwise with the last
	 */
	public CameraPath(String filePath, boolean startsWithFirst) {
		final File file = Resources.getFile(filePath);
		
		try {
			FileInputStream fstream = new FileInputStream(file);
			
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String currentLine;
			
			// Loop through file line by line
			while ((currentLine = br.readLine()) != null) {
				String[] tokens = currentLine.split("\\t");
				
				if (tokens.length == 0) continue;
				
				if (tokens[0].equals("Transform")) {
					if (tokens[1].equals("Position")) {
						positions = parseVectors(br);
					} else if (tokens[1].equals("Point of Interest")) {
						pointOfInterests = parseVectors(br);
					} else if (tokens[1].equals("Orientation")) {
						orientations = parseVectors(br);
					}
				}
				if (tokens.length < 2) continue;
				
				switch (tokens[1]) {
				case "Units Per Second":
					this.unitsPerSecond = Double.parseDouble(tokens[2]);
					break;
				case "Source Width":
					this.sourceWidth = Integer.parseInt(tokens[2]);
					break;
				case "Source Height":
					this.sourceHeight = Integer.parseInt(tokens[2]);
					break;
				case "Source Pixel Aspect Ratio":
					this.sourcePixelAspectRatio = Integer.parseInt(tokens[2]);
					break;
				case "Comp Pixel Aspect Ratio":
					this.compPixelAspectRatio = Integer.parseInt(tokens[2]);
					break;
				}
				
			}
			
			fstream.close();
			in.close();
			br.close();
			
		} catch (Exception e) {
			Log.error(e);
			throw new RuntimeException("Error while parsing cameraPath: '" + filePath + "'", e);
		}
		
		// Calculate point of interests from orientation
		if (orientations != null) {
			pointOfInterests = new ArrayList<Vector>();
			for (int i = 0; i < orientations.size(); i++) {
				Vector orientation = orientations.get(i);
				Matrix m = new Matrix();
				m.rotation(orientation.x(), orientation.y(), orientation.z());
				final Vector vector2 = positions.get(i);
				Vector vector = m.multiplied(vector2).subtract(vector2);
				pointOfInterests.add(vector);
			}
		}
		
		frameCount = Math.min(positions.size(), pointOfInterests.size());
		int index = (startsWithFirst) ? 0 : frameCount - 1;
		resetToFirst(positions, index);
		resetToFirst(pointOfInterests, index);
		
		Log.debug("Loaded cameraPath('%s'): %s fps, %s frames.", filePath, unitsPerSecond, frameCount);
		
	}
	
	public CameraPath setOffsets(Vector offsetPosition, Vector offsetLookAt) {
		this.offsetLookAt = offsetLookAt;
		this.offsetPosition = offsetPosition;
		return this;
	}
	
	/**
	 * Call to set camera appropriate to current frame-position
	 */
	public void camera() {
		Vector cameraPosition = null;
		Vector cameraLookAt = null;
		
		if (isFinished()) {
			start = System.currentTimeMillis() / 1000.0;
		}
		
		final double d = System.currentTimeMillis() / 1000.0 - start;
		int index = (int) (d * unitsPerSecond);
		if (index >= frameCount) {
			cameraPosition = positions.get(frameCount - 1).clone();
			cameraLookAt = pointOfInterests.get(frameCount - 1).clone();
			start = Double.NaN;
		} else {
			cameraPosition = positions.get(index).clone();
			cameraLookAt = pointOfInterests.get(index).clone();
		}
		
		final Vector up = Vector.Y;// TODO calculate up vector
		Camera.position(cameraPosition.add(offsetPosition));
		Camera.lookAt(cameraLookAt.add(offsetLookAt), up);
	}
	
	public boolean isFinished() {
		return Double.isNaN(start);
	}
	
	private void resetToFirst(List<Vector> list, int offsetIndex) {
		final int size = list.size();
		if (list == null || size == 0) return;
		Vector offsetVector = list.get(offsetIndex).clone();
		for (int i = 0; i < size; i++) {
			final Vector vector = list.get(i);
			vector.x(vector.x() * -1);
			vector.y(vector.y() * -1);
			vector.subtract(offsetVector);
		}
	}
	
	private List<Vector> parseVectors(BufferedReader br) throws IOException {
		String currentLine;
		boolean parsingFrames = false;
		
		List<Vector> vectorList = new ArrayList<Vector>();
		
		// Loop through file line by line
		while ((currentLine = br.readLine()) != null) {
			String[] tokens = currentLine.split("\\t");
			
			if (tokens.length == 1) return vectorList;
			
			if (tokens[1].equals("Frame")) {
				parsingFrames = true;
				continue;
			}
			
			if (!parsingFrames) System.out.println("idk what im doing");
			
			double x = Double.parseDouble(tokens[2]);
			double y = Double.parseDouble(tokens[3]);
			double z = Double.parseDouble(tokens[4]);
			
			vectorList.add(new Vector(x, y, z));
		}
		
		return vectorList;
	}
}
