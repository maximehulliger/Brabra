package cs211.tangiblegame.geo;

import processing.core.PApplet;
import processing.core.PVector;

public class Quaternion {

	public static final Quaternion identity = new Quaternion();

	private float W, X, Y, Z;      	// components of a quaternion

	public PVector rotAxis = null;	// updated when rotated, always normalized or null
	public float angle = 0;

	private static final int manucureAge = 2; // #rotate before normalize.
	private int age = 0;

	public Quaternion(float w, float x, float y, float z) {
		W = w;
		X = x;
		Y = y;
		Z = z;
	}

	public Quaternion() {
		this(1, 0, 0, 0);
	}
	
	public void set(float w, float x, float y, float z) {
		W = w;
		X = x;
		Y = y;
		Z = z;
	}

	public Quaternion(PVector rotation) {
		angle = rotation.mag();
		rotation.normalize();
		this.rotAxis = rotation;
		initFromAxis();
	}

	/** Makes quaternion from normalized axis */
	public Quaternion(float angle, PVector nAxis) {
		this.angle = angle;
		this.rotAxis = nAxis;
		initFromAxis();
	}

	public Quaternion get() {
		return new Quaternion(W, X, Y, Z);
	}

	public PVector rotAxis() {
		if (rotAxis == null) {
			updateAxis();
		}
		if (rotAxis != null)
			return PVector.mult(rotAxis, angle);
		else
			return null;
	}

	public String toString() {
		return "quat: (w:"+W+", "+X+", "+Y+", "+Z+")";
	}

	// ---- manipulation methods

	public Quaternion rotatedBy(Quaternion r) {
		return get().rotate(r);
	}

	public Quaternion rotate(Quaternion r) {
		mult(r);

		if (++age > manucureAge) {
			age = 0;
			normalize();
		}

		updateAxis();

		return this;
	}

	/** axis must be normalized */
	public void updateAxis() {
		// update rotation axis & angle
		float halfomega = PApplet.acos(W);
		float s = PApplet.sin(halfomega);
		if (s == 0) { //no rotation
			angle = 0;
			rotAxis = null;
		} else {
			float invSinHO = 1/s;
			angle = halfomega*2;
			rotAxis = new PVector( X*invSinHO, Y*invSinHO, Z*invSinHO );
			
			rotAxis.normalize();
		}
	}

	public Quaternion withOppositeAngle() {
		return new Quaternion(W, -X, -Y, -Z).normalize();
	}

	public void addAngularMomentum(PVector dL) {
		if (angle == 0) {
			rotAxis = dL;
			angle = dL.mag();
		} else {
			//get L and add dL
			rotAxis.setMag(angle);
			rotAxis.add(dL);
			angle = rotAxis.mag();
		}
		rotAxis.normalize();
		initFromAxis();
	}

	/*
	public boolean isZeroEps(boolean setToZero) {
		if (X + Y + Z < TangibleGame.EPSILON) {
			if (setToZero) {
				W = 1;
				X = Y = Z = 0;
			}
			return true;
		} else
			return false;
	}*/

	// Rotates towards other quaternion
	public static Quaternion slerp(Quaternion a, Quaternion b, float t)
	{
		float omega, cosom, sinom, sclp, sclq;

		cosom = a.X*b.X + a.Y*b.Y + a.Z*b.Z + a.W*b.W;

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

			return new Quaternion ( sclp*a.W + sclq*b.W, sclp*a.X + sclq*b.X,
					sclp*a.Y + sclq*b.Y, sclp*a.Z + sclq*b.Z);
		} else {
			/*X =-a.Y;
				Y = a.X;
				Z =-a.W;*/

			sclp = PApplet.sin((1f-t) * PApplet.PI * 0.5f);
			sclq = PApplet.sin(t * PApplet.PI * 0.5f);

			return new Quaternion (a.Z, sclp*a.X + sclq*b.X,
					sclp*a.Y + sclq*b.Y, sclp*a.Z + sclq*b.Z);
		}
	}

	// ---- basic operations


	/** axis must be normalized */
	public void initFromAxis() { 
		float omega = 0.5f * angle; 
		float s = PApplet.sin(omega);
		if (PApplet.abs(s) > Float.MIN_VALUE) {
			W = PApplet.cos(omega);
			X = s*rotAxis.x;
			Y = s*rotAxis.y;
			Z = s*rotAxis.z;
			normalize();
		} else {
			W = 1;
			X = Y = W = 0;
		}
	}

	public Quaternion multBy(Quaternion q) {
		return get().mult(q);
	}

	// mult by a normalized quaternion
	public Quaternion mult(Quaternion q) {
		//if (q.W != 1) { //identity
		W = W*q.W - X*q.X - Y*q.Y - Z*q.Z;
		X = W*q.X + X*q.W - Y*q.Z + Z*q.Y;
		Y = W*q.Y + Y*q.W - Z*q.X + X*q.Z;
		Z = W*q.Z + Z*q.W - X*q.Y + Y*q.X;
		//}
		return this;
	}

	// get the quaternion inverse == conjugates
	/*private Quaternion inverse() {
		return new Quaternion(W, -X, -Y, -Z);
	}*/

	public Quaternion normalized() {
		return get().normalize();
	}

	public Quaternion normalize() {
		float norm = PApplet.sqrt(W*W + X*X + Y*Y + Z*Z);
		if (norm == 0) {
			W = 1;
			X = Y = Z = 0;
		} else {
			float invNorm = 1f/norm;
			W *= invNorm;
			X *= invNorm;
			Y *= invNorm;
			Z *= invNorm;
		}
		return this;
	}




	//Example of rotating PVector about a directional PVector
	/*PVector rotate(PVector v, PVector r, float a) {
		Quaternion Q1 = new Quaternion(0, v.x, v.y, v.z);
		Quaternion Q2 = new Quaternion(PApplet.cos(a / 2), r.x * PApplet.sin(a / 2), r.y * PApplet.sin(a / 2), r.z * PApplet.sin(a / 2));
		Quaternion Q3 = Q2.mult(Q1).mult(Q2.inverse());
		return new PVector(Q3.X, Q3.Y, Q3.Z);
	}*/
}
