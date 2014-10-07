package audiodrive.audio;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import audiodrive.utilities.Range;

public class DecodedChannel implements Channel {
	
	private int channel;
	private float[] samples;
	private int iteration;
	private int sampleCount;
	private float sampleRate;
	private int offset = 0;
	
	DecodedChannel(int index, float[] samples, int sampleCount, float sampleRate, int iteration) {
		channel = index;
		this.samples = samples;
		this.sampleCount = sampleCount;
		this.sampleRate = sampleRate;
		this.iteration = iteration;
	}
	
	protected DecodedChannel(Channel channel) {
		this.channel = channel.getIndex();
		samples = channel.getSamples();
		sampleCount = channel.getSampleCount();
		sampleRate = channel.getSampleRate();
		iteration = channel.getIteration();
	}
	
	Channel setIteration(int samples) {
		iteration = samples;
		rewind();
		return this;
	}
	
	@Override
	public boolean hasMoreSamples() {
		return sampleCount - offset > 0;
	}
	
	@Override
	public float[] nextSamples() {
		if (!hasMoreSamples()) return null;
		float[] samples = new float[iteration];
		int remaining = sampleCount - offset;
		int n = Math.min(iteration, remaining);
		System.arraycopy(this.samples, offset, samples, 0, n);
		offset += iteration;
		return samples;
	}
	
	@Override
	public Channel rewind() {
		offset = 0;
		return this;
	}
	
	@Override
	public float[] getSamples() {
		return samples;
	}
	
	@Override
	public float[] getSamples(int index) {
		float[] samples = new float[iteration];
		int offset = index * iteration;
		int remaining = sampleCount - offset;
		System.arraycopy(this.samples, offset, samples, 0, Math.min(iteration, remaining));
		return samples;
	}
	
	@Override
	public Stream<float[]> stream() {
		int indices = sampleCount / iteration;
		return IntStream.range(0, indices).mapToObj(index -> {
			return getSamples(index);
		});
	}
	
	@Override
	public int getIndex() {
		return channel;
	}
	
	@Override
	public int getSampleCount() {
		return sampleCount;
	}
	
	@Override
	public float getSampleRate() {
		return sampleRate;
	}
	
	@Override
	public int getIteration() {
		return iteration;
	}
	
	@Override
	public String toString() {
		return (channel < 0) ? "channel mix" : "channel " + channel;
	}
	
	public Range getRange() {
		return Range.of(getSamples());
	}
	
}