package brabra.game.physic.geo;

import brabra.game.physic.Collider;
import brabra.game.physic.geo.Line.Projection;
import brabra.game.scene.Object;
import brabra.game.scene.SceneLoader.Attributes;

/** A sphere. */
public class Sphere extends Collider {
	
	private float radius;

	public Sphere(Vector location, Quaternion rotation, float radius) {
		super(location, rotation);
	  	setName("Ball");
		setRadius(radius);
	}

	public Sphere(float radius) {
		this(Vector.zero, Quaternion.identity, radius);
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
	
	public static boolean areSpheresColliding(Vector p1, float r1, Vector p2, float r2) {
	    Vector v = p1.minus(p2);
	    return v.magSq() < (r1+r2)*(r1+r2);
	}

	public Projection projetteSur(Line ligne) {
		float proj = ligne.projectionFactor(location());
		return new Projection(proj-this.radius, proj+this.radius);
	}

	// --- Setters ---
	
	public void setRadius(float radius) {
		super.setRadiusEnveloppe(radius);
		this.radius = radius;
	    model.notifyChange(Change.Size);
	}
	
	public void setMass(float mass) {
		super.setMass(mass);
		if (inverseMass > 0) {
			final float fact = mass*radius*2/5;
			super.inertiaMom = Vector.cube(fact);
			super.inverseInertiaMom = Vector.cube(1/fact);
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
}