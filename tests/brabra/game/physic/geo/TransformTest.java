package brabra.game.physic.geo;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

import brabra.ProTest;

public class TransformTest <T extends Transform<Integer>> extends ProTest {

	static {
		initApp();
	}
	
	@Test
	public void consistencyTest() {
		ArrayList<T> trans = someTransform(5);
		T t0 = trans.get(0);
		T t1 = trans.get(1);
		//T trans2 = trans.get(2);
		t0.setParent(t1, null);
		assertEquals("simple full parent", t1.location(), t0.locationRel.plus(t1.locationRel));
	}
	
	@Test
	public void translationTest() {
		for (int i=0; i< 50; i++) {
			Vector v = Vector.randomVec(100);
			Vector t = Vector.randomVec(100);
			assertEquals("at iter "+i, t, absolute(zero, t, identity));
			assertEquals("at iter "+i, t.plus(v), absolute(v, t, identity));
		}
	}

//	@Test
//	public void testCopy() {
//	}
	
	@SuppressWarnings("unchecked")
	private ArrayList<T> someTransform(int n) {
		ArrayList<T> trans = new ArrayList<>(n);
		for (int i=0; i<n; i++) {
			final Vector loc = Vector.randomVec(100);
			final Transform<Integer> t = new  Transform<Integer>(n);
			t.set(loc, null);
			trans.add((T)t);
		}
		return trans;
	}
}
