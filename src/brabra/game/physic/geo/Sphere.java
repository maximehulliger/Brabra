package brabra.game.physic.geo;

import org.ode4j.ode.DMass;
import org.ode4j.ode.DSpace;
import org.ode4j.ode.DWorld;
import org.ode4j.ode.OdeHelper;

import brabra.game.physic.Collider;
import brabra.game.scene.Object;
import brabra.game.scene.SceneLoader.Attributes;

/** A sphere. */
public class Sphere extends Collider {
	
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
		super.setRadiusEnveloppe(radius);
		this.radius = radius;
	    model.notifyChange(Change.Size);
	}
	
	public void setMass(float mass) {
		super.setMass(mass);
		if (inverseMass > 0 && body != null) {
			DMass m = OdeHelper.createMass();
			m.setSphereTotal(mass, radius);
			super.body.setMass (m);
		}
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
		super.body = OdeHelper.createBody (world);
		//mass
		if (inverseMass > 0) {
			DMass m = OdeHelper.createMass();
			m.setSphereTotal(mass, radius);
			super.body.setMass (m);
		} else
			body.setKinematic();
		//shape
		super.geom = OdeHelper.createSphere (space, radius);
		super.geom.setBody(super.body);
		//location & rotation
		body.setPosition(position.toOde());
		body.setQuaternion(rotation.toOde());
	}
}