package cs211.tangiblegame;

import java.util.regex.Matcher;

import cs211.tangiblegame.geo.Quaternion;
import cs211.tangiblegame.realgame.RealGame;
import processing.core.PApplet;
import processing.core.PVector;

/** 
 * An abstract class mastering Processing. 
 * Provide a lot of useful methods, mostly syntactic sugar. 
 * Free to use once extended :) 
 * */
public abstract class ProMaster extends Master {
	private static final float epsilon = PApplet.EPSILON / 100;
	
	protected static TangibleGame app;
	protected static RealGame game;
	
	// --- some constants ---
	
	public static final PVector zero = new PVector(0, 0, 0);
	public static final PVector farfarAway = new PVector(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
	public static final PVector left = new PVector(1, 0, 0);
	public static final PVector right = new PVector(-1, 0, 0);
	public static final PVector up = new PVector(0, 1, 0);
	public static final PVector down = new PVector(0, -1, 0);
	public static final PVector front = new PVector(0, 0, -1);
	public static final PVector behind = new PVector(0, 0, 1);
	public static final Quaternion identity = new Quaternion();
	public static final float small = 0.05f;
	public static final float far = 10_000;
	public static final float pi = PApplet.PI;
	
	// --- General syntactic sugar ---
	
	protected void line(PVector v1, PVector v2) {
		app.line(v1.x,v1.y,v1.z,v2.x,v2.y,v2.z);
	}

	// --- PVector syntactic sugar ---

	protected static PVector vec(float x, float y, float z) {
		return new PVector(x, y, z);
	}
	
	protected static PVector vec(float x, float y) {
		return new PVector(x, y);
	}
	
	protected PVector normalized(PVector p) {
		PVector pp = p.copy();
		return pp.normalize();
	}
	
	public static PVector add(PVector v1, PVector v2) {
		return PVector.add(v1, v2);
	}
	
	public static PVector sub(PVector v1, PVector v2) {
		return PVector.sub(v1, v2);
	}

	public static PVector mult(PVector v1, float f) {
		return PVector.mult(v1, f);
	}

	protected static PVector up(float lenght) { 
		return mult(up, lenght); 
	}
	
	protected static PVector front(float lenght) { 
		return mult(front, lenght); 
	}
	
	protected static PVector right(float lenght) { 
		return mult(right, lenght); 
	}

	// --- PVector help methods ---

	/** Return the distance between the 2 vectors squared. */
	protected static float distSq(PVector p1, PVector p2) {
		return sub(p1, p2).magSq();
	}
	
	/** 
	 * Return a vector from "(x,y,z)" format or 
	 * a direction vector (front, behind(back), right, left, up, down) or
	 * zero or null.
	 **/
	protected static PVector vec(String vec) {
		if (vec.equals("zero"))
			return zero;
		else if (vec.equals("front"))
			return front;
		else if (vec.equals("behind") || vec.equals("back"))
			return behind;
		else if (vec.equals("right"))
			return right;
		else if (vec.equals("left"))
			return left;
		else if (vec.equals("up"))
			return up;
		else if (vec.equals("down"))
			return down;
		
		Matcher matcher = floatPattern.matcher(vec);
		float[] values = new float[3];
		for (int i=0; i<3; i++) {
			if (matcher.find()) {
				values[i] = Float.parseFloat(matcher.group());
			} else {
				System.out.println("wrong vector format for \""+vec+"\", taking zero");
				return zero;
			}
		}
		return vec(values[0],values[1],values[2]);
	}

	/** Deep copy. */
	protected static PVector[] copy(PVector[] vectors) {
		PVector[] ret = new PVector[vectors.length];
		for (int i=0; i<vectors.length; i++)
			ret[i] = vectors[i].copy();
		return ret;
	}
	
	/** return vec(d,d,d) */
	protected static PVector cube(float d) {
		return vec(d, d, d);
	}
	
	protected static PVector moyenne(PVector[] points) {
		PVector moyenne = zero.copy();
		for (PVector p : points)
			moyenne.add(p);
		moyenne.div(points.length);
		return moyenne;
	}

	/** effectue une multiplication element par element. (utile pour L = Iw) */
	protected static PVector multMatrix(PVector matriceDiag, PVector vector) {
		return new PVector(
				matriceDiag.x * vector.x,
				matriceDiag.y * vector.y,
				matriceDiag.z * vector.z );
	}

	/** retourne un vecteur avec xyz dans [0,1] */
	protected static PVector randomVec() {
		return vec(randomBi(), randomBi(), randomBi());
	}

	// --- EPSILON (small value) ---

	/** Return true if p is zero or zero eps (nearly zero). if clean & zero eps, reset p. */
	public static boolean isZeroEps(PVector p, boolean clean) {
		if (p.equals(zero))
			return true;
		else {
			if (isZeroEps(p.x) && isZeroEps(p.y) && isZeroEps(p.z)) {
				if (clean)
					p.x = p.y = p.z = 0;
				return true;
			} else
				return p.equals(zero);
			
			/*if (p.x != 0 && isZeroEps(p.x)) p.x = 0;
			if (p.y != 0 && isZeroEps(p.y)) p.y = 0;
			if (p.z != 0 && isZeroEps(p.z)) p.z = 0;*/
			//return isZeroEps(p.x) && isZeroEps(p.y) && isZeroEps(p.z);
		}
	}

	/** Return true if f is nearly zero. */
	protected static boolean isZeroEps(float f) {
		return f==0 || isConstrained(f, -epsilon, epsilon);	
	}

	/** return true if close to zero. if clean, set p1 to p2. */
	protected static boolean equalsEps(PVector p1, PVector p2, boolean clean) {
		boolean isZero = isZeroEps( PVector.sub(p1, p2), false );
		if (clean && isZero)
			p1.set(p2);
		return isZero;
	}
	
	/** Return true if f1 is nearly equal to f2. */
	public static boolean equalEps(float f1, float f2) {
		return isZeroEps(f1 - f2);
	}
	
	// --- Transformations (location, rotation) ---
	
	protected static PVector model(PVector rel) {
		return new PVector( app.modelX(rel.x, rel.y, rel.z), app.modelY(rel.x, rel.y, rel.z), app.modelZ(rel.x, rel.y, rel.z) );
	}
	
	protected static PVector absolute(PVector rel, PVector trans, Quaternion rotation) {
		app.pushMatrix();
		if (!rotation.equals(identity))
			rotateBy(rotation);
		if (!trans.equals(zero))
			translate(trans);
		PVector ret = model(rel);
		app.popMatrix();
		return ret;
	}

	protected static PVector local(PVector abs, PVector trans, Quaternion rotation) {
		return absolute( PVector.sub(abs, trans), zero, rotation.withOppositeAngle());
	}

	protected static void translate(PVector t) {
		app.translate(t.x, t.y, t.z);
	}

	protected static void rotateBy(Quaternion rotation) {
		rotateBy(rotation.rotAxis());
	}

	protected static void rotateBy(PVector rotation) {
		if (rotation == null)
			return;
		app.rotate(rotation.mag(), rotation.x, rotation.y, rotation.z);
	}

	protected static PVector screenPos(PVector pos3D) {
		return new PVector( app.screenX(pos3D.x, pos3D.y, pos3D.z), app.screenY(pos3D.x, pos3D.y, pos3D.z) );
	}

	protected static PVector[] absolute(PVector[] v, PVector trans, Quaternion rotation) {
		PVector[] ret = new PVector[v.length];
		for (int i=0; i<v.length; i++)
			ret[i] = absolute(v[i], trans, rotation);
		return ret;
	}
}
