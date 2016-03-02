package cs211.tangiblegame;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cs211.tangiblegame.geo.Quaternion;
import cs211.tangiblegame.realgame.RealGame;
import processing.core.PApplet;
import processing.core.PVector;

// Processing master
public abstract class ProMaster {
	public static TangibleGame app;
	protected static RealGame game;
	protected static Random random;
	
	private static float epsilon = PApplet.EPSILON / 100;
	protected static final Pattern floatPattern = Pattern.compile("[+-]?\\d+[.]?\\d*");
	protected static final Pattern intPattern = Pattern.compile("[+]?\\d+");
	protected static int colorButtonOk, colorButtonRejected, colorQuad;
	protected static int color0, color255;
	
	// some constants
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
	
	

	public static void init(TangibleGame app) {
		ProMaster.app = app;
		
		random = new Random();
		color0 = app.color(0);
		color255 = app.color(255);
		colorButtonOk = app.color(0, 255, 0, 150);
		colorButtonRejected = app.color(255, 0, 0, 150);
		colorQuad = app.color(200, 100, 0, 120);
	}

	//---- general mastery
	
	/** angle en radian => [-pi, pi] */
	public static float entrePiEtMoinsPi(float a) {
		if (a > PApplet.PI) return a - PApplet.TWO_PI;
		else if (a < -PApplet.PI) return a + PApplet.TWO_PI;
		else return a;
	}
	
	/** [min, max] => [min2, max2] */
	public static float map(float val, float min, float max, float min2, float max2, boolean constrain) {
		return (clamp(val, min, max, constrain)-min)/(max-min)*(max2-min2) + min2;
	}
	
	/** [min, max] => [0, 1] */
	public static float clamp(float val, float min, float max, boolean constrain) {
		if (constrain)
			val = PApplet.constrain(val, min, max);
		return (val - min)/(max - min);
	}
	
	/** [min, max] */
	public static float random(float min, float max) {
		return min + (max-min) * random.nextFloat();
	}
	
	public static PVector moyenne(PVector[] points) {
		PVector moyenne = zero.copy();
		for (PVector p : points)
			moyenne.add(p);
		moyenne.div(points.length);
		return moyenne;
	}

	/** effectue une multiplication slot par slot. (utile pour L = Iw) */
	public static PVector multMatrix(PVector matriceDiagOnly, PVector vector) {
		return new PVector(
				matriceDiagOnly.x * vector.x,
				matriceDiagOnly.y * vector.y,
				matriceDiagOnly.z * vector.z );
	}

	//---- processing mastery

	protected static PVector vec(String vec) {
		if (vec==null || vec.equals("zero"))
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
	
	// --- EPSILON (small value) ---

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

	public static boolean isZeroEps(float f) {
		return f==0 || isConstrained(f, -epsilon, epsilon);	
	}

	/** return true if close to zero. if clean, set p1 to p2. */
	public static boolean equalsEps(PVector p1, PVector p2, boolean clean) {
		boolean isZero = isZeroEps( PVector.sub(p1, p2), false );
		if (clean && isZero)
			p1.set(p2);
		return isZero;
	}
	
	public static boolean equalEps(float f, float other) {
		return isZeroEps(f - other);
	}
	
	// --- processing syntactic sugar ---
	
	protected static PVector vec(float x, float y, float z) {
		return new PVector(x, y, z);
	}
	
	protected static PVector vec(float x, float y) {
		return new PVector(x, y);
	}
	
	/** return vec(d,d,d) */
	protected static PVector cube(float d) {
		return new PVector(d, d, d);
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

	public static float distSq(PVector p1, PVector p2) {
		return PVector.sub(p1, p2).magSq();
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
	
	/** retourne un vecteur avec xyz dans [0,1] */
	public static PVector randomVec() {
		return new PVector(randomBi(), randomBi(), randomBi());
	}

	protected void line(PVector v1, PVector v2) {
		app.line(v1.x,v1.y,v1.z,v2.x,v2.y,v2.z);
	}
	
	// --- general syntactic sugar ---

	public static int sgn(float a) {
		if (a == 0)
			return 0;
		else if (a>0)
			return 1;
		else 
			return -1;
	}
	
	/** a random value in [-1, 1] */ 
	public static float randomBi() {
		return random(-1, 1);
	}
	
	public static float sq(float t) {
		return t*t;
	}

	public static float sqrt(float t) {
		return PApplet.sqrt(t);
	}

	public static float min(float a, float b) {
		return a<b ? a : b;
	}

	public static float max(float a, float b) {
		return a>b ? a : b;
	}

	/** retourne true si v E [min, max] */
	public static boolean isConstrained(float v, float min, float max) {
		return v>=min && v<=max;
	}
	
	public static PVector[] copy(PVector[] vectors) {
		PVector[] ret = new PVector[vectors.length];
		for (int i=0; i<vectors.length; i++)
			ret[i] = vectors[i].copy();
		return ret;
	}
	
	public static PVector[] absolute(PVector[] v, PVector trans, Quaternion rotation) {
		PVector[] ret = new PVector[v.length];
		for (int i=0; i<v.length; i++)
			ret[i] = absolute(v[i], trans, rotation);
		return ret;
	}

	//------ Transformations (location, rotation)

	public static PVector absolute(PVector rel, PVector trans, Quaternion rotation) {
		app.pushMatrix();
		translate(trans);
		app.pushMatrix();
		rotateBy(rotation);
		PVector ret = new PVector( app.modelX(rel.x, rel.y, rel.z), app.modelY(rel.x, rel.y, rel.z), app.modelZ(rel.x, rel.y, rel.z) );
		app.popMatrix();
		app.popMatrix();
		return ret;
	}

	public static PVector local(PVector abs, PVector trans, Quaternion rotation) {
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

	public PVector screenPos(PVector pos3D) {
		return new PVector( app.screenX(pos3D.x, pos3D.y, pos3D.z), app.screenY(pos3D.x, pos3D.y, pos3D.z) );
	}
}
