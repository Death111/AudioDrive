package audiodrive.audio;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import audiodrive.audio.analysis.FastFourierTransformation;
import audiodrive.utilities.Log;
import audiodrive.utilities.Stopwatch;

public class AudioAnalyzer {
	
	private int thresholdWindowSize = 20;
	private float thresholdMultiplier = 1.8f;
	
	private AtomicBoolean done = new AtomicBoolean();
	private Stopwatch stopwatch = new Stopwatch();
	
	private AudioFile file;
	private DecodedAudio samples;
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
	
	public AudioAnalyzer analyze(AudioFile file) {
		if (file.equals(this.file)) return null;
		this.file = file;
		done.set(false);
		Log.debug("analyzing \"%s\"...", file.getName());
		stopwatch.start();
		samples = new AudioDecoder().decode(file);
		Log.trace("decoding took %.3f seconds", stopwatch.getSeconds());
		double duration = samples.getSampleCount() / samples.getSampleRate();
		ArrayList<Channel> channels = new ArrayList<>(samples.getChannels());
		channels.add(samples.getMix());
		List<AnalyzedChannel> analyzedChannels = channels.stream().parallel().map(this::analyze).collect(Collectors.toList());
		AnalyzedChannel analyzedMix = analyzedChannels.remove(analyzedChannels.size() - 1);
		results = new AnalyzedAudio(samples, duration, analyzedChannels, analyzedMix);
		Log.debug("analyzation took %.3f seconds total", stopwatch.stop());
		int min = (int) (results.getDuration() / 60);
		int sec = (int) (results.getDuration() - min * 60);
		Log.debug(
			"analyzation results:"
				+ "%n%s min %s sec duration"
				+ "%n%s channels"
				+ "%n%s samples per second"
				+ "%n%s samples"
				+ "%n%s spectra"
				+ "%n%s bands"
				+ "%n%s samples per iteration"
				+ "%n%.3f iterations per second",
			min,
			sec,
			results.getChannelCount(),
			results.getSampleRate(),
			results.getSampleCount(),
			results.getSpectraCount(),
			results.getBandCount(),
			results.getIteration(),
			results.getIterationRate());
		done.set(true);
		return this;
	}
	
	private AnalyzedChannel analyze(Channel channel) {
		Stopwatch stopwatch = new Stopwatch().start();
		List<float[]> spectra = calculateSpectra(channel);
		Log.trace("spectra calculation of %s took %.3f seconds", channel, stopwatch.getSeconds());
		List<Float> spectralSum = calculateSpectralSum(spectra, channel);
		Log.trace("spectralSum calculation of %s took %.3f seconds", channel, stopwatch.getSeconds());
		List<Float> spectralFlux = calculateSpectralFlux(spectra, channel);
		Log.trace("spectralFlux calculation of %s took %.3f seconds", channel, stopwatch.getSeconds());
		List<Float> threshold = calculateThreshold(spectralFlux);
		Log.trace("threshold calculation of %s took %.3f seconds", channel, stopwatch.getSeconds());
		List<Float> prunnedSpectralFlux = calculatePrunnedSpectralFlux(spectralFlux, threshold);
		Log.trace("prunnedSpectralFlux of %s calculation took %.3f seconds", channel, stopwatch.getSeconds());
		List<Float> peaks = calculatePeaks(prunnedSpectralFlux);
		Log.trace("peaks calculation of %s took %.3f seconds", channel, stopwatch.getSeconds());
		return new AnalyzedChannel(channel, spectra, spectralSum, spectralFlux, threshold, prunnedSpectralFlux, peaks);
	}
	
	private List<float[]> calculateSpectra(Channel channel) {
		return channel.stream().parallel().map(samples -> {
			FastFourierTransformation fft = new FastFourierTransformation(channel.getIteration(), channel.getSampleRate());
			fft.window(FastFourierTransformation.HAMMING);
			fft.forward(samples);
			return fft.getSpectrum();
		}).collect(Collectors.toList());
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
	
	public Audio getSamples() {
		return samples;
	}
	
	public AnalyzedAudio getResults() {
		return results;
	}
	
}
