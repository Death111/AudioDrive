package audiodrive.model;

import java.util.List;

import audiodrive.audio.AudioAnalyzer;
import audiodrive.model.geometry.Vector;

public class Track {
	
	private List<Vector> vectors;
	private double duration;
	private int smoothing;
	private AudioAnalyzer analyzer;
	
	public Track(List<Vector> vectors, double duration, int smoothing, AudioAnalyzer analyzation) {
		this.vectors = vectors;
		this.duration = duration;
		this.smoothing = smoothing;
		this.analyzer = analyzation;
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
	
	public AudioAnalyzer getAnalyzer() {
		return analyzer;
	}
	
}
