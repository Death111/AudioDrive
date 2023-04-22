package audiodrive.audio;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.tritonus.share.sampled.FloatSampleBuffer;

public class AudioDecoder {
	
	/** Private constructor to prevent instantiation. */
	private AudioDecoder() {
		throw new IllegalStateException("This class shall not be instantiated.");
	}
	
	public static AudioInputStream stream(AudioResource audio) {
		try {
			AudioInputStream stream = AudioSystem.getAudioInputStream(audio.getUrl());
			return AudioSystem.getAudioInputStream(getDecodingFormat(stream.getFormat()), stream);
		} catch (UnsupportedAudioFileException | IOException exception) {
			throw new RuntimeException(exception);
		}
	}
	
	public static DecodedAudio decode(AudioResource audio) {
		AudioInputStream stream = stream(audio);
		AudioFormat format = stream.getFormat();
		List<byte[]> list = new LinkedList<>();
		int bufferSize = 1024 * format.getFrameSize();
		int byteCount = 0;
		try {
			while (true) {
				byte[] bytes = new byte[bufferSize];
				int n = stream.read(bytes, 0, bytes.length);
				if (n == -1) break;
				list.add(bytes);
				byteCount += n;
			}
			stream.close();
		} catch (IOException exception) {
			exception.printStackTrace();
		}
		int totalSampleCount = byteCount / format.getFrameSize();
		FloatSampleBuffer samples = new FloatSampleBuffer(format.getChannels(), totalSampleCount, format.getSampleRate());
		int offset = 0;
		for (byte[] bytes : list) {
			int remaining = totalSampleCount - offset;
			int sampleCount = Math.min(bytes.length / format.getFrameSize(), remaining);
			samples.setSamplesFromBytes(bytes, 0, format, offset, sampleCount);
			offset += sampleCount;
		}
		return new DecodedAudio(audio, samples);
	}
	
	public static AudioFormat getDecodingFormat(AudioFormat format) {
		return new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, format.getSampleRate(), 16, format.getChannels(), format.getChannels() * 2, format.getSampleRate(), false);
	}
	
	/** converts 16 bit integer [-32768, 32767] float values to [-1, 1] float values */
	public static float[] convert(float[] samples) {
		float[] converted = new float[samples.length];
		for (int i = 0; i < samples.length; i++) {
			float sample = samples[i];
			float divisor = sample > 0 ? 32767 : 32768;
			converted[i] = sample / divisor;
		}
		return converted;
	}
	
}
