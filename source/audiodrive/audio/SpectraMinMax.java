package audiodrive.audio;

/**
 * Can be used to retrieve the min, max and middle value of a single band
 * 
 * @author Death
 *
 */
public class SpectraMinMax {

	public double max = Double.MIN_VALUE;
	public double min = Double.MAX_VALUE;
	public double middle = 0;

	public SpectraMinMax(AnalyzedChannel channel, int band) {
		double middle = 0;

		final int size = channel.getSpectra().size();
		for (int i = 0; i < size; i++) {
			final float[] spectrum = channel.getSpectrum(i);
			float f = spectrum[band];
			middle += f;
			if (f > max)
				max = f;
			else if (f < min)
				min = f;
		}

		middle /= size;
		this.middle = middle;
	}

}
