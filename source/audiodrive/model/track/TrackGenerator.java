package audiodrive.model.track;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import audiodrive.audio.AudioAnalyzer;
import audiodrive.audio.AudioAnalyzer.Results;
import audiodrive.audio.AudioFile;
import audiodrive.audio.Samples;
import audiodrive.model.Track;
import audiodrive.model.geometry.Vector;
import audiodrive.ui.Plot;
import audiodrive.utilities.Log;

public class TrackGenerator {

	private AudioAnalyzer analyzer = new AudioAnalyzer();
	private int smoothing;
	private boolean useAverage = false;
	private Results mixed;
	private Results left;
	private Results right;

	public Track generate(AudioFile file, int smoothing) {
		this.smoothing = smoothing;
		Log.debug("analyzing " + file.getName() + " ...");
		Log.debug("Samplerate: " + file.getAudioFormat().getSampleRate());
		Log.debug("Framerate: " + file.getAudioFormat().getFrameRate());
		analyzer.analyze(file);
		Samples samples = analyzer.getSamples();
		Log.debug(samples.getCount() + " samples");
		double duration = samples.getCount() / samples.getSampleRate();
		Log.debug(duration + " seconds");

		mixed = analyzer.getResultsOfMixedChannels();
		left = analyzer.getResults(0);
		right = analyzer.getResults(1);
		Log.debug(mixed.spectralFlux.size() + " spectra");

		List<Vector> vectorinates = useAverage ? calculateUsingAverage() : calculate();

		Log.debug(vectorinates.size() + " vectorinates");
		// plot("Left", analyzer.getResults(0));
		// plot("Right", analyzer.getResults(1));
		return new Track(vectorinates, duration, smoothing);
	}

	private List<Vector> calculate() {
		double max = mixed.threshold.stream().mapToDouble(v -> v).max().getAsDouble();
		List<Vector> vectorinates = new ArrayList<>();
		double x = 0;
		double y = 0;
		double z = 0;
		int index = 0;
		for (float value : mixed.threshold) {
			if (index % smoothing == 0) {
				vectorinates.add(new Vector(x, y, z));
			}
			// float difference = right.threshold.get(index) - left.threshold.get(index);
			float difference = right.spectralSum.get(index) - left.spectralSum.get(index);
			int direction = Math.abs(difference) > 100 ? (int) Math.signum(difference) : 0;
			x += 0.005;
			y += (0.5 - (value / max)) * 0.005;
			z += direction * 0.005;
			index++;
		}
		return vectorinates;
	}

	private List<Vector> calculateUsingAverage() {
		double max = mixed.threshold.stream().mapToDouble(v -> v).max().getAsDouble();
		List<Vector> vectorinates = new ArrayList<>();
		double x = 0;
		double y = 0;
		double z = 0;
		for (int i = 0; i < mixed.threshold.size(); i += smoothing) {
			double xi = 0;
			double yi = 0;
			double zi = 0;
			vectorinates.add(new Vector(x, y, z));
			for (int j = 0; j < smoothing; j++) {
				int index = i + j;
				if (index >= mixed.threshold.size()) return vectorinates;
				double value = mixed.threshold.get(i + j);
				int direction = left.threshold.get(index) > right.threshold.get(index) ? 1 : -1;
				xi += 0.005;
				yi += (0.5 - (value / max)) * 0.01;
				zi += direction * 0.01;
			}
			x += xi / smoothing;
			y += yi / smoothing;
			z += zi / smoothing;
		}
		return vectorinates;
	}

	public AudioAnalyzer getAnalyzer() {
		return analyzer;
	}

	public int getSmoothing() {
		return smoothing;
	}

	private static void plot(String title, Results results) {
		Plot plot = new Plot(title, 1024, 512);
		plot.plot(results.spectralFlux, 1, Color.red);
		plot.plot(results.threshold, 1, Color.green);
		plot.plot(results.peaks, 1, 0.7f, true, Color.blue);
	}

}
