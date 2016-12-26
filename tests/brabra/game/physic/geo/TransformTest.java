package brabra.game.physic.geo;

import static org.junit.Assert.*;

import org.junit.Test;

import brabra.ProTest;

public class TransformTest extends ProTest {

	static {
		initApp();
	}
	
	//final static private Quaternion toRight = new Quaternion(up, pi/2);
	
	@Test
	public void translationTest() {
		for (int i=0; i< 10; i++) {
			final Vector v1 = Vector.randomVec(100);
			final Vector v2 = Vector.randomVec(100);
			assertEquals("at iter "+i, v2, ProTransform.absolute(zero, v2, identity));
			assertEquals("at iter "+i, v2.plus(v1), ProTransform.absolute(v1, v2, identity));
		}
	}

	@Test
	public void testCopy() {
		for (Transform t : someTransform(5)) {
			final Transform newT = new Transform().copy(t);
			assertEquals("t", newT, t);
		}
	}

	@Test
	public void testAbsRel() {
		final float epsilon = 2E-4f;
		for (Transform t : someTransform(10)) {
			final Vector vec = Vector.randomVec(100);
			
			// abs -> rel -> abs
			final Vector vecFromRel = t.absolute(t.relative(vec));
			final Vector dirFromRel = t.absoluteDir(t.relativeDir(vec));
			assertEqualsEps("", vec, vecFromRel, epsilon);
			assertEqualsEps("", vec, dirFromRel, epsilon);
			
			// rel -> abs -> rel
			final Vector vecFromAbs = t.relative(t.absolute(vec));
			final Vector dirFromAbs = t.relativeDir(t.absoluteDir(vec));
			assertEqualsEps("", vec, vecFromAbs, epsilon);
			assertEqualsEps("", vec, dirFromAbs, epsilon);
		}
	}
	
	@Test
	public void testRelLoc() {
		final float epsilon = 2E-4f;
		for (Transform t : someTransform(10)) {
			final Vector vec = Vector.randomVec(100);
			
			// loc -> rel -> loc
			final Vector vecFromRel = t.localFromRel(t.relativeFromLocal(vec));
			assertEqualsEps("", vec, vecFromRel, epsilon);
			
			// rel -> rel -> rel
//			final Vector vecFromAbs = t.relativeFromLocal(t.localFromRel(vec));
//			final Vector dirFromAbs = t.localDirFromRel(t.absoluteDir(vec));
//			assertEqualsEps("", vec, vecFromAbs, epsilon);
//			assertEqualsEps("", vec, dirFromAbs, epsilon);
		}
	}
}
