package audiodrive.audio;

import java.util.List;

import audiodrive.utilities.Arithmetic;

public class AnalyzedChannel extends DecodedChannel {
	
	private final List<float[]> spectra;
	private final List<AnalyzationData> bands;
	private final AnalyzationData spectralSum;
	private final AnalyzationData spectralFlux;
	private final AnalyzationData threshold;
	private final AnalyzationData prunnedSpectralFlux;
	private final AnalyzationData peaks;
	private final float maximum;
	
	AnalyzedChannel(Channel channel,
					List<float[]> spectra,
					List<AnalyzationData> bands,
					AnalyzationData spectralSum,
					AnalyzationData spectralFlux,
					AnalyzationData threshold,
					AnalyzationData prunnedSpectralFlux,
					AnalyzationData peaks) {
		super(channel);
		this.spectra = spectra;
		this.bands = bands;
		this.spectralSum = spectralSum;
		this.spectralFlux = spectralFlux;
		this.threshold = threshold;
		this.prunnedSpectralFlux = prunnedSpectralFlux;
		this.peaks = peaks;
		maximum = (float) bands.stream().mapToDouble(AnalyzationData::maximum).max().getAsDouble();
	}
	
	/**
	 * Returns a list containing the spectrum for each iteration of samples.
	 */
	public List<float[]> getSpectra() {
		return spectra;
	}
	
	/**
	 * Returns a list containing the separate frequency bands.
	 */
	public List<AnalyzationData> getBands() {
		return bands;
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
	
	/**
	 * Returns the minimum amplitude of this channel.
	 */
	public float getMinimum() {
		return 0;
	}
	
	/**
	 * Returns the maximum amplitude of this channel.
	 */
	public float getMaximum() {
		return maximum;
	}
	
	/**
	 * Clamps an absolute amplitude value to the range [0,1] proportional to the channels amplitude range.
	 */
	public float clamp(float amplitude) {
		return (float) Arithmetic.clamp(amplitude / getMaximum());
	}
	
}