package audiodrive.model.track;

import java.util.ArrayList;
import java.util.List;

import audiodrive.audio.AudioAnalyzer.AnalyzedChannel;
import audiodrive.audio.AudioAnalyzer.AnalyzedAudio;
import audiodrive.model.Track;
import audiodrive.model.geometry.Vector;
import audiodrive.utilities.Log;

public class TrackGenerator {
	
	private int smoothing;
	private boolean useAverage = false;
	private AnalyzedChannel mixed;
	private AnalyzedChannel left;
	private AnalyzedChannel right;
	
	public Track generate(AnalyzedAudio file, int smoothing) {
		this.smoothing = smoothing;
		mixed = file.mixed;
		left = file.channels.get(0);
		right = file.channels.get(1);
		List<Vector> vectorinates = useAverage ? calculateUsingAverage() : calculate();
		Log.debug(vectorinates.size() + " vectorinates");
		return new Track(file, vectorinates, file.duration, smoothing);
	}
	
	private List<Vector> calculate() {
		double max = mixed.threshold.stream().mapToDouble(v -> v).max().getAsDouble();
		List<Vector> vectorinates = new ArrayList<>();
		double x = 0;
		double y = 0;
		double z = 0;
		int index = 0;
		for (float value : mixed.threshold) {
			if (index % smoothing == 0) {
				vectorinates.add(new Vector(x, y, z));
			}
			// float difference = right.threshold.get(index) - left.threshold.get(index);
			float difference = right.spectralSum.get(index) - left.spectralSum.get(index);
			int direction = Math.abs(difference) > 100 ? (int) Math.signum(difference) : 0;
			x += 0.005;
			y += (0.5 - (value / max)) * 0.005;
			z += direction * 0.005;
			index++;
		}
		return vectorinates;
	}
	
	private List<Vector> calculateUsingAverage() {
		double max = mixed.threshold.stream().mapToDouble(v -> v).max().getAsDouble();
		List<Vector> vectorinates = new ArrayList<>();
		double x = 0;
		double y = 0;
		double z = 0;
		for (int i = 0; i < mixed.threshold.size(); i += smoothing) {
			double xi = 0;
			double yi = 0;
			double zi = 0;
			vectorinates.add(new Vector(x, y, z));
			for (int j = 0; j < smoothing; j++) {
				int index = i + j;
				if (index >= mixed.threshold.size()) return vectorinates;
				double value = mixed.threshold.get(i + j);
				int direction = left.threshold.get(index) > right.threshold.get(index) ? 1 : -1;
				xi += 0.005;
				yi += (0.5 - (value / max)) * 0.01;
				zi += direction * 0.01;
			}
			x += xi / smoothing;
			y += yi / smoothing;
			z += zi / smoothing;
		}
		return vectorinates;
	}
	
	public int getSmoothing() {
		return smoothing;
	}
	
}
