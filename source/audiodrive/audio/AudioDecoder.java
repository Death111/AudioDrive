package audiodrive.audio;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.tritonus.share.sampled.FloatSampleBuffer;

public class AudioDecoder {
	
	private AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
	
	public AudioInputStream stream(AudioFile file) {
		return AudioSystem.getAudioInputStream(getDecodingFormat(file), file.open());
	}
	
	public DecodedAudio decode(AudioFile file) {
		AudioInputStream stream = stream(file);
		AudioFormat format = stream.getFormat();
		if (!format.getEncoding().equals(encoding)) throw new IllegalArgumentException("Encoding of the audio input stream differs from the decoder's encoding.");
		List<byte[]> list = new LinkedList<>();
		int bufferSize = 1024 * format.getFrameSize();
		try {
			while (true) {
				byte[] bytes = new byte[bufferSize];
				int n = stream.read(bytes, 0, bytes.length);
				if (n == -1) break;
				list.add(bytes);
			}
			stream.close();
		} catch (IOException exception) {
			exception.printStackTrace();
		}
		int totalSampleCount = list.size() * bufferSize / format.getFrameSize();
		FloatSampleBuffer samples = new FloatSampleBuffer(format.getChannels(), totalSampleCount, format.getSampleRate());
		int offset = 0;
		for (byte[] bytes : list) {
			int sampleCount = bytes.length / format.getFrameSize();
			samples.setSamplesFromBytes(bytes, 0, format, offset, sampleCount);
			offset += sampleCount;
		}
		return new DecodedAudio(file, samples);
	}
	
	private AudioFormat getDecodingFormat(AudioFile file) {
		AudioFormat format = file.getAudioFormat();
		return new AudioFormat(encoding, format.getSampleRate(), 16, format.getChannels(), format.getChannels() * 2, format.getSampleRate(), false);
	}
	
	public AudioDecoder setEncoding(AudioFormat.Encoding encoding) {
		this.encoding = encoding;
		return this;
	}
	
	public AudioFormat.Encoding getEncoding() {
		return encoding;
	}
	
}
