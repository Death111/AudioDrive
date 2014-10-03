package audiodrive.utilities;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import audiodrive.Resources;
import audiodrive.model.geometry.Matrix;
import audiodrive.model.geometry.Vector;
import audiodrive.ui.components.Camera;
import audiodrive.ui.components.Scene;

public class CameraPath {
	
	private double duration;
	private double frameRate;
	private int frameCount;
	private int sourceWidth;
	private int sourceHeight;
	private int sourcePixelAspectRatio;
	private int compPixelAspectRatio;
	private boolean reverse = false;
	
	private List<Vector> positions = null;
	private List<Vector> pointOfInterests = null;
	private List<Vector> ups = null;
	private List<Vector> orientations = null;
	
	private Vector offsetLookAt;
	private Vector offsetPosition;
	
	private double time;
	
	/**
	 * Creates a camera path from the given filename
	 * 
	 * @param filePath Path to camera file
	 * @param reverse if true, play the animation backwards
	 */
	public CameraPath(String filePath, boolean startsWithFirst) {
		
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new DataInputStream(Resources.get(filePath).openStream())))) {
			String currentLine;
			
			// Loop through file line by line
			while ((currentLine = reader.readLine()) != null) {
				String[] tokens = currentLine.split("\\t");
				
				if (tokens.length == 0) continue;
				
				if (tokens[0].equals("Transform")) {
					if (tokens[1].equals("Position")) {
						positions = parseVectors(reader);
					} else if (tokens[1].equals("Point of Interest")) {
						pointOfInterests = parseVectors(reader);
					} else if (tokens[1].equals("Orientation")) {
						orientations = parseVectors(reader);
					}
				}
				if (tokens.length < 2) continue;
				
				switch (tokens[1]) {
				case "Units Per Second":
					frameRate = Double.parseDouble(tokens[2]);
					break;
				case "Source Width":
					sourceWidth = Integer.parseInt(tokens[2]);
					break;
				case "Source Height":
					sourceHeight = Integer.parseInt(tokens[2]);
					break;
				case "Source Pixel Aspect Ratio":
					sourcePixelAspectRatio = Integer.parseInt(tokens[2]);
					break;
				case "Comp Pixel Aspect Ratio":
					compPixelAspectRatio = Integer.parseInt(tokens[2]);
					break;
				}
				
			}
		} catch (Exception e) {
			Log.error(e);
			throw new RuntimeException("Error while parsing cameraPath: '" + filePath + "'", e);
		}
		
		// Calculate point of interests from orientation
		if (orientations != null) {
			pointOfInterests = new ArrayList<Vector>();
			ups = new ArrayList<>();
			for (int i = 0; i < orientations.size(); i++) {
				Vector orientation = orientations.get(i);
				Matrix m = new Matrix();
				m.rotation(orientation.x(), orientation.y(), orientation.z());
				Vector up = m.multiplied(Vector.Y);
				final Vector position = positions.get(i);
				Vector vector = m.multiplied(position).subtract(position);
				pointOfInterests.add(vector);
				ups.add(up);
			}
		}
		
		frameCount = Math.min(positions.size(), pointOfInterests.size());
		duration = frameCount / frameRate;
		
		int index = (startsWithFirst) ? 0 : frameCount - 1;
		resetToFirst(positions, index);
		resetToFirst(pointOfInterests, index);
		
		Log.debug("Loaded cameraPath('%s'): %s fps, %s frames, %.1f seconds.", filePath, frameRate, frameCount, duration);
		
		reset();
	}
	
	public CameraPath setOffsets(Vector offsetPosition, Vector offsetLookAt) {
		this.offsetPosition = offsetPosition.clone();
		this.offsetLookAt = offsetLookAt.clone();
		return this;
	}
	
	/**
	 * Call to set camera appropriate to current frame-position
	 */
	public void camera() {
		if (isFinished()) return;
		time += Scene.deltaTime();
		
		int index = Arithmetic.clamp((int) Math.round(time * frameRate), 0, frameCount - 1);
		if (reverse) index = frameCount - 1 - index;
		Vector cameraPosition = positions.get(index);
		Vector cameraLookAt = pointOfInterests.get(index);
		Vector up = ups.get(index);
		
		Camera.position(offsetPosition.plus(cameraPosition));
		Camera.lookAt(offsetLookAt.plus(cameraLookAt), up);
	}
	
	public boolean isFinished() {
		return time >= duration;
	}
	
	public boolean isSkippable() {
		return time < duration - 0.8;
	}
	
	public void skip() {
		time = duration - 0.8;
	}
	
	public void reset() {
		time = 0;
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
