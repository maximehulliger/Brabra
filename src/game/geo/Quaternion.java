package game.geo;

import processing.core.PApplet;
import processing.core.PVector;
/***************************************************************************
 * Quaternion class written by BlackAxe / Kolor aka Laurent Schmalen in 1997
 * Translated to Java(with Processing) by RangerMauve in 2012
 * this class is freeware. you are fully allowed to use this class in non-
 * commercial products. Use in commercial environment is strictly prohibited
 */

public class Quaternion {
	public  float W, X, Y, Z;      // components of a quaternion

	// default constructor
	public Quaternion() {
		W = 1f;
		X = 0f;
		Y = 0f;
		Z = 0f;
	}

	// initialized constructor

	public Quaternion(float w, float x, float y, float z) {
		W = w;
		X = x;
		Y = y;
		Z = z;
	}

	// quaternion multiplication
	public Quaternion mult (Quaternion q) {
		float w = W*q.W - (X*q.X + Y*q.Y + Z*q.Z);

		float x = W*q.X + q.W*X + Y*q.Z - Z*q.Y;
		float y = W*q.Y + q.W*Y + Z*q.X - X*q.Z;
		float z = W*q.Z + q.W*Z + X*q.Y - Y*q.X;

		W = w;
		X = x;
		Y = y;
		Z = z;
		return this;
	}

	// conjugates the quaternion
	public Quaternion conjugate () {
		X = -X;
		Y = -Y;
		Z = -Z;
		return this;
	}

	// inverts the quaternion
	public Quaternion reciprical () {
		float norme = PApplet.sqrt(W*W + X*X + Y*Y + Z*Z);
		if (norme == 0.0)
			norme = 1f;

		float recip = 1f / norme;

		W =  W * recip;
		X = -X * recip;
		Y = -Y * recip;
		Z = -Z * recip;

		return this;
	}

	// sets to unit quaternion
	public Quaternion normalize() {
		float norme = PApplet.sqrt(W*W + X*X + Y*Y + Z*Z);
		if (norme == 0.0)
		{
			W = 1f; 
			X = Y = Z = 0f;
		}
		else
		{
			float recip = 1f/norme;

			W *= recip;
			X *= recip;
			Y *= recip;
			Z *= recip;
		}
		return this;
	}

	// Makes quaternion from axis
	public Quaternion fromAxis(float Angle, float x, float y, float z) { 
		float omega, s, c;

		s = PApplet.sqrt(x*x + y*y + z*z);

		if (PApplet.abs(s) > Float.MIN_VALUE)
		{
			c = 1f/s;

			x *= c;
			y *= c;
			z *= c;

			omega = -0.5f * Angle;
			s = PApplet.sin(omega);

			X = s*x;
			Y = s*y;
			Z = s*z;
			W = PApplet.cos(omega);
		}
		else
		{
			X = Y = 0.0f;
			Z = 0.0f;
			W = 1.0f;
		}
		normalize();
		return this;
	}

	public Quaternion fromAxis(float Angle, PVector axis) {
		return fromAxis(Angle, axis.x, axis.y, axis.z);
	}

	// Rotates towards other quaternion
	public void slerp(Quaternion a, Quaternion b, float t)
	{
		float omega, cosom, sinom, sclp, sclq;


		cosom = a.X*b.X + a.Y*b.Y + a.Z*b.Z + a.W*b.W;


		if ((1.0f+cosom) > Float.MIN_VALUE)
		{
			if ((1.0f-cosom) > Float.MIN_VALUE)
			{
				omega = PApplet.acos(cosom);
				sinom = PApplet.sin(omega);
				sclp = PApplet.sin((1.0f-t)*omega) / sinom;
				sclq = PApplet.sin(t*omega) / sinom;
			}
			else
			{
				sclp = 1.0f - t;
				sclq = t;
			}

			X = sclp*a.X + sclq*b.X;
			Y = sclp*a.Y + sclq*b.Y;
			Z = sclp*a.Z + sclq*b.Z;
			W = sclp*a.W + sclq*b.W;
		}
		else
		{
			X =-a.Y;
			Y = a.X;
			Z =-a.W;
			W = a.Z;

			sclp = PApplet.sin((1f-t) * PApplet.PI * 0.5f);
			sclq = PApplet.sin(t * PApplet.PI * 0.5f);

			X = sclp*a.X + sclq*b.X;
			Y = sclp*a.Y + sclq*b.Y;
			Z = sclp*a.Z + sclq*b.Z;
		}
	}

	public Quaternion exp()
	{                               
		float Mul;
		float Length = PApplet.sqrt(X*X + Y*Y + Z*Z);

		if (Length > 1.0e-4)
			Mul = PApplet.sin(Length)/Length;
		else
			Mul = 1f;

		W = PApplet.cos(Length);

		X *= Mul;
		Y *= Mul;
		Z *= Mul; 

		return this;
	}

	public Quaternion log()
	{
		float Length;

		Length = PApplet.sqrt(X*X + Y*Y + Z*Z);
		Length = PApplet.atan(Length/W);

		W = 0f;

		X *= Length;
		Y *= Length;
		Z *= Length;

		return this;
	}

	//Example of rotating PVector about a directional PVector
	PVector rotate(PVector v, PVector r, float a) {
		Quaternion Q1 = new Quaternion(0, v.x, v.y, v.z);
		Quaternion Q2 = new Quaternion(PApplet.cos(a / 2), r.x * PApplet.sin(a / 2), r.y * PApplet.sin(a / 2), r.z * PApplet.sin(a / 2));
		Quaternion Q3 = Q2.mult(Q1).mult(Q2.conjugate());
		return new PVector(Q3.X, Q3.Y, Q3.Z);
	}
}
