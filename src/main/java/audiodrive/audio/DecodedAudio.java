package audiodrive.audio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.tritonus.share.sampled.FloatSampleBuffer;

import audiodrive.utilities.Arithmetic;

public class DecodedAudio implements Audio {
	
	private final String name;
	private final AudioResource resource;
	private final int sampleCount;
	private final double sampleRate;
	private final int channelCount;
	private final List<DecodedChannel> channels;
	private final DecodedChannel mix;
	private int iteration;
	private double iterationRate;
	
	DecodedAudio(AudioResource file, FloatSampleBuffer buffer) {
		name = file.getName().substring(0, file.getName().lastIndexOf("."));
		this.resource = file;
		sampleCount = buffer.getSampleCount();
		sampleRate = buffer.getSampleRate();
		channelCount = buffer.getChannelCount();
		List<DecodedChannel> channels = new ArrayList<>();
		for (int channel = 0; channel < channelCount; channel++) {
			channels.add(new DecodedChannel(channel, buffer.getChannel(channel), sampleCount, (float) sampleRate, iteration));
		}
		this.channels = Collections.unmodifiableList(channels);
		float[] mix = new float[sampleCount];
		for (int index = 0; index < sampleCount; index++) {
			float value = 0;
			for (int channel = 0; channel < channelCount; channel++) {
				value += buffer.getChannel(channel)[index];
			}
			mix[index++] = value / channelCount;
		}
		this.mix = new DecodedChannel(-1, mix, sampleCount, (float) sampleRate, iteration);
		setIteration(Arithmetic.nextPowerOfTwo((int) (buffer.getSampleRate() / 50)));
	}
	
	protected DecodedAudio(Audio audio) {
		name = audio.getName();
		resource = audio.getResource();
		sampleCount = audio.getSampleCount();
		sampleRate = audio.getSampleRate();
		iteration = audio.getIteration();
		iterationRate = audio.getIterationRate();
		channelCount = audio.getChannelCount();
		channels = null;
		mix = null;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public AudioResource getResource() {
		return resource;
	}
	
	@Override
	public int getSampleCount() {
		return sampleCount;
	}
	
	@Override
	public double getSampleRate() {
		return sampleRate;
	}
	
	@Override
	public int getChannelCount() {
		return channelCount;
	}
	
	@Override
	public List<? extends Channel> getChannels() {
		return Collections.unmodifiableList(channels);
	}
	
	@Override
	public DecodedChannel getChannel(int channel) {
		return channels.get(channel);
	}
	
	@Override
	public DecodedChannel getMix() {
		return mix;
	}
	
	@Override
	public DecodedAudio setIteration(int samples) {
		iteration = samples;
		iterationRate = sampleRate / iteration;
		channels.forEach(channel -> {
			channel.setIteration(samples);
		});
		mix.setIteration(samples);
		return this;
	}
	
	@Override
	public int getIteration() {
		return iteration;
	}
	
	@Override
	public double getIterationRate() {
		return iterationRate;
	}
	
	@Override
	public String toString() {
		return "Decoded " + resource.getName();
	}
	
}
