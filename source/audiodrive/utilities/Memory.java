package audiodrive.utilities;

import java.io.File;

public class Memory {
	
	/** Private constructor to prevent instantiation. */
	private Memory() {
		throw new IllegalStateException("This class shall not be instantiated.");
	}
	
	private static final double KB = 1024.0;
	private static final double MB = 1024.0 * KB;
	private static final double GB = 1024.0 * MB;
	
	public static void print() {
		/* Total number of processors or cores available to the JVM */
		Log.info("Available processors (cores): " + Runtime.getRuntime().availableProcessors());
		
		/* Total amount of free memory available to the JVM */
		Log.info("Free memory: " + inMB(Runtime.getRuntime().freeMemory()));
		
		/* This will return Long.MAX_VALUE if there is no preset limit */
		long maxMemory = Runtime.getRuntime().maxMemory();
		/* Maximum amount of memory the JVM will attempt to use */
		Log.info("Maximum memory: " + (maxMemory == Long.MAX_VALUE ? "no limit" : inMB(maxMemory)));
		
		/* Total memory currently in use by the JVM */
		Log.info("Total memory: " + inMB(Runtime.getRuntime().totalMemory()));
		
		/* Get a list of all filesystem roots on this system */
		File[] roots = File.listRoots();
		
		/* For each filesystem root, print some info */
		for (File root : roots) {
			Log.info("File system root: " + root.getAbsolutePath());
			Log.info("Total space: " + inGB(root.getTotalSpace()));
			Log.info("Free space: " + inGB(root.getFreeSpace()));
			Log.info("Usable space: " + inGB(root.getUsableSpace()));
		}
	}
	
	public static String inKB(long bytes) {
		return String.format("%.2f KB", bytes / KB);
	}
	
	public static String inMB(long bytes) {
		return String.format("%.2f MB", bytes / MB);
	}
	
	public static String inGB(long bytes) {
		return String.format("%.2f GB", bytes / GB);
	}
	
}
