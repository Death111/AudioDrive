package audiodrive.model.track;

import java.util.ArrayList;
import java.util.List;

import audiodrive.audio.AnalyzedAudio;
import audiodrive.audio.AnalyzedChannel;
import audiodrive.model.geometry.Vector;
import audiodrive.utilities.Log;
import audiodrive.utilities.Range;

public class TrackGenerator {
	
	private int smoothing = 15;
	
	private double deltaX = 0.2;
	private double deltaY = 2.0;
	private double deltaZ = 1.0;
	
	private AnalyzedChannel mixed;
	private AnalyzedChannel left;
	private AnalyzedChannel right;
	
	public Track generate(AnalyzedAudio file) {
		mixed = file.getMix();
		Log.debug(Range.of(mixed.getThreshold()));
		left = file.getChannel(0);
		right = file.getChannel(1);
		List<Vector> vectorinates = calculate();
		Log.debug(vectorinates.size() + " vectorinates");
		return new Track(file, vectorinates, file.getDuration(), smoothing);
	}
	
	private List<Vector> calculate() {
		double max = mixed.getThreshold().stream().mapToDouble(v -> v).max().getAsDouble();
		List<Vector> vectorinates = new ArrayList<>();
		double x = 0;
		double y = 0;
		double z = 0;
		int index = 0;
		for (float value : mixed.getThreshold()) {
			if (index % smoothing == 0) {
				vectorinates.add(new Vector(x, y, z));
			}
			float difference = right.getThreshold().get(index) - left.getThreshold().get(index);
			int direction = Math.abs(difference) > 1 ? (int) Math.signum(difference) : 0;
			x += direction * deltaX;
			y += (0.5 - (value / max)) * deltaY;
			z += deltaZ;
			index++;
		}
		return vectorinates;
	}
	
}
