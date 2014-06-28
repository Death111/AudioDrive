package audiodrive.audio;

import java.util.List;

public interface Audio {
	
	/**
	 * Returns the original audio file.
	 */
	AudioFile getFile();
	
	/**
	 * Indicates the total number of samples.
	 */
	int getSampleCount();
	
	/**
	 * Indicates the number of samples per second.
	 */
	double getSampleRate();
	
	/**
	 * Indicates the total number of channels.
	 */
	int getChannelCount();
	
	/**
	 * Returns the list of audio channels.
	 */
	List<? extends Channel> getChannels();
	
	/**
	 * Returns the audio channel with the specified index.
	 */
	Channel getChannel(int channel);
	
	/**
	 * Returns the mix channel which contains data from all audio channels.
	 */
	DecodedChannel getMix();
	
	/**
	 * Specifies the number of samples that are processed on each iteration.
	 */
	Audio setIteration(int samples);
	
	/**
	 * Indicates the number of samples that are processed on each iteration.
	 */
	int getIteration();
	
	/**
	 * Indicates the number of iterations per second.
	 */
	double getIterationRate();
	
}