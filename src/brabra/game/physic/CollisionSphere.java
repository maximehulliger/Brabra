package brabra.game.physic;

import brabra.game.physic.geo.Line;
import brabra.game.physic.geo.Sphere;
import brabra.game.physic.geo.Vector;

public final class CollisionSphere extends Collision {
	Sphere sphere;
	
	public CollisionSphere(Sphere collider, Collider obstacle) {
		super(collider, obstacle);
		this.sphere = collider;
	}

	public void resolve() {
		if (!areCollidingFast(c1, c2))
			return;
		
		impact = c2.projette(sphere.location());
		if (distSq(sphere.location(), impact) >= sq(sphere.radius())) 
			return;
		
		Line colLine = c2.collisionLineFor(sphere.location());
		norm = colLine.norm;
		Vector toContact = norm.multBy(-1);
		toContact.setMag(sphere.radius());
		Vector contact = sphere.location().plus(toContact);
		correction = impact.minus(contact);
		nulle = false;
	}
}
