package audiodrive.audio;

import java.util.List;

public class AnalyzedChannel extends DecodedChannel {
	
	private final List<float[]> spectra;
	private final AnalyzationData spectralSum;
	private final AnalyzationData spectralFlux;
	private final AnalyzationData threshold;
	private final AnalyzationData prunnedSpectralFlux;
	private final AnalyzationData peaks;
	
	AnalyzedChannel(Channel channel,
					List<float[]> spectra,
					AnalyzationData spectralSum,
					AnalyzationData spectralFlux,
					AnalyzationData threshold,
					AnalyzationData prunnedSpectralFlux,
					AnalyzationData peaks) {
		super(channel);
		this.spectra = spectra;
		this.spectralSum = spectralSum;
		this.spectralFlux = spectralFlux;
		this.threshold = threshold;
		this.prunnedSpectralFlux = prunnedSpectralFlux;
		this.peaks = peaks;
	}
	
	/**
	 * Returns a list containing the spectrum for each iteration of samples.
	 */
	public List<float[]> getSpectra() {
		return spectra;
	}
	
	/**
	 * Returns the spectrum for a given iteration index.
	 */
	public float[] getSpectrum(int index) {
		return spectra.get(index);
	}
	
	/**
	 * Returns a list containing the spectral sum for each iteration of samples.
	 */
	public AnalyzationData getSpectralSum() {
		return spectralSum;
	}
	
	/**
	 * Returns a list containing the spectral flux for each iteration of samples.
	 */
	public AnalyzationData getSpectralFlux() {
		return spectralFlux;
	}
	
	/**
	 * Returns a list containing the prunned spectral flux for each iteration of samples.
	 */
	public AnalyzationData getPrunnedSpectralFlux() {
		return prunnedSpectralFlux;
	}
	
	/**
	 * Returns a list containing the threshold for each iteration of samples.
	 */
	public AnalyzationData getThreshold() {
		return threshold;
	}
	
	/**
	 * Returns a list containing the peak for each iteration of samples. A peak value of 0 indicates that there was no peak.
	 */
	public AnalyzationData getPeaks() {
		return peaks;
	}
	
}