package cs211.tangiblegame.geo;

import java.util.ArrayList;

import cs211.tangiblegame.TangibleGame;
import cs211.tangiblegame.ProMaster;
import processing.core.*;

/** une ligne caracterisée soit par 2 points (soit par 1 point et une direction, à venir peut être). peut être fini.*/
public final class Line extends ProMaster {
	public final PVector base;   	//a
	public final PVector vector;	//a->b
	public final float vectorMag;
	public final PVector norm;
	public final boolean finite;

	//prend 2 points pour former une ligne de a au b. peut être finit.
	public Line(PVector base, PVector b, boolean finite) {
		this.finite = finite;
		this.base = base;
		this.vector = PVector.sub(b, base);
		this.vectorMag = vector.mag();
		norm = vector.copy();
		norm.normalize();
	}

	// retourne la projection du point sur la ligne.
	public PVector projette(PVector p) {
		return PVector.add( base, projetteLocal(p));
	}

	// retourne la projection du point par rapport � la base de la ligne
	public PVector projetteLocal(PVector p) {
		if (!finite)
			return PVector.mult(norm, projectionFactor(p));
		else
			return PVector.mult(norm, TangibleGame.constrain(projectionFactor(p), 0, vectorMag));
	}

	// retourne le facteur de projection du point relativement � la norme
	public float projectionFactor(PVector p) {
		return PVector.sub( p, base ).dot(norm);
	}

	/** retourne true si le point est dans l'hyperplan formé par cette ligne. */
	public boolean isFacing(PVector point) {
		float pf = projectionFactor(point);
		return 0 <= pf;// && pf <= vectorMag;
	}

	// projette les points sur la ligne
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

	// retourne un tableau des points étant projeté sous la ligne.
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

		//projection non nulle sur une droite quelconque. 'de' est toujours plus petit ou égal à 'a'.
		public Projection(float de, float a) {
			if (de > a)
				throw new IllegalArgumentException("projection invalide de "+de+" à "+a+" !");
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
