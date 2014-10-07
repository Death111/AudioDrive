package audiodrive.ui.effects;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL11.GL_NO_ERROR;
import static org.lwjgl.opengl.GL11.glGetError;
import static org.lwjgl.opengl.GL20.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import audiodrive.utilities.Sort;

public class ShaderProgram {
	
	public static final Pattern StructPattern = Pattern.compile("struct\\s+\\w+\\s*\\{[^}]+\\}");
	public static final Pattern UniformPattern = Pattern.compile("uniform\\s+\\w+\\s+\\w+(\\[[^]]*\\])?;");
	
	public final int program;
	
	private String name;
	private List<Shader> shaders = new LinkedList<>();
	private Map<String, Uniform> uniforms = new HashMap<>();
	
	/**
	 * Creates, compiles, links and validates a shader program with vertex and fragment shader.
	 * 
	 * @param sharedShaderFilepath shared path and name of the .vs and .fs shader files
	 */
	public ShaderProgram(String sharedShaderFilepath) {
		this(sharedShaderFilepath + ".vs", sharedShaderFilepath + ".fs");
	}
	
	/**
	 * Creates, compiles, links and validates a shader program with vertex and fragment shader.
	 * 
	 * @param vertexShaderFilepath path to the vertex shader source file
	 * @param fragmentShaderFilepath path to the fragment shader source file
	 */
	public ShaderProgram(String vertexShaderFilepath, String fragmentShaderFilepath) {
		if (vertexShaderFilepath == null || vertexShaderFilepath.isEmpty() || fragmentShaderFilepath == null || vertexShaderFilepath.isEmpty()) {
			throw new IllegalArgumentException("Filepath for vertex and fragment shader have to be specified.");
		}
		parseName(fragmentShaderFilepath);
		program = glCreateProgram();
		attach(new Shader(vertexShaderFilepath, GL_VERTEX_SHADER));
		attach(new Shader(fragmentShaderFilepath, GL_FRAGMENT_SHADER));
		link();
		validate();
	}
	
	@Override
	protected void finalize() throws Throwable {
		glDeleteProgram(program);
	}
	
	private void parseName(String string) {
		int start = string.indexOf('/');
		int end = string.indexOf('.');
		name = (start < 0) ? string : string.substring(start + 1, (end < 0) ? string.length() : end);
	}
	
	public ShaderProgram setName(String name) {
		this.name = name;
		return this;
	}
	
	public Uniform uniform(String name) {
		if (!uniforms.containsKey(name)) throw new ShaderException("Couln't find uniform \"" + name + "\" in shader \"" + getName() + "\".");
		return uniforms.get(name);
	}
	
	public void attach(Shader shader) {
		glAttachShader(program, shader.id);
		shaders.add(shader);
	}
	
	public void link() {
		glLinkProgram(program);
		if (glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE) throw new ShaderException("Couldn't link shader program \"" + name + "\".", this);
		uniforms.clear();
		shaders.forEach(Shader::findUniforms);
	}
	
	public void validate() {
		glValidateProgram(program);
		if (glGetProgrami(program, GL_VALIDATE_STATUS) == 0) throw new ShaderException("Couldn't validate shader program \"" + name + "\".", this);
	}
	
	public void bind() {
		glUseProgram(program);
	}
	
	public void unbind() {
		glUseProgram(0);
	}
	
	public int getId() {
		return program;
	}
	
	public String getName() {
		return name;
	}
	
	public List<Shader> getShaders() {
		return new ArrayList<Shader>(shaders);
	}
	
	public List<Uniform> getUniforms() {
		return uniforms.values().stream().sorted(Sort.comparingToString()).collect(Collectors.toList());
	}
	
	public String getLog() {
		return glGetProgramInfoLog(program, 1000).trim();
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("--- Shader Program ---");
		builder.append(System.lineSeparator());
		shaders.forEach(shader -> {
			builder.append(shader);
		});
		builder.append("---");
		return builder.toString();
	}
	
	public class Uniform {
		
		public final int location;
		public final String name;
		public final String type;
		public final int size;
		public final boolean array;
		
		private Uniform(String name, String type, int size) {
			location = glGetUniformLocation(program, name);
			// location is -1, if the uniform is not existing or not used in the shader. since we parse the file, we only get existing ones and can ignore this
			// if (location == -1) throw new ShaderException("Couln't find uniform \"" + name + "\" or it's not used in the shader.", ShaderProgram.this);
			this.name = name;
			this.type = type;
			this.size = size;
			array = name.contains("[]");
		}
		
		/**
		 * Passes double values as float values to the shader uniform.
		 */
		public ShaderProgram set(double... values) {
			float[] floats = new float[values.length];
			for (int i = 0; i < values.length; i++) {
				floats[i] = (float) values[i];
			}
			set(floats);
			return ShaderProgram.this;
		}
		
		/**
		 * Passes float values to the shader uniform.
		 */
		public ShaderProgram set(float... values) {
			if (array) {
				for (int i = 0; i < values.length; i++) {
					String index = name.replace("[]", "[" + i + "]");
					uniforms.get(index).set(values[i]);
				}
				return ShaderProgram.this;
			}
			switch (values.length) {
			case 1:
				glUniform1f(location, values[0]);
				break;
			case 2:
				glUniform2f(location, values[0], values[1]);
				break;
			case 3:
				glUniform3f(location, values[0], values[1], values[2]);
				break;
			case 4:
				glUniform4f(location, values[0], values[1], values[2], values[3]);
				break;
			default:
				throw new ShaderException("Maximum number of values to set for one uniform is 4.");
			}
			if (glGetError() != GL_NO_ERROR) new ShaderException("Couln't set uniform \"" + name + "\" to " + Arrays.asList(values), ShaderProgram.this);
			return ShaderProgram.this;
		}
		
		/**
		 * Passes int values to the shader uniform.
		 */
		public ShaderProgram set(int... values) {
			if (array) {
				for (int i = 0; i < values.length; i++) {
					String index = name.replace("[]", "[" + i + "]");
					uniforms.get(index).set(values[i]);
				}
				return ShaderProgram.this;
			}
			switch (values.length) {
			case 1:
				glUniform1i(location, values[0]);
				break;
			case 2:
				glUniform2i(location, values[0], values[1]);
				break;
			case 3:
				glUniform3i(location, values[0], values[1], values[2]);
				break;
			case 4:
				glUniform4i(location, values[0], values[1], values[2], values[3]);
				break;
			default:
				throw new ShaderException("Maximum number of values to set for one uniform is 4.");
			}
			if (glGetError() != GL_NO_ERROR) new ShaderException("Couln't set uniform \"" + name + "\" to " + Arrays.asList(values), ShaderProgram.this);
			return ShaderProgram.this;
		}
		
		@Override
		public String toString() {
			if (size > 1) return type + "[" + size + "] " + name;
			return type + " " + name;
		}
		
	}
	
	public class Shader {
		
		public final int id;
		public final int type;
		public final String filepath;
		public final String source;
		
		private Map<String, String> structs = new HashMap<>();
		
		/**
		 * Creates, loads and compiles a shader.
		 * 
		 * @param filepath path to the shader source file
		 * @param type shader type
		 */
		public Shader(String filepath, int type) {
			id = glCreateShader(type);
			this.filepath = filepath;
			this.type = type;
			source = load(filepath);
			compile(source, type);
		}
		
		@Override
		protected void finalize() throws Throwable {
			glDeleteShader(id);
		}
		
		private String load(String filepath) {
			StringBuilder source = new StringBuilder();
			try (BufferedReader reader = new BufferedReader(new FileReader(filepath))) {
				reader.lines().forEach(line -> {
					source.append(line).append('\n');
				});
			} catch (FileNotFoundException notFoundExternal) {
				InputStream inputStream = ClassLoader.getSystemResourceAsStream(filepath);
				if (inputStream == null) throw new RuntimeException("Couldn't find " + getType().toLowerCase() + " file.", notFoundExternal);
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream(filepath)))) {
					reader.lines().forEach(line -> {
						source.append(line).append('\n');
					});
				} catch (IOException exception) {
					throw new RuntimeException("Couldn't load shader \"" + filepath + "\".", exception);
				}
			} catch (IOException exception) {
				throw new RuntimeException("Couldn't load shader \"" + filepath + "\".", exception);
			}
			return source.toString();
		}
		
		private void findUniforms() {
			Matcher structMatcher = StructPattern.matcher(source);
			while (structMatcher.find()) {
				parseStruct(structMatcher.group());
			}
			Matcher uniformMatcher = UniformPattern.matcher(source);
			while (uniformMatcher.find()) {
				parseUniform(uniformMatcher.group());
			}
		}
		
		private void parseStruct(String struct) {
			int seperator = struct.indexOf('{');
			String type = struct.substring("struct".length(), seperator).trim();
			String content = struct.substring(seperator + 1, struct.length() - 1);
			structs.put(type, content);
		}
		
		private void parseUniform(String uniform) {
			parseUniform(uniform, null);
		}
		
		private void parseUniform(String uniform, String parent) {
			String[] split = uniform.split("\\s+|;|\\[|\\]");
			if (split[0].startsWith("//")) return;
			String type = (parent == null) ? split[1] : split[0];
			String name = (parent == null) ? split[2] : parent + "." + split[1];
			int size = 1;
			if (parent == null) {
				if (split.length == 4) size = parseSize(split[3]);
			} else {
				if (split.length == 3) size = parseSize(split[2]);
			}
			if (structs.containsKey(type)) {
				String[] components = structs.get(type).split(";");
				int arrayLength = size;
				Stream.of(components).map(String::trim).filter(s -> !s.isEmpty()).forEach(part -> {
					if (arrayLength == 1) {
						parseUniform(part, name);
					} else {
						parseUniform(part, name + "[]");
						for (int i = 0; i < arrayLength; i++) {
							parseUniform(part, name + "[" + i + "]");
						}
					}
				});
			} else {
				if (size > 1) type += "[" + size + "]";
				uniforms.put(name, new Uniform(name, type, size));
			}
		}
		
		private int parseSize(String string) {
			try {
				return Integer.parseInt(string);
			} catch (Exception exception) {
				Matcher matcher = Pattern.compile(string + "\\s*=\\s*\\d+\\s*;").matcher(source);
				if (matcher.find()) {
					String group = matcher.group();
					String number = group.substring(group.indexOf("=") + 1, group.length() - 1).trim();
					return Integer.parseInt(number);
				}
			}
			return 0;
		}
		
		private void compile(String source, int type) {
			glShaderSource(id, source);
			glCompileShader(id);
			if (glGetShaderi(id, GL_COMPILE_STATUS) == 0) throw new ShaderException("Couldn't compile shader \"" + filepath + "\".", this);
		}
		
		public String getType() {
			switch (type) {
			case GL_VERTEX_SHADER:
				return "Vertex Shader";
			case GL_FRAGMENT_SHADER:
				return "Fragment Shader";
			default:
				return "Unknown Shader";
			}
		}
		
		public String getLog() {
			return glGetShaderInfoLog(id, 1000).trim();
		}
		
		@Override
		public String toString() {
			return getType() + " \"" + filepath + "\":" + System.lineSeparator() + source;
		}
		
	}
	
	public static class ShaderException extends RuntimeException {
		
		private ShaderException(String message) {
			super(message);
		}
		
		private ShaderException(String message, ShaderProgram program) {
			super(message + System.lineSeparator() + program.getLog());
		}
		
		private ShaderException(String message, Shader shader) {
			super(message + System.lineSeparator() + shader.getLog());
		}
		
	}
	
}
