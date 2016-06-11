package brabra.game.physic.geo;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

import brabra.ProTest;
import brabra.game.physic.geo.Transform.ParentRelationship;

public class TransformTest extends ProTest {

	static {
		initApp();
	}
	
	//final static private Quaternion toRight = new Quaternion(up, pi/2);
	
	@Test
	public void simpleParentTest() {
		final int nTest = 5;
		final ArrayList<Transform> trans = someTransform(nTest*2);
		for (int i=0; i<nTest*2; i+=2) {
			final Transform t0 = trans.get(i);
			final Transform t1 = trans.get(i+1);
			//T trans2 = trans.get(2);
			t1.setParent(t0, ParentRelationship.Static);
			final Vector loc1 = t1.location();
			final Vector loc2 = t0.location().plus(t1.locationRel);
			assertEquals("simple full parent", loc1, loc2);
		}
	}
	
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
	public void testAbsLocal() {
		for (Transform t : someTransform(5)) {
			final Vector vec = Vector.randomVec(100);
			
			// abs -> loc -> abs
			final Vector vecFromLoc = t.absoluteFromLocal(t.local(vec));
			final Vector dirFromLoc = t.absoluteDirFromLocal(t.localDir(vec));
			assertEqualsEps("", vec, vecFromLoc);
			assertEqualsEps("", vec, dirFromLoc);
			
			// loc -> abs -> loc
			final Vector vecFromAbs = t.local(t.absoluteFromLocal(vec));
			final Vector dirFromAbs = t.localDir(t.absoluteDirFromLocal(vec));
			assertEqualsEps("", vec, vecFromAbs);
			assertEqualsEps("", vec, dirFromAbs);
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
