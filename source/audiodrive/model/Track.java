package audiodrive.model;

import java.util.List;

import audiodrive.model.geometry.Vector;

public class Track {

	private List<Vector> vectors;
	private double duration;
	private int smoothing;

	public Track(List<Vector> vectors, double duration, int smoothing) {
		this.vectors = vectors;
		this.duration = duration;
		this.smoothing = smoothing;
	}
	
	public List<Vector> getVectors() {
		return vectors;
	}
	
	public double getDuration() {
		return duration;
	}
	
	public int getSmoothing() {
		return smoothing;
	}

}
