package brabra.game.physic;

import brabra.game.physic.geo.Vector;
import brabra.game.physic.geo.Sphere;

public class CollisionSphereSphere extends Collision<Sphere, Sphere> {

	public CollisionSphereSphere(Sphere c1, Sphere c2) {
		super(c1, c2);
	}

	public void resolve() {
		Vector c1Toc2 = c2.location().minus(c1.location());
		float distSq = c1Toc2.magSq();
		if (distSq >= sq(c1.radius()) + sq(c2.radius()))
			ignore();
		else {
			float dist = sqrt(distSq);
			impact = c1.location().plus(c2.location()).div(2);
			norm = c1Toc2.normalized();
			correction = c1Toc2.withMag(dist - c1.radius() - c2.radius());
		}
	}
	
/*
	
	
	public Vector projette(Vector point) {
		Vector v = point.minus(location()).withMag(radius);
		return v.plus(location());
	}
	
	public Line collisionLineFor(Vector p) {
		//on prend le vecteur this->c. la ligne part du perimetre a  c.
		Vector sc = p.minus(location());
		sc.setMag(radius);
		Vector base = location().plus(sc);
		return new Line(base, base.plus(sc), false);
	}*/

}
