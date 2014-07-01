package audiodrive.audio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.decoder.SampleBuffer;
import javazoom.jl.player.AudioDevice;
import javazoom.jl.player.FactoryRegistry;

public class AudioPlayer {
	
	private AudioDevice audio;
	private Bitstream bitstream;
	private Decoder decoder;
	private Thread thread;
	private File file;
	
	private boolean loop = false;
	private boolean paused = false;
	private boolean stopped = true;
	private boolean closed = true;
	
	public AudioPlayer() {}
	
	public void play(File file) {
		this.file = file;
		play();
	}
	
	/** Starts or pauses the playback. Also opens the resources if needed. */
	public void play() {
		if (isPlaying()) {
			pause();
			return;
		}
		if (closed) open();
		synchronized (thread) {
			paused = false;
			thread.notify();
		}
	}
	
	/** Pauses the playback if it's not stopped already. */
	public void pause() {
		if (stopped) return;
		synchronized (thread) {
			paused = true;
		}
	}
	
	/** Stops the playback. Also closes the resources. */
	public void stop() {
		if (stopped) return;
		synchronized (thread) {
			stopped = true;
		}
		close();
	}
	
	/** Indicates, whether the playback should repeat infinitely. */
	public void setLooping(boolean flag) {
		loop = flag;
	}
	
	/** Empty callback function, called when playback has ended. */
	public void ended() {}
	
	public boolean isPlaying() {
		return !closed && !stopped && !paused;
	}
	
	public boolean isPaused() {
		return paused;
	}
	
	public boolean isStopped() {
		return stopped;
	}
	
	private void open() {
		if (!closed) return;
		closed = false;
		
		try {
			bitstream = new Bitstream(new FileInputStream(file));
		} catch (FileNotFoundException exception1) {
			exception1.printStackTrace();
		}
		decoder = new Decoder();
		try {
			audio = FactoryRegistry.systemRegistry().createAudioDevice();
			audio.open(decoder);
		} catch (JavaLayerException exception) {
			exception.printStackTrace();
		}
		
		thread = new Thread() {
			@Override
			public void run() {
				
				while (true) {
					synchronized (thread) {
						if (stopped) break;
						if (paused) {
							try {
								wait();
							} catch (InterruptedException exception) {
								exception.printStackTrace();
							}
						}
					}
					if (!decodeFrame()) break;
				}
				
				audio.flush();
				close();
			}
		};
		
		stopped = false;
		paused = true;
		thread.start();
	}
	
	private void close() {
		if (closed) return;
		closed = true;
		
		try {
			audio.close();
			bitstream.close();
		} catch (BitstreamException exception) {
			exception.printStackTrace();
		}
		audio = null;
		bitstream = null;
		
		if (!stopped) {
			if (loop) play();
			else ended();
		}
	}
	
	/**
	 * Decodes a single frame.
	 * 
	 * @return true if there are more frames to decode, false otherwise.
	 */
	private boolean decodeFrame() {
		try {
			Header header = bitstream.readFrame();
			if (header == null) return false;
			
			SampleBuffer output = (SampleBuffer) decoder.decodeFrame(header, bitstream);
			audio.write(output.getBuffer(), 0, output.getBufferLength());
			
			bitstream.closeFrame();
		} catch (JavaLayerException exception) {
			exception.printStackTrace();
		}
		
		return true;
	}
	
}
