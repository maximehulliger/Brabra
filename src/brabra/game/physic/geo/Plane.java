package brabra.game.physic.geo;

import org.ode4j.ode.DMass;
import org.ode4j.ode.DPlane;
import org.ode4j.ode.DSpace;
import org.ode4j.ode.DWorld;
import org.ode4j.ode.OdeHelper;

import brabra.Debug;
import brabra.game.physic.Collider;
import brabra.game.scene.Object;
import brabra.game.scene.SceneLoader.Attributes;
import brabra.game.physic.geo.Vector;
import processing.core.PApplet;

/** A plane characterized by a normal++ and 2 direction vectors. Default norm is up (y+). Can be finite or infinite. */
public class Plane extends Collider {
	
	private final static Vector infiniteSize = Vector.cube(Float.POSITIVE_INFINITY);
	/** The size of the plane. using x & z. */
	private final Vector size = new Vector(); 
	/** Native relative coordonates (4 points). */
	private Vector[] natCo;
	/** Flag indicating if the plane is finite. */
	private boolean finite;

	// absolute variables:
	private Line normale, vx, vz;

	/** Create a plan (quad) of size size2d (x,z). */
	public Plane(Vector size2d) {
		super.setName("Quad");
		setSize(size2d);
	}
	
	/** Create an infinite plan (with infinite mass). */
	public Plane() {
		super.setName("Plane");
		setMass(-1);
		setSize(infiniteSize);
	}

	public void copy(Object o) {
		super.copy(o);
		Plane op;
		if ((op = this.as(Plane.class)) != null)
			setSize(op.size);
	}
	
	// --- Getters --- 

	/** Return the normal of the plane (starting in the plane). */
	public Line normale() {
		updateAbs();
		return normale;
	}

	/** Retourne un point appartenant au plan. le plan doit être fini. */
	public Vector randomPoint() {
		updateAbs();
		if (!finite)
			throw new IllegalArgumentException(
					"are you sure that do you want a random point in an infinite plane ?");
		return vx.base.plus(
				vx.norm.multBy(randomBi() * size.x), 
				vz.norm.multBy(randomBi() * size.z));
	}
	
	public Vector size() {
		return size;
	}
	
	// --- Setters ---

	public void setMass(float mass) {
		if(mass > 0 && !finite) {
			Debug.err("An infinite plane without an infinite mass is a bad idea :/");
			mass = 0;
		}
		super.setMass(mass);
		if (inverseMass > 0 && body != null) {
			DMass m = OdeHelper.createMass();
			m.setBoxTotal(mass, size.x, 0, size.z);
			super.body.setMass (m);
		} 
	}

	/** Set the size taking x & z from size2d. Set the plan to infinite if size2d is null. */
	public void setSize(Vector size2d) {
		if (size2d.x <= 0 || size2d.z <= 0) {
	    	Debug.err("A plane shouldn't have x or z size component null or smaller than 0: keeping "+this.size+" instead of "+size2d+".");
	    } else {
	    	size2d.y = 0;
			this.size.set(size2d);
			this.finite = size2d != infiniteSize;
			this.natCo = getNatCo(finite ? size2d : new Vector(1, 0, 1));
			
			if (!finite)
				setMass(-1);
			
			updateAbs();
		    model.notifyChange(Change.Size);
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

	protected void updateAbs() {
		// for plane
		Vector[] vertices = absolute(natCo);
		vx = new Line(vertices[0], vertices[1], finite); 	// x
		vz = new Line(vertices[0], vertices[2], finite); 	// z
		Vector norm = vz.norm.cross(vx.norm).normalized();
		normale = new Line(position, position.plus(norm), true);
	}

	public void validate(Attributes atts) {
		super.validate(atts);
		
		final String size = atts.getValue("size");
		if (size != null)
			setSize(vec(size));
	}
	
	// --- Collider & physic implementation ---

	public float projetteSur(Vector normale) {
		updateAbs();
		return vx.norm.dot(normale)*size.x/2 + vz.norm.dot(normale)*size.z/2;
	}

	public void displayShape() {
		float x = finite ? vx.vectorMag/2 : far;
		float z = finite ? vz.vectorMag/2 : far;
		app.beginShape(PApplet.QUADS);
	    app.vertex(-x,0,-z);
	    app.vertex(x,0,-z);
	    app.vertex(x,0,z);
	    app.vertex(-x,0,z);
		app.endShape();
	}

	// --- private ---

	private static Vector[] getNatCo(Vector size2d) {
		return new Vector[] { 
				new Vector(-size2d.x/2, 0, -size2d.z/2), 
				new Vector(size2d.x/2, 0, -size2d.z/2),
				new Vector(-size2d.x/2, 0, size2d.z/2), 
				new Vector(size2d.x/2, 0, size2d.z/2)};
	}

	@Override
	public void addToScene(DWorld world, DSpace space) {
		super.body = OdeHelper.createBody (world);
		//shape
		DPlane plane = OdeHelper.createPlane(space, -1, 1, 0, 0);
		super.geom = plane;
		super.geom.setBody(super.body);
		//location & rotation
		Vector norm = normale.norm;
		plane.setParams(norm.x, norm.y, norm.z, norm.x*position.x+norm.y*position.y+norm.z*position.z);
		//mass
		if (inverseMass > 0) {
			DMass m = OdeHelper.createMass();
			m.setBoxTotal(mass, size.x, 0, size.z);
			super.body.setMass (m);
		} else
			body.setKinematic();
	}
}