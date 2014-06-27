package audiodrive.utilities;

import java.util.Comparator;

public class Sort {
	
	/** Private constructor to prevent instantiation. */
	private Sort() {
		throw new IllegalStateException("This class shall not be instantiated.");
	}
	
	public static Comparator<Object> comparingToString() {
		return new Comparator<Object>() {
			@Override
			public int compare(Object o1, Object o2) {
				if (o1 != null && o2 != null) return o1.toString().compareTo(o2.toString());
				if (o1 != null) return 1;
				if (o2 != null) return -1;
				return 0;
			}
		};
	}
	
}
