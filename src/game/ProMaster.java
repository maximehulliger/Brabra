package game;

import java.util.Random;

import processing.core.PApplet;
import processing.core.PVector;

// Processing master
public abstract class ProMaster {
	public static Game app;
	public static Random random;
	public static final PVector zero = new PVector(0, 0, 0);
	public static final PVector farfarAway = new PVector(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
	public static final PVector left = new PVector(1, 0, 0);
	public static final PVector up = new PVector(0, 1, 0);
	public static final PVector front = new PVector(0, 0, -1);

	public static void init(Game app) {
		ProMaster.app = app;
		random = new Random();
	}
	
	//---- general mastery
	
	public static float random(float min, float max) {
		return min + (max-min) * random.nextFloat();
	}
	
	public static float sq(float t) {
		return t*t;
	}
	
	//retourne true si v E [min, max]
	public static boolean isConstrained(float v, float min, float max) {
		return v>=min && v<=max;
	}
	
	public static float min(float a, float b) {
		return a<b ? a : b;
	}
	
	public static float max(float a, float b) {
		return a>b ? a : b;
	}

	public static float entrePiEtMoinsPi(float a) {
		if (a > PApplet.PI) return a - PApplet.TWO_PI;
		else if (a < -PApplet.PI) return a + PApplet.TWO_PI;
		else return a;
	}
	
	//secondes -> frames
	public static int toFrame(float seconde) {
		return Game.round(seconde * app.frameRate);
	}
	
	public static int sgn(float a) {
		if (isZeroEps(a))
			return 0;
		else if (a>0)
			return 1;
		else 
			return -1;
	}
	
	//---- processing mastery

	protected static PVector vec(float x, float y, float z) {
		return new PVector(x, y, z);
	}
	
	public static PVector[] copy(PVector[] vectors) {
		PVector[] ret = new PVector[vectors.length];
	  	for (int i=0; i<vectors.length; i++)
	  		ret[i] = vectors[i].get();
	  	return ret;
	}

	public static PVector[] absolute(PVector[] v, PVector trans, PVector rotation, PVector baseRot) {
		PVector[] ret = new PVector[v.length];
	  	for (int i=0; i<v.length; i++)
	  		ret[i] = absolute(v[i], trans, rotation, baseRot);
	    return ret;
	}
	
	public static float distSq(PVector p1, PVector p2) {
		return PVector.sub(p1, p2).magSq();
	}
	
	public static boolean equalsEps(PVector p1, PVector p2) {
		return isZeroEps( PVector.sub(p1, p2), false);
	}
	
	public static boolean isZeroEps(PVector p, boolean setToZero) {
		if (p.equals(zero))
			return true;
		else {
			if (setToZero) {
				if (p.x != 0 && isZeroEps(p.x))
					p.x = 0;
				if (p.y != 0 && isZeroEps(p.y))
					p.y = 0;
				if (p.z != 0 && isZeroEps(p.z))
					p.z = 0;
				return p.equals(zero);
			} else {
				return isZeroEps(p.x) && isZeroEps(p.y) && isZeroEps(p.z);
			}
			
		}
	}
	
	protected static boolean isZeroEps(float f) {
		return f==0 || (f <= Game.EPSILON && f >= -Game.EPSILON);	
	}
	
	public static PVector moyenne(PVector[] points) {
		PVector moyenne = zero.get();
		for (PVector p : points)
			moyenne.add(p);
		moyenne.div(points.length);
		return moyenne;
	}
	
	// effectue une multiplication slot par slot. (utile pour L = Iw)
	public static PVector multMatrix(PVector matriceDiagOnly, PVector vector) {
		return new PVector(
				matriceDiagOnly.x * vector.x,
				matriceDiagOnly.y * vector.y,
				matriceDiagOnly.z * vector.z );
	}
	
	//------ Transformations (location, rotation)
	  
	  public static PVector absolute(PVector rel, PVector trans, PVector rotation, PVector baseRot) {
			app.pushMatrix();
			translate(trans);
				app.pushMatrix();
				rotate(baseRot);
				rotate(rotation);
			  	PVector ret = new PVector( app.modelX(rel.x, rel.y, rel.z), app.modelY(rel.x, rel.y, rel.z), app.modelZ(rel.x, rel.y, rel.z) );
				app.popMatrix();
			app.popMatrix();
			return ret;
	  }
	  
	  public static PVector local(PVector abs, PVector trans, PVector rotation, PVector baseRot) {
		  return absolute( PVector.sub(abs, trans), zero, PVector.mult(baseRot, -1), PVector.mult(rotation, -1));
	  }

	  protected static void translate(PVector t) {
		  app.translate(t.x, t.y, t.z);
	  }

	  protected static void rotate(PVector rotation) {
		  /*app.rotateY(rot.y);
		  app.rotateX(rot.x);
		  app.rotateZ(rot.z);*/
		  app.rotate(rotation.mag(), rotation.x, rotation.y, rotation.z);
	  }
	  
	  public PVector screenPos(PVector pos3D) {
		  return new PVector( app.screenX(pos3D.x, pos3D.y, pos3D.z), app.screenY(pos3D.x, pos3D.y, pos3D.z) );
	  }
}
