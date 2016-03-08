package cs211.tangiblegame.physic;

import cs211.tangiblegame.geo.Line;
import cs211.tangiblegame.geo.Sphere;
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
		
		impact = obstacle.projette(sphere.locationAbs);
		if (distSq(sphere.locationAbs, impact) >= sq(sphere.radius)) 
			return;
		
		Line colLine = obstacle.collisionLineFor(sphere.locationAbs);
		norm = colLine.norm;
		PVector toContact = PVector.mult(norm, -1);
		toContact.setMag(sphere.radius);
		PVector contact = PVector.add(sphere.locationAbs, toContact);
		correction = PVector.sub(impact, contact);
		nulle = false;
	}
}
