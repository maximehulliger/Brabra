package brabra.game.physic;

import brabra.game.physic.geo.Line;
import brabra.game.physic.geo.Plane;
import brabra.game.physic.geo.Quaternion;
import processing.core.PVector;

/** 
 * A convex polyhedron. Pseudo cause not totally working. 
 * Children of polyhedron should updateAbs vertices & edges too (via set()).
 **/
public abstract class PseudoPolyedre extends Collider {
	
	/** Vertices in absolute. */
	private PVector[] vertices = null;
	/** Edges in absolute. */
	private Line[] edges = null;
	
	public PseudoPolyedre(PVector loc, Quaternion rot, float radiusEnveloppe) {
		super(loc, rot, radiusEnveloppe);
	}
	
	public abstract void display();

	public abstract PVector projette(PVector point);

	/** to update abs the vertices & edges. */
	protected void setAbs(PVector[] vertices, Line[] edges) {
		this.vertices = vertices;
		this.edges = edges;
	}

	public Line[] edges() {
		updateAbs();
		assert(edges != null);
		return edges;
	}
	
	public PVector[] vertices() {
		updateAbs();
		assert(vertices != null);
		return vertices;
	}

	/** Return the edges intruding over the line. */
	public PVector[] getIntruderPointOver(Line colLine) {
		return colLine.intruders(vertices());
	}
	
	// --- polyhedron specific methods (abstract) ---
	
	/** Retourne le point qui est le plus contre cette normale (par rapport au centre) */
	public abstract PVector pointContre(PVector normale);
	
	/** Return true if the point is inside the object. */
	public abstract boolean isIn(PVector abs);
	
	/** Retourne la moitié de la mag de la projection (depuis le centre du pp.) */
	public abstract float projetteSur(PVector normale);

	/** Return the potential plans that an object at this location could collide against. */
	public abstract Plane[] plansSeparationFor(PVector colliderLocation);
}
