package brabra;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import brabra.ProMaster;
import brabra.game.physic.geo.Quaternion;
import processing.core.PVector;

public class QuaternionTest extends ProTest {
	private final static List<Quaternion> dirSample = new ArrayList<>();
	
	private int iter = 0;
	
	// Before before & once only
	static {
		for (String dir : ProMaster.directions)
			dirSample.add(Quaternion.fromDirection(vec(dir)));
	}
	
	@Before
	public void setUp() throws Exception {
		iter = 0;
	}

	@Test
	public void testDirection() {
		for (Quaternion q : dirSample) {
			iter++;
			assertEqualsEps("at iter "+iter, q, new Quaternion(q.rotAxis(), q.angle()));
		}
	}

	@Test
	public void updateTest() {
		for (Quaternion q : dirSample) {
			iter++;
			assertTrue(q.equalsAxis( q.copy().initFromAxis() ));
			float angle = q.angle();
			PVector rotAxis = q.rotAxis();
			q.updateAxis();
			assertEqualsEps("at iter "+iter, q, new Quaternion(rotAxis, angle));
		}
	}

	@Test
	public void randomTest() {
		final int sampleSize = 50;
		// rotAxis -> wxyz -> rotAxis
		for (int i=0; i < sampleSize; i++) {
			Quaternion q1 = new Quaternion(randomVec(1), random(-pi, pi));
			float angle = q1.angle();
			PVector rotAxis = q1.rotAxis();
			q1.updateAxis();
			assertEquals("at iter "+i, angle, q1.angle(), epsilon);
			assertEqualsEps("at iter "+i, rotAxis, q1.rotAxis());
			assertEqualsEps("at iter "+i, q1, new Quaternion(rotAxis, angle));
		}
		
		// rotAxis -> wxyz -> rotAxis
		for (int i=0; i < sampleSize; i++) {
			Quaternion q1 = new Quaternion(randomVec(1), random(-pi, pi));
			float angle = q1.angle();
			PVector rotAxis = q1.rotAxis();
			q1.updateAxis();
			assertEquals("at iter "+i, angle, q1.angle(), epsilon);
			assertEquals("at iter "+i, rotAxis, q1.rotAxis());
			assertEquals("at iter "+i, q1, new Quaternion(rotAxis, angle));
		}
	}
}
