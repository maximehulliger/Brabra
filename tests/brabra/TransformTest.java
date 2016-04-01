package brabra;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import brabra.ProTest;
import brabra.game.physic.geo.Vector;

public class TransformTest extends ProTest {
	
	@Before
	public void setup() {
		initApp();
	}

	@Test
	public void translation() {
		for (int i=0; i< 50; i++) {
			Vector v = Vector.randomVec(100);
			Vector t = Vector.randomVec(100);
			assertEquals("at iter "+i, t, absolute(zero, t, identity));
			assertEquals("at iter "+i, t.plus(v), absolute(v, t, identity));
		}
	}
	
	@Test
	public void rotation() {
		assertTrue(true);
	}
}
