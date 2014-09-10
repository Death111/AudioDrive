package audiodrive.utilities;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Files {
	
	/** Private constructor to prevent instantiation. */
	private Files() {
		throw new IllegalStateException("This class shall not be instantiated.");
	}
	
	public static List<File> list(String directory, String extension, boolean recursive) {
		return list(new File(directory), extension, recursive);
	}
	
	public static List<File> list(File directory, String extension, boolean recursive) {
		List<File> list = new ArrayList<>();
		Arrays.stream(directory.listFiles()).forEach(file -> {
			if (file.isDirectory()) {
				if (recursive) list.addAll(list(file, extension, recursive));
			} else {
				if (file.getName().endsWith(extension)) list.add(file);
			}
		});
		return list;
	}
	
	public static Optional<File> find(String directory, String name) {
		return find(new File(directory), name);
	}
	
	public static Optional<File> find(File directory, String name) {
		for (File file : directory.listFiles()) {
			if (file.isDirectory()) {
				Optional<File> optional = find(file, name);
				if (optional.isPresent()) return optional;
			} else {
				if (file.getName().endsWith(name)) return Optional.of(file);
			}
		}
		return Optional.empty();
	}
	
}
