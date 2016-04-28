package brabra.game.physic.geo;
	
import brabra.game.physic.geo.Line.Projection;

public class Ellipsoide extends Sphere {
	
	private final Vector r;
	
	public Ellipsoide(Vector location, Quaternion rotation, Vector rayons) {
		super(location, rotation, max(rayons.array()));
		this.r = rayons.copy();
		setMyMass();
	}
	
	private void setMyMass() {
		if (inverseMass > 0) {
			float fact = mass*2/5;
			super.inertiaMom = new Vector(fact*r.x, fact*r.y, fact*r.z);
			super.inverseInertiaMom = new Vector(
					1/inertiaMom.x,
					1/inertiaMom.y,
					1/inertiaMom.z );
		}
	}

	public void display() {
		pushLocal();
		if (!displayColliderMaybe()) {
			color.fill();
			displayShape();
		}
		popLocal();
	}
	
	public void displayShape() {
		app.scale(r.x, r.y, r.z);
		app.sphere(1);
	}

	public Projection projetteSur(Line ligne) {
		final Vector locAbs = transform.location();
		Vector n = ligne.norm;
		Vector sc = vec(n.x*r.x, n.y*r.y, n.z*r.z);
		Vector c1 = locAbs.plus(sc);
		Vector c2 = locAbs.minus(sc);
		return ligne.projette( new Vector[] { c1, c2 } );
	}

	public Line collisionLineFor(Vector p) {
		final Vector locAbs = transform.location();
		Vector to = p.minus(locAbs);
		Vector sc = vec(to.x/r.x, to.y/r.y, to.z/r.z); //en coordonees spheriques
		sc.normalize();
		Vector base = locAbs.plus(sc.multElementsBy(r));
		return new Line(base , base.plus(sc), false);
	}

	public Vector projette(Vector point) {
		final Vector locAbs = transform.location();
		Vector sproj = super.projette(point.minus(locAbs).divElementsBy(r));
		return sproj.multElementsBy(r).plus(locAbs);
	}
}
