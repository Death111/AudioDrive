package audiodrive.model;

import java.util.List;

import audiodrive.audio.AnalyzedAudio;
import audiodrive.model.geometry.Vector;

public class Track {
	
	private AnalyzedAudio audio;
	private List<Vector> vectors;
	private double duration;
	private int smoothing;
	
	public Track(AnalyzedAudio audio, List<Vector> vectors, double duration, int smoothing) {
		this.audio = audio;
		this.vectors = vectors;
		this.duration = duration;
		this.smoothing = smoothing;
	}
	
	public AnalyzedAudio getAudio() {
		return audio;
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
