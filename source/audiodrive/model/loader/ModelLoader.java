package audiodrive.model.loader;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL14.GL_TEXTURE_LOD_BIAS;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.newdawn.slick.opengl.Texture;

import audiodrive.Resources;
import audiodrive.model.geometry.Face;
import audiodrive.model.geometry.TextureCoordinate;
import audiodrive.model.geometry.Vector;
import audiodrive.model.geometry.Vertex;
import audiodrive.utilities.Files;
import audiodrive.utilities.Log;

/**
 * This class loads a 3d-model from a obj-file. Can only load triangle faces!
 * 
 * @author Death
 *
 */
public class ModelLoader {
	
	private static boolean smoothShadingWarning;
	
	private ModelLoader() {
		throw new IllegalStateException("This class shall not be instantiated.");
	}
	
	/**
	 * Loads a .obj file into one Model-Object (combines multiple objects to one)
	 * 
	 * @param fileName Name of the file to be opened and parsed (without extension, .obj and .png will be automatically set)
	 * @return Loaded Model or null if an error occured
	 */
	public static Model loadModel(String fileName) {
		List<Model> models = loadModels(fileName);
		List<Face> faces = new ArrayList<Face>();
		
		if (models == null) return null;
		
		Log.debug("As requested combine '" + models.size() + "' models into one.");
		for (Model model : models) {
			final List<Face> currentFaces = model.getFaces();
			faces.addAll(currentFaces);
		}
		
		// TODO What shall the name and texture of the model be?
		final Model model = new Model(Files.plainName(fileName), faces);
		final Texture texture = models.get(0).getTexture();
		model.setTexture(texture);
		glGenerateMipmap(GL_TEXTURE_2D);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_LOD_BIAS, -0.5f);
		return model;
	}
	
	/**
	 * Loads a .obj file into n Model-Objects ( where n is the number of declared objects in the file)
	 * 
	 * @param fileName Name of the file to be opened and parsed (without extension, .obj and .png will be automatically set)
	 * @return Loaded Model or null if an error occured
	 */
	public static List<Model> loadModels(String fileName) {
		// TODO Implement exception handling
		if (fileName.endsWith(".obj")) fileName = fileName.substring(0, fileName.indexOf(".obj"));
		
		final String modelFileName = fileName + ".obj";
		final String textureFileName = fileName + ".png";
		
		Log.debug("Loading model '" + modelFileName + "'");
		
		URL resource = Resources.get(modelFileName);
		Optional<Texture> texture = Resources.getOptional(textureFileName).map(Resources::getTexture);
		
		List<Model> models = new ArrayList<Model>();
		
		final List<Vector> vectors = new ArrayList<Vector>();
		final List<Vector> normals = new ArrayList<Vector>();
		final List<TextureCoordinate> textureCoordinates = new ArrayList<TextureCoordinate>();
		
		List<Face> faces = null;
		
		String modelName = "";
		int modelCount = 0;
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new DataInputStream(resource.openStream())))) {
			
			String currentLine;
			// Loop through file line by line
			while ((currentLine = reader.readLine()) != null) {
				
				String splitted[] = currentLine.split(" "); // Split at spaces
				
				Vector currentVector;
				int length = splitted.length;
				
				switch (splitted[0]) {
				case "v":
					// Vertex - Format: v x y z
					if (length != 4) {
						Log.error("Vertex with '" + length + "' coordinates was given. Expected '3'.");
						return null;
					}
					currentVector = getVector(splitted);
					vectors.add(currentVector);
					break;
				case "vt":
					// Texture coordinates - Format: vt x y
					if (length != 3) {
						Log.error("TextureCoordinate with '" + length + "' coordinates was given. Expected '2'.");
						return null;
					}
					TextureCoordinate textureCoordinate = new TextureCoordinate(Double.parseDouble(splitted[1]), -1 * Double.parseDouble(splitted[2]));
					textureCoordinates.add(textureCoordinate);
					break;
				case "vn":
					// Normale - Format: vn x y z
					if (length != 4) {
						Log.error("Normal with '" + length + "' coordinates was given. Expected '3'.");
						return null;
					}
					currentVector = getVector(splitted);
					normals.add(currentVector);
					break;
				case "f":
					// Face - Format: f v1/vt1/vn1 v2/vt2/vn2 v3/vt3/vn3
					if (length != 4) {
						Log.error("Face with '" + length + "' corners was given. Only faces out of '3' vertexes are supported!");
						return null;
					}
					
					String[] vertex1 = splitted[1].split("/");
					String[] vertex2 = splitted[2].split("/");
					String[] vertex3 = splitted[3].split("/");
					
					Vertex vertexObject1 = getVertexObject(vectors, normals, textureCoordinates, vertex1);
					Vertex vertexObject2 = getVertexObject(vectors, normals, textureCoordinates, vertex2);
					Vertex vertexObject3 = getVertexObject(vectors, normals, textureCoordinates, vertex3);
					
					Face face = new Face(vertexObject1, vertexObject2, vertexObject3);
					faces.add(face);
					break;
				
				case "#": // Comment
					break;
				case "o":
					String newModelName = splitted[1];
					Log.debug("Found object-declaration: '" + newModelName + "'.");
					modelCount++;
					
					if (faces != null) {
						Log.debug("Saving model '" + modelName + "'.");
						Model model = new Model(modelName, faces);
						texture.ifPresent(model::setTexture);
						models.add(model);
					}
					modelName = newModelName;
					faces = new ArrayList<Face>();
					break;
				case "s": // smooth shading
					if (!smoothShadingWarning) {
						Log.warning("Smooth Shading was used, but is not yet supported.");
						smoothShadingWarning = true;
					}
					break;
				default:
					Log.warning("unkown item '" + splitted[0] + "'");
					break;
				
				}
			}
			
			if (faces != null) {
				Log.debug("Saving model '" + modelName + "'.");
				Model model = new Model(modelName, faces);
				texture.ifPresent(model::setTexture);
				models.add(model);
			}
			
			Log.debug("Successfully loaded '" + modelCount + "' models.");
			return models;
			
		} catch (Exception e) {
			Log.error("Error while loading model: " + e);
		}
		
		return null;
	}
	
	private static Vertex getVertexObject(final List<Vector> vectors, final List<Vector> normals, final List<TextureCoordinate> textureCoordinates, String[] vertex1) {
		
		Vertex v1 = new Vertex();
		
		// Parse Vertex
		try {
			int vertexIndex = Integer.parseInt(vertex1[0]);
			v1.position = vectors.get(vertexIndex - 1);
		} catch (NumberFormatException nfe) {
			Log.error("No vertex was given.");
		}
		
		// Parse texture coordinate
		try {
			int textureCoordinateIndex = Integer.parseInt(vertex1[1]);
			v1.textureCoordinate = textureCoordinates.get(textureCoordinateIndex - 1);
		} catch (NumberFormatException nfe) {
			Log.trace("No textureCoordinate was given.");
		}
		
		// Parse Normal
		try {
			int normalIndex = Integer.parseInt(vertex1[2]);
			v1.normal = normals.get(normalIndex - 1);
		} catch (NumberFormatException nfe) {
			Log.error("No normal was given.");
		}
		
		return v1;
	}
	
	private static Vector getVector(String[] splitted) {
		Vector currentVector;
		currentVector = new audiodrive.model.geometry.Vector(Double.parseDouble(splitted[1]), Double.parseDouble(splitted[2]), Double.parseDouble(splitted[3]));
		return currentVector;
	}
}
