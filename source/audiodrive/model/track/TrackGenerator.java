package audiodrive.model.track;

import java.util.ArrayList;
import java.util.List;

import audiodrive.AudioDrive;
import audiodrive.audio.AnalyzedAudio;
import audiodrive.audio.AnalyzedChannel;
import audiodrive.model.geometry.Vector;
import audiodrive.utilities.Arithmetic;
import audiodrive.utilities.Log;

public class TrackGenerator {
	
	private double deltaX = 0.2;
	private double deltaY = 2.0;
	private double deltaZ = 1.0;
	
	public Track generate(AnalyzedAudio audio) {
		int smoothing = Math.max(10, AudioDrive.Settings.getInteger("track.smoothing"));
		AnalyzedChannel mixed = audio.getMix();
		AnalyzedChannel left = audio.getChannel(0);
		AnalyzedChannel right = audio.getChannel(1);
		List<Vector> vectorinates = new ArrayList<>();
		double x = 0;
		double y = 0;
		double z = 0;
		int iterationCount = audio.getIterationCount();
		for (int iteration = 0; iteration < iterationCount; iteration++) {
			if (iteration % smoothing == 0) {
				vectorinates.add(new Vector(x, y, z));
			}
			float difference = right.getThreshold().get(iteration) - left.getThreshold().get(iteration);
			int direction = Math.abs(difference) > 1 ? (int) Math.signum(difference) : 0;
			x += direction * deltaX;
			y += (0.5 - mixed.getThreshold().getClamped(iteration)) * deltaY;
			z += deltaZ;
		}
		Log.debug(vectorinates.size() + " vectorinates");
		List<Block> blocks = new ArrayList<>();
		int offset = (int) audio.getIterationRate() / 4;
		Block last = null;
		// set minimum distance between successive blocks to 0.1 seconds
		int minumumDistance = (int) (audio.getIterationRate() * 0.1);
		for (int iteration = offset; iteration < iterationCount - offset; iteration++) {
			double intensity = mixed.getSpectralSum().getClamped(iteration);
			double calmness = 1 - intensity;
			double leftFlux = left.getSpectralFlux().getClamped(iteration);
			double rightFlux = left.getSpectralFlux().getClamped(iteration);
			double leftPeak = left.getPeaks().getClamped(iteration);
			double rightPeak = right.getPeaks().getClamped(iteration);
			double threshold = 0.2 + calmness * 0.1; // calm music -> fewer blocks
			if (leftFlux + leftPeak < threshold && rightFlux + leftPeak < threshold) continue;
			int rail = (int) Math.signum(Arithmetic.significance(leftPeak - rightPeak, 0.01));
			if (last != null && last.rail() == rail && iteration - last.iteration() < minumumDistance) continue;
			threshold = 0.3 + calmness * 0.2; // intense music -> more collectables, fewer obstacles
			boolean collectable = leftFlux > threshold || rightFlux > threshold;
			blocks.add(last = new Block(collectable, iteration, rail));
		}
		Log.debug(blocks.size() + " blocks");
		return new Track(audio, vectorinates, blocks, smoothing);
	}
	
}
