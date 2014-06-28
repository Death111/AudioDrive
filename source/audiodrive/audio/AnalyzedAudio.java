package audiodrive.audio;

import java.util.Collections;
import java.util.List;

public class AnalyzedAudio extends DecodedAudio {
	
	private final double duration;
	private final int bandCount;
	private final int spectraCount;
	private final List<AnalyzedChannel> channels;
	private final AnalyzedChannel mix;
	
	AnalyzedAudio(DecodedAudio audio, double duration, List<AnalyzedChannel> channels, AnalyzedChannel mix) {
		super(audio);
		this.duration = duration;
		this.channels = Collections.unmodifiableList(channels);
		this.mix = mix;
		bandCount = mix.getSpectra().get(0).length;
		spectraCount = mix.getSpectra().size();
	}
	
	/**
	 * Indicates the frequency of a specific frequency band.
	 */
	public double getFrequencyOfBand(int band) {
		return (double) band / getIteration() * getSampleRate();
	}
	
	/**
	 * Returns the play back duration of the audio track.
	 */
	public double getDuration() {
		return duration;
	}
	
	/**
	 * Indicates the number of frequency bands.
	 */
	public int getBandCount() {
		return bandCount;
	}
	
	/**
	 * Indicates the number of frequency spectra.
	 */
	public int getSpectraCount() {
		return spectraCount;
	}
	
	/**
	 * Returns the list of audio channels.
	 */
	@Override
	public List<AnalyzedChannel> getChannels() {
		return channels;
	}
	
	/**
	 * Returns the audio channel with the specified index.
	 */
	@Override
	public AnalyzedChannel getChannel(int index) {
		return channels.get(index);
	}
	
	/**
	 * Returns the mix channel which contains data from all audio channels.
	 */
	@Override
	public AnalyzedChannel getMix() {
		return mix;
	}
	
	@Override
	public String toString() {
		return "Analyzed " + getFile().getName();
	}
	
}