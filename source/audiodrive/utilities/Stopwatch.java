package audiodrive.utilities;

import java.time.Duration;

public class Stopwatch {
	
	private static double NanosToSecondsDivisor = 1000000000.0;
	
	private boolean running;
	private long start;
	private long timestamp;
	
	/**
	 * Creates a stop watch to measure durations using time stamps.<br>
	 * A running stop watch timer won't cause any performance decrease.
	 */
	public Stopwatch() {}
	
	/**
	 * Starts the stop watch timer.<br>
	 * I. e. sets the start time stamp.
	 */
	public synchronized Stopwatch start() {
		start = timestamp = System.nanoTime();
		running = true;
		return this;
	}
	
	/**
	 * Indicates, whether the stop watch timer is currently running.<br>
	 * I. e. the start time stamp has been.
	 */
	public boolean isRunning() {
		return running;
	}
	
	/**
	 * Adds the given number of seconds to the ongoing time measurement.
	 */
	public synchronized Stopwatch addSeconds(double seconds) {
		if (!running) throw new RuntimeException("Timer has not been started.");
		long nanoseconds = Math.round(seconds * NanosToSecondsDivisor);
		start += nanoseconds;
		timestamp += nanoseconds;
		return this;
	}
	
	/**
	 * Adds the given duration to the ongoing time measurement.
	 */
	public synchronized Stopwatch addSeconds(Duration duration) {
		if (!running) throw new RuntimeException("Timer has not been started.");
		long nanoseconds = duration.toNanos();
		start += nanoseconds;
		timestamp += nanoseconds;
		return this;
	}
	
	/**
	 * Returns the number of seconds passed since the last {@linkplain #getSeconds()} or {@linkplain #start()} call.
	 */
	public synchronized double getSeconds() {
		if (!running) throw new RuntimeException("Timer has not been started.");
		long now = System.nanoTime();
		double time = (now - timestamp) / NanosToSecondsDivisor;
		timestamp = now;
		return time;
	}
	
	/**
	 * Returns the duration passed since the last {@linkplain #getSeconds()} or {@linkplain #start()} call.
	 */
	public synchronized Duration getDuration() {
		if (!running) throw new RuntimeException("Timer has not been started.");
		long now = System.nanoTime();
		Duration duration = Duration.ofNanos(now - timestamp);
		timestamp = now;
		return duration;
	}
	
	/**
	 * Returns the number of seconds passed since the stop watch timer had been started.<br>
	 * I. e. since the last {@linkplain #start()} call.
	 */
	public synchronized double getTotalSeconds() {
		if (!running) throw new RuntimeException("Timer has not been started.");
		long now = System.nanoTime();
		return (now - start) / NanosToSecondsDivisor;
	}
	
	/**
	 * Returns the duration passed since the stop watch timer had been started.<br>
	 * I. e. since the last {@linkplain #start()} call.
	 */
	public synchronized Duration getTotalDuration() {
		if (!running) throw new RuntimeException("Timer has not been started.");
		long now = System.nanoTime();
		return Duration.ofNanos(now - start);
	}
	
	/**
	 * Stops the stop watch timer and returns the number of seconds passed since the timer had been started.<br>
	 * I. e. since the last {@linkplain #start()} call.
	 */
	public synchronized double stop() {
		double time = getTotalSeconds();
		running = false;
		return time;
	}
	
}
