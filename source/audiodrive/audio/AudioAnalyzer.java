package audiodrive.audio;

import java.util.ArrayList;
import java.util.List;

import audiodrive.audio.Samples.Channel;
import audiodrive.audio.analysis.FFT;

public class AudioAnalyzer {
	
	private int thresholdWindowSize = 20;
	private float thresholdMultiplier = 1.8f;
	
	private Samples samples;
	private List<Results> results;
	private Results mixedResult;
	
	/**
	 * Sets the threshold window size. (default: 20)
	 */
	public AudioAnalyzer setThresholdWindowSize(int thresholdWindowSize) {
		this.thresholdWindowSize = thresholdWindowSize;
		return this;
	}
	
	/**
	 * Sets the threshold multiplier. (default: 1.8)
	 */
	public AudioAnalyzer setThresholdMultiplier(double thresholdMultiplier) {
		this.thresholdMultiplier = (float) thresholdMultiplier;
		return this;
	}
	
	public AudioAnalyzer analyze(AudioFile file) {
		samples = new AudioDecoder().samplify(file);
		int channels = samples.getBuffer().getChannelCount();
		results = new ArrayList<>(channels);
		for (int i = 0; i < channels; i++) {
			results.add(analyze(samples.channel(i)));
		}
		return this;
	}

	private Results analyze(Channel channel) {
		List<Float> spectralFlux = calculateSpectralFlux(channel);
		List<Float> threshold = calculateThreshold(spectralFlux);
		List<Float> prunnedSpectralFlux = calculatePrunnedSpectralFlux(spectralFlux, threshold);
		List<Float> peaks = calculatePeaks(prunnedSpectralFlux);
		return new Results(spectralFlux, threshold, prunnedSpectralFlux, peaks);
	}
	
	private List<Float> calculateSpectralFlux(Channel channel) {
		List<Float> spectralFlux = new ArrayList<Float>();
		float[] lastSpectrum = null;
		while (channel.hasMoreSamples()) {
			float[] samples = channel.nextSamples();
			FFT fft = new FFT(channel.getIteration(), channel.getSampleRate());
			fft.window(FFT.HAMMING);
			fft.forward(samples);
			float[] spectrum = fft.getSpectrum();
			
			if (lastSpectrum != null) {
				float flux = 0;
				for (int i = 0; i < spectrum.length; i++) {
					float value = (spectrum[i] - lastSpectrum[i]);
					flux += value < 0 ? 0 : value;
				}
				spectralFlux.add(flux);
			}
			lastSpectrum = spectrum;
		}
		return spectralFlux;
	}
	
	private List<Float> calculateThreshold(List<Float> spectralFlux) {
		List<Float> threshold = new ArrayList<Float>(spectralFlux.size());
		for (int i = 0; i < spectralFlux.size(); i++) {
			int start = Math.max(0, i - thresholdWindowSize);
			int end = Math.min(spectralFlux.size() - 1, i + thresholdWindowSize);
			float mean = 0;
			for (int j = start; j <= end; j++)
				mean += spectralFlux.get(j);
			mean /= (end - start);
			threshold.add(mean * thresholdMultiplier);
		}
		return threshold;
	}
	
	private List<Float> calculatePrunnedSpectralFlux(List<Float> spectralFlux, List<Float> threshold) {
		List<Float> prunnedSpectralFlux = new ArrayList<Float>(threshold.size());
		for (int i = 0; i < threshold.size(); i++) {
			if (threshold.get(i) <= spectralFlux.get(i)) prunnedSpectralFlux.add(spectralFlux.get(i) - threshold.get(i));
			else prunnedSpectralFlux.add((float) 0);
		}
		return prunnedSpectralFlux;
	}
	
	private List<Float> calculatePeaks(List<Float> prunnedSpectralFlux) {
		List<Float> peaks = new ArrayList<Float>(prunnedSpectralFlux.size());
		for (int i = 0; i < prunnedSpectralFlux.size() - 1; i++) {
			if (prunnedSpectralFlux.get(i) > prunnedSpectralFlux.get(i + 1)) peaks.add(prunnedSpectralFlux.get(i));
			else peaks.add((float) 0);
		}
		return peaks;
	}

	public Samples getSamples() {
		return samples;
	}

	public List<Results> getResults() {
		return results;
	}

	public Results getResults(int channel) {
		return results.get(channel);
	}
	
	public Results getResultsOfMixedChannels() {
		if (mixedResult == null) mixedResult = analyze(samples.mixed());
		return mixedResult;
	}

	public static class Results {
		public final List<Float> spectralFlux;
		public final List<Float> threshold;
		public final List<Float> prunnedSpectralFlux;
		public final List<Float> peaks;

		private Results(List<Float> spectralFlux, List<Float> threshold, List<Float> prunnedSpectralFlux, List<Float> peaks) {
			this.spectralFlux = spectralFlux;
			this.threshold = threshold;
			this.prunnedSpectralFlux = prunnedSpectralFlux;
			this.peaks = peaks;
		}
		
	}
	
}
