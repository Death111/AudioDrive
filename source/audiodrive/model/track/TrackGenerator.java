package audiodrive.model.track;

import java.util.ArrayList;
import java.util.List;

import audiodrive.audio.AnalyzedAudio;
import audiodrive.audio.AnalyzedChannel;
import audiodrive.model.geometry.Vector;
import audiodrive.utilities.Log;

public class TrackGenerator {
	
	private int smoothing = 15;
	
	private double deltaX = 0.2;
	private double deltaY = 2.0;
	private double deltaZ = 1.0;
	
	public Track generate(AnalyzedAudio audio) {
		AnalyzedChannel mixed = audio.getMix();
		AnalyzedChannel left = audio.getChannel(0);
		AnalyzedChannel right = audio.getChannel(1);
		
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
			y += (0.5 - mixed.getThreshold().clamp(value)) * deltaY;
			z += deltaZ;
			index++;
		}
		
		Log.debug(vectorinates.size() + " vectorinates");
		return new Track(audio, vectorinates, smoothing);
	}
	
}
