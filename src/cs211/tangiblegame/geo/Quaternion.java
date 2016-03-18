package cs211.tangiblegame.geo;

import cs211.tangiblegame.ProMaster;
import processing.core.PApplet;
import processing.core.PVector;

public class Quaternion extends ProMaster {
	public static final Quaternion identity = new NQuaternion(new Quaternion(), 
			() -> { throw new IllegalArgumentException("pas touche à l'identity /!\\"); });
	public static final Quaternion toTurnAround = new Quaternion(up, PApplet.PI);

	private float w, x, y, z;      	// components of a quaternion
	private boolean validRotAxis = false;
	private PVector rotAxis = null;	// lazily updated when rotated, always normalized or null (if valid)
	private float angle = 0;
	
	private static final int manucureAge = 1; // #rotate before normalize.
	private int age = 0;

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
	public Quaternion(PVector axis, float angle) {
		set(axis, angle);
	}

	/** Makes quaternion from angle * normalized axis */
	public Quaternion(PVector axis) {
		set(axis, axis.mag());
	}

	// ---- manipulation methods
	
	/** Set wxyz of the quaternion and return it. */
	public Quaternion set(float w, float x, float y, float z) {
		this.w = w;
		this.x = x;
		this.y = y;
		this.z = z;
		if (!isNormalized())
			normalize();
		validRotAxis = false;
		return this;
	}
	
	/** Set wxyz of the quaternion and return it. */
	public Quaternion set(PVector axis, float angle) {
		if (!isConstrained(angle, -pi, pi))
			game.debug.log(4, "quaternion set angle: "+angle+" pas dans [-pi,pi]");
		this.angle = entrePiEtMoinsPi(angle);
		this.rotAxis = axis;
		initFromAxis();
		return this;
	}

	/** Set wxyz of the quaternion and return it. */
	public Quaternion set(Quaternion rot) {
		set(rot.w, rot.x, rot.y, rot.z);
		if (rot.isIdentity()) {
			angle = 0;
			rotAxis = null;
			validRotAxis = true;
		}
		return this;
	}

	/** set to identity & validRotAxis to true. */
	private Quaternion reset() {
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

	public void addAngularMomentum(PVector dL) {
		assert (!dL.equals(zero));
		PVector rotAxis = rotAxisAngle();
		PVector newRotAxis = (rotAxis == null) ? dL : PVector.add(rotAxis, dL);
		set( newRotAxis, newRotAxis.mag() );
	}

	/** Check if this (wxyz) nearly equals other and if so & clean set it to other. */
	public boolean equalsEps(Quaternion other, boolean clean) {
		if (equalsEps(w, other.w) && equalsEps(x, other.x) && 
				equalsEps(y, other.y) && equalsEps(z, other.z)) {
			if (clean && !equals(other))
				set(other);
			return true;
		} else
			return false;
	}

	/** Check if this (axis & angle) nearly equals other and if so & clean set it to other. */
	public boolean equalsEpsAxis(Quaternion other, boolean clean) {
		if (equalsEps(rotAxis(), other.rotAxis(), false) && 
				equalsEps(angle(), other.angle())) {
			if (clean && !equals(other))
				set(other);
			return true;
		} else
			return false;
	}

	/** Check if rotation is null and if so & clean reset it. */
	public boolean isZeroEps(boolean clean) {
		return equalsEpsAxis(identity, clean);
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
	public PVector rotAxis() {
		updateAxis();
		return rotAxis;
	}
	
	/** return the rot axis or null if identity. */
	public PVector rotAxisAngle() {
		updateAxis();
		return (rotAxis == null) ? null : mult(rotAxis, angle);
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
		boolean ret = equalsWXYZ(other);
		return ret;
	}

	public boolean equalsAxis(Quaternion other) {
		return rotAxis() == other.rotAxis() && angle() == other.angle();
	}
	
	public boolean equalsWXYZ(Quaternion other) {
		return other.w == w && other.x == x && other.y == y && other.z == z;
	}

	public boolean isNormalized() {
		return equalsEps(normSq(), 1);
	}
	
	public String toString() {
		if (app.debug.testMode) 
			return toStringAll();
		else{
			updateAxis();
			boolean id = isIdentity();
			
			assert((rotAxis == null || angle == 0) == id);
			assert((rotAxis == null) == (angle == 0));
				
			//return "quat: (wxyz:"+w+", "+x+", "+y+", "+z+") norm: "+PApplet.sqrt(normSq())+" angle: "+angle;
			return "quat "+ (id ? "identity" : "axis: "+rotAxis+" angle: "+angle*180/pi+"°");
		}
	}
	
	public String toStringAll() {
		updateAxis();
		return    "  quat: (wxyz:"+w+", "+x+", "+y+", "+z+") \n"
				+ "  norm: "+PApplet.sqrt(normSq())+" age: "+age+" valitRotAxis: "+validRotAxis+"\n"
				+ "  axis: "+rotAxis+"\n"
				+ "  angle: "+angle;
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
	
	/** build a quaternion from a direction vector and return it. */
	public static Quaternion fromDirection(PVector vDirection) {
		if (equalsEps(vDirection, behind, false))
			return toTurnAround.copy();
		else if (equalsEps(vDirection, front, false))
			return identity.copy();
				
		vDirection.normalize();
		PVector up = ProMaster.up.copy();
        // setup basis vectors describing the rotation given the input vector and assuming an initial up direction.
		PVector vRight = up.cross(vDirection);    
		up = vDirection.cross(vRight);	// The actual up vector
        if (up.equals(zero)) {
        	up = up.copy();
        }
        
        float in = 1.0f + vRight.x + up.y + vDirection.z;
        if (in > 0) {
        	Quaternion qrot = new Quaternion();
            qrot.w = PApplet.sqrt(in) / 2.0f;
	        float dfwScale = qrot.w * 4.0f;
	        qrot.x = (vDirection.y - up.z) / dfwScale;
	        qrot.y = (vRight.z - vDirection.x) / dfwScale;
	        qrot.z = (up.x - vRight.y) / dfwScale;
	        return qrot;
        } else
        	return identity.copy();
    }

	/** Rotates a towards b with factor t[0,1] */
	public static Quaternion slerp(Quaternion a, Quaternion b, float t)
	{
		float omega, cosom, sinom, sclp, sclq;

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
				// validate rotAxis
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
				float halfomega = PApplet.acos(w);
				float s = PApplet.sin(halfomega);
				if (halfomega == 0 || s == 0) { //no rotation
					reset();
				} else {
					float invSinHO = 1/s;
					angle = halfomega*2;
					rotAxis = new PVector( x*invSinHO, y*invSinHO, z*invSinHO );
					rotAxis.normalize();
				}
			}
			validRotAxis = true;
		}
	}

	private Quaternion normalize() {
		float norm = PApplet.sqrt(normSq());
		assert (!equalsEps(norm, 0));
		if (norm != 1) {
			float invNorm = 1f/norm;
			set(w * invNorm,
				x * invNorm,
				y * invNorm,
				z * invNorm);
		}
		return this;
	}
	
	// mult by a normalized quaternion
	private Quaternion mult(Quaternion q) {
		if (!q.isIdentity()) {
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
