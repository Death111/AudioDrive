package audiodrive.audio;

import java.util.ArrayList;
import java.util.List;

/**
 * Can be used to retrieve the min, max and middle value of a single band
 * 
 * @author Death
 *
 */
public class SpectraMinMax {
	
	public static List<MinMax> getMinMax(AnalyzedChannel channel) {
		final List<float[]> spectra = channel.getSpectra();
		List<MinMax> minMax = new ArrayList<>(spectra.get(0).length);
		
		final int size = spectra.get(0).length;
		
		// loop through all bands
		for (int band = 0; band < size; band++) {
			double min = Double.MAX_VALUE;
			double max = Double.MIN_VALUE;
			double middle = Double.MAX_VALUE;
			// loop through all spectra
			for (int spectrumNumber = 0; spectrumNumber < spectra.size(); spectrumNumber++) {
				float[] currentSpectrum = spectra.get(spectrumNumber);
				float f = currentSpectrum[band];
				middle += f;
				if (f > max) max = f;
				if (f < min) min = f;
			}
			
			middle /= size;
			minMax.add(new MinMax(min, max, middle));
		}
		
		return minMax;
	}
	
}
