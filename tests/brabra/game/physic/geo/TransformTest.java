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
		for (int i=0; i< 50; i++) {
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
}
