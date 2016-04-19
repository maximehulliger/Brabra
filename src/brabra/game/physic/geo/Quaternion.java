package brabra.game.physic.geo;

import brabra.Master;
import brabra.ProMaster;
import processing.core.PApplet;
import brabra.game.physic.Physic;
import brabra.game.physic.geo.Vector;
import brabra.game.Observable.NQuaternion;

public class Quaternion extends ProMaster {
	
	public static final Quaternion identity = new NQuaternion(new Quaternion(), 
			() -> { throw new IllegalArgumentException("pas touche à l'identity /!\\"); });

	private float w, x, y, z;      	// components of a quaternion
	private boolean validRotAxis = false;
	private Vector rotAxis = null;	// lazily updated when rotated, always normalized or null (if valid)
	private float angle = 0;
	
	private static final int manucureAge = 1; // #rotate before normalize.
	private int age = 0;
	private boolean justNormalized = false;

	public Quaternion(float w, float x, float y, float z) {
		set(w, x, y, z);
		if (isIdentity())
			validRotAxis = true;
		if (!equalsEps(1, normSq()))
			normalize();
	}

	public Quaternion() {
		set(1, 0, 0, 0);
		validRotAxis = true;
	}
	
	public Quaternion(Quaternion q) {
		this(q.w, q.x, q.y, q.z);
	}

	/** Makes quaternion from angle and axis */
	public Quaternion(Vector axis, float angle) {
		set(axis, angle);
	}

	/** Makes quaternion from angle * normalized axis */
	public Quaternion(Vector axis) {
		set(axis, axis.mag());
	}

	// ---- manipulation methods
	
	/** Set the quaternion from WXYZ and return it. */
	public Quaternion set(float w, float x, float y, float z) {
		this.w = w;
		this.x = x;
		this.y = y;
		this.z = z;
		if (justNormalized)
			justNormalized = false;
		else
			normalize();
		validRotAxis = false;
		return this;
	}
	
	/** Set the quaternion from rotation axis & angular velocity and return it. */
	public Quaternion set(Vector axis, float angle) {
		if (!isConstrained(angle, -pi, pi))
			game.debug.log(4, "quaternion set angle: "+angle+" pas dans [-pi,pi]");
		this.angle = entrePiEtMoinsPi(angle);
		this.rotAxis = axis;
		initFromAxis();
		return this;
	}
	
	/** Set the quaternion from rotation axis * angular velocity and return it. */
	public Quaternion set(Vector axisAngle) {
		return set(axisAngle, axisAngle.mag());
	}

	/** Set the quaternion from another quaternion (deep copy) and return it. */
	public Quaternion set(Quaternion quat) {
		// we trust that the quat is valid.
		this.w = quat.w;
		this.x = quat.x;
		this.y = quat.y;
		this.z = quat.z;
		this.angle = quat.angle;
		this.rotAxis = quat.rotAxis;
		this.validRotAxis = quat.validRotAxis;
		return this;
	}

	/** set to identity & validRotAxis to true. */
	private Quaternion reset() {
		if (!isIdentity())
			set(identity);
		return this;
	}
	
	public Quaternion setAngle(float angle) {
		this.angle = entrePiEtMoinsPi(angle);
		initFromAxis();
		return this;
	}
	
	/** Mult/turn the quaternion and return it. */
	public Quaternion rotate(Quaternion r) {
		if (!r.isIdentity()) {
			mult(r);
			if (++age >= manucureAge) {
				normalize();
				age = 0;
			}
		}
		return this;
	}

	public void addAngularMomentum(Vector dL) {
		assert (!dL.equals(zero));
		Vector rotAxis = rotAxisAngle();
		Vector newRotAxis = (rotAxis == null) ? dL : rotAxis.plus(dL);
		set( newRotAxis, newRotAxis.mag() );
	}

	/** Check if this (wxyz) nearly equals other and if so & clean set it to other. */
	public boolean equalsEpsWXYZ(Quaternion other, boolean clean) {
		if (equalsEps(this.w, other.w) && equalsEps(this.x, other.x) && 
				equalsEps(this.y, other.y) && equalsEps(this.z, other.z)) {
			if (clean && !equals(other))
				set(other);
			return true;
		} else
			return false;
	}

	/** Check if this (axis & angle) nearly equals other and if so & clean set it to other. */
	public boolean equalsEpsAxis(Quaternion other, boolean clean) {
		updateAxis();
		if (rotAxis == null && rotAxis == other.rotAxis)
			return true;
		else if (rotAxis().equalsEps(other.rotAxis(), false) && 
				equalsEps(angle(), other.angle())) {
			if (clean && !equals(other))
				set(other);
			return true;
		} else
			return false;
	}
	
	public boolean equalsEps(Quaternion other, boolean clean) {
		return equalsEpsWXYZ(other, clean) && equalsEpsAxis(other, clean);
	}

	/** Check if rotation is null and if so & clean reset it. */
	public boolean isZeroEps(boolean clean) {
		return equalsEps(identity, clean);
	}
	
	// --- immutable stuff ---

	public Quaternion copy() {
		Quaternion q = new Quaternion(this);
		if (validRotAxis) {
			q.validRotAxis = true;
			q.angle = angle;
			q.rotAxis = rotAxis;
		}
		return q;
	}

	/** return the rot axis or null if identity. */
	public Vector rotAxis() {
		updateAxis();
		return rotAxis;
	}
	
	/** return the rot axis or null if identity. */
	public Vector rotAxisAngle() {
		updateAxis();
		return (rotAxis == null) ? null : rotAxis.multBy(angle);
	}
	
	public float angle() {
		updateAxis();
		return (rotAxis == null) ? 0 : angle;
	}
	
	public boolean isIdentity() {
		boolean wxyzNull = w == 1 && x == 0 && y == 0 && z == 0;
		return wxyzNull;
	}
	
	public boolean equals(Object other) {
		if (other == null) return false;
	    if (other == this) return true;
	    return (other instanceof Quaternion) 
	    		? equals((Quaternion)other) : false;
	}

	public boolean equals(Quaternion other) {
		return equalsWXYZ(other);
	}

	public boolean equalsAxis(Quaternion other) {
		return rotAxis() == other.rotAxis() && angle() == other.angle();
	}
	
	public boolean equalsWXYZ(Quaternion other) {
		return other.w == w && other.x == x && other.y == y && other.z == z;
	}
	
	public String toString() {
		if (app != null && app.debug.testMode) 
			return toStringAll();
		else
			return "quat "+ (isIdentity() ? "identity" : "axis: "+rotAxis()+" angle: "+angle()*180/pi+"°");
	}
	
	public String toStringAll() {
		updateAxis();
		return    "  quat: (wxyz:"+w+", "+x+", "+y+", "+z+") \n"
				+ "  norm: "+PApplet.sqrt(normSq())+" age: "+age+" valitRotAxis: "+validRotAxis+"\n"
				+ "  axis: "+rotAxis+"\n"
				+ "  angle: "+angle;
	}
	
	public String formated() {
		//TODO not good epsilon
		return rotAxis()==null ? "identity" 
				: "around "+rotAxis().formated(Physic.epsilon)+" with angle "+Master.formatFloat(angle()*180/pi, Physic.epsilon)+"°";
	}

	public Quaternion rotatedBy(Quaternion r) {
		return copy().rotate(r);
	}

	public Quaternion withAngle(float angle) {
		return copy().setAngle(angle);
	}

	public Quaternion multBy(Quaternion q) {
		return copy().mult(q);
	}

	public Quaternion withOppositeAngle() {
		return new Quaternion(w, -x, -y, -z).normalize();
	}
	
	/** Retutn a new Quaternion representing a quaternion from a direction vector and return it.  */
	public static Quaternion fromDirection(Vector forward) {
		forward.normalize();
		if (forward.equalsEps(up, false))
			return new Quaternion(left, PApplet.PI/2);
		else if (forward.equalsEps(down, false))
			return new Quaternion(left, -PApplet.PI/2);
		else 
			return fromDirection(forward, up);
	}
	
	/** Retutn a new Quaternion representing a quaternion from 2 direction vectors and return it. Forward and upwards shouldn't be colinear. */
	public static Quaternion fromDirection(Vector forward, Vector upwards) {
		forward.normalize();
		upwards.normalize();
		assert(!upwards.cross(forward).equals(zero));
		final Vector right = upwards.cross(forward).normalized();    
		final Vector up = forward.cross(right).normalized();
        final float in = 1.0f + right.x + up.y + forward.z;
        if (in == 0)
        	return identity.copy();
        else {
        	assert (in > 0);
			final float w = PApplet.sqrt(in) / 2.0f;
	        final float dfwScale = w * 4.0f;
	        return new Quaternion(w, (forward.y - up.z) / dfwScale,
	        		(right.z - forward.x) / dfwScale, (up.x - right.y) / dfwScale);
        }
    }

	/** Return a new Quaternion with a rotation between a and b with factor t[0,1]. */
	public static Quaternion slerp(Quaternion a, Quaternion b, float t) {
		final float omega, cosom, sinom, sclp, sclq;
		cosom = a.x*b.x + a.y*b.y + a.z*b.z + a.w*b.w;
		if ((1.0f+cosom) > Float.MIN_VALUE) {
			if ((1.0f-cosom) > Float.MIN_VALUE) {
				omega = PApplet.acos(cosom);
				sinom = PApplet.sin(omega);
				sclp = PApplet.sin((1.0f-t)*omega) / sinom;
				sclq = PApplet.sin(t*omega) / sinom;
			} else {
				sclp = 1.0f - t;
				sclq = t;
			}
			return new Quaternion ( sclp*a.w + sclq*b.w, sclp*a.x + sclq*b.x,
					sclp*a.y + sclq*b.y, sclp*a.z + sclq*b.z);
		} else {
			sclp = PApplet.sin((1f-t) * PApplet.PI * 0.5f);
			sclq = PApplet.sin(t * PApplet.PI * 0.5f);
			return new Quaternion (a.z, sclp*a.x + sclq*b.x,
					sclp*a.y + sclq*b.y, sclp*a.z + sclq*b.z);
		}
	}

	// ---- basic private operations

	/** rotAxis, angle -> wxyz. update wxyz, rot axis if needed (!validRotAxis) */
	public Quaternion initFromAxis() {
		assert( !(rotAxis!=null && rotAxis.equals(zero)) );
		
		// if to identity
		if (validRotAxis && (rotAxis==null || angle == 0))
			reset();
		else {
			float omega = 0.5f * angle; 
			float s = PApplet.sin(omega);
			if (s == 0)
				reset();
			else {
				// validate rotAxis (angle ok)
				rotAxis.normalize();
				validRotAxis = true;
				set(PApplet.cos(omega),
					s*rotAxis.x,
					s*rotAxis.y,
					s*rotAxis.z);
				normalize();
			}
		}
		return this;
	}

	/** wxyz -> rotAxis, angle. update rot axis & angle if needed (!validRotAxis). set validRotAxis to true. */
	public void updateAxis() {
		if (!validRotAxis) {
			if (equalsWXYZ(identity)) {
				angle = 0;
				rotAxis = null;
			} else {
				final float halfomega = PApplet.acos(w);
				final float s = PApplet.sin(halfomega);
				if (halfomega == 0 || s == 0) { //no rotation
					reset();
				} else {
					float invSinHO = 1/s;
					angle = halfomega*2;
					rotAxis = new Vector( x*invSinHO, y*invSinHO, z*invSinHO );
					rotAxis.normalize();
				}
			}
			validRotAxis = true;
		}
	}

	private Quaternion normalize() {
		final float norm = PApplet.sqrt(normSq());
		assert (!equalsEps(norm, 0));
		if (norm != 1) {
			float invNorm = 1/norm;
			justNormalized = true;
			set(w * invNorm,
				x * invNorm,
				y * invNorm,
				z * invNorm);
		}
		return this;
	}
	
	// mult by a normalized quaternion
	private Quaternion mult(Quaternion q) {
		if (isIdentity())
			set(q);
		else if (!q.isIdentity()) {
			set(w*q.w - x*q.x - y*q.y - z*q.z,
				w*q.x + x*q.w - y*q.z + z*q.y,
				w*q.y + y*q.w - z*q.x + x*q.z,
				w*q.z + z*q.w - x*q.y + y*q.x);
		}
		return this;
	}
	
	private float normSq() {
		return w*w + x*x + y*y + z*z;
	}
}
