package brabra.game.physic;

import brabra.ProMaster;
import brabra.game.physic.geo.Vector;

/** Represent a collision with 2 colliders (c1 & c2), an impact point on c1 and a norm. */
public abstract class Collision<C1 extends Collider, C2 extends Collider> extends ProMaster {
	protected final C1 c1;
	protected final C2 c2;
	protected Vector impact = null;
	protected Vector norm = null;
	protected Vector correction = null;
	protected boolean ignored = false;

	public Collision(C1 c1, C2 c2) {
		this.c1 = c1;
		this.c2 = c2;
		assert( c1.affectedByPhysic() || c2.affectedByPhysic() );
	}
	
	/** method to solve the collision, meaning set impact, norm & correction. */
	public abstract void resolve();
	
	public void apply() {
		if (!ignored) {
			
			// absolute velocity difference from c2 to c1
			Vector velAbs = c2.velocityAt(impact).minus(c1.velocityAt(impact));
			
			float relVdotN = velAbs.dot(norm);
			
			// to avoid (=0) velocity null or perpendiculaire, or object already leaving each other.
			if (relVdotN >= 0) //vitesse dans la même direction que la normale -> pas géré (ignoré)
				return;
			
			// TODO relative to the mass of each
			c2.move(correction);
			
			Vector r1 = impact.minus(c1.location());
			Vector r2 = impact.minus(c2.location());
			Vector i1 = c1.inverseInertiaMom.multElementsBy(r1.copy().cross(norm).cross(r1));
			Vector i2 = c2.inverseInertiaMom.multElementsBy(r2.copy().cross(norm).cross(r2));
			float forRot = i1.plus(i2).dot(norm);
			Vector impulse = norm.multBy( -(1+c2.restitution*c1.restitution)*relVdotN/(c1.inverseMass+c2.inverseMass+forRot) );
			
			if (c1.affectedByPhysic())
				c1.applyImpulse(impact, impulse.multBy(-1));
			if (c2.affectedByPhysic())
				c2.applyImpulse(impact, impulse);
			
			
			c1.onCollision(c2, impact);
			c2.onCollision(c1, impact);
		}
	}
	
	public void ignore() {
		ignored = true;
	}
	
	protected boolean areCollidingFast(Collider c1, Collider c2) {
		return c1.doCollideFast(c2) && c2.doCollideFast(c1);
	}
}
