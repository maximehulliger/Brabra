package brabra.game.physic.geo;

import java.util.ArrayList;

import brabra.ProMaster;
import brabra.Brabra;
import brabra.game.Color;
import brabra.game.scene.Object;

/** A line characterized by 2 points (or 1 point and 1 vector). Can be finite.*/
public final class Line extends ProMaster {
	
	/** Starting point of the line. */
	public final Vector base;
	/** End point of the line (if infinite, just indicates the direction). */
	public final Vector end;
	/** Indicate if finite (segment, a->b) or infinite (line, -->a->b-->) */
	public final boolean finite;
	/** Base -> end. */
	public final Vector vector;
	/** Direction normalized. */
	public final Vector norm;
	/** the magnitude of the segment or +infinite if infinite. */
	public final float vectorMag;
	
	/** Take 2 points to form a line from base to end. can be finite. */
	public Line(Vector base, Vector end, boolean finite) {
		this.finite = finite;
		this.base = base;
		this.end = end;
		this.vector = end.minus(base);
		this.vectorMag = finite ? vector.mag() : Float.POSITIVE_INFINITY;
		this.norm = vector.normalized();
	}

	/** Display the segment if finite or an infinite half-line (from a) with the specified color. */
	public void display() {
		line(base, finite ? end : norm.multBy(far));
	}

	/** Display the segment if finite or an infinite half-line (from a) with the specified color. */
	public void display(Color lineColor) {
		line(base, finite ? end : norm.multBy(far), lineColor);
	}

	/** Display the line plus a ball of diameter 1 at the basis (from) of the line in the specified color. */
	public void display(Color lineColor, Color baseColor) {
		display(lineColor);
		sphere(base, 1, baseColor);
	}
	
	/** Return a new line in absolute space from this object's relative space. */
	public Line absoluteFrom(Object o) {
		return new Line(o.transform.absolute(base), o.transform.absolute(end), finite);
	}

	/** retourne la projection du point sur la ligne. */
	public Vector projette(Vector p) {
		return base.plus(projetteLocal(p));
	}

	/** retourne la projection du point par rapport à la base de la ligne */
	public Vector projetteLocal(Vector p) {
		return !finite
			? norm.multBy(projectionFactor(p))
			: norm.multBy(Brabra.constrain(projectionFactor(p), 0, vectorMag));
	}

	/** retourne le facteur de projection du point relativement à la norme */
	public float projectionFactor(Vector p) {
		return p.minus(base).dot(norm);
	}

	/** retourne true si le point est dans l'hyperplan formé par cette ligne ou segment. */
	public boolean isFacing(Vector point) {
		float pf = projectionFactor(point);
		return 0 <= pf && pf <= vectorMag;
	}

	/** projette les points sur la ligne */
	public Projection projette(Vector[] points) {
		assert (points.length > 0);
		float min = Float.MAX_VALUE ;
		float max = Float.MIN_VALUE ;
		for (Vector p : points) {
			float proj = projectionFactor(p);
			if (proj > max) max = proj;
			if (proj < min) min = proj;
		}
		return new Projection(min, max);
	}

	/** retourne un array des canditats projeté sous la ligne. */
	public Vector[] intruders(Vector[] candidates) {
		ArrayList<Vector> intruders = new ArrayList<>();
		for (Vector cand : candidates) {
			float proj = projectionFactor(cand);
			if ( proj < 0)
				intruders.add(cand);
		}
		return intruders.toArray(new Vector[intruders.size()]);
	}

	/** projection sur une ligne [de <= a] */
	public static class Projection {
		
		public final float from,  to;

		/** projection non nulle sur une droite quelconque. 'de' est toujours plus petit ou Ã©gal Ã  'a'. */
		public Projection(float from, float to) {
			if (from > to)
				throw new IllegalArgumentException("'from' should be smaller or equal than 'to' "
						+ "for a valid projection (from "+from+" to "+to+").");
			this.from = from;
			this.to = to;
		}
		
		public Projection(float de) {
			this(de, Float.MAX_VALUE);
		}

		public boolean intersectionne(Projection other) {
			return !(this.from > other.to || this.to > other.from);
		}

		public float empietementSur(Projection other) {
			return max(this.from - other.to, this.to - other.from);
		}

		public boolean comprend(float proj) {
			return from<=proj && proj<=to;
		}
	}
}
