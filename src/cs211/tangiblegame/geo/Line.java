package cs211.tangiblegame.geo;

import java.util.ArrayList;

import cs211.tangiblegame.TangibleGame;
import cs211.tangiblegame.ProMaster;
import processing.core.*;

/** A line characterized by 2 points (or 1 point and 1 vector). Can be finite.*/
public final class Line extends ProMaster {
	/** a. */
	public final PVector base;
	/** a -> b. */
	public final PVector vector;
	public final float vectorMag;
	/** Direction normalized. */
	public final PVector norm;
	/** Indicate if finite (segment, a->b) or infinite (line, -->a->b-->) */
	public final boolean finite;

	/** Take 2 points to form a line from a to b. can be finite. */
	public Line(PVector a, PVector b, boolean finite) {
		this.finite = finite;
		this.base = a;
		this.vector = PVector.sub(b, a);
		this.vectorMag = vector.mag();
		norm = vector.copy();
		norm.normalize();
	}

	/** retourne la projection du point sur la ligne. */
	public PVector projette(PVector p) {
		return PVector.add( base, projetteLocal(p));
	}

	/** retourne la projection du point par rapport ‡ la base de la ligne */
	public PVector projetteLocal(PVector p) {
		if (!finite)
			return PVector.mult(norm, projectionFactor(p));
		else
			return PVector.mult(norm, TangibleGame.constrain(projectionFactor(p), 0, vectorMag));
	}

	/** retourne le facteur de projection du point relativement ‡ la norme */
	public float projectionFactor(PVector p) {
		return PVector.sub( p, base ).dot(norm);
	}

	/** retourne true si le point est dans l'hyperplan form√© par cette ligne. */
	public boolean isFacing(PVector point) {
		float pf = projectionFactor(point);
		return 0 <= pf;// && pf <= vectorMag;
	}

	/** projette les points sur la ligne */
	public Projection projette(PVector[] points) {
		if (points.length == 0) {
			System.out.println("projette sans points !");
			return null;
		}
		float min = Float.MAX_VALUE ;
		float max = Float.MIN_VALUE ;
		for (PVector p : points) {
			float proj = projectionFactor(p);
			if (proj > max) max = proj;
			if (proj < min) min = proj;
		}
		return new Projection(min, max);
	}

	/** retourne un array des canditats projetÈ sous la ligne. */
	public PVector[] intruders(PVector[] candidates) {
		ArrayList<PVector> intruders = new ArrayList<>();
		for (PVector cand : candidates) {
			float proj = projectionFactor(cand);
			if ( proj < 0) {
				intruders.add(cand);
			}
		}
		PVector[] ret = new PVector[intruders.size()];
		return intruders.toArray(ret);
	}

	/** projection sur une ligne [de <= a] */
	public static class Projection {
		public final float de,  a;

		/** projection non nulle sur une droite quelconque. 'de' est toujours plus petit ou √©gal √† 'a'. */
		public Projection(float de, float a) {
			if (de > a)
				throw new IllegalArgumentException("projection invalide de "+de+" √† "+a+" !");
			this.de = de;
			this.a = a;
		}
		
		public Projection(float de) {
			this(de, Float.MAX_VALUE);
		}

		public boolean intersectionne(Projection other) {
			return !(this.de > other.a || this.a > other.de);
		}

		public float empietementSur(Projection other) {
			return max(this.de - other.a, this.a - other.de);
		}

		public boolean comprend(float proj) {
			return de<=proj && proj<=a;
		}
	}
}
