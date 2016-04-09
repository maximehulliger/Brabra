package brabra.game.physic.geo;

import brabra.ProMaster;
import processing.core.PApplet;
import brabra.game.physic.Physic;
import brabra.game.physic.geo.Vector;
import brabra.game.Observable.NQuaternion;

public class Quaternion extends ProMaster {
	
	public static final float 	epsilonRotAxis = pi/360,
								epsilonAngle = 3E-4f,
								epsilonWXYZ = 1E-3f;
	public static final Quaternion identity = new NQuaternion(new Quaternion(), 
			() -> { throw new IllegalArgumentException("pas touche à l'identity /!\\"); });

	private float w, x, y, z;      	// components of a quaternion
	private boolean validRotAxis = false;
	private Vector rotAxis = null;	// lazily updated when rotated, always normalized or null (if valid)
	private float angle = 0;
	
	private static final int manucureAge = 1; // #rotate before normalize.
	private int age = 0;
	
	public Quaternion(float w, float x, float y, float z) {
		set(w, x, y, z);
		validRotAxis = isIdentity();
	}

	public Quaternion() {
		validRotAxis = true;
		setWXYZ(1, 0, 0, 0, false);
	}
	
	public Quaternion(Quaternion q) {
		set(q);
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
	
	/** Set the quaternion from WXYZ, normalize this, unvalidate rot axis and return it. */
	public Quaternion set(float w, float x, float y, float z) {
		validRotAxis = false;
		setWXYZ(w, x, y, z, true);
		return this;
	}
	
	/** Set the quaternion from rotation axis & angle and return it. */
	public Quaternion set(Vector rotAxis, float angle) {
		angle = mod(angle, twoPi);
		assert(isConstrained(angle, 0, twoPi));
		
		assert( !(rotAxis!=null && rotAxis.equals(zero)) );
		if (rotAxis==null || angle == 0)
			reset();
		else {
			final float omega = 0.5f * angle; 
			final float s = PApplet.sin(omega);
			if (s == 0)
				reset();
			else {
				this.rotAxis = rotAxis.normalized();
				this.angle = entrePiEtMoinsPi(angle);
				this.validRotAxis = false;
				setWXYZ(PApplet.cos(omega),
					s*this.rotAxis.x,
					s*this.rotAxis.y,
					s*this.rotAxis.z,
					true);
			}
		}
		return this;
	}
	
	/** Set the quaternion from rotation axis * angular velocity and return it. */
	public Quaternion set(Vector axisAngle) {
		return set(axisAngle, axisAngle.mag());
	}

	/** Set the quaternion from another quaternion (deep copy). */
	public void set(Quaternion quat) {
		// we trust that the quat is valid.
		this.angle = quat.angle;
		this.rotAxis = quat.rotAxis;
		this.validRotAxis = quat.validRotAxis;
		setWXYZ(quat.w, quat.x, quat.y, quat.z, false);
	}

	/** set to identity & validRotAxis to true. */
	private Quaternion reset() {
		if (!isIdentity())
			set(identity);
		return this;
	}
	
	public Quaternion setAngle(float angle) {
		set(rotAxis, angle);
		return this;
	}
	
	/** Mult/turn the quaternion by r and return it. */
	public Quaternion rotate(Quaternion r) {
		if (!r.isIdentity()) {
			if (isIdentity())
				set(r);
			else if (!r.isIdentity()) {
				final boolean normalize = ++age >= manucureAge;
				if (normalize)
					age = 0;
				setWXYZ(r.w*w - r.x*x - r.y*y - r.z*z,
						r.w*x + r.x*w - r.y*z + r.z*y,
						r.w*y + r.y*w - r.z*x + r.x*z,
						r.w*z + r.z*w - r.x*y + r.y*x,
					normalize);
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
		if (Physic.equalsEps(this.w, other.w, epsilonWXYZ) && 
				Physic.equalsEps(this.x, other.x, epsilonWXYZ) && 
				Physic.equalsEps(this.y, other.y, epsilonWXYZ) && 
				Physic.equalsEps(this.z, other.z, epsilonWXYZ)) {
			if (clean && !equals(other))
				set(other);
			return true;
		} else
			return false;
	}

	/** Check if this (axis & angle) nearly equals other and if so & clean set it to other. */
	public boolean equalsEpsAxis(Quaternion other, boolean clean) {
		if (rotAxisAngle().equals(other.rotAxisAngle()))
			return true;
		else if (rotAxis().equalsEps(other.rotAxis(), false, epsilonRotAxis) && 
				Physic.equalsEps(angle(), other.angle(), epsilonAngle)) {
			if (clean && !equals(other))
				set(other);
			return true;
		} else
			return false;
	}
	
	public boolean equalsEps(Quaternion other, boolean clean) {
		return equalsEpsWXYZ(other, clean);
	}

	/** Check if rotation is null and if so & clean reset it. */
	public boolean isZeroEps(boolean clean) {
		return equalsEps(identity, clean);
	}
	
	// --- immutable stuff ---

	public Quaternion copy() {
		return new Quaternion(this);
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
	
	/** Return the angle of rotation in radians. */
	public float angle() {
		updateAxis();
		return angle;
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
	public static boolean printEverything = true;
	public String toString() {
		if (printEverything) 
			return toStringAll();
		else{
			if (isIdentity())
				return "quat identity"; 
			else {
				updateAxis();
				return "quat axis: "+rotAxis+" angle: "+(angle*180/pi)+"°";
			}
		}
	}
	
	public String toStringAll() {
		updateAxis();
		return    "  quat: (wxyz:"+w+", "+x+", "+y+", "+z+") \n"
				+ "  norm: "+PApplet.sqrt(normSq())+" age: "+age+"\n"
				+ "  axis: "+rotAxis+"\n"
				+ "  angle: "+angle+" rad";
	}

	public Quaternion rotatedBy(Quaternion r) {
		return copy().rotate(r);
	}

	public Quaternion withAngle(float angle) {
		return copy().setAngle(angle);
	}

	public Quaternion withOppositeAngle() {
		return new Quaternion(w, -x, -y, -z).normalizeWXYZ();
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

	/** wxyz -> rotAxis, angle. update rot axis & angle if needed (!validRotAxis). set validRotAxis to true. */
	private void updateAxis() {
		if (!validRotAxis) {
			if (equalsWXYZ(identity)) {
				angle = 0;
				rotAxis = null;
				validRotAxis = true;
			} else {
				final float halfomega = PApplet.acos(w);
				final float s = PApplet.sin(halfomega);
				if (halfomega == 0 || s == 0) { //no rotation
					reset();
				} else {
					final float invSinHO = 1/s;
					angle = entrePiEtMoinsPi(halfomega*2);
					rotAxis = new Vector( x*invSinHO, y*invSinHO, z*invSinHO );
					rotAxis.normalize();
					validRotAxis = true;
				}
			}
		}
	}
	
	// --- Private ---

	/** Set the quaternion from WXYZ. Normalize WXYZ if normalize. */
	protected void setWXYZ(float w, float x, float y, float z, boolean normalize) {
		this.w = w;
		this.x = x;
		this.y = y;
		this.z = z;
		if (normalize)
			normalizeWXYZ();
	}
	
	/** Normalize the WXYZ components. */
	private Quaternion normalizeWXYZ() {
		final float normSq = normSq();
		assert (!equalsEps(normSq, 0));
		if (normSq != 1) {
			final float norm = PApplet.sqrt(normSq);
			final float invNorm = 1/norm;
			setWXYZ(w * invNorm,
				x * invNorm,
				y * invNorm,
				z * invNorm,
				false);
		}
		return this;
	}
	
	private float normSq() {
		return w*w + x*x + y*y + z*z;
	}
}
