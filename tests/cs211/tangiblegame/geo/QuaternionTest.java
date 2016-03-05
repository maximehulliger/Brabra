package cs211.tangiblegame.geo;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import cs211.tangiblegame.ProMaster;
import cs211.tangiblegame.geo.Quaternion;
import processing.core.PVector;

public class QuaternionTest extends Quaternion {
	final List<Quaternion> sample1 = new ArrayList<>();
	final List<Quaternion> sample2 = new ArrayList<>();
	
	@Before
	public void setUp() throws Exception {
		for (String dir : ProMaster.directions)
			sample1.add(Quaternion.fromDirection(vec(dir)));
	}

	@Test
	public void test() {
		for (Quaternion q : sample1) {
			assertEquals(q, new Quaternion(q.rotAxis(), q.angle()));
		}
	}

	@Test
	public void updateTest() {
		for (Quaternion q : sample1) {
			float angle = q.angle();
			PVector rotAxis = q.rotAxis();
			q.updateAxis();
			assertEquals(q, new Quaternion(rotAxis, angle));
		}
	}

	@Test
	public void randomTest() {
		final float epsilon = 0;//0.00_001f;
		final int sampleSize = 50;
		// rotAxis -> wxyz -> rotAxis
		for (int i=0; i < sampleSize; i++) {
			Quaternion q1 = new Quaternion(randomVec(), random(-pi, pi));
			float angle = q1.angle();
			PVector rotAxis = q1.rotAxis();
			q1.updateAxis();
			assertEquals(angle, q1.angle(), epsilon);
			assertEquals(rotAxis, q1.rotAxis());
			assertEquals(q1, new Quaternion(rotAxis, angle));
		}
		// rotAxis -> wxyz -> rotAxis
		for (int i=0; i < sampleSize; i++) {
			Quaternion q1 = new Quaternion(randomVec(), random(-pi, pi));
			float angle = q1.angle();
			PVector rotAxis = q1.rotAxis();
			q1.updateAxis();
			assertEquals(angle, q1.angle(), epsilon);
			assertEquals(rotAxis, q1.rotAxis());
			assertEquals(q1, new Quaternion(rotAxis, angle));
		}
	}
}
