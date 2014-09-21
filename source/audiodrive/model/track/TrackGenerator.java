package audiodrive.model.track;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import audiodrive.AudioDrive;
import audiodrive.audio.AnalyzedAudio;
import audiodrive.audio.AnalyzedChannel;
import audiodrive.model.geometry.Vector;
import audiodrive.model.track.interpolation.CatmullRom;
import audiodrive.utilities.Arithmetic;
import audiodrive.utilities.Log;

public class TrackGenerator {
	
	private double deltaX = 3.0;
	private double deltaY = 2.0;
	private double deltaZ = 1.0;
	
	public Track generate(AnalyzedAudio audio) {
		Log.info("Generating track...");
		int smoothing = Math.max(10, AudioDrive.Settings.getInteger("track.smoothing"));
		AnalyzedChannel mixed = audio.getMix();
		AnalyzedChannel left = audio.getChannel(0);
		AnalyzedChannel right = audio.getChannel(1);
		
		// generate track vectorinates based on audio analyzation results
		List<Vector> vectorinates = new ArrayList<>();
		double x = 0;
		double y = 0;
		double z = 0;
		for (int iteration = 0; iteration < audio.getIterationCount(); iteration++) {
			if (iteration % smoothing == 0) {
				vectorinates.add(new Vector(x, y, z));
			}
			double direction = right.getSpectralSum().getClamped(iteration) - left.getSpectralSum().getClamped(iteration);
			x += direction * deltaX;
			y += (0.5 - mixed.getThreshold().getClamped(iteration)) * deltaY;
			z += deltaZ;
		}
		Log.debug(vectorinates.size() + " vectorinates");
		
		// generate spline by interpolating vectorinates
		List<Vector> spline = CatmullRom.interpolate(vectorinates, smoothing, CatmullRom.Type.Centripetal);
		double iterationRate = spline.size() / audio.getDuration();
		Log.debug(spline.size() + " spline points");
		
		// generate blocks based on spline length and audio analyzation results
		List<Block> blocks = new ArrayList<>();
		Block[] previous = new Block[3];
		// no blocks within 1 second from start and end
		int offset = (int) Math.round(iterationRate);
		// set minimum distance between successive blocks to 0.2 seconds
		int minumumDistance = (int) Math.round(iterationRate * 0.2);
		for (int iteration = offset; iteration < spline.size() - offset; iteration++) {
			double intensity = mixed.getSpectralSum().getClamped(iteration);
			double calmness = 1 - intensity;
			double leftFlux = left.getSpectralFlux().getClamped(iteration);
			double rightFlux = right.getSpectralFlux().getClamped(iteration);
			double leftPeak = left.getPeaks().getClamped(iteration);
			double rightPeak = right.getPeaks().getClamped(iteration);
			double threshold = 0.25 + calmness * 0.2; // calm music -> fewer blocks
			if (leftFlux + leftPeak < threshold && rightFlux + rightPeak < threshold) continue;
			int rail = (int) Math.signum(Arithmetic.significance(leftFlux - rightFlux, 0.05));
			int r = rail + 1;
			if (previous[r] != null && iteration - previous[r].iteration() < minumumDistance) continue;
			threshold = 0.4 + calmness * 0.2; // intense music -> more collectables, fewer obstacles
			boolean allPreviousAreObstacles = Arrays.stream(previous).filter(Objects::nonNull).noneMatch(Block::isCollectable);
			boolean collectable = allPreviousAreObstacles ? true : (leftFlux > threshold || rightFlux > threshold);
			blocks.add(previous[r] = new Block(collectable, iteration, rail));
		}
		Log.debug(blocks.size() + " blocks");
		
		Log.debug("Generation complete");
		return new Track(audio, spline, blocks, smoothing);
	}
}
