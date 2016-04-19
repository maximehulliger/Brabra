package brabra.game.physic.geo;

import static org.junit.Assert.*;
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
	public void allTest() {
		consistencyTest(someQuats(10));
	}

	@Test
	public void initWXYZTest() {
		consistencyTest(someQuatsFromWXYZ(100));
	}
	
	@Test
	public void initDirectionTest() {
		consistencyTest(quatsFromDir());
	}

	@Test
	public void initAxisTest() {
		consistencyTest(someQuatsFromAxis(100));
	}

	private void consistencyTest(Iterable<Quaternion> quatsToTest) {
		for (Quaternion q : quatsToTest) {
			iter++;
			final Quaternion q2 = new Quaternion(q);
			final Quaternion q3 = new Quaternion(q.rotAxis(), q.angle());
			
			assertEqualsEps("at iter "+iter, q, q2);
			assertEqualsEps("at iter "+iter, q, q3);
		}
	}

	@Test
	public void rotateTest() {
		for (Quaternion r2 : someQuats(100)) {
			iter++;
			final Quaternion r1 = r2.withAngle( r2.angle()/2 );
			final Quaternion r4 = r2.withAngle( r2.angle()*2 );
			final Quaternion r12 = r1.rotatedBy(r1);
			final Quaternion r24 = r2.rotatedBy(r2);
			
			assertFalse("at iter "+iter, r1.equals(identity));
			assertFalse("at iter "+iter, r4.equals(identity));
			assertFalse("at iter "+iter, r12.equals(identity));
			assertFalse("at iter "+iter, r24.equals(identity));
			assertEqualsEps("at iter "+iter, r2, r12);
			assertEqualsEps("at iter "+iter, r4, r24);
		}
	}
	
	@Test
	public void rotate360Test() {
		for (Vector v : both(Vector.directions, someVectors(100))) {
			iter++;
			final int steps = random(2, 64);
			// rotate to make a 360° turn
			final float angle = twoPi/steps;
			Quaternion r = new Quaternion(v, angle);
			Quaternion acc = identity.copy();
			for (int i=0; i<steps; i++) {
				acc.rotate(r);
				if (i>0 && i<steps-1)
					assertFalse("at mult "+(i+1)+"/"+(steps+1), acc.equals(identity));
			}
			
			assertEqualsEps("at iter "+iter+", "+steps+" steps ("+angle*toDegrees+") around "+v+"\n", identity, acc);
		}
	}

	@Test
	public void contraryTest() {
		for (Quaternion q : someQuatsFromWXYZ(10)) {
			iter++;
			assertEqualsEps("at iter "+iter, q, q.contrary());
		}
	}

	@Test
	public void unityTest() {
		assertEqualsEps("unity contrary", Quaternion.identity.contrary(), Quaternion.identity);
		
		for (Vector v : both(Vector.directions, someVectors(10))) {
			iter++;
			final Quaternion u2 = new Quaternion(v, twoPi);
			final Quaternion u3 = new Quaternion(null, pi);
			final Quaternion u4 = new Quaternion(v, 0);
			final Quaternion uEps1 = new Quaternion(v, twoPi - Quaternion.epsilonAngle/2);
			final Quaternion uEps2 = new Quaternion(v, twoPi + Quaternion.epsilonAngle/2);
			
			assertEqualsEps("at iter "+iter, Quaternion.identity, u2);
			assertEqualsEps("at iter "+iter, Quaternion.identity, u3);
			assertEqualsEps("at iter "+iter, Quaternion.identity, u4);
			assertEqualsEps("at iter "+iter, Quaternion.identity, uEps1);
			assertEqualsEps("at iter "+iter, Quaternion.identity, uEps2);
		}
	}
	
	@Test
	public void oppositeAngleTest() {
		for (Quaternion q : someQuats(50)) {
			iter++;
			final float qa = q.angle();
			final Quaternion qOp1 = q.withOppositeAngle();
			final Quaternion qOp2 = q.withAngle(-qa);
			final Quaternion q1 = qOp1.withAngle(-qOp1.angle());
			final Quaternion q2 = qOp2.withOppositeAngle();

			assertEqualsEps("at iter "+iter+" with angle = "+q.angle(), qOp1, qOp2);
			assertEqualsEps("at iter "+iter+" with angle = "+q.angle(), q, q1);
			assertEqualsEps("at iter "+iter+" with angle = "+q.angle(), q, q2);
		}
	}

	@Test
	public void rotateOppositeTest() {
		for (Quaternion q : someQuats(50)) {
			iter++;
			final Quaternion q2 = q.withOppositeAngle();
			
			assertEqualsEps("at iter "+iter, q.rotatedBy(q2), Quaternion.identity);
		}
	}

	private List<Quaternion> quatsFromDir() {
		List<Quaternion> l = new ArrayList<Quaternion>(6);
		for (Vector dir : Vector.directions)
			l.add(Quaternion.fromDirection(dir, Vector.up));
		assertTrue(l.size() == 6);
		return l;
	}

	private List<Quaternion> quatsFromDirNonId() {
		List<Quaternion> l = new ArrayList<Quaternion>(6);
		Quaternion q;
		for (Vector dir : Vector.directions)
			if (!(q = Quaternion.fromDirection(dir, Vector.up)).isIdentity())
				l.add(q);
		assertTrue(l.size() == 5);
		return l;
	}

	private List<Quaternion> someQuatsFromWXYZ(int n) {
		List<Quaternion> l = new ArrayList<Quaternion>(n);
		for (int i=0; i<n; i++) {
			Quaternion qm = null;
			while (qm == null || qm.isIdentity()) {
				qm = new Quaternion(randomBi(), randomBi(), randomBi(), randomBi());
			}
			l.add(qm);
		}
		assertTrue(l.size() == n);
		return l;
	}

	private List<Quaternion> someQuatsFromAxis(int n) {
		List<Quaternion> l = new ArrayList<Quaternion>(n);
		for (Vector v : someVectors(n)) {
			Quaternion qm = null;
			while (qm == null || qm.isIdentity())
				qm = new Quaternion(v, random(-pi, pi));
			l.add(qm);
		}
		assertTrue(l.size() == n);
		return l;
	}
	
	private List<Quaternion> someQuats(int n) {
		final int sampleCount = 5 + 2 * n;
		final List<Quaternion> sample =  new ArrayList<>(sampleCount);
		for (Quaternion q : both(quatsFromDirNonId(), both(someQuatsFromWXYZ(n), someQuatsFromAxis(n)))) {
			if (!q.isIdentity())
				sample.add(q);
		}
		
		assertTrue("problem with quats test sample: have only "+sample.size()+" over "+sampleCount+".", sample.size() == sampleCount);
		
		return sample;
	}
}