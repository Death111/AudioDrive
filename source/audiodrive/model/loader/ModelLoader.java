package audiodrive.model.loader;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import audiodrive.model.geometry.Face;
import audiodrive.model.geometry.TextureCoordinate;
import audiodrive.model.geometry.Vector;
import audiodrive.model.geometry.VertexObject;

/**
 * This class loads a 3d-model from a obj-file. Can only load triangle faces!
 * 
 * @author Death
 *
 */
public class ModelLoader {
	// TODO Die textur(png) mit lwjgl laden und dem model zufügen irgendwie
	private static Logger logger = Logger.getLogger(ModelLoader.class);

	private ModelLoader() {
		throw new IllegalStateException("This class shall not be instantiated.");
	}

	/**
	 * Loads a .obj file into an Model-Object
	 * 
	 * @param fileName
	 *            Name of the file to be opened and parsed
	 * @return Loaded Model or null if an error occured
	 */
	public static Model loadModel(String fileName) {
		// TODO Implement exception handling

		logger.info("Model from file '" + fileName + "' shall be loaded.");

		logger.debug("Loading file '" + fileName + "' as Model");
		File file = new File(fileName);

		if (!file.exists()) {
			logger.error("Could no find file '" + fileName + "'.");
			return null;
		}

		String fileExtenstion = file.getName().substring(
				file.getName().lastIndexOf('.'));
		if (!fileExtenstion.equals(".obj")) {
			logger.error("File extension '" + fileExtenstion
					+ "' is not supported!");
			return null;
		}

		final List<Vector> vectors = new ArrayList<Vector>();
		final List<Face> faces = new ArrayList<Face>();
		final List<Vector> normals = new ArrayList<Vector>();
		final List<TextureCoordinate> textureCoordinates = new ArrayList<TextureCoordinate>();

		FileInputStream fstream;
		int modelCount = 0;
		try {
			fstream = new FileInputStream(file);

			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			String currentLine;
			// Loop through file line by line
			while ((currentLine = br.readLine()) != null) {

				String splitted[] = currentLine.split(" "); // Split at spaces

				Vector currentVector;
				int length = splitted.length;
				switch (splitted[0]) {
				case "v":
					// Vertex - Format: v x y z
					if (length != 4) {
						logger.error("Vertex with '" + length
								+ "' coordinates was given. Expected '3'.");
						return null;
					}
					currentVector = getVector(splitted);
					vectors.add(currentVector);
					break;
				case "vt":
					// Texture coordinates - Format: vt x y
					if (length != 3) {
						logger.error("TextureCoordinate with '" + length
								+ "' coordinates was given. Expected '2'.");
						return null;
					}
					TextureCoordinate textureCoordinate = new TextureCoordinate(
							Double.parseDouble(splitted[1]),
							Double.parseDouble(splitted[2]));
					textureCoordinates.add(textureCoordinate);
					break;
				case "vn":
					// Normale - Format: vn x y z
					if (length != 4) {
						logger.error("Normal with '" + length
								+ "' coordinates was given. Expected '3'.");
						return null;
					}
					currentVector = getVector(splitted);
					normals.add(currentVector);
					break;
				case "f":
					// Face - Format: f v1/vt1/vn1 v2/vt2/vn2 v3/vt3/vn3
					if (length != 4) {
						logger.error("Face with '"
								+ length
								+ "' corners was given. Only faces out of '3' vertexes are supported!");
						return null;
					}

					String[] vertex1 = splitted[1].split("/");
					String[] vertex2 = splitted[2].split("/");
					String[] vertex3 = splitted[3].split("/");

					VertexObject vertexObject1 = getVertexObject(vectors,
							normals, textureCoordinates, vertex1);
					VertexObject vertexObject2 = getVertexObject(vectors,
							normals, textureCoordinates, vertex2);
					VertexObject vertexObject3 = getVertexObject(vectors,
							normals, textureCoordinates, vertex3);

					Face face = new Face(vertexObject1, vertexObject2,
							vertexObject3);
					faces.add(face);
					break;

				case "#": // Comment
					break;
				case "o":
					modelCount++;
					logger.debug("Found object-declaration: '" + splitted[1]
							+ "'.");
					break;
				case "s": // smooth shading
					logger.warn("Smooth Shading was used, but is not yet supported.");
					break;
				default:
					logger.warn("unkown item '" + splitted[0] + "'");
					break;

				}
			}

			if (modelCount > 1) {
				logger.warn("More than '1' model was found in file. This model does now include '"
						+ modelCount + "' models.");
			}
			logger.info("Successfully loaded '" + faces.size() + "' faces.");
			Model model = new Model(faces);
			return model;

		} catch (FileNotFoundException fnfe) {
			logger.error(fnfe);
		} catch (NumberFormatException | IOException e) {
			logger.error(e);
		}

		return null;
	}

	private static VertexObject getVertexObject(final List<Vector> vectors,
			final List<Vector> normals,
			final List<TextureCoordinate> textureCoordinates, String[] vertex1) {

		VertexObject v1 = new VertexObject();

		// Parse Vertex
		try {
			int vertexIndex = Integer.parseInt(vertex1[0]);
			v1.position = vectors.get(vertexIndex - 1);
		} catch (NumberFormatException nfe) {
			logger.error("No vertex was given.");
		}

		// Parse texture coordinate
		try {
			int textureCoordinateIndex = Integer.parseInt(vertex1[1]);
			v1.textureCoordinate = textureCoordinates
					.get(textureCoordinateIndex - 1);
		} catch (NumberFormatException nfe) {
			logger.debug("No textureCoordinate was given.");
		}

		// Parse Normal
		try {
			int normalIndex = Integer.parseInt(vertex1[2]);
			v1.normal = normals.get(normalIndex - 1);
		} catch (NumberFormatException nfe) {
			logger.error("No normal was given.");
		}

		return v1;
	}

	private static Vector getVector(String[] splitted) {
		Vector currentVector;
		currentVector = new audiodrive.model.geometry.Vector(
				Double.parseDouble(splitted[1]),
				Double.parseDouble(splitted[2]),
				Double.parseDouble(splitted[3]));
		return currentVector;
	}
}
