package audiodrive.model;

import java.util.List;

import audiodrive.audio.AudioAnalyzer.AnalyzedAudio;
import audiodrive.model.geometry.Vector;

public class Track {
	
	private AnalyzedAudio file;
	private List<Vector> vectors;
	private double duration;
	private int smoothing;
	
	public Track(AnalyzedAudio file, List<Vector> vectors, double duration, int smoothing) {
		this.file = file;
		this.vectors = vectors;
		this.duration = duration;
		this.smoothing = smoothing;
	}
	
	public AnalyzedAudio getFile() {
		return file;
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
