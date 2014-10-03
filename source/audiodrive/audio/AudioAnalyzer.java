package audiodrive.audio;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import audiodrive.AudioDrive;
import audiodrive.audio.analysis.FastFourierTransformation;
import audiodrive.utilities.Arithmetic;
import audiodrive.utilities.Log;
import audiodrive.utilities.Stopwatch;

public class AudioAnalyzer {
	
	private AtomicBoolean done = new AtomicBoolean();
	private Stopwatch stopwatch = new Stopwatch();
	
	private AudioResource file;
	private DecodedAudio samples;
	private AnalyzedAudio results;
	
	private int thresholdWindowSize;
	private float thresholdMultiplier;
	
	public AudioAnalyzer() {
		thresholdWindowSize = Arithmetic.clamp(AudioDrive.Settings.getInteger("audio.analyzation.window"), 5, 1000);
		thresholdMultiplier = (float) Arithmetic.clamp(AudioDrive.Settings.getDouble("audio.analyzation.threshold"), 0.5, 5.0);
	}
	
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
	
	public AudioAnalyzer analyze(AudioResource file) {
		if (file.equals(this.file)) return null;
		Log.info("Analyzing audio...");
		this.file = file;
		done.set(false);
		Log.debug("Analyzing \"%s\"...", file.getName());
		stopwatch.start();
		try {
			samples = AudioDecoder.decode(file);
		} catch (OutOfMemoryError error) {
			Log.debug("Not enough memory available to decode file \"%s\".", error, file.getName());
			done.set(true);
			return this;
		} catch (Exception exception) {
			done.set(true);
			return this;
		}
		Log.trace("Decoding took %.3f seconds", stopwatch.getSeconds());
		double duration = samples.getSampleCount() / samples.getSampleRate();
		ArrayList<Channel> channels = new ArrayList<>(samples.getChannels());
		channels.add(samples.getMix());
		List<AnalyzedChannel> analyzedChannels;
		try {
			analyzedChannels = channels.stream().parallel().map(this::analyze).collect(Collectors.toList());
		} catch (OutOfMemoryError error) {
			Log.debug("Not enough memory available to analyze file \"%s\".", error, file.getName());
			done.set(true);
			return this;
		}
		AnalyzedChannel analyzedMix = analyzedChannels.remove(analyzedChannels.size() - 1);
		results = new AnalyzedAudio(samples, duration, analyzedChannels, analyzedMix);
		Log.debug("Analyzation took %.3f seconds total", stopwatch.stop());
		int minutes = (int) (results.getDuration() / 60);
		int seconds = (int) Math.round(results.getDuration() - minutes * 60);
		Log.debug(
			"Analyzation results:"
				+ "%n%s minutes %s seconds duration"
				+ "%n%s channels"
				+ "%n%s samples per second"
				+ "%n%s samples"
				+ "%n%s spectra"
				+ "%n%s bands"
				+ "%n%s samples per iteration"
				+ "%n%.3f iterations per second",
			minutes,
			seconds,
			results.getChannelCount(),
			results.getSampleRate(),
			results.getSampleCount(),
			results.getIterationCount(),
			results.getBandCount(),
			results.getIteration(),
			results.getIterationRate());
		Log.info("Analyzation complete");
		done.set(true);
		return this;
	}
	
	private AnalyzedChannel analyze(Channel channel) {
		Stopwatch stopwatch = new Stopwatch().start();
		List<float[]> spectra = calculateSpectra(channel);
		Log.trace("Spectra calculation of %s took %.3f seconds", channel, stopwatch.getSeconds());
		List<AnalyzationData> bands = calculateBands(spectra);
		Log.trace("Band calculation of %s took %.3f seconds", channel, stopwatch.getSeconds());
		AnalyzationData spectralSum = calculateSpectralSum(spectra, channel);
		Log.trace("SpectralSum calculation of %s took %.3f seconds", channel, stopwatch.getSeconds());
		AnalyzationData spectralFlux = calculateSpectralFlux(spectra, channel);
		Log.trace("SpectralFlux calculation of %s took %.3f seconds", channel, stopwatch.getSeconds());
		AnalyzationData threshold = calculateThreshold(spectralFlux);
		Log.trace("Threshold calculation of %s took %.3f seconds", channel, stopwatch.getSeconds());
		AnalyzationData prunnedSpectralFlux = calculatePrunnedSpectralFlux(spectralFlux, threshold);
		Log.trace("PrunnedSpectralFlux of %s calculation took %.3f seconds", channel, stopwatch.getSeconds());
		AnalyzationData peaks = calculatePeaks(prunnedSpectralFlux);
		Log.trace("Peaks calculation of %s took %.3f seconds", channel, stopwatch.getSeconds());
		return new AnalyzedChannel(channel, spectra, bands, spectralSum, spectralFlux, threshold, prunnedSpectralFlux, peaks);
	}
	
	private List<float[]> calculateSpectra(Channel channel) {
		return channel.stream().parallel().map(samples -> {
			FastFourierTransformation fft = new FastFourierTransformation(channel.getIteration(), channel.getSampleRate());
			fft.window(FastFourierTransformation.HAMMING);
			fft.forward(samples);
			return fft.getSpectrum();
		}).collect(Collectors.toList());
	}
	
	private List<AnalyzationData> calculateBands(List<float[]> spectra) {
		int numberOfSpectra = spectra.size();
		int numberOfBands = spectra.get(0).length;
		IntStream bands = IntStream.range(0, numberOfBands);
		return bands.parallel().mapToObj(b -> {
			float[] band = new float[numberOfSpectra];
			for (int s = 0; s < numberOfSpectra; s++) {
				band[s] = spectra.get(s)[b];
			}
			return band;
		}).map(AnalyzationData::new).collect(Collectors.toList());
	}
	
	private AnalyzationData calculateSpectralSum(List<float[]> spectra, Channel channel) {
		float[] values = new float[spectra.size()];
		int index = 0;
		for (float[] spectrum : spectra) {
			float sum = 0;
			for (int i = 0; i < spectrum.length; i++) {
				sum += spectrum[i];
			}
			values[index++] = sum;
		}
		return new AnalyzationData(values);
	}
	
	private AnalyzationData calculateSpectralFlux(List<float[]> spectra, Channel channel) {
		float[] values = new float[spectra.size()];
		int index = 0;
		float[] lastSpectrum = null;
		for (float[] spectrum : spectra) {
			float flux = 0;
			if (lastSpectrum != null) {
				for (int i = 0; i < spectrum.length; i++) {
					float value = (spectrum[i] - lastSpectrum[i]);
					flux += value < 0 ? 0 : value;
				}
			}
			values[index++] = flux;
			lastSpectrum = spectrum;
		}
		return new AnalyzationData(values);
	}
	
	private AnalyzationData calculateThreshold(AnalyzationData spectralFlux) {
		float[] values = new float[spectralFlux.size()];
		int index = 0;
		for (int i = 0; i < spectralFlux.size(); i++) {
			int start = Math.max(0, i - thresholdWindowSize);
			int end = Math.min(spectralFlux.size() - 1, i + thresholdWindowSize);
			float mean = 0;
			for (int j = start; j <= end; j++)
				mean += spectralFlux.get(j);
			mean /= (end - start);
			values[index++] = mean * thresholdMultiplier;
		}
		return new AnalyzationData(values);
	}
	
	private AnalyzationData calculatePrunnedSpectralFlux(AnalyzationData spectralFlux, AnalyzationData threshold) {
		float[] values = new float[spectralFlux.size()];
		int index = 0;
		for (int i = 0; i < threshold.size(); i++) {
			if (threshold.get(i) <= spectralFlux.get(i)) values[index++] = spectralFlux.get(i) - threshold.get(i);
			else values[index++] = 0;
		}
		return new AnalyzationData(values);
	}
	
	private AnalyzationData calculatePeaks(AnalyzationData prunnedSpectralFlux) {
		float[] values = new float[prunnedSpectralFlux.size()];
		int index = 0;
		for (int i = 0; i < prunnedSpectralFlux.size() - 1; i++) {
			if (prunnedSpectralFlux.get(i) > prunnedSpectralFlux.get(i + 1)) values[index++] = prunnedSpectralFlux.get(i);
			else values[index++] = 0;
		}
		return new AnalyzationData(values);
	}
	
	public Audio getSamples() {
		return samples;
	}
	
	public AnalyzedAudio getResults() {
		return results;
	}
	
}
