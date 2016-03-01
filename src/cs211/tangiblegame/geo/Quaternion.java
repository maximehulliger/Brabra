package cs211.tangiblegame.geo;

import cs211.tangiblegame.ProMaster;
import processing.core.PApplet;
import processing.core.PVector;

public class Quaternion {

	public static final Quaternion identity = new Quaternion();
	public static final Quaternion toTurnAround = new Quaternion(ProMaster.up, PApplet.PI);

	
	private float w, x, y, z;      	// components of a quaternion
	private boolean validRotAxis = false;
	private PVector rotAxis = null;	// lazily updated when rotated, always normalized or null (if valid)
	private float angle = 0;
	
	private static final int manucureAge = 1; // #rotate before normalize.
	private int age = 0;

	public Quaternion(float w, float x, float y, float z) {
		set(w, x, y, z);
		if (equals(identity))
			validRotAxis = true;
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
		validRotAxis = false;
		return this;
	}
	
	/** Set wxyz of the quaternion and return it. */
	public Quaternion set(PVector axis, float angle) {
		this.angle = angle;
		this.rotAxis = axis;
		initFromAxis();
		return this;
	}

	/** Set wxyz of the quaternion and return it. */
	public Quaternion set(Quaternion rot) {
		return set(rot.w, rot.x, rot.y, rot.z);
	}

	/** set to identity */
	private Quaternion reset() {
		set(identity);
		angle = 0;
		rotAxis = null;
		validRotAxis = true;
		return this;
	}
	
	public Quaternion setAngle(float angle) {
		this.angle = angle;
		initFromAxis();
		return this;
	}
	
	/** Mult/turn the quaternion and return it. */
	public Quaternion rotate(Quaternion r) {
		if (!r.equals(identity)) {
			mult(r);
			if (++age >= manucureAge) {
				normalize();
				age = 0;
			}
		}
		return this;
	}

	public void addAngularMomentum(PVector dL) {
		/*if (ProMaster.isZeroEps(dL, false))
			return;
		*/PVector rotAxis = rotAxis();
		
		if (rotAxis == null) {
			System.out.println("ang mom: from ident");
			rotAxis = dL;
		} else {
			rotAxis = PVector.add(rotAxis, dL);
		}
		set( rotAxis, rotAxis.mag() );
	}

	/** check if rotation is null and if so resets it. */
	public boolean isZeroEps(boolean clean) {
		//if (ProMaster.equalEps(PApplet.abs(w), 1)) {
		if (ProMaster.equalEps(PApplet.abs(w), 1) && ProMaster.isZeroEps(x)
				&& ProMaster.isZeroEps(y) && ProMaster.isZeroEps(z)) {
			if (clean && !equals(identity))
				reset();
			return true;
		} else
			return false;
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

	/** return the rot axis * angle or null. */
	public PVector rotAxis() {
		updateAxis();
		return (rotAxis == null) ? null : PVector.mult(rotAxis,angle);
	}
	
	public float angle() {
		updateAxis();
		return (rotAxis == null) ? 0 : angle;
	}
	
	public boolean equals(Quaternion other) {
		return other.w == w && other.x == x && other.y == y && other.z == z;
	}

	public String toString() {
		return "quat: (wxyz:"+w+", "+x+", "+y+", "+z+")";
	}
	
	public Quaternion rotatedBy(Quaternion r) {
		return copy().rotate(r);
	}

	public Quaternion multBy(Quaternion q) {
		return copy().mult(q);
	}

	public Quaternion normalized() {
		return copy().normalize();
	}

	public Quaternion withOppositeAngle() {
		return new Quaternion(w, -x, -y, -z).normalize();
	}
	
	/** build a quaternion from a direction vector and return it. */
	public static Quaternion fromDirection(PVector vDirection) {
		if (ProMaster.equalsEps(vDirection, ProMaster.behind))
			return toTurnAround.copy();
				
		vDirection.normalize();
		PVector up = ProMaster.up.copy();
        // setup basis vectors describing the rotation given the input vector and assuming an initial up direction.
		PVector vRight = up.cross(vDirection);    
		up = vDirection.cross(vRight);	// The actual up vector
        if (up.equals(ProMaster.zero)) {
        	up = ProMaster.up.copy();
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

	/** rotAxis, angle -> wxyz. update wxyz, rot axis if needed */
	private Quaternion initFromAxis() {
		assert( !(rotAxis!=null && rotAxis.equals(ProMaster.zero)) );
		// if to identity
		
		if (validRotAxis && (rotAxis==null || ProMaster.isZeroEps(angle))) {
			System.out.println("reset.. angle: "+angle);
			reset();
		} else {
			System.out.println("dl mag: "+rotAxis.mag());
			
			if (!validRotAxis) {
				rotAxis.normalize();
				validRotAxis = true;
			}
			
			float omega = 0.5f * angle; 
			float s = PApplet.sin(omega);
			if (PApplet.abs(s) == Float.MIN_VALUE) {
				System.out.println(" /!\\ hej ! ça se passe dans quaternion !");
				reset(); // really happens ?
			} else {
				set(PApplet.cos(omega),
					s*rotAxis.x,
					s*rotAxis.y,
					s*rotAxis.z);
				normalize(); //really needed ?
			}
		}
		return this;
	}

	/** wxyz -> rotAxis, angle. update rot axis & angle if needed. */
	private void updateAxis() {
		if (!validRotAxis) {
			float halfomega = PApplet.acos(w);
			float s = PApplet.sin(halfomega);
			if (ProMaster.isZeroEps(s)) { //no rotation
				System.out.println("updateAxis: "+this);
				angle = 0;
				rotAxis = null;
			} else {
				float invSinHO = 1/s;
				angle = halfomega*2;
				rotAxis = new PVector( x*invSinHO, y*invSinHO, z*invSinHO );
				rotAxis.normalize();
			}
			validRotAxis = true;
		}
	}

	private Quaternion normalize() {
		float norm = PApplet.sqrt(w*w + x*x + y*y + z*z);
		assert (!ProMaster.equalEps(norm, 0));
		if (!ProMaster.equalEps(norm, 1)){
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
		if (!q.equals(identity)) {
			set(w*q.w - x*q.x - y*q.y - z*q.z,
				w*q.x + x*q.w - y*q.z + z*q.y,
				w*q.y + y*q.w - z*q.x + x*q.z,
				w*q.z + z*q.w - x*q.y + y*q.x);
		}
		return this;
	}
}
