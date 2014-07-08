package audiodrive.audio;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import audiodrive.Resources;

public class AudioFile extends File {
	
	private AudioInputStream inputStream;
	
	public AudioFile(String name) {
		this(Resources.getFile(name));
	}
	
	public AudioFile(File file) {
		super(file, "");
	}
	
	public AudioFileFormat getFormat() {
		try {
			return AudioSystem.getAudioFileFormat(this);
		} catch (UnsupportedAudioFileException | IOException exception) {
			throw new RuntimeException(exception);
		}
	}
	
	public AudioFormat getAudioFormat() {
		return getFormat().getFormat();
	}
	
	/**
	 * Opens an audio input stream on the file.
	 */
	public AudioInputStream open() {
		try {
			inputStream = AudioSystem.getAudioInputStream(this);
		} catch (UnsupportedAudioFileException | IOException exception) {
			throw new RuntimeException(exception);
		}
		return inputStream;
	}
	
	public AudioInputStream getInputStream() {
		return inputStream;
	}
	
	public void close() {
		try {
			inputStream.close();
			inputStream = null;
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}
	
	public Playback play() {
		return new Playback(this).start();
	}
	
}
