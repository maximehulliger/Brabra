package brabra.game.physic;

import brabra.game.physic.geo.Line;
import brabra.game.physic.geo.Plane;
import brabra.game.physic.geo.Quaternion;
import brabra.game.physic.geo.Vector;

/** 
 * A convex polyhedron. Pseudo cause not totally working. 
 * Children of polyhedron should updateAbs vertices & edges too (via set()).
 **/
public abstract class PseudoPolyedre extends Collider {
	
	protected final static Vector infiniteSize = Vector.cube(Float.POSITIVE_INFINITY);
	
	/** Vertices in absolute. */
	private Vector[] vertices = null;
	/** Edges in absolute. */
	private Line[] edges = null;
	
	public PseudoPolyedre(Vector loc, Quaternion rot) {
		super(loc, rot);
	}
	
	public abstract void display();

	public abstract Vector projette(Vector point);

	/** to update abs the vertices & edges. */
	protected void setAbs(Vector[] vertices, Line[] edges) {
		this.vertices = vertices;
		this.edges = edges;
	}

	public Line[] edges() {
		updateAbs();
		assert(edges != null);
		return edges;
	}
	
	public Vector[] vertices() {
		updateAbs();
		assert(vertices != null);
		return vertices;
	}

	/** Return the edges intruding over the line. */
	public Vector[] getIntruderPointOver(Line colLine) {
		return colLine.intruders(vertices());
	}
	
	// --- polyhedron specific methods (abstract) ---
	
	/** Retourne le point qui est le plus contre cette normale absolue (par rapport au centre) */
	public abstract Vector pointContre(Vector normaleAbs);
	
	/** Return true if the point is inside the object. */
	public abstract boolean isIn(Vector abs);
	
	/** Retourne la moitié de la mag de la projection (depuis le centre du pp.) */
	public abstract float projetteSur(Vector normale);

	/** Return the potential plans that an object at this location could collide against. */
	public abstract Plane[] plansSeparationFor(Vector colliderLocation);
}
