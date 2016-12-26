package brabra.game.physic.geo;

import org.ode4j.ode.DBody;
import org.ode4j.ode.DGeom;
import org.ode4j.ode.DMass;
import org.ode4j.ode.DSpace;
import org.ode4j.ode.DWorld;
import org.ode4j.ode.OdeHelper;

import brabra.game.physic.Body;
import brabra.game.scene.Object;
import brabra.game.scene.SceneLoader.Attributes;

/** A sphere. */
public class Sphere extends Body {
	
	private float radius;

	public Sphere(float radius) {
		setName("Ball");
		setRadius(radius);
	}

	public void copy(Object o) {
		super.copy(o);
		Sphere os = this.as(Sphere.class);
		if (os != null) {
			setRadius(os.radius);
		}
	}
	
	// --- Getters ---
	
	public float radius() {
		return radius;
	}
	
	// --- Setters ---
	
	public void setRadius(float radius) {
		this.radius = radius;
	    model.notifyChange(Change.Size);
	}
	
	public void setOdeMass(DBody body) {
		if (mass() > 0) {
			DMass m = OdeHelper.createMass();
			m.setSphereTotal(mass(), radius);
			body.setMass (m);
		} else
			body.setKinematic();
	}
	
	// --- life cycle ---

	public void display() {
		pushLocal();
		displayInteractionMaybe();
		if (!displayColliderMaybe()) {
			color.fill();
			displayShape();
		}
		popLocal();
	}

	public void displayShape() {
		app.sphere(radius);
	}

	public void validate(Attributes atts) {
		super.validate(atts);
		
		final String tRadius = atts.getValue("radius");
		if (tRadius != null)
			setRadius(Float.parseFloat(tRadius));
		else {
			final String tSize = atts.getValue("size");
			if (tSize != null)
				setRadius(Float.parseFloat(tSize));
		}
	}

	@Override
	public void addToScene(DWorld world, DSpace space) {
		DBody body = OdeHelper.createBody (world);
		DGeom geom = OdeHelper.createSphere (space, radius);
		geom.setBody(body);
		setBody(body);
	}
}