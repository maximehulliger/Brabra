package cs211.tangiblegame.physic;

import cs211.tangiblegame.geo.Line;
import cs211.tangiblegame.geo.Line.Projection;
import cs211.tangiblegame.geo.Quaternion;
import processing.core.*;

/** A class able to init and react to a collision. */
public abstract class Collider extends Body {
	public final static boolean drawCollider = false;
	
	public final float radiusEnveloppe;
	
	public Collider(PVector location, Quaternion rotation, float radiusEnveloppe) {
	  super(location, rotation);
	  this.radiusEnveloppe = radiusEnveloppe;
	}
	
	public Collider withName(String name) {
		super.setName(name);
		return this;
	}
	
	public boolean doCollideFast(Collider c) {
		boolean parency = (hasParent() && parent() == c || c.parent() == this || (parent() == c.parent()));
		assert (!parency);
		boolean contactEnveloppe = PVector.sub(this.locationAbs, c.locationAbs).magSq() < sq(this.radiusEnveloppe + c.radiusEnveloppe);
		return contactEnveloppe;
	}
	
	public abstract void display();
	
	public abstract Projection projetteSur(Line ligne);
	
	// --- obstacle --- ?
	
	//point Ã  la surface du collider le plus dans l'obstacle.
	//public abstract PVector[] getIntruderPointOver(Line colLine);
	
	/** La ligne sur laquelle on va projeter. Basé à la surface de l'obstacle. */
	public abstract Line collisionLineFor(PVector p);
	
	/** Projette ce point dans la face la plus proche de l'obstacle. */
	public abstract PVector projette(PVector point);
}
