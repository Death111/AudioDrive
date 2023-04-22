package audiodrive.utilities;

import java.util.Optional;

public class Get {
	
	/** Private constructor to prevent instantiation. */
	private Get() {
		throw new IllegalStateException("This class shall not be instantiated.");
	}
	
	public static <T> Optional<T> optional(T optional) {
		return Optional.ofNullable(optional);
	}
	
}
