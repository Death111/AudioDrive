package audiodrive.audio;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.tritonus.share.sampled.FloatSampleBuffer;

public class AudioDecoder {
	
	public AudioInputStream stream(AudioFile file) {
		return AudioSystem.getAudioInputStream(getDecodingFormat(file), file.open());
	}
	
	public DecodedAudio decode(AudioFile file) {
		AudioInputStream stream = stream(file);
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
		return new DecodedAudio(file, samples);
	}
	
	private AudioFormat getDecodingFormat(AudioFile file) {
		AudioFormat format = file.getAudioFormat();
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
