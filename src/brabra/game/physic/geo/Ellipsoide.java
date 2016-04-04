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
		Vector n = ligne.norm;
		Vector sc = vec(n.x*r.x, n.y*r.y, n.z*r.z);
		Vector c1 = locationAbs.plus(sc);
		Vector c2 = locationAbs.minus(sc);
		return ligne.projette( new Vector[] { c1, c2 } );
	}

	public Line collisionLineFor(Vector p) {
		Vector to = p.minus(locationAbs);
		Vector sc = vec(to.x/r.x, to.y/r.y, to.z/r.z); //en coordonees spheriques
		sc.normalize();
		Vector base = locationAbs.plus(sc.multElementsBy(r));
		return new Line(base , base.plus(sc), false);
	}

	public Vector projette(Vector point) {
		Vector sproj = super.projette(point.minus(locationAbs).divElementsBy(r));
		return sproj.multElementsBy(r).plus(locationAbs);
	}
}
