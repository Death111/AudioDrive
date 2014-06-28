package audiodrive.audio;

import java.util.Collections;
import java.util.List;

public class AnalyzedChannel extends DecodedChannel {
	
	private final List<float[]> spectra;
	private final List<Float> spectralSum;
	private final List<Float> spectralFlux;
	private final List<Float> threshold;
	private final List<Float> prunnedSpectralFlux;
	private final List<Float> peaks;
	
	AnalyzedChannel(Channel channel,
					List<float[]> spectra,
					List<Float> spectralSum,
					List<Float> spectralFlux,
					List<Float> threshold,
					List<Float> prunnedSpectralFlux,
					List<Float> peaks) {
		super(channel);
		this.spectra = Collections.unmodifiableList(spectra);
		this.spectralSum = Collections.unmodifiableList(spectralSum);
		this.spectralFlux = Collections.unmodifiableList(spectralFlux);
		this.threshold = Collections.unmodifiableList(threshold);
		this.prunnedSpectralFlux = Collections.unmodifiableList(prunnedSpectralFlux);
		this.peaks = Collections.unmodifiableList(peaks);
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
	public List<Float> getSpectralSum() {
		return spectralSum;
	}
	
	/**
	 * Returns a list containing the spectral flux for each iteration of samples.
	 */
	public List<Float> getSpectralFlux() {
		return spectralFlux;
	}
	
	/**
	 * Returns a list containing the prunned spectral flux for each iteration of samples.
	 */
	public List<Float> getPrunnedSpectralFlux() {
		return prunnedSpectralFlux;
	}
	
	/**
	 * Returns a list containing the threshold for each iteration of samples.
	 */
	public List<Float> getThreshold() {
		return threshold;
	}
	
	/**
	 * Returns a list containing the peak for each iteration of samples. A peak value of 0 indicates that there was no peak.
	 */
	public List<Float> getPeaks() {
		return peaks;
	}
	
}