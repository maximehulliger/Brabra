package brabra.game.physic;

import brabra.ProMaster;
import processing.core.*;

/** Represent a collision with an impact point, a norm & 2 colliders. */
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
		//assert( !collider.affectedByCollision ); TODO
	}
	
	public abstract void resolve();
	
	public void apply() {
		if (nulle) return;
		
		PVector absVelocity = PVector.sub(collider.velocityAt(impact), obstacle.velocityAt(impact));
		
		float relVdotN = absVelocity.dot(norm);

		if (relVdotN > 0) //vitesse dans la même direction que la normale -> pas géré (ignoré)
			return;
		
		// TODO: should be loc abs does nothing now (locationAbs doesn't update locationRel.. yet)
		//collider.location().add(correction);
		
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
