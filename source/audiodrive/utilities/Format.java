package audiodrive.utilities;

public class Format {
	
	/** Private constructor to prevent instantiation. */
	private Format() {
		throw new IllegalStateException("This class shall not be instantiated.");
	}
	
	public static String seconds(double seconds) {
		return seconds(seconds, 3);
	}
	
	public static String seconds(double seconds, int millisecondPrecision) {
		int min = (int) (seconds / 60);
		int sec = (int) (seconds - min * 60);
		millisecondPrecision = Arithmetic.clamp(millisecondPrecision, 0, 3);
		if (millisecondPrecision > 0) {
			long ms = (long) ((seconds - min * 60 - sec) * factor(millisecondPrecision));
			return String.format("%02d:%02d:%0" + millisecondPrecision + "d", min, sec, ms);
		}
		return String.format("%02d:%02d", min, sec);
	}
	
	private static int factor(int millisecondPrecision) {
		switch (millisecondPrecision) {
		case 3:
			return 1000;
		case 2:
			return 100;
		case 1:
			return 10;
		default:
			return 1;
		}
	}
	
}
