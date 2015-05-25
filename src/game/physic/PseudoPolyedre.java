package game.physic;

import processing.core.PVector;
import game.geo.Line;
import game.geo.Plane;

public abstract class PseudoPolyedre extends Collider {
	
	protected PVector[] sommets = null;
	protected Line[] arretes = null;
	
	public PseudoPolyedre(PVector loc, PVector rot, float radiusEnveloppe) {
		super(loc, rot, radiusEnveloppe);
	}
	
	public abstract void display();
	
	public abstract PVector pointContre(PVector normale);
	
	public abstract boolean isIn(PVector abs);
	
	//retourne la moitié de la mag de la projection (depuis le centre du pp.)
	public abstract float projetteSur(PVector normale);

	public PVector[] getIntruderPointOver(Line colLine) {
		return colLine.intruders(sommets);
	}
	
	public abstract Plane[] plansSeparationFor(PVector colliderLocation);

	public abstract PVector projette(PVector point);
}
