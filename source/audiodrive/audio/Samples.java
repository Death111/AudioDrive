package audiodrive.audio;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.tritonus.share.sampled.FloatSampleBuffer;

public class Samples {
	
	private int iteration = 1024;
	private List<Channel> channels = new ArrayList<>();
	private Channel mixed;
	private int sampleCount;
	private float sampleRate;
	private int channelCount;
	
	public Samples(FloatSampleBuffer buffer) {
		sampleCount = buffer.getSampleCount();
		sampleRate = buffer.getSampleRate();
		channelCount = buffer.getChannelCount();
		for (int channel = 0; channel < channelCount; channel++) {
			channels.add(new Channel(channel, buffer.getChannel(channel)));
		}
		float[] mix = new float[sampleCount];
		for (int index = 0; index < sampleCount; index++) {
			float value = 0;
			for (int channel = 0; channel < channelCount; channel++) {
				value += buffer.getChannel(channel)[index];
			}
			mix[index++] = value / channelCount;
		}
		mixed = new Channel(-1, mix);
	}
	
	public Channel channel(int channel) {
		return channels.get(channel);
	}
	
	public Channel mixed() {
		return mixed;
	}
	
	/**
	 * Indicates the total number of channels.
	 */
	public int getChannelCount() {
		return channelCount;
	}
	
	/**
	 * Indicates the total number of samples.
	 */
	public int getSampleCount() {
		return sampleCount;
	}
	
	/**
	 * Indicates the number of samples per second.
	 */
	public float getSampleRate() {
		return sampleRate;
	}
	
	/**
	 * Indicates the number of iterations per second.
	 */
	public double getIterationRate() {
		return (double) getSampleRate() / getIteration();
	}
	
	/**
	 * Specifies the number of samples that are processed on each iteration.
	 */
	public Samples setIteration(int samples) {
		iteration = samples;
		channels.forEach(Channel::rewind);
		mixed.rewind();
		return this;
	}
	
	/**
	 * Indicates the number of samples that are processed on each iteration.
	 */
	public int getIteration() {
		return iteration;
	}
	
	public List<Channel> getChannels() {
		return channels;
	}
	
	public class Channel {
		
		private int channel;
		private int offset = 0;
		private float[] samples;
		
		public Channel(int index, float[] samples) {
			channel = index;
			this.samples = samples;
		}
		
		/**
		 * Indicates whether there are more samples to iterate over.
		 */
		public boolean hasMoreSamples() {
			return sampleCount - offset > 0;
		}
		
		/**
		 * Returns the next iteration of samples.
		 */
		public float[] nextSamples() {
			if (!hasMoreSamples()) return null;
			float[] samples = new float[iteration];
			int remaining = sampleCount - offset;
			int n = Math.min(iteration, remaining);
			System.arraycopy(this.samples, offset, samples, 0, n);
			offset += iteration;
			return samples;
		}
		
		/**
		 * Returns the next iteration of samples at a specific index.
		 */
		public float[] getSamples(int index) {
			float[] samples = new float[iteration];
			int offset = index * iteration;
			int remaining = sampleCount - offset;
			System.arraycopy(this.samples, offset, samples, 0, Math.min(iteration, remaining));
			return samples;
		}
		
		/**
		 * Returns a stream of sample iterations. The number of samples per iteration is equal to {@linkplain #getIteration()}.
		 */
		public Stream<float[]> stream() {
			int indices = sampleCount / iteration;
			return IntStream.range(0, indices).mapToObj(index -> {
				return getSamples(index);
			});
		}
		
		/**
		 * Rewinds the channel by setting the iteration offset back to zero.
		 */
		public Channel rewind() {
			offset = 0;
			return this;
		}
		
		/**
		 * Indicates the number of samples.
		 */
		public int getSampleCount() {
			return sampleCount;
		}
		
		/**
		 * Indicates the number of samples per second.
		 */
		public float getSampleRate() {
			return sampleRate;
		}
		
		/**
		 * Indicates the number of samples that are processed on each iteration.
		 */
		public int getIteration() {
			return iteration;
		}
		
		@Override
		public String toString() {
			return (channel < 0) ? "channel mix" : "channel " + channel;
		}
		
	}
	
}
