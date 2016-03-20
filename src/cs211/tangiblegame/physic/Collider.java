package cs211.tangiblegame.physic;

import cs211.tangiblegame.Color;
import cs211.tangiblegame.geo.Line;
import cs211.tangiblegame.geo.Line.Projection;
import cs211.tangiblegame.geo.Quaternion;
import processing.core.*;

/** A class able to init and react to a collision. */
public abstract class Collider extends Body {
	
	public final static Color colliderColor = new Color(255, 0, 0, 150, 255, 0, 0);
	
	public static boolean displayAllColliders = false;
	
	private final float radiusEnveloppe;
	private boolean displayCollider = false;
	
	public Collider(PVector location, Quaternion rotation, float radiusEnveloppe) {
	  super(location, rotation);
	  assert(radiusEnveloppe > 0);
	  this.radiusEnveloppe = radiusEnveloppe;
	}
	
	public Collider withName(String name) {
		super.setName(name);
		return this;
	}
	
	public float radiusEnveloppe() {
		return radiusEnveloppe;
	}
	
	public void setDisplayCollider(boolean displayCollider) {
		this.displayCollider = displayCollider;
	}
	
	public boolean doCollideFast(Collider c) {
		boolean contactEnveloppe = PVector.sub(this.locationAbs, c.locationAbs).magSq() < sq(this.radiusEnveloppe + c.radiusEnveloppe);
		return contactEnveloppe;
	}
	
	/** To display the active collider (without color, in relative space). */
	public abstract void displayCollider();
	
	/** Return the projection of this collider on the line. */
	public abstract Projection projetteSur(Line ligne);
	
	/** 
	 * Display the collider... maybe. 
	 * In local space (should be call after pushLocal()). 
	 * Return true if the collider was displayed.
	 **/
	protected boolean displayColliderMaybe() {
		final boolean display = displayCollider || displayAllColliders;
		if (display) {
			colliderColor.fill();
			displayCollider();
		}
		return display;
	}
	
	// --- obstacle --- ?
	
	//point Ã  la surface du collider le plus dans l'obstacle.
	//public abstract PVector[] getIntruderPointOver(Line colLine);
	
	/** La ligne sur laquelle on va projeter. Basé à la surface de l'obstacle. */
	public abstract Line collisionLineFor(PVector p);
	
	/** Projette ce point dans la face la plus proche de l'obstacle. */
	public abstract PVector projette(PVector point);
}
