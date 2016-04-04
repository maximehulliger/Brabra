package brabra;

import brabra.game.Color;
import brabra.game.RealGame;
import brabra.game.physic.Physic;
import brabra.game.physic.geo.Quaternion;
import processing.core.PApplet;
import brabra.game.physic.geo.Vector;
import brabra.game.scene.Camera;


/** 
 * An abstract class mastering Processing. 
 * Provide a lot of useful methods, mostly syntactic sugar. 
 * Free to use once extended :) 
 * */
public abstract class ProMaster extends Master {
	
	protected static Brabra app;
	protected static RealGame game;
	
	// --- some constants ---

	protected static final float close = Camera.close;
	protected static final float far = Camera.far;
	protected static final Vector zero = Vector.zero;
	protected static final Vector left = Vector.left;
	protected static final Vector right = Vector.right;
	protected static final Vector up = Vector.up;
	protected static final Vector down = Vector.down;
	protected static final Vector front = Vector.front;
	protected static final Vector behind = Vector.behind;
	protected static final Quaternion identity = Quaternion.identity;
	protected static final float pi = PApplet.PI;
	protected static final float halfPi = PApplet.HALF_PI;
	protected static final float twoPi = PApplet.TWO_PI;
	
	// --- General syntactic sugar ---

	protected void line(Vector v1, Vector v2) {
		app.line(v1.x,v1.y,v1.z,v2.x,v2.y,v2.z);
	}

	protected void line(Vector v1, Vector v2, Color color) {
		color.fill();
		line(v1, v2);
	}
	
	protected void sphere(Vector pos, float radius, Color color) {
		app.pushMatrix();
		translate(pos);
		color.fill();
		app.sphere(radius);
		app.popMatrix();
	}

	protected static int round(float f) {
		return PApplet.round(f);
	}

	// --- Vector syntactic sugar ---

	protected static Vector vec(float x, float y, float z) {
		return new Vector(x, y, z);
	}
	
	protected static Vector vec(float x, float y) {
		return new Vector(x, y, 0);
	}
	
	protected static Vector vec(String s) {
		final Vector v = Vector.fromString(s);
		if (v != null)
			return v;
		else {
			debug.err("wrong vector format for \""+s+"\", taking zero");
			return zero.copy();
		}
	}
	
	/** Add dome vectors. */
	protected static Vector add(Vector... v) {
		if (v.length <= 1)
			throw new IllegalArgumentException("add at least 2 vectors !");
		else {
			return zero.copy().add(v);
		}	
	}
	
	protected static Vector front(float lenght) { 
		return front.multBy(lenght); 
	}
	
	protected static Vector behind(float lenght) { 
		return behind.multBy(lenght); 
	}
	
	protected static Vector up(float lenght) { 
		return up.multBy(lenght); 
	}

	protected static Vector down(float lenght) { 
		return down.multBy(lenght); 
	}
	
	protected static Vector right(float lenght) { 
		return right.multBy(lenght); 
	}

	protected static Vector left(float lenght) { 
		return left.multBy(lenght); 
	}

	protected static Vector x(float lenght) { 
		return vec(lenght,0,0); 
	}
	
	protected static Vector y(float lenght) { 
		return vec(0,lenght,0); 
	}
	
	protected static Vector z(float lenght) { 
		return vec(0,0,lenght); 
	}

	protected static Vector yawAxis(float lenght) { 
		return y(lenght); 
	}
	
	protected static Vector pitchAxis(float lenght) { 
		return x(lenght); 
	}
	
	protected static Vector rollAxis(float lenght) { 
		return z(lenght);
	}	

	// --- epsilon shortcut ---
	
	/** Return true if f is nearly zero. */
	public static boolean isZeroEps(float f) {
		return Physic.isZeroEps(f);
	}

	/** Return true if f1 is nearly equal to f2. */
	public static boolean equalsEps(float f1, float f2) {
		return Physic.equalsEps(f1, f2);
	}
	
	// --- Vector help methods ---

	/** Return the distance between the 2 vectors squared. */
	protected static float distSq(Vector p1, Vector p2) {
		return p1.minus(p2).magSq();
	}
	
	// --- Transformations (location, rotation) ---

	/** Return the absolute location of rel (translated & rotated, from reseted matrix)*/
	protected static synchronized Vector absolute(Vector rel, Vector trans, Quaternion rotation) {
		boolean rotNull = rotation.equals(identity);
		boolean transNull = trans.equals(zero);
		if (rotNull && transNull)
			return rel;
		else if (rotNull)
			return add( rel, trans );
		else {
			app.pushMatrix();
			app.resetMatrix();
			if (!transNull)
				translate(trans);
			if (!rotNull)
				rotateBy(rotation);
			Vector ret = model(rel);
			app.popMatrix();
			return ret;
		}
	}

	protected static synchronized void translate(Vector t) {
		app.translate(t.x, t.y, t.z);
	}

	protected static void rotateBy(Quaternion rotation) {
		rotateBy(rotation.rotAxis(), rotation.angle());
	}

	protected static synchronized void rotateBy(Vector rotAxis, float angle) {
		if (rotAxis == null)
			return;
		app.rotate(angle, rotAxis.x, rotAxis.y, rotAxis.z);
	}

	protected static synchronized Vector screenPos(Vector pos3D) {
		return vec( app.screenX(pos3D.x, pos3D.y, pos3D.z), app.screenY(pos3D.x, pos3D.y, pos3D.z) );
	}

	protected static Vector relative(Vector abs, Vector trans, Quaternion rotation) {
		return absolute( abs.minus(trans), zero, rotation.withOppositeAngle());
	}

	protected static Vector[] absolute(Vector[] v, Vector trans, Quaternion rotation) {
		Vector[] ret = new Vector[v.length];
		for (int i=0; i<v.length; i++)
			ret[i] = absolute(v[i], trans, rotation);
		return ret;
	}

	protected static synchronized Vector model(Vector rel) {
		return new Vector( app.modelX(rel.x, rel.y, rel.z), app.modelY(rel.x, rel.y, rel.z), app.modelZ(rel.x, rel.y, rel.z) );
	}
}
