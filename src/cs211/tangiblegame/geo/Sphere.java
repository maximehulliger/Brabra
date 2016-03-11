package cs211.tangiblegame.geo;

import cs211.tangiblegame.geo.Line.Projection;
import cs211.tangiblegame.physic.Collider;
import processing.core.*;

/** une sphere. */
public class Sphere extends Collider {
	public float radius;

	public Sphere(PVector location, Quaternion rotation, float radius) {
		super(location, rotation, radius);
	  	this.radius = radius;
	  	setName("Ball");
	}

	public Sphere(PVector location, float radius) {
		this(location, identity, radius);
	}
	
	public void setMass(float mass) {
		super.setMass(mass);
		if (inverseMass > 0) {
			float fact = mass*radius*2/5;
			super.inertiaMom = new PVector(fact, fact, fact);
			super.inverseInertiaMom = new PVector(
					1/inertiaMom.x,
					1/inertiaMom.y,
					1/inertiaMom.z );
		}
	}
	
	public Projection projetteSur(Line ligne) {
	  float proj = ligne.projectionFactor(locationAbs);
	  return new Projection(proj-this.radius, proj+this.radius);
	}
	
	public PVector[] getIntruderPointOver(Line colLine) {
		PVector[] candidates = new PVector[2];
		candidates[0] = PVector.add( locationAbs, PVector.mult(colLine.norm, radius));
		candidates[1] = PVector.sub( locationAbs, PVector.mult(colLine.norm, radius));
		return colLine.intruders(candidates);
	}
	
	public PVector projette(PVector point) {
		PVector v = PVector.sub( point, locationAbs );
		v.setMag(radius);
		return PVector.add( v, locationAbs );
	}
	
	public void display() {
		pushLocal();
		color.fill();
		app.sphere(radius);
		popLocal();
	}
	
	public Line collisionLineFor(PVector p) {
		//on prend le vecteur this->c. la ligne part du perimetre à c.
		PVector sc = PVector.sub(p, locationAbs);
		sc.setMag(radius);
		PVector base = PVector.add(locationAbs, sc);
		return new Line( base,
				PVector.add(base, sc), false);
	}
	
	public static boolean areSpheresColliding(PVector p1, float r1, PVector p2, float r2) {
	    PVector v = PVector.sub( p1, p2);
	    return v.magSq() < (r1+r2)*(r1+r2);
	}
}