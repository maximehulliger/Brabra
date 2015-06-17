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
		
		impact = obstacle.projette(sphere.location);
		if (distSq(sphere.location, impact) >= sq(sphere.radius)) 
			return;
		
		Line colLine = obstacle.collisionLineFor(sphere.location);
		norm = colLine.norm;
		PVector contact = sphere.projette(impact);
		correction = PVector.sub(impact, contact);
		nulle = false;
	}
}
