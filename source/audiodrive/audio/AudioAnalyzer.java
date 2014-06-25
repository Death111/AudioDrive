package audiodrive.audio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import audiodrive.audio.Samples.Channel;
import audiodrive.audio.analysis.FFT;
import audiodrive.utilities.Log;

public class AudioAnalyzer {
	
	private int thresholdWindowSize = 20;
	private float thresholdMultiplier = 1.8f;
	
	private AtomicBoolean done = new AtomicBoolean();
	
	private AudioFile file;
	private Samples samples;
	private AnalyzedAudio results;
	
	/**
	 * Sets the threshold window size. (default: 20)
	 */
	public AudioAnalyzer setThresholdWindowSize(int thresholdWindowSize) {
		this.thresholdWindowSize = thresholdWindowSize;
		return this;
	}
	
	public int getThresholdWindowSize() {
		return thresholdWindowSize;
	}
	
	/**
	 * Sets the threshold multiplier. (default: 1.8)
	 */
	public AudioAnalyzer setThresholdMultiplier(double thresholdMultiplier) {
		this.thresholdMultiplier = (float) thresholdMultiplier;
		return this;
	}
	
	public float getThresholdMultiplier() {
		return thresholdMultiplier;
	}
	
	public boolean isDone() {
		return done.get();
	}
	
	private long timestamp;
	
	public AudioAnalyzer analyze(AudioFile file) {
		done.set(false);
		Log.debug("analyzing \"%s\"...", file.getName());
		timestamp = System.nanoTime();
		if (file.equals(this.file)) return null;
		this.file = file;
		samples = new AudioDecoder().samplify(file);
		float duration = samples.getCount() / samples.getSampleRate();
		List<Channel> channels = samples.getChannels();
		channels.add(samples.mixed());
		List<AnalyzedChannel> analyzedChannels = channels.stream().parallel().map(this::analyze).collect(Collectors.toList());
		AnalyzedChannel analyzedMix = analyzedChannels.remove(analyzedChannels.size() - 1);
		results = new AnalyzedAudio(file, duration, samples, analyzedChannels, analyzedMix);
		Long time = System.nanoTime();
		Log.debug("analyzation took %.3f seconds", (time - timestamp) / 1000000000.0);
		done.set(true);
		return this;
	}
	
	private AnalyzedChannel analyze(Channel channel) {
		List<float[]> spectra = calculateSpectra(channel);
		List<Float> spectralSum = calculateSpectralSum(spectra, channel);
		List<Float> spectralFlux = calculateSpectralFlux(spectra, channel);
		List<Float> threshold = calculateThreshold(spectralFlux);
		List<Float> prunnedSpectralFlux = calculatePrunnedSpectralFlux(spectralFlux, threshold);
		List<Float> peaks = calculatePeaks(prunnedSpectralFlux);
		return new AnalyzedChannel(channel, spectra, spectralSum, spectralFlux, threshold, prunnedSpectralFlux, peaks);
	}
	
	private List<float[]> calculateSpectra(Channel channel) {
		List<float[]> spectra = new ArrayList<float[]>();
		while (channel.hasMoreSamples()) {
			float[] samples = channel.nextSamples();
			FFT fft = new FFT(channel.getIteration(), channel.getSampleRate());
			fft.window(FFT.HAMMING);
			fft.forward(samples);
			spectra.add(fft.getSpectrum());
		}
		return spectra;
	}
	
	private List<Float> calculateSpectralSum(List<float[]> spectra, Channel channel) {
		List<Float> spectralSum = new ArrayList<Float>();
		for (float[] spectrum : spectra) {
			float sum = 0;
			for (int i = 0; i < spectrum.length; i++) {
				sum += spectrum[i];
			}
			spectralSum.add(sum);
		}
		return spectralSum;
	}
	
	private List<Float> calculateSpectralFlux(List<float[]> spectra, Channel channel) {
		List<Float> spectralFlux = new ArrayList<Float>();
		float[] lastSpectrum = null;
		for (float[] spectrum : spectra) {
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
	
	public AnalyzedAudio getResults() {
		return results;
	}
	
	public static class AnalyzedAudio {
		
		public final AudioFile file;
		public final float duration;
		public final Samples samples;
		public final List<AnalyzedChannel> channels;
		public final AnalyzedChannel mixed;
		
		private AnalyzedAudio(AudioFile file, float duration, Samples samples, List<AnalyzedChannel> channels, AnalyzedChannel mixed) {
			this.file = file;
			this.duration = duration;
			this.samples = samples;
			this.channels = Collections.unmodifiableList(channels);
			this.mixed = mixed;
		}
		
	}
	
	public static class AnalyzedChannel {
		
		public final Channel channel;
		public final List<float[]> spectra;
		public final List<Float> spectralSum;
		public final List<Float> spectralFlux;
		public final List<Float> threshold;
		public final List<Float> prunnedSpectralFlux;
		public final List<Float> peaks;
		
		private AnalyzedChannel(Channel channel,
		                        List<float[]> spectra,
		                        List<Float> spectralSum,
		                        List<Float> spectralFlux,
		                        List<Float> threshold,
		                        List<Float> prunnedSpectralFlux,
		                        List<Float> peaks) {
			this.channel = channel;
			this.spectra = Collections.unmodifiableList(spectra);
			this.spectralSum = Collections.unmodifiableList(spectralSum);
			this.spectralFlux = Collections.unmodifiableList(spectralFlux);
			this.threshold = Collections.unmodifiableList(threshold);
			this.prunnedSpectralFlux = Collections.unmodifiableList(prunnedSpectralFlux);
			this.peaks = Collections.unmodifiableList(peaks);
		}
		
	}
	
}
