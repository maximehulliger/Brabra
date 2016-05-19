package brabra;

import brabra.game.Color;
import brabra.game.RealGame;
import brabra.game.physic.geo.ProTransform;
import brabra.game.physic.geo.Quaternion;
import processing.core.PApplet;
import brabra.game.physic.geo.Vector;
import brabra.game.scene.Camera;


/**
 * An abstract class mastering Processing. 
 * Provide a lot of useful methods, mostly syntactic sugar. 
 * Free to use once extended :) 
 **/
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
	protected static final float toDegrees = 180/pi;
	
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
		ProTransform.translate(pos);
		color.fill();
		app.sphere(radius);
		app.popMatrix();
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
			Debug.err("wrong vector format for \""+s+"\", taking zero");
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
	
	// --- Vector help methods ---

	/** Return the distance between the 2 vectors squared. */
	protected static float distSq(Vector p1, Vector p2) {
		return p1.minus(p2).magSq();
	}
}
