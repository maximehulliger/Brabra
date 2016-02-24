package cs211.tangiblegame.physic;

import cs211.tangiblegame.geo.Line;
import cs211.tangiblegame.geo.Quaternion;
import processing.core.*;

/** une classe pouvant entraîner et réagir à une collision.*/
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
		boolean parenty = (parent == c || c.parent == this || (parent == c.parent && parent != null));
		assert (!parenty);
		boolean contactEnveloppe = PVector.sub(this.location, c.location).magSq() < sq(this.radiusEnveloppe + c.radiusEnveloppe);
		return contactEnveloppe;
	}
	
	public abstract void display();
	
	public abstract Line.Projection projetteSur(Line ligne);
	
	//------ obstacle: 
	
	// la ligne sur laquelle on va projeter. basé à la surface de l'obstacle
	public abstract Line collisionLineFor(PVector p);
	
	//projette ce point dans la face la plus proche de l'obstacle. 
	public abstract PVector projette(PVector point);

	/*public boolean hasIntrudersOver(Line colLine) {
		// TODO Auto-generated method stub
		return false;
	}*/
}
