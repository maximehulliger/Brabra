package brabra.game.physic;

import brabra.ProMaster;
import brabra.game.physic.geo.Vector;

/** Represent a collision with an impact point, a norm & 2 colliders. */
public abstract class Collision extends ProMaster {
	protected final Collider c1, c2;
	protected Vector impact = null;
	protected Vector correction = null;
	protected Vector norm = null;
	protected boolean nulle = true;

	public Collision(Collider c1, Collider c2) {
		this.c1 = c1;
		this.c2 = c2;
		assert( c1.affectedByPhysic() || c2.affectedByPhysic() );
		assert( c1.inverseMass > 0 || c1.inverseMass > 0 );
	}
	
	public abstract void resolve();
	
	public void apply() {
		if (!nulle) {
			
			// absolute velocity difference from c2 to c1
			Vector velAbs = c1.velocityAt(impact).minus(c2.velocityAt(impact));
			
			float relVdotN = velAbs.dot(norm);
			
			// to avoid (=0) velocity null or perpendiculaire, or object already leaving each other.
			if (relVdotN >= 0) //vitesse dans la même direction que la normale -> pas géré (ignoré)
				return;
			
			// TODO relative to the mass of each
			c1.move(correction);
			
			Vector impulse = norm.multBy( -(c2.restitution+c1.restitution)*relVdotN/(c1.inverseMass+c2.inverseMass) );
			
			if (c1.affectedByPhysic())
				c1.applyImpulse(impact, impulse);
			if (c2.affectedByPhysic())
				c2.applyImpulse(impact, impulse.multBy(-1));
			
			
			c1.onCollision(c2, impact);
			c2.onCollision(c1, impact);
		}
	}
	
	protected boolean areCollidingFast(Collider c1, Collider c2) {
		return c1.doCollideFast(c2) && c2.doCollideFast(c1);
	}
}
