package brabra.game.physic.geo;

import java.util.regex.Matcher;

import org.ode4j.math.DVector3;
import org.ode4j.math.DVector3C;

import brabra.Master;
import brabra.game.physic.Physic;
import processing.core.PVector;

public class Vector extends PVector {
	private static final long serialVersionUID = 1;
	
	public static final Vector zero = new Vector(0,0,0);
	public static final Vector left = new Vector(1, 0, 0);
	public static final Vector right = new Vector(-1, 0, 0);
	public static final Vector up = new Vector(0, 1, 0);
	public static final Vector down = new Vector(0, -1, 0);
	public static final Vector front = new Vector(0, 0, -1);
	public static final Vector behind = new Vector(0, 0, 1);
	public static final Vector[] directions = new Vector[] {
			left, right, up, down, front, behind };
	
	// --- Contructor ---
	
	public Vector() {
		super();
	}

	public Vector(float x, float y, float z) {
		super(x,y,z); 
	}

	public Vector(float x, float y) {
		super(x,y); 
	}

	public Vector(PVector v) {
		super(v.x,v.y,v.z); 
	}

	public Vector(DVector3C v) {
		super((float)v.get0(), (float)v.get1(), (float)v.get2()); 
	}

	/** 
	 * Return a vector from "(x,y,z)" format or 
	 * a direction vector (front, behind(back), right, left, up, down) or
	 * zero or null (if invalid).
	 **/
	public static Vector fromString(String vec) {
		// 1. known vector name
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
		else 
			return parseVector(vec, null);
	}
	
	/** 
	 * Get a Vector from a "(x[,y[,z]])" format string.
	 * return defaultValue(directly) if the string is invalid.
	 **/
	public static Vector parseVector(String vec, Vector defaultValue) {
		// 2. extract 3 float from string.
		Matcher matcher = Master.floatPattern.matcher(vec);
		float[] values = new float[3];
		for (int i=0; i<3; i++) {
			if (matcher.find())
				values[i] = Float.parseFloat(matcher.group());
			else
				return defaultValue;
		}
		return new Vector(values[0],values[1],values[2]);
	}
	
	// --- Getters ---

	public DVector3 toOde() {
		return new DVector3(x, y, z);
	}
	
	public Vector copy() {
		return new Vector(this);
	}
	
	public String formated(float epsilon) {
		return isZeroEps(false) ? "zero" : "[ " //TODO add epsilon
				+ Master.formatFloat(x, epsilon) + ", "
				+ Master.formatFloat(y, epsilon) + ", "
				+ Master.formatFloat(z, epsilon) + " ]";
	}

	public Vector normalized() {
		return new Vector(copy().normalize());
	}

	/** Return a new vector: this + v... */
	public Vector plus(Vector... v) {
		return copy().add(v);
	}

	/** Return a new vector: this - v. */
	public Vector minus(Vector v) {
		return copy().sub(v);
	}

	/** Return a new vector: this * f. */
	public Vector multBy(float f) {
		return copy().mult(f);
	}

	public Vector cross(PVector v) {
		return new Vector(super.cross(v));
	}

	public Vector limited(float max) {
		return copy().limit(max);
	}

	public Vector withMag(float mag) {
		return copy().setMag(mag);
	}

	/** Multiply this vector element by element. */
	public Vector multElementsBy(Vector other) {
		return copy().multElements(other);
	}

	/** Divide this vector element by element. */
	public Vector divElementsBy(Vector other) {
		return copy().divElements(other);
	}
	
	// --- modifyers ---

	public Vector set(PVector v) {
		super.set(v);
		return this;
	}

	public Vector set(float x, float y, float z) {
		super.set(x,y,z);
		return this;
	}

	/** Add some vectors to this. */
	public Vector add(Vector... vs) {
		for (int i=0; i<vs.length; i++)
			super.add(vs[i]);
		return this;
	}
	
	public Vector sub(Vector v) {
		super.sub(v);
		return this;
	}
	
	public Vector mult(float f) {
		super.mult(f);
		return this;
	}

	public Vector div(float f) {
		super.mult(1/f);
		return this;
	}

	public Vector limit(float max) {
		super.limit(max);
		return this;
	}

	public Vector setMag(float mag) {
		super.setMag(mag);
		return this;
	}

	/** Multiply this vector element by element. */
	public Vector multElements(Vector other) {
		return set(this.x * other.x, this.y * other.y, this.z * other.z);
	}

	/** Divide this vector element by element. */
	public Vector divElements(Vector other) {
		return set(this.x / other.x, this.y / other.y, this.z / other.z);
	}
	
	// --- static ---

	public static Vector sumOf(Vector... vs) {
		Vector ret = vs[0].copy();
		for (int i=1; i<vs.length; i++)
			ret.add(vs[1]);
		return ret;
	}

	/** return new Vector(d,d,d). */
	public static Vector cube(float d) {
		return new Vector(d, d, d);
	}

	/** Return a new vector with xyz in [-norm,norm] */
	public static Vector randomVec(float vMax) {
		return new Vector(Master.random(-vMax, vMax), Master.random(-vMax, vMax), Master.random(-vMax, vMax));
	}

	/** Deep copy. */
	public static Vector[] copy(Vector[] vectors) {
		Vector[] ret = new Vector[vectors.length];
		for (int i=0; i<vectors.length; i++)
			ret[i] = vectors[i].copy();
		return ret;
	}
	
	public static Vector average(Vector[] vectors) {
		return Vector.sumOf(vectors).div(vectors.length);
	}

	// --- Equals epsilon ---

	/** Return true if p is zero or zero eps (nearly zero). if clean & zero eps, reset p. */
	public boolean isZeroEps(boolean clean) {
		return isZeroEps(this, clean, Physic.epsilon);
	}
	
	/** return true if this is close to reference. if clean, set p1 to p2. */
	public boolean equalsEps(Vector reference, boolean clean) {
		return equalsEps(this, reference, clean, Physic.epsilon);
	}
	
	/** return true if this is close to reference after epsilon. if clean, set p1 to p2. */
	public boolean equalsEps(Vector reference, boolean clean, float epsilon) {
		return equalsEps(this, reference, clean, epsilon);
	}
	
	/** return true if close to zero. if clean, set p1 to p2. */
	private static boolean equalsEps(Vector v, Vector reference, boolean clean, float epsilon) {
		if (v == reference)
			return true;
		else if (v == null || reference == null)
			return false;
		else {
			final boolean isZero = isZeroEps(v.minus(reference), false, epsilon);
			if (clean && isZero)
				v.set(reference);
			return isZero;
		}
	}

	/** Return true if p is zero or zero eps (nearly zero). if clean & zero eps, reset p. */
	public static boolean isZeroEps(PVector v, boolean clean, float epsilon) {
		if (v.equals(zero))
			return true;
		else {
			if (Physic.isZeroEps(v.x, epsilon) && Physic.isZeroEps(v.y, epsilon) && Physic.isZeroEps(v.z, epsilon)) {
				if (clean)
					v.x = v.y = v.z = 0;
				return true;
			} else
				return false;
		}
	}

}
