package brabra.game.physic.geo;

import brabra.Debug;
import brabra.game.physic.Collider;
import brabra.game.physic.PseudoPolyedre;
import brabra.game.physic.geo.Line.Projection;
import brabra.game.scene.Object;
import brabra.game.scene.SceneLoader.Attributes;
import brabra.game.physic.geo.Vector;
import processing.core.PApplet;

/** A plane characterized by a normal++ and 2 direction vectors. Default norm is up (y+). Can be finite or infinite. */
public class Plane extends PseudoPolyedre {
	
	/** The size of the plane. using x & z. */
	private final Vector size = new Vector(); 
	/** Native relative coordonates (4 points). */
	private Vector[] natCo;
	/** Flag indicating if the plane is finite. */
	private boolean finite;

	// absolute variables:
	private Line normale, vx, vz;

	/** Create a plan (quad) of size size2d (x,z). */
	public Plane(Vector loc, Quaternion rot, Vector size2d) {
		super(loc, rot);
		super.setName("Quad");
		setSize(size2d);
	}

	/** Create a plan (quad) of size size2d (x,z). */
	public Plane(Vector size2d) {
		this(Vector.zero, Quaternion.identity, size2d);
	}
	
	/** Create an infinite plan (with infinite mass). */
	public Plane(Vector loc, Quaternion rot) {
		super(loc, rot);
		super.setName("Plane");
		setSize(infiniteSize);
	}

	/** Create an infinite plan (with infinite mass). */
	public Plane() {
		this(Vector.zero, Quaternion.identity);
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
		if (inverseMass > 0) {
			float fact = mass/12;
			super.inertiaMom = new Vector(
					fact*sq(size.z), 
					fact*(sq(size.x) + sq(size.z)), 
					fact*sq(size.x) );
			super.inverseInertiaMom = new Vector(
					1/inertiaMom.x,
					1/inertiaMom.y,
					1/inertiaMom.z );
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
			
			super.setRadiusEnveloppe(finite ? size2d.mag()/2 : Float.POSITIVE_INFINITY);
			if (!finite && !ghost())
				setMass(-1);
			
			updateAbs();
		    model.notifyChange(Change.Size);
	    }
	}

	// --- life cycle ---

	public void display() {
		pushLocal();
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
		normale = new Line(location(), location().plus(norm), true);
		// for polyhedron
		Line v3 = new Line(vertices[1], vertices[3], true); // x'
		Line v4 = new Line(vertices[2], vertices[3], true); // z'
		setAbs(vertices, new Line[] { vx, vz, v3, v4 });
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

	public Line collisionLineFor(Vector p) {
		return isFacing(p) ? normale() : new Line(projette(p), p, false);
	}

	public boolean isIn(Vector abs) {
		return isFacing(abs) && normale().projectionFactor(abs)<0;
	}

	public Vector projette(Vector point) {
		updateAbs();
		return add( vx.projette(point) , vz.projetteLocal(point) );
	}

	public boolean doCollideFast(Collider c) {
		return c.projetteSur(normale()).comprend(0) && (!finite || super.doCollideFast(c));
	}

	public Vector pointContre(Vector normale) {
		if (!finite) {
			Debug.log("sure ?");
			return normale.multBy(Float.POSITIVE_INFINITY);
		}
		updateAbs();
		return Vector.sumOf(location(),
				vx.norm.multBy(size.x * -0.5f * sgn(vx.norm.dot(normale))),
				vz.norm.multBy(size.z * -0.5f * sgn(vz.norm.dot(normale))));
	}

	public Projection projetteSur(Line ligne) {
		updateAbs();
		if (ligne == normale)
			throw new IllegalArgumentException("why projecting a plane on himself ??");
			//return new Projection(0, 0);
		else if (finite)
			return ligne.projette( vertices() );
		else
			return new Projection(Float.MIN_VALUE, Float.MAX_VALUE);
	}

	public Plane[] plansSeparationFor(Vector colliderLocation) {
		updateAbs();
		return new Plane[] {this};
	}

	// --- private ---

	private boolean isFacing(Vector p) {
		updateAbs();
		return !finite || (vx.isFacing(p) && vz.isFacing(p));
	}

	private static Vector[] getNatCo(Vector size2d) {
		return new Vector[] { 
				new Vector(-size2d.x/2, 0, -size2d.z/2), 
				new Vector(size2d.x/2, 0, -size2d.z/2),
				new Vector(-size2d.x/2, 0, size2d.z/2), 
				new Vector(size2d.x/2, 0, size2d.z/2)};
	}
}