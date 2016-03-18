package cs211.tangiblegame.geo;

import cs211.tangiblegame.geo.Line.Projection;
import cs211.tangiblegame.physic.Collider;
import cs211.tangiblegame.physic.PseudoPolyedre;
import processing.core.*;

/** A plane characterized by 3 points. Default norm is up. Can be finite or infinite. */
public class Plane extends PseudoPolyedre {
	
	private final PVector size; //x et z
	private final PVector[] natCo; 	//native relative coordonates (4 points)
	private final boolean finite;

	// absolute variables:
	private Line normale;
	private Line v1; //sur x
	private Line v2; //sur z

	/** Create a plan (quad) of size size2d (x,z). */
	public Plane(PVector loc, Quaternion rot, PVector size2d) {
		super( loc, rot, size2d.mag()/2 );
		this.size = size2d;
		this.natCo = getNatCo(size2d);
		this.finite = true;
		setName("Quad");
	}

	/** Create an infinite plan. */
	public Plane(PVector loc, Quaternion rot) {
		super( loc, rot, Float.MAX_VALUE );
		this.size = null;
		this.natCo = getNatCo(new PVector(1, 0, 1));
		this.finite = false;
		setName("Plane");
	}

	/** Return the normal of the plane (starting in the plane). */
	public Line normale() {
		updateAbs();
		return normale;
	}

	/** Retourne un point appartenant au plan. le plan doit être fini. */
	public PVector randomPoint() {
		updateAbs();
		if (!finite)
			throw new IllegalArgumentException(
					"are you sure that do you want a random point in an infinite plane ?");
		return add(v1.base.copy(),
				mult(v1.norm, random.nextFloat() * size.x),
				PVector.mult(v2.norm, random.nextFloat() * size.z));
	}
	
	//------ surcharge:

	public void setMass(float mass) {
		super.setMass(mass);
		if (!finite && !ghost && mass!=-1)
			throw new IllegalArgumentException("An infinite plane without an infinite mass is a bad idea :/");
		if (mass!=-1 && inverseMass > 0) {
			float fact = mass/12;
			super.inertiaMom = new PVector(
					fact*sq(size.z), 
					fact*(sq(size.x) + sq(size.z)), 
					fact*sq(size.x) );
			super.inverseInertiaMom = new PVector(
					1/inertiaMom.x,
					1/inertiaMom.y,
					1/inertiaMom.z );
		}
	}

	public float projetteSur(PVector normale) {
		updateAbs();
		return v1.norm.dot(normale)*size.x/2 + v2.norm.dot(normale)*size.z/2;
	}

	public void display() {
		pushLocal();
		color.fill();
		float x = finite ? v1.vectorMag/2 : far;
		float z = finite ? v2.vectorMag/2 : far;
		app.beginShape(PApplet.QUADS);
	    app.vertex(-x,0,-z);
	    app.vertex(x,0,-z);
	    app.vertex(x,0,z);
	    app.vertex(-x,0,z);
		app.endShape();
		popLocal();
	}

	public Line collisionLineFor(PVector p) {
		if (isFacing(p))
			return normale();
		else
			return new Line(projette(p), p, false);
	}

	public boolean isIn(PVector abs) {
		updateAbs();
		return isFacing(abs) && normale.projectionFactor(abs)<0;
	}

	public PVector projette(PVector point) {
		updateAbs();
		return add( v1.projette(point) , v2.projetteLocal(point) );
	}

	public boolean doCollideFast(Collider c) {
		updateAbs();
		return c.projetteSur(normale).comprend(0) && (!finite || super.doCollideFast(c));
	}

	//retourne le point qui est le plus contre cette normale (par rapport au centre)
	public PVector pointContre(PVector normale) {
		if (!finite)
			return vec(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE); //TODO
		updateAbs();
		PVector proj = PVector.mult( v1.norm, size.x/2 * -sgn(v1.norm.dot(normale)) );
		proj.add( PVector.mult( v2.norm, size.z/2 * -sgn(v2.norm.dot(normale))) );
		proj.add(locationAbs);
		return proj;
	}

	public Projection projetteSur(Line ligne) {
		updateAbs();
		if (ligne == normale)
			throw new IllegalArgumentException("why projecting a plane on himself ??");
			//return new Projection(0, 0);
		else if (finite)
			return ligne.projette( sommets );
		else
			return new Projection(Float.MIN_VALUE, Float.MAX_VALUE);
	}

	public Plane[] plansSeparationFor(PVector colliderLocation) {
		updateAbs();
		return new Plane[] {this};
	}

	public boolean updateAbs() {
		if (super.updateAbs()) {
			super.sommets = absolute(natCo);
	
			v1 = new Line(sommets[0], sommets[1], finite); //sur x
			v2 = new Line(sommets[0], sommets[2], finite); //sur z
	
			PVector norm = v2.norm.cross(v1.norm);
			norm.normalize(); // ? plus besoin
			normale = new Line(sommets[0], PVector.add( sommets[0], norm ), false);
	
			super.arretes = new Line[] {
					v1, v2,
					new Line(sommets[1], sommets[3], true),
					new Line(sommets[2], sommets[3], true)};
			return true;
		} else
			return false;
	}

	//----- private

	private boolean isFacing(PVector p) {
		updateAbs();
		return !finite || (v1.isFacing(p) && v2.isFacing(p));
	}

	private static PVector[] getNatCo(PVector size2d) {
		return new PVector[] { 
				new PVector(-size2d.x/2, 0, -size2d.z/2), 
				new PVector(size2d.x/2, 0, -size2d.z/2),
				new PVector(-size2d.x/2, 0, size2d.z/2), 
				new PVector(size2d.x/2, 0, size2d.z/2)};
	}
}