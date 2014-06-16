package audiodrive.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sun.misc.JavaLangAccess;
import sun.misc.SharedSecrets;

public class Log {

	public static final String DefaultPropertyFileName = "logging.properties";

	private final static JavaLangAccess Access = SharedSecrets.getJavaLangAccess();

	/** Private constructor to prevent instantiation. */
	private Log() {
		throw new IllegalStateException("This class shall not be instantiated.");
	}

	static {
		// initialize levels for the log manager
		Level.getLevels();
		// get configuration file
		String fileName = System.getProperty("java.util.logging.config.file");
		if (fileName == null) fileName = DefaultPropertyFileName;
		File file = new File(fileName);
		try {
			if (!file.exists()) {
				// if no configuration file exists create a default one
				Properties properties = new Properties() {
					@Override
					public synchronized Enumeration<Object> keys() {
						return Collections.enumeration(new TreeSet<Object>(super.keySet()));
					}
				};
				properties.setProperty(".level", "ALL");
				properties.setProperty("handlers", ConsoleHandler.class.getName() + ", " + FileHandler.class.getName());
				properties.setProperty(ConsoleHandler.class.getName() + ".level", "TRACE");
				properties.setProperty(ConsoleHandler.class.getName() + ".format", "{level}: {message}\n{exception}");
				properties.setProperty(ConsoleHandler.class.getName() + ".format.time", "HH:mm:ss.SSS");
				properties.setProperty(FileHandler.class.getName() + ".level", "DEBUG");
				properties.setProperty(FileHandler.class.getName() + ".format", Formatter.DefaultFormat);
				properties.setProperty(FileHandler.class.getName() + ".format.origin", Formatter.DefaultOriginFormat);
				properties.setProperty(FileHandler.class.getName() + ".format.time", Formatter.DefaultTimeFormat);
				properties.setProperty(FileHandler.class.getName() + ".pattern", "logs/log.txt");
				try (FileOutputStream outputStream = new FileOutputStream(file)) {
					properties.store(outputStream, null);
				}
			}
			// load configuration
			try (FileInputStream inputStream = new FileInputStream(file)) {
				LogManager.getLogManager().readConfiguration(inputStream);
			}
			// workaround to create missing folders
			String pattern = LogManager.getLogManager().getProperty(FileHandler.class.getName() + ".pattern");
			int index = pattern.lastIndexOf('/');
			String directory = ".";
			if (index > 0) {
				directory = pattern.substring(0, index);
				new File(directory).mkdirs();
			}
			// remove old logs if specified
			String clear = LogManager.getLogManager().getProperty(FileHandler.class.getName() + ".clear");
			if (clear.toLowerCase().equals("true")) for (File log : new File(directory).listFiles()) {
				log.delete();
			}
		} catch (SecurityException | IOException exception) {}
	}

	private static StackTraceElement getCaller() {
		Throwable throwable = new Throwable();
		int depth = Access.getStackTraceDepth(throwable);
		boolean lookingForLogger = true;
		for (int i = 0; i < depth; i++) {
			// Calling getStackTraceElement directly prevents the VM from paying the cost of building the entire stack frame.
			StackTraceElement frame = Access.getStackTraceElement(throwable, i);
			String cname = frame.getClassName();
			boolean isLogger = Log.class.getName().equals(cname);
			if (lookingForLogger) {
				// Skip all frames until we have found the first logger frame.
				if (isLogger) {
					lookingForLogger = false;
				}
			} else {
				if (!isLogger) {
					// skip reflection call
					if (!cname.startsWith("java.lang.reflect.") && !cname.startsWith("sun.reflect.")) {
						// We've found the relevant frame.
						return frame;
					}
				}
			}
		}
		return null;
	}

	public static void trace(Object message) {
		log(Level.TRACE, String.valueOf(message), (Object[]) null);
	}

	public static void trace(String message, Object... parameters) {
		log(Level.TRACE, message, parameters);
	}

	public static void trace(String message, Throwable throwable, Object... parameters) {
		log(Level.TRACE, message, throwable, parameters);
	}

	public static void debug(Object message) {
		log(Level.DEBUG, String.valueOf(message), (Object[]) null);
	}

	public static void debug(String message, Object... parameters) {
		log(Level.DEBUG, message, parameters);
	}

	public static void debug(String message, Throwable throwable, Object... parameters) {
		log(Level.DEBUG, message, throwable, parameters);
	}

	public static void info(Object message) {
		log(Level.INFO, String.valueOf(message), (Object[]) null);
	}

	public static void info(String message, Object... parameters) {
		log(Level.INFO, message, parameters);
	}

	public static void info(String message, Throwable throwable, Object... parameters) {
		log(Level.INFO, message, throwable, parameters);
	}

	public static void warning(Object message) {
		log(Level.WARNING, String.valueOf(message), (Object[]) null);
	}

	public static void warning(String message, Object... parameters) {
		log(Level.WARNING, message, parameters);
	}

	public static void warning(String message, Throwable throwable, Object... parameters) {
		log(Level.WARNING, message, throwable, parameters);
	}

	public static void warning(Throwable throwable) {
		log(Level.WARNING, "", throwable);
	}

	public static void error(Object message) {
		log(Level.ERROR, String.valueOf(message), (Object[]) null);
	}

	public static void error(String message, Object... parameters) {
		log(Level.ERROR, message, parameters);
	}

	public static void error(String message, Throwable throwable, Object... parameters) {
		log(Level.ERROR, message, throwable, parameters);
	}

	public static void error(Throwable throwable) {
		log(Level.ERROR, "", throwable);
	}

	private static void log(java.util.logging.Level level, String message, Object... parameters) {
		log(level, message, (Throwable) null, parameters);
	}

	private static void log(java.util.logging.Level level, String message, Throwable thrown, Object... parameters) {
		StackTraceElement caller = getCaller();
		Record record = new Record(level, message, parameters);
		record.setThrown(thrown);
		if (caller == null) {
			Logger.getGlobal().log(record);
			return;
		}
		record.setSourceClassName(caller.getClassName());
		record.setSourceMethodName(caller.getMethodName());
		record.setSourceLineNumber(caller.getLineNumber());
		Logger.getLogger(caller.getClassName()).log(record);
	}

	public static class Level extends java.util.logging.Level {

		public static final Level ERROR = new Level("ERROR", Level.WARNING.intValue() + 50);
		public static final Level DEBUG = new Level("DEBUG", Level.FINE.intValue() + 100);
		public static final Level TRACE = new Level("TRACE", Level.FINE.intValue() + 50);

		public static final List<java.util.logging.Level> levels = Arrays.asList(ERROR, WARNING, INFO, DEBUG, TRACE);

		public Level(String name, int value) {
			super(name, value);
		}

		public static List<java.util.logging.Level> getLevels() {
			return levels;
		}

	}

	public static class Record extends java.util.logging.LogRecord {

		private int sourceLineNumber = -1;
		private String sourceClassName;

		public Record(java.util.logging.Level level, String message, Object... parameters) {
			super(level, message);
			setParameters(parameters);
		}

		public void setSourceLineNumber(int sourceLineNumber) {
			this.sourceLineNumber = sourceLineNumber;
		}

		public int getSourceLineNumber() {
			return sourceLineNumber;
		}

		@Override
		public String getSourceClassName() {
			if (sourceClassName == null) {
				String sourceClassPath = getSourceClassPath();
				if (sourceClassPath != null) {
					int index = sourceClassPath.lastIndexOf(".");
					sourceClassName = (index > 0) ? sourceClassPath.substring(index + 1) : sourceClassPath;
				}
			}
			return sourceClassName;
		}

		public String getSourceClassPath() {
			return super.getSourceClassName();
		}

	}

	public static class ConsoleHandler extends java.util.logging.StreamHandler {

		public ConsoleHandler() {
			setOutputStream(System.out);
			setFormatter(getFormatterProperty(getClass().getName()));
		}

		@Override
		public void publish(LogRecord record) {
			super.publish(record);
			flush();
		}

		@Override
		public void close() {
			flush();
		}

	}

	public static class FileHandler extends java.util.logging.FileHandler {
		
		public FileHandler() throws IOException, SecurityException {
			setFormatter(getFormatterProperty(getClass().getName()));
		}
	}

	private static Formatter getFormatterProperty(String className) {
		String formatter = LogManager.getLogManager().getProperty(className + ".formatter");
		if (formatter != null) {
			try {
				return (Formatter) ClassLoader.getSystemClassLoader().loadClass(formatter).newInstance();
			} catch (Exception exception) {}
		}
		return Log.Formatter.create(className);
	}

	public static class Formatter extends java.util.logging.Formatter {

		public static final String DefaultFormat = "[{time}] {origin}\n{level}: {message}\n{exception}";
		public static final String DefaultOriginFormat = "{class}.{method}():{line}";
		public static final String DefaultTimeFormat = "yyyy-MM-dd HH:mm:ss.SSS";

		private static final Pattern LogPattern = Pattern.compile("\\{(\\d+_)?(time|origin|level|message|exception)(_\\d+)?\\}");
		private static final Pattern OriginPattern = Pattern.compile("\\{(classpath|class|method|line)\\}");
		
		private Map<String, String> lengths = new HashMap<>();

		private String format;
		private String originFormat;
		private SimpleDateFormat timeFormat;
		private final Date date = new Date();

		public static Formatter create(String className) {
			LogManager manager = LogManager.getLogManager();
			return new Log.Formatter(manager.getProperty(className + ".format"), manager.getProperty(className + ".format.time"), manager.getProperty(className + ".format.origin"));
		}

		public Formatter() {
			this(null, null, null);
		}

		public Formatter(String format, String timeFormat, String originFormat) {
			this.format = (format != null) ? format : DefaultFormat;
			this.originFormat = (originFormat != null) ? originFormat : DefaultOriginFormat;
			this.timeFormat = (timeFormat != null) ? new SimpleDateFormat(timeFormat) : new SimpleDateFormat(DefaultTimeFormat);
			this.format = format.replace("\n", System.lineSeparator());
			this.originFormat = originFormat.replace("\n", System.lineSeparator());
			
			// parse and remember specified group lengths
			Matcher matcher = LogPattern.matcher(format);
			while (matcher.find()) {
				String group = matcher.group();
				if (group.contains("_")) {
					String[] parts = group.substring(1, group.length() - 1).split("_");
					String one = parts[0], two = parts[1];
					if (Pattern.matches("\\d+", one)) lengths.put(group, one);
					else if (Pattern.matches("\\d+", two)) lengths.put(group, "-" + two);
				}
			}
		}

		@Override
		public String format(LogRecord record) {
			StringBuffer buffer = new StringBuffer();
			Matcher matcher = LogPattern.matcher(format);
			while (matcher.find()) {
				matcher.appendReplacement(buffer, "");
				String group = matcher.group();
				String replacement = "";
				if (group.contains("time")) {
					date.setTime(record.getMillis());
					replacement = timeFormat.format(date);
				} else if (group.contains("origin")) {
					replacement = formatOrigin(record);
				} else if (group.contains("level")) {
					replacement = record.getLevel().toString();
				} else if (group.contains("message")) {
					replacement = formatMessage(record);
				} else if (group.contains("exception")) {
					replacement = formatThrowable(record);
				}
				if (lengths.containsKey(group)) {
					buffer.append(String.format("%" + lengths.get(group) + "s", replacement));
				} else {
					buffer.append(replacement);
				}
			}
			matcher.appendTail(buffer);
			return buffer.toString();
		}

		@Override
		public synchronized String formatMessage(LogRecord record) {
			String message = record.getMessage();
			Object[] parameters = record.getParameters();
			if (parameters == null || parameters.length == 0) return message;
			try {
				return String.format(message, parameters);
			} catch (Exception exception) {
				return message + " (" + concatenate(parameters) + ")";
			}
		}

		public String concatenate(Object... objects) {
			StringBuilder builder = new StringBuilder();
			boolean first = true;
			for (Object object : objects) {
				if (first) first = false;
				else builder.append(", ");
				builder.append(object);
			}
			return builder.toString();
		}

		public String formatOrigin(LogRecord record) {
			if (record.getSourceClassName() == null && record.getSourceMethodName() == null) return "unknown origin";
			StringBuffer buffer = new StringBuffer();
			Matcher matcher = OriginPattern.matcher(originFormat);
			while (matcher.find()) {
				matcher.appendReplacement(buffer, "");
				String group = matcher.group();
				if (group.contains("{classpath}")) {
					buffer.append((record instanceof Log.Record) ? ((Log.Record) record).getSourceClassPath() : record.getSourceClassName());
				} else if (group.contains("{class}")) {
					buffer.append(record.getSourceClassName());
				} else if (group.contains("{method}")) {
					buffer.append(record.getSourceMethodName());
				} else if (group.contains("{line}")) {
					buffer.append((record instanceof Log.Record) ? String.valueOf(((Log.Record) record).getSourceLineNumber()) : "unknown line");
				}
			}
			matcher.appendTail(buffer);
			return buffer.toString();
		}

		public String formatThrowable(LogRecord record) {
			if (record.getThrown() != null) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				record.getThrown().printStackTrace(pw);
				pw.close();
				return sw.toString();
			}
			return "";
		}

	}

}
