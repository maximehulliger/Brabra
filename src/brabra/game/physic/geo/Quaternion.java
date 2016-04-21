package brabra.game.physic.geo;

import brabra.Master;
import brabra.ProMaster;
import processing.core.PApplet;
import brabra.game.physic.Physic;
import brabra.game.physic.geo.Vector;
import brabra.game.Observable.NQuaternion;

public class Quaternion extends ProMaster {
	
	public static final float 	epsilonRotAxis = 1E-7f,
								epsilonAngle = 1E-6f,//pi/(360*1000),
								epsilonWXYZ = 1E-6f;
	public static final Quaternion identity = new NQuaternion(new Quaternion(), 
			() -> { throw new IllegalArgumentException("pas touche � l'identity /!\\"); });

	// components of a quaternion
	private float w = 1;  
	private final Vector xyz = zero.copy();
	
	// lazily updated component when rotated, rotAxis always normalized or null (if valid)
	private boolean validRotAxis = true;
	private Vector rotAxis = null;	
	private float angle = 0;

	
	// --- Builders ---

	/** Create a new null (identity) Quaternion from wxyz. */
	public Quaternion() {
		// everything is already set :)
	}
	
	/** Create a new Quaternion from wxyz. */
	public Quaternion(float w, float x, float y, float z) {
		this(w, new Vector(x,y,z));
	}

	/** Create a new Quaternion from angle and axis. */
	public Quaternion(Vector axis, float angle) {
		set(axis == null ? null : axis.copy(), angle);
	}
	
	/** Create a copy of the Quaternion. */
	public Quaternion(Quaternion q) {
		set(q);
	}
	
	public Quaternion (float w, Vector xyz) {
		set(w, xyz);
	}

	// --- Getters ---
	
	/** return the rot axis or null if identity. */
	public Vector rotAxis() {
		updateAxis();
		return rotAxis == null ? null : rotAxis.copy();
	}
	
	/** return the rot axis or null if identity. */
	public Vector rotAxisAngle() {
		updateAxis();
		return rotAxis == null ? null : rotAxis.multBy(angle);
	}
	
	/** Return the angle of rotation in radians. */
	public float angle() {
		updateAxis();
		return angle;
	}
	
	public boolean isIdentity() {
		return validRotAxis && rotAxis == null ? true : w == 1 || w == -1;
	}
	
	private boolean testing = false;
	/** Collection of assert to ensure a valid quat. */
	private void testThis() {
		if (!testing) {
			testing = true;
			final boolean id = isIdentity();
			if (id) {
				if (validRotAxis) {
					assert (rotAxis==null);
					assert (angle == 0);
				}
				assert (w == 1 || w == -1);
				assert (xyz.equals(zero));
			} else {
				if (validRotAxis) {
					assert (rotAxis!=null);
					assert (angle != 0);
				}
				assert (w != 1 && w != -1);
				assert (!xyz.equals(zero));
			}
			
			testing = false;
		}
	}
	
	public boolean equals(Object other) {
		if (other == null) return false;
	    if (other == this) return true;
	    return (other instanceof Quaternion) 
	    		? equals((Quaternion)other) : false;
	}

	public boolean equals(Quaternion other) {
		return w == other.w && xyz.equals(other.xyz);
	}

	public boolean equalsAxis(Quaternion other) {
		return rotAxis() == other.rotAxis() && angle() == other.angle();
	}
	
	public static boolean printEverything = true;
	
	public String toString() {
		if (app != null && app.debug.testMode) 
			return toStringDebug();
		else
			return isIdentity() ? "Identity" : "around: "+rotAxis()+" with angle "+angle()*toDegrees+"�";
	}
	
	public String toStringDebug() {
		return    "quat: (wxyz:"+w+", "+xyz.x+", "+xyz.y+", "+xyz.z+")"
				+ (validRotAxis 
					? " \naround "+rotAxis()+" with angle "+(angle()*toDegrees)+"�"
					: " with unknown axis");
	}
	
	// --- Setters ---
	
	/** Set the quaternion from WXYZ, normalize this, unvalidate rot axis. */
	public void set(float w, Vector xyz) {
		setWXYZ(w, xyz, true);
		checkIdentity();
	}
	
	/** Set the quaternion from rotation axis & angle and return it. */
	public void set(Vector rotAxis, float angle) {
		angle = mod(angle, twoPi);
		assert(isConstrained(angle, 0, twoPi));
		
		if (rotAxis == null || angle == 0)
			reset();
		else {
			assert !(rotAxis.equals(zero));
			rotAxis.normalize();
			
			final float halfOmega = angle/2; 
			setWXYZ(PApplet.cos(halfOmega), rotAxis.multBy(PApplet.sin(halfOmega)), true);
			if (!checkIdentity()) {
				//TODO: Mmmh.. why does commenting the next line change that much the test results ?
				setRotAxisAngle(rotAxis, angle, false);
			}
		}
		testThis();
	}
	
	public String formated() {
		//TODO not good epsilon
		return rotAxis()==null ? "identity" 
				: "around "+rotAxis().formated(Physic.epsilon)+" with angle "+Master.formatFloat(angle()*180/pi, Physic.epsilon)+"�";
	}

	/** wxyz -> (rotAxis, angle). update rot axis & angle if needed (!validRotAxis). set validRotAxis to true. */
	private void updateAxis() {
		if (!validRotAxis) {
			if (isIdentity()) {
				reset();
			} else {
				//more stable than halfomega = PApplet.acos(w);
				final float halfOmega = PApplet.acos(w);//atan2(xyz.mag(), w);
				final float s = PApplet.sin(halfOmega);
				if (halfOmega == 0 || s == 0) { //no rotation
					reset();
				} else {
					final Vector rotAxis = xyz.multBy(1/s);
					if (rotAxis.equals(Vector.zero))
						reset();
					else
						setRotAxisAngle(rotAxis, 2*halfOmega, true);
				}
			}
		}
		testThis();
	}

	/** Set the quaternion from another quaternion (deep copy). */
	public void set(Quaternion quat) {
		// we trust that the quat is valid.
		setWXYZ(quat.w, quat.xyz, false);
		testThis();
		if (quat.validRotAxis) 
			setRotAxisAngle(quat.rotAxis, quat.angle, false);
		testThis();
	}
	
	public void setAngle(float angle) {
		set(rotAxis(), angle);
	}
	
	/** Mult/turn the quaternion by r and return it. */
	public void rotate(Quaternion r) {
		if (!r.isIdentity()) {
			if (isIdentity())
				set(r);
			else {
				set(r.w*w - r.xyz.x*xyz.x - r.xyz.y*xyz.y - r.xyz.z*xyz.z,
						new Vector(
						r.w*xyz.x + r.xyz.x*w - r.xyz.y*xyz.z + r.xyz.z*xyz.y,
						r.w*xyz.y + r.xyz.y*w - r.xyz.z*xyz.x + r.xyz.x*xyz.z,
						r.w*xyz.z + r.xyz.z*w - r.xyz.x*xyz.y + r.xyz.y*xyz.x)
					);
			}
		}
		testThis();
	}

	// --- Other creators ---

	public Quaternion copy() {
		return new Quaternion(this);
	}
	
	/** return the equivalent opposite quaternion (minus rotAxis & minus angle is the same rotation). */
	public Quaternion contrary() {
		Quaternion q = new Quaternion(-w, xyz.multBy(-1));
		if (isIdentity())
			q.setRotAxisAngle(null, 0, false);
		else
			q.setRotAxisAngle(rotAxis().multBy(-1), -angle, false);
		return q;
	}

	public Quaternion rotatedBy(Quaternion r) {
		final Quaternion q = copy();
		q.rotate(r);
		return q;
	}

	public Quaternion withAngle(float angle) {
		final Quaternion q = copy();
		q.setAngle(angle);
		return q;
	}

	public Quaternion withOppositeAngle() {
		return new Quaternion(w, xyz.multBy(-1));
	}
	
	// --- Static Builder ---
	
	/** Retutn a new Quaternion representing a quaternion from 2 direction vectors and return it. Forward and upwards shouldn't be colinear. */
	public static Quaternion fromDirection(Vector forward, Vector upwards) {
		forward = forward.normalized();
		upwards = upwards.normalized();
		if (forward.equalsEps(front, false))		// front
    		return identity.copy();
		else if (forward.equalsEps(behind, false))	// behind
    		return new Quaternion(Vector.up, pi);
		else if (forward.equalsEps(up, false))		// up
			return new Quaternion(left, PApplet.PI/2);
		else if (forward.equalsEps(down, false))	// down
			return new Quaternion(left, -PApplet.PI/2);
		else {
			final Vector right = upwards.cross(forward).normalized();    
			assert(!right.equals(zero));
			final Vector up = forward.cross(right).normalized();
			assert(!up.equals(zero));
			final float in = 1.0f + right.x + up.y + forward.z;
	        if (in == 0) {
	        	return identity.copy();
	        } else {
	        	assert (in > 0);
				final float w = PApplet.sqrt(in) / 2.0f;
		        final float dfwScale = w * 4.0f;
		        return new Quaternion(w, (forward.y - up.z) / dfwScale,
		        		(right.z - forward.x) / dfwScale, (up.x - right.y) / dfwScale);
	        }
		}
    }

	/** Return a new Quaternion with a rotation between a and b with factor t[0,1]. */
	public static Quaternion slerp(Quaternion a, Quaternion b, float t) {
		final float omega, cosom, sinom, scla, sclb;
		cosom = a.xyz.x*b.xyz.x + a.xyz.y*b.xyz.y + a.xyz.z*b.xyz.z + a.w*b.w;
		if ((1.0f+cosom) > Float.MIN_VALUE) {
			if ((1.0f-cosom) > Float.MIN_VALUE) {
				omega = PApplet.acos(cosom);
				sinom = PApplet.sin(omega);
				scla = PApplet.sin((1.0f-t)*omega) / sinom;
				sclb = PApplet.sin(t*omega) / sinom;
			} else {
				scla = 1.0f - t;
				sclb = t;
			}
			return new Quaternion( scla*a.w + sclb*b.w, a.xyz.multBy(scla).plus(b.xyz.multBy(sclb)) );
		} else {
			scla = PApplet.sin((1f-t) * PApplet.PI * 0.5f);
			sclb = PApplet.sin(t * PApplet.PI * 0.5f);
			return new Quaternion (a.w, a.xyz.multBy(scla).plus(b.xyz.multBy(sclb)));
		}
	}
	
	// --- Equal Epsilon ---

	/** Check if the quaternion is nearly null. If so & clean, reset it. */
	public boolean isZeroEps(boolean clean) {
		return equalsEps(identity, clean);
	}

	/** Check if the quaternion is nearly equal to the other. If so & clean, set this to other. */
	public boolean equalsEps(Quaternion other, boolean clean) {
		return equalsEpsWXYZ(other, clean);
	}

	/** Check if this (wxyz) nearly equals other and if so & clean set it to other. */
	public boolean equalsEpsWXYZ(Quaternion other, boolean clean) {
		if ((Physic.equalsEps(w, other.w, epsilonWXYZ) && xyz.equalsEps(other.xyz, false, epsilonWXYZ))
				|| (Physic.equalsEps(w, -other.w, epsilonWXYZ) && xyz.equalsEps(other.xyz.multBy(-1), false, epsilonWXYZ))) {
			if (clean && !equals(other))
				set(other);
			return true;
		} else
			return false;
	}

	/** Check if this (axis & angle) nearly equals other and if so & clean set it to other. */
	public boolean equalsEpsAxis(Quaternion other, boolean clean) {
		final Vector rMe = rotAxis(), rOther = other.rotAxis();
		final float aMe = angle(), aOther = other.angle(), aOtherMinus = -aOther;
		final boolean aEqu = anglesEquEps(aMe, aOther), aEquMinus = anglesEquEps(aMe, aOtherMinus);
		if (rMe == null) {
			if (aEqu && clean && !equals(other))
				set(other);
			return aEqu;
		} else if ((rMe.equalsEps(rOther, false, epsilonRotAxis) && aEqu)
				|| rMe.multBy(-1).equalsEps(rOther, false, epsilonRotAxis) && aEquMinus){
			if (clean && !equals(other))
				set(other);
			return true;
		} else
			return false;
	}

	// --- Private ---

	/** set to identity & validRotAxis to true. */
	private void reset() {
		w = 1;
		if (!xyz.equals(zero))
			xyz.set(zero);
		rotAxis = null;
		angle = 0;
		validRotAxis = true;
	}
	
	/** Set WXYZ of the quaternion (set validRotAxis to false if not identity). Normalize WXYZ if normalize. */
	private void setWXYZ(float w, Vector xyz, boolean normalize) {
		// if identity
		if ((w == -1 || w == 1)) {
			reset();
		} else {
			this.w = w;
			this.xyz.set(xyz);
			if (normalize)
				normalizeWXYZ();
			this.validRotAxis = false;
		}
	}

	/** Set the rotation axis and angle of the quaternion (set validRotAxis to true). Normalize rotAxis if normalize. */
	private void setRotAxisAngle(Vector rotAxis, float angle, boolean normalize) {
		if (rotAxis == null || (angle = validAngle(angle)) == 0) {
			this.rotAxis = null;
			this.angle = 0;
		} else {
			this.rotAxis = rotAxis.copy();
			this.angle = angle;
			// normalize rotAxis if needed
			assert (!this.rotAxis.equals(zero));
			if (normalize)
				this.rotAxis.normalize();
		}
		this.validRotAxis = true;
		testThis();
	}
	
	/** Return an angle in radian in [-pi;pi[*/
	private float validAngle(float angle) {
		while (angle < 0)
			angle+=twoPi;
		return mod(angle+pi, twoPi)-pi;
	}
	
	private boolean anglesEquEps(float a1, float a2) {
		return Physic.equalsEps(a1, a2, epsilonAngle) || Physic.equalsEps(validAngle(max(a1, a2)+epsilonAngle)-epsilonAngle, min(a1, a2));
	}
	
	/** Normalize the WXYZ components. */
	private Quaternion normalizeWXYZ() {
		final float normSq = normSq();
		assert (!Physic.equalsEps(normSq, 0));
		if (normSq != 1) {
			final float norm = PApplet.sqrt(normSq);
			final float invNorm = 1/norm;
			setWXYZ(w * invNorm, xyz.multBy(invNorm), false);
		}
		return this;
	}
	
	/** Check if the quat is a quaternion from w. return true if it was Identity (independently of update or not). */
	private boolean checkIdentity() {
		final boolean id = w == 1 || w == -1;
		if (id) {
			if (!validRotAxis) { // =~ not checked
				reset();
			}
		} else {
			// should be checked before setting the angles
			assert (!validRotAxis);
		}
		return id;
	}
	
	private float normSq() {
		return w*w + xyz.magSq();
	}
}
