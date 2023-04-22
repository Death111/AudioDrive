package audiodrive.utilities;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Memory {
	
	/** Private constructor to prevent instantiation. */
	private Memory() {
		throw new IllegalStateException("This class shall not be instantiated.");
	}
	
	/**
	 * Returns the maximum amount of memory the Java Virtual Machine will attempt to use.
	 * 
	 * @return the maximum amount of memory that can be allocated
	 */
	public static Value maximum() {
		return new Value(Runtime.getRuntime().maxMemory());
	}
	
	/**
	 * Returns the amount of memory currently in use by the Java Virtual Machine.
	 * 
	 * @return the amount of memory currently allocated
	 */
	public static Value allocated() {
		return new Value(Runtime.getRuntime().totalMemory());
	}
	
	/**
	 * Returns the total amount of memory available to the Java Virtual Machine.
	 * 
	 * @return the total amount of memory available
	 */
	public static Value usable() {
		return new Value(Runtime.getRuntime().freeMemory() + (Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory()));
	}
	
	/**
	 * Returns the amount of memory currently available to the Java Virtual Machine.
	 * 
	 * @return the amount of memory currently available
	 */
	public static Value free() {
		return new Value(Runtime.getRuntime().freeMemory());
	}
	
	/**
	 * Returns the amount of memory currently used by the program.
	 * 
	 * @return the amount of memory currently used
	 */
	public static Value used() {
		return new Value(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
	}
	
	/**
	 * Returns the memory state of the Java Virtual Machine.
	 * 
	 * @return the memory state
	 */
	public static State state() {
		return new State();
	}
	
	/**
	 * Returns the memory states of the file system roots.
	 * 
	 * @return the file system's memory states
	 */
	public static List<State> diskStates() {
		return Stream.of(File.listRoots()).map(State::new).filter(state -> state.total.bytes > 0).collect(Collectors.toList());
	}
	
	public static enum Unit {
		
		B("Byte"), KB("Kilobyte"), MB("Megabyte"), GB("Gigabyte"), TB("Terabyte"), PB("Petabyte"), EB("Exabyte"), ZB("Zettabyte"), YB("Yottabyte");
		
		private final String label;
		
		private Unit(String label) {
			this.label = label;
		}
		
		public double convert(long bytes) {
			return bytes / Math.pow(1024.0, ordinal());
		}
		
		public static Unit valueOf(long bytes) {
			if (bytes <= 0) return Unit.B;
			int power = (int) (Math.log(bytes) / Math.log(1024));
			Unit[] units = values();
			if (power > units.length) return units[units.length - 1];
			return units[power];
		}
		
		public String label() {
			return label;
		}
		
	};
	
	public static class Value {
		
		private final long bytes;
		
		public Value(long bytes) {
			if (bytes < 0) throw new IllegalArgumentException("Memory values have to be positive.");
			this.bytes = bytes;
		}
		
		public long inBits() {
			return bytes * 8;
		}
		
		public long inBytes() {
			return bytes;
		}
		
		public double inKB() {
			return Unit.KB.convert(bytes);
		}
		
		public double inMB() {
			return Unit.MB.convert(bytes);
		}
		
		public double inGB() {
			return Unit.GB.convert(bytes);
		}
		
		public double inTB() {
			return Unit.TB.convert(bytes);
		}
		
		public String toString(Unit unit) {
			if (bytes == Long.MAX_VALUE) return "Unlimited";
			return String.format("%.1f %s", unit.convert(bytes), unit);
		}
		
		@Override
		public String toString() {
			return toString(Unit.valueOf(bytes));
		}
		
	}
	
	public static class State {
		
		private final File file;
		private final Value total;
		private final Value allocated;
		private final Value usable;
		private final Value free;
		private final Value used;
		
		public State(File file) {
			this.file = file;
			total = new Value(file.getTotalSpace());
			allocated = total;
			usable = new Value(file.getUsableSpace());
			free = new Value(file.getFreeSpace());
			used = new Value(file.getTotalSpace() - file.getUsableSpace());
		}
		
		public State() {
			file = null;
			total = Memory.maximum();
			allocated = Memory.allocated();
			usable = Memory.usable();
			free = Memory.free();
			used = Memory.used();
		}
		
		public File file() {
			return file;
		}
		
		public Value total() {
			return total;
		}
		
		public Value allocated() {
			return allocated;
		}
		
		public Value usable() {
			return usable;
		}
		
		public Value free() {
			return free;
		}
		
		public Value used() {
			return used;
		}
		
		@Override
		public String toString() {
			if (file == null) {
				return "Java VM (" + used() + " / " + total() + ") (" + allocated() + " allocated)";
			} else if (file.isDirectory()) {
				return "Directory \"" + file.getAbsolutePath() + "\" (" + used() + " / " + total() + ")";
			} else {
				return "File \"" + file.getAbsolutePath() + "\" (" + used() + " / " + total() + ")";
			}
		}
		
	}
	
}