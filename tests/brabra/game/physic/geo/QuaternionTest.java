package brabra.game.physic.geo;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import brabra.ProTest;
import brabra.game.physic.geo.Quaternion;
import brabra.game.physic.geo.Vector;

public class QuaternionTest extends ProTest {
	
	private int iter = 0;
	
	@Before
	public void setUp() throws Exception {
		iter = 0;
	}

	@Test
	public void randomWXYZTest() {
		for (int i=0; i<10; i++) {
			// init by wxyz
			float w = randomBi(), x = randomBi(), y = randomBi(), z = randomBi();
			final Quaternion q3 = new Quaternion(w, x, y, z);
			final Quaternion q4 = new Quaternion(q3.rotAxis(), q3.angle());
			
			assertTrue(q3.equalsAxis(q4));
			assertEqualsEps("", q3.rotAxisAngle(), q4.rotAxisAngle(), Quaternion.epsilonRotAxis);
			assertEqualsEps("", q3, q4);
			assertTrue(isConstrained(q3.angle(), -pi, pi));
		}
	}

	@Test
	public void rotateTest() {
		// rotate to make a 360° turn
		for (Vector v : Vector.directions) {
			final int steps = 8;
			Quaternion r = new Quaternion(v, 2*pi/steps);
			Quaternion acc = identity.copy();
			for (int i=0; i<steps; i++) 
				acc.rotate(r);
			
			assertEqualsEps("for rotate around vector "+v, acc, identity);
		}
	}

	@Test
	public void rotateInverseTest() {
		for (Vector v : someVectors(10)) {
			final Quaternion q1 = new Quaternion(v, random(-pi, pi));
			final Quaternion q2 = q1.withOppositeAngle();
			
			assertEqualsEps("", q1.rotatedBy(q2), Quaternion.identity);
		}
	}

	@Test
	public void unityTest() {
		// rotatation of 360° around an axis
		for (Vector v : someVectors(10)) {
			final Quaternion u2 = new Quaternion(v, twoPi);
			
			assertEqualsEps("", u2, Quaternion.identity);
		}
	}
	
	@Test
	public void mainDirectionTest() {
		// test the rot axis.
		for (Quaternion q1 : quatsFromDir()) {
			iter++;
			Quaternion q2 = new Quaternion(q1.rotAxis(), q1.angle());
			
			assertEqualsEps("at iter "+iter, q2, q1);
		}
	}

	@Test
	public void randomAxisTest() {
		for (Vector v : someVectors(10)) {
			// init by rot axis.
			final Quaternion q1 = new Quaternion(v, random(-pi, pi));
			final Quaternion q2 = new Quaternion(q1.rotAxis(), q1.angle());
			
			assertEqualsEps("", q1, q2);
		}
	}
	
	private List<Quaternion> quatsFromDir() {
		List<Quaternion> l = new ArrayList<Quaternion>(6);
		for (Vector dir : Vector.directions)
			l.add(Quaternion.fromDirection(dir));
		return l;
	}
}