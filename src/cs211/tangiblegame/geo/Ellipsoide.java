package cs211.tangiblegame.geo;
	
import processing.core.PApplet;
import processing.core.PVector;
import cs211.tangiblegame.geo.Line.Projection;

public class Ellipsoide extends Sphere {
	
	private final PVector r;
	
	public Ellipsoide(PVector location, Quaternion rotation, PVector rayons) {
		super(location, rotation, PApplet.max(rayons.array()));
		this.r = rayons.copy();
		setMyMass();
	}
	
	private void setMyMass() {
		if (inverseMass > 0) {
			float fact = mass*2/5;
			super.inertiaMom = new PVector(fact*r.x, fact*r.y, fact*r.z);
			super.inverseInertiaMom = new PVector(
					1/inertiaMom.x,
					1/inertiaMom.y,
					1/inertiaMom.z );
		}
	}

	public void display() {
		pushLocal();
		color.fill();
		app.scale(r.x, r.y, r.z);
		app.sphere(1);
		popLocal();
	}

	public Projection projetteSur(Line ligne) {
		PVector n = ligne.norm;
		PVector sc = vec(n.x*r.x, n.y*r.y, n.z*r.z);
		PVector c1 = PVector.add(locationAbs, sc);
		PVector c2 = PVector.sub(locationAbs, sc);
		return ligne.projette( new PVector[] { c1, c2 } );
	}

	public Line collisionLineFor(PVector p) {
		PVector to = PVector.sub( p, locationAbs );
		PVector sc = vec(to.x/r.x, to.y/r.y, to.z/r.z); //en coordonees spheriques
		sc.normalize();
		PVector base = PVector.add(locationAbs, vec(sc.x*r.x, sc.y*r.y, sc.z*r.z));
		return new Line(base , PVector.add(base, sc), false);
	}

	public PVector projette(PVector point) {
		PVector to = PVector.sub( point, locationAbs );
		PVector sproj = super.projette( vec(to.x/r.x, to.y/r.y, to.z/r.z) );
		return PVector.add( vec(sproj.x*r.x, sproj.y*r.y, sproj.z*r.z), locationAbs );
	}
}
