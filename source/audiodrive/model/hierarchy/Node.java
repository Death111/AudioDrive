package audiodrive.model.hierarchy;

import java.util.ArrayList;
import java.util.List;

import audiodrive.model.Renderable;
import audiodrive.model.geometry.transform.Placement;
import audiodrive.model.geometry.transform.Rotation;
import audiodrive.model.geometry.transform.Scaling;
import audiodrive.model.geometry.transform.Transformation;
import audiodrive.model.geometry.transform.Translation;
import audiodrive.ui.GL;

public abstract class Node implements Renderable {
	
	private List<Transformation> transformations = new ArrayList<>();
	private List<Node> children = new ArrayList<>();
	
	private Placement placement = new Placement();
	private Translation translation = new Translation();
	private Rotation rotation = new Rotation();
	private Scaling scaling = new Scaling();
	
	@Override
	public void render() {
		boolean transformed = hasTransformations();
		if (transformed) GL.pushMatrix();
		transform();
		draw();
		children.forEach(Node::render);
		if (transformed) GL.popMatrix();
	}
	
	public boolean hasTransformations() {
		if (!placement.ignorable() || !translation.ignorable() || !rotation.ignorable() || !scaling.ignorable()) return true;
		return transformations.stream().anyMatch(transformation -> !transformation.ignorable());
	}
	
	public void transform() {
		placement.apply();
		translation.apply();
		rotation.apply();
		scaling.apply();
		transformations.stream().forEach(Transformation::apply);
	}
	
	public abstract void draw();
	
	public List<Transformation> transformations() {
		return transformations;
	}
	
	public List<Node> children() {
		return children;
	}
	
	public Placement placement() {
		return placement;
	}
	
	public Translation translation() {
		return translation;
	}
	
	public Rotation rotation() {
		return rotation;
	}
	
	public Scaling scaling() {
		return scaling;
	}
	
	public Node scaling(Scaling scaling) {
		this.scaling = scaling;
		return this;
	}
	
}
