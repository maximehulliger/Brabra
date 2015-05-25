package game.physic;

import processing.core.PVector;
import game.geo.Line;
import game.geo.Sphere;

public final class CollisionSphere extends Collision {
	Sphere sphere;
	
	public CollisionSphere(Sphere collider, Collider obstacle) {
		super(collider, obstacle);
		this.sphere = collider;
	}

	public void resolve() {
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
