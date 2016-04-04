package brabra.game.physic;

import brabra.game.physic.geo.Line;
import brabra.game.physic.geo.Sphere;
import processing.core.PVector;

public final class CollisionSphere extends Collision {
	Sphere sphere;
	
	public CollisionSphere(Sphere collider, Collider obstacle) {
		super(collider, obstacle);
		this.sphere = collider;
	}

	public void resolve() {
		if (!areCollidingFast(collider, obstacle))
			return;
		
		impact = obstacle.projette(sphere.location());
		if (distSq(sphere.location(), impact) >= sq(sphere.radius)) 
			return;
		
		Line colLine = obstacle.collisionLineFor(sphere.location());
		norm = colLine.norm;
		PVector toContact = PVector.mult(norm, -1);
		toContact.setMag(sphere.radius);
		PVector contact = PVector.add(sphere.location(), toContact);
		correction = PVector.sub(impact, contact);
		nulle = false;
	}
}
