package audiodrive.audio;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.tritonus.share.sampled.FloatSampleBuffer;

public class Samples {
	
	private FloatSampleBuffer buffer;
	private int iteration = 1024;
	private List<Channel> channels = new ArrayList<>();
	private Channel mixed;
	
	public Samples(FloatSampleBuffer buffer) {
		this.buffer = buffer;
		for (int i = 0; i < buffer.getChannelCount(); i++) {
			channels.add(new Channel(i));
		}
		mixed = new Channel(-1);
	}
	
	public Channel channel(int channel) {
		return channels.get(channel);
	}
	
	public Channel mixed() {
		return mixed;
	}
	
	public FloatSampleBuffer getBuffer() {
		return buffer;
	}
	
	/**
	 * Indicates the total number of samples.
	 */
	public int getCount() {
		return buffer.getSampleCount();
	}
	
	/**
	 * Indicates the number of samples per second.
	 */
	public float getSampleRate() {
		return buffer.getSampleRate();
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
		
		public Channel(int index) {
			channel = index;
		}
		
		/**
		 * Indicates whether there are more samples to iterate over.
		 */
		public boolean hasMoreSamples() {
			return buffer.getSampleCount() - offset > 0;
		}
		
		/**
		 * Returns the next iteration of samples.
		 */
		public float[] nextSamples() {
			if (!hasMoreSamples()) return null;
			float[] samples = new float[iteration];
			int remaining = buffer.getSampleCount() - offset;
			int n = Math.min(iteration, remaining);
			if (channel < 0) {
				int samplesIndex = 0;
				for (int i = offset; i < offset + n; i++) {
					final int index = i;
					double value = Arrays.stream(buffer.getAllChannels()).mapToDouble(channel -> ((float[]) channel)[index]).sum();
					samples[samplesIndex++] = (float) value / buffer.getChannelCount();
				}
			} else {
				System.arraycopy(buffer.getChannel(channel), offset, samples, 0, n);
			}
			offset += iteration;
			return samples;
		}
		
		/**
		 * Rewinds the channel by setting the iteration offset back to zero.
		 */
		public Channel rewind() {
			offset = 0;
			return this;
		}
		
		/**
		 * Indicates the number of samples per second.
		 */
		public float getSampleRate() {
			return buffer.getSampleRate();
		}
		
		/**
		 * Indicates the number of samples that are processed on each iteration.
		 */
		public int getIteration() {
			return iteration;
		}
		
	}
	
}
