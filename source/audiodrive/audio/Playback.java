package audiodrive.audio;

import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class Playback {
	
	private AudioFile file;
	private Thread thread;
	private AudioInputStream stream;
	private SourceDataLine line;
	private byte[] buffer;
	
	private volatile boolean pause = false;
	private volatile boolean restart = false;
	private volatile boolean stop = true;
	private volatile boolean loop = false;
	
	public Playback(AudioFile file) {
		this.file = file;
	}
	
	/** Starts or restarts the playback. Also opens the necessary resources. */
	public Playback start() {
		if (initialized()) {
			restart = true;
		} else {
			initialize();
			stop = false;
			thread.start();
		}
		return this;
	}
	
	/** Resumes the playback if it's not stopped already. */
	public Playback resume() {
		if (!isPaused()) throw new RuntimeException("Can't resume the playback if it's not paused.");
		synchronized (thread) {
			pause = false;
			thread.notify();
		}
		return this;
	}
	
	/** Pauses the playback if it's not stopped already. */
	public Playback pause() {
		if (!isRunning()) throw new RuntimeException("Can't pause the playback if it's not running.");
		pause = true;
		return this;
	}
	
	/**
	 * Toggles between play and pause. <br>
	 * <i><br>
	 * If the playback is running, it will be paused.<br>
	 * If the playback is paused, it will be resumed.<br>
	 * If the playback is stopped, it will be started.<br>
	 * </i>
	 */
	public Playback toggle() {
		if (isRunning()) pause();
		else if (isPaused()) resume();
		else if (isStopped()) start();
		return this;
	}
	
	/** Stops the playback. Also closes the resources. */
	public Playback stop() {
		stop = true;
		return this;
	}
	
	/** Indicates, whether the playback should repeat infinitely. */
	public void setLooping(boolean flag) {
		loop = flag;
	}
	
	/** Empty callback function, called when playback has ended. */
	public void ended() {}
	
	/**
	 * Indicates that the playback is running. I. e. the audio is currently playing.
	 */
	public boolean isRunning() {
		return initialized() && !isPaused() && !isStopped();
	}
	
	/**
	 * Indicates that the playback is paused. I. e. the playback has been started and paused afterwards.
	 */
	public boolean isPaused() {
		return initialized() && pause;
	}
	
	/**
	 * Indicates that the playback is stopped. I. e. the playback has been stopped or not yet started.
	 */
	public boolean isStopped() {
		return stop || !initialized();
	}
	
	/**
	 * Sets the playback volume for this audio.<br>
	 * <br>
	 * <i>Range: 0.0 - 2.0</i>
	 */
	public Playback setVolume(double volume) {
		FloatControl control = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
		float decibel = Math.min(control.getMaximum(), (float) (Math.log(volume) / Math.log(10.0) * 20.0));
		control.setValue(decibel);
		return this;
	}
	
	/**
	 * Returns the playback volume for this audio, rounded to three decimal places.<br>
	 * <br>
	 * <i>Range: 0.0 - 2.0</i>
	 */
	public double getVolume() {
		FloatControl control = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
		double volume = Math.round(Math.pow(Math.E, control.getValue() * Math.log(10) / 20) * 1000.0) * 0.001;
		return volume;
	}
	
	public Playback setMute(Boolean mute) {
		BooleanControl control = (BooleanControl) line.getControl(BooleanControl.Type.MUTE);
		control.setValue(mute);
		return this;
	}
	
	public boolean isMute() {
		BooleanControl control = (BooleanControl) line.getControl(BooleanControl.Type.MUTE);
		return control.getValue();
	}
	
	private boolean initialized() {
		return thread != null;
	}
	
	private void initialize() {
		if (initialized()) return;
		thread = new Thread() {
			@Override
			public void run() {
				open();
				while (true) {
					synchronized (this) {
						if (stop || restart) break;
						if (pause) try {
							line.stop();
							wait();
							line.start();
							if (stop || restart) break;
						} catch (InterruptedException exception) {
							throw new RuntimeException(exception);
						}
					}
					if (!play()) break;
				}
				if (restart || loop) {
					restart = false;
					run();
					return;
				}
				if (!stop) line.drain();
				close();
				thread = null;
				ended();
			};
		};
	}
	
	private void open() {
		if (stream != null) close();
		try {
			stream = new AudioDecoder().stream(file);
			line = AudioSystem.getSourceDataLine(stream.getFormat());
			buffer = new byte[1024 * stream.getFormat().getFrameSize()];
			line.open();
			line.start();
		} catch (LineUnavailableException exception) {
			throw new RuntimeException(exception);
		}
	}
	
	/**
	 * Plays a single audio frame.
	 * 
	 * @return true if there are more frames to play, false otherwise.
	 */
	private boolean play() {
		int n;
		try {
			n = stream.read(buffer, 0, buffer.length);
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
		if (n == -1) return false;
		line.write(buffer, 0, n);
		return true;
	}
	
	private void close() {
		if (stream == null) return;
		try {
			stream.close();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
		line.stop();
		line.close();
		stream = null;
		buffer = null;
		line = null;
	}
	
}