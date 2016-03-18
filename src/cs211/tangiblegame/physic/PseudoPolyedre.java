package cs211.tangiblegame.physic;

import cs211.tangiblegame.geo.Line;
import cs211.tangiblegame.geo.Plane;
import cs211.tangiblegame.geo.Quaternion;
import processing.core.PVector;

public abstract class PseudoPolyedre extends Collider {
	
	protected PVector[] sommets = null;
	protected Line[] arretes = null;
	
	public PseudoPolyedre(PVector loc, Quaternion rot, float radiusEnveloppe) {
		super(loc, rot, radiusEnveloppe);
	}
	
	public abstract void display();

	public abstract PVector projette(PVector point);
	
	/** Retourne le point qui est le plus contre cette normale (par rapport au centre) */
	public abstract PVector pointContre(PVector normale);
	
	/** Return true if the point is inside the object. */
	public abstract boolean isIn(PVector abs);
	
	/** Retourne la moitié de la mag de la projection (depuis le centre du pp.) */
	public abstract float projetteSur(PVector normale);

	/** Return the edges intruding over the line. */
	public PVector[] getIntruderPointOver(Line colLine) {
		return colLine.intruders(sommets);
	}
	
	/** Return the potential plans that an object at this location could collide against. */
	public abstract Plane[] plansSeparationFor(PVector colliderLocation);
}
