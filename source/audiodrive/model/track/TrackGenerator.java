package audiodrive.model.track;

import java.util.ArrayList;
import java.util.List;

import audiodrive.AudioDrive;
import audiodrive.audio.AnalyzedAudio;
import audiodrive.audio.AnalyzedChannel;
import audiodrive.audio.SpectraMinMax;
import audiodrive.model.geometry.Vector;
import audiodrive.model.tower.MusicTower;
import audiodrive.model.tower.RotationTower;
import audiodrive.model.tower.TubeTower;
import audiodrive.utilities.Log;

public class TrackGenerator {

	private double deltaX = 0.2;
	private double deltaY = 2.0;
	private double deltaZ = 1.0;

	public Track generate(AnalyzedAudio audio) {
		int smoothing = Math.max(10, AudioDrive.Settings.getInteger("smoothing"));
		AnalyzedChannel mixed = audio.getMix();
		AnalyzedChannel left = audio.getChannel(0);
		AnalyzedChannel right = audio.getChannel(1);
		List<Vector> vectorinates = new ArrayList<>();
		double x = 0;
		double y = 0;
		double z = 0;
		for (int iteration = 0; iteration < audio.getIterationCount(); iteration++) {
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
		for (int iteration = 0; iteration < audio.getIterationCount(); iteration++) {
			float leftPeak = left.getPeaks().getClamped(iteration);
			float rightPeak = right.getPeaks().getClamped(iteration);
			Integer rail = determineRail(leftPeak, rightPeak, 0.2);
			if (rail != null) {
				blocks.add(new Block(true, iteration, rail));
			} else {
				rail = determineRail(leftPeak, rightPeak, 0.1);
				if (rail != null)
					blocks.add(new Block(false, iteration, rail));
			}
		}
		Log.debug(blocks.size() + " blocks");

		List<MusicTower> musicTowers = new ArrayList<>();
		final int band = 1; // Use bass criteria
		final SpectraMinMax spectraMinMax = new SpectraMinMax(mixed, band);
		for (int iteration = 0; iteration < audio.getIterationCount(); iteration++) {
			final float f = mixed.getSpectrum(iteration)[band];
			// if current value is in the last 10th of the range put tower
			if (f > spectraMinMax.max - spectraMinMax.max / 10) {
				if (musicTowers.size() % 4 == 0)
					musicTowers.add(new RotationTower(iteration));
				else
					musicTowers.add(new TubeTower(iteration));
			}

		}
		Log.debug(musicTowers.size() + " musicTowers");
		return new Track(audio, vectorinates, blocks, musicTowers, smoothing);
	}

	private Integer determineRail(double left, double right, double threshold) {
		boolean leftSide = left > threshold;
		boolean rightSide = right > threshold;
		if (leftSide && rightSide)
			return 0;
		if (leftSide || rightSide)
			return leftSide ? -1 : 1;
		return null;
	}
}
