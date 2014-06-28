package audiodrive.audio;

import java.util.stream.Stream;

public interface Channel {
	
	/**
	 * Indicates whether there are more samples to iterate over.
	 */
	boolean hasMoreSamples();
	
	/**
	 * Returns the next iteration of samples.
	 */
	float[] nextSamples();
	
	/**
	 * Rewinds the channel by setting the iteration offset back to zero.
	 */
	Channel rewind();
	
	/**
	 * Returns all samples of the channel.
	 */
	float[] getSamples();
	
	/**
	 * Returns the next iteration of samples at a specific index.
	 */
	float[] getSamples(int index);
	
	/**
	 * Returns a stream of sample iterations. The number of samples per iteration is equal to {@linkplain #getIteration()}.
	 */
	Stream<float[]> stream();
	
	/**
	 * Returns the channel index. A value of -1 indicates the channel mix.
	 */
	int getIndex();
	
	/**
	 * Indicates the number of samples.
	 */
	int getSampleCount();
	
	/**
	 * Indicates the number of samples per second.
	 */
	float getSampleRate();
	
	/**
	 * Indicates the number of samples that are processed on each iteration when using {@linkplain #nextSamples()} or {@linkplain #stream()}.
	 */
	int getIteration();
	
}