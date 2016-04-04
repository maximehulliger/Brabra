package brabra.game.physic.geo;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import brabra.ProTest;
import brabra.game.physic.Physic;
import brabra.game.physic.geo.Quaternion;
import brabra.game.physic.geo.Vector;

public class QuaternionTest extends ProTest {
	private final static List<Quaternion> dirSample = new ArrayList<>();
	
	private int iter = 0;
	
	// Before before & once only
	static {
		for (Vector dir : directions)
			dirSample.add(Quaternion.fromDirection(dir));
	}
	
	@Before
	public void setUp() throws Exception {
		iter = 0;
	}

	@Test
	public void initFromAxisTest() {
		for (Quaternion q : dirSample) {
			iter++;
			assertEqualsEps("at iter "+iter, q, new Quaternion(q.rotAxis(), q.angle()));
		}
	}

	@Test
	public void coherenceTest() {
		for (Quaternion q : dirSample) {
			iter++;
			Quaternion qp = q.copy().initFromAxis();
			assertEqualsEps("at iter "+iter, q, qp);
			
			float angle = q.angle();
			Vector rotAxis = q.rotAxis();
			q.updateAxis();
			assertEqualsEps("at iter "+iter, q, new Quaternion(rotAxis, angle));
		}
	}

	@Test
	public void randomTest() {
		final int sampleSize = 50;
		// rotAxis -> wxyz -> rotAxis
		for (int i=0; i < sampleSize; i++) {
			Quaternion q1 = new Quaternion(Vector.randomVec(1), random(-pi, pi));
			float angle = q1.angle();
			Vector rotAxis = q1.rotAxis();
			q1.updateAxis();
			assertEquals("at iter "+i, angle, q1.angle(), Physic.epsilon);
			assertEqualsEps("at iter "+i, rotAxis, q1.rotAxis());
			assertEqualsEps("at iter "+i, q1, new Quaternion(rotAxis, angle));
		}
		
		// rotAxis -> wxyz -> rotAxis
		for (int i=0; i < sampleSize; i++) {
			Quaternion q1 = new Quaternion(Vector.randomVec(1), random(-pi, pi));
			float angle = q1.angle();
			Vector rotAxis = q1.rotAxis();
			q1.updateAxis();
			assertEquals("at iter "+i, angle, q1.angle(), Physic.epsilon);
			assertEqualsEps("at iter "+i, rotAxis, q1.rotAxis());
			assertEqualsEps("at iter "+i, q1, new Quaternion(rotAxis, angle));
		}
	}
}
