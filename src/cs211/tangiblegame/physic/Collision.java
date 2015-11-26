package cs211.tangiblegame.physic;

import cs211.tangiblegame.ProMaster;
import processing.core.*;

/** représente une collision avec le point d'impact, la norme et les acteurs*/
public abstract class Collision extends ProMaster {
	protected final Collider collider;
	protected final Collider obstacle;
	protected PVector impact = null;
	protected PVector correction = null;
	protected PVector norm = null;
	protected boolean nulle = true;

	public Collision(Collider collider, Collider obstacle) {
		this.collider = collider;
		this.obstacle = obstacle;
		assert( !collider.affectedByCollision );
	}
	
	public abstract void resolve();
	
	public void apply() {
		if (nulle) return;
		
		PVector absVelocity = PVector.sub(collider.velocityAt(impact), obstacle.velocityAt(impact));
		
		float relVdotN = absVelocity.dot(norm);

		if (relVdotN > 0) //vitesse dans la même direction que la normale -> pas géré (ignoré)
			return;
		
		collider.location.add(correction);
		
		if (relVdotN == 0) //vitesse nulle ou perpendiculaire à la normale -> aucun effet
			return;	
			
		PVector impulse = PVector.mult(norm, -(obstacle.restitution+collider.restitution)*relVdotN/(collider.inverseMass+obstacle.inverseMass));
		
		if (collider.affectedByCollision)
			collider.applyImpulse(impact, impulse);
		if (obstacle.affectedByCollision)
			obstacle.applyImpulse(impact, PVector.mult(impulse, -1));
		
		
		collider.onCollision(obstacle, impact);
		obstacle.onCollision(collider, impact);
	}
	
	protected boolean areCollidingFast(Collider c1, Collider c2) {
		return c1.doCollideFast(c2) && c2.doCollideFast(c1);
	}
}
