package game.geo;

import game.physic.Collider;
import processing.core.*;

/** une sphere. */
public class Sphere extends Collider {
	public float radius;
	
	public Sphere(PVector location, float mass, float radius) {
	  super(location, zero, radius);
	  this.radius = radius;
	  setMass(mass);
	}
	
	protected void setMass(float mass) {
		super.setMass(mass);
		if (inverseMass > 0) {
			//float fact = mass/12;
			float fact = mass*radius*2/5;
			super.inertiaMom = new PVector(fact, fact, fact);
			super.inverseInertiaMom = new PVector(
					1/inertiaMom.x,
					1/inertiaMom.y,
					1/inertiaMom.z );
		}
	}
	
	public Line.Projection projetteSur(Line ligne) {
	  float proj = ligne.projectionFactor(this.location);
	  return ligne.new Projection(proj-this.radius, proj+this.radius);
	}
	
	public PVector[] getIntruderPointOver(Line colLine) {
		PVector[] candidates = new PVector[2];
		candidates[0] = PVector.add( location, PVector.mult(colLine.norm, radius));
		candidates[1] = PVector.sub( location, PVector.mult(colLine.norm, radius));
		return colLine.intruders(candidates);
	}
	
	public PVector projette(PVector point) {
		PVector v = PVector.sub( point, location );
		v.setMag(radius);
		return PVector.add( v, location );
	}
	
	public void display() {
		pushLocal();
		app.sphere(radius);
		popLocal();
	}
	
	public Line collisionLineFor(PVector p) {
		//on prend le vecteur this->c. la ligne part du perimetre à c.
		PVector sc = PVector.sub(p, location);
		PVector out = null;
		sc.setMag(out, radius);
		return new Line( PVector.add(location, sc), p, false);
	}
	
	public static boolean areSpheresColliding(PVector p1, float r1, PVector p2, float r2) {
	    PVector v = PVector.sub( p1, p2);
	    return v.magSq() < (r1+r2)*(r1+r2);
	}
}