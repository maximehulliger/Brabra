package brabra;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import brabra.ProTest;
import processing.core.PVector;

public class TransformTest extends ProTest {
	
	@Before
	public void setup() {
		initApp();
	}

	@Test
	public void translation() {
		for (int i=0; i< 50; i++) {
			PVector v = randomVec(100);
			PVector t = randomVec(100);
			assertEquals("at iter "+i, t, absolute(zero, t, identity));
			assertEquals("at iter "+i, add(t,v), absolute(v, t, identity));
		}
	}
	
	@Test
	public void rotation() {
		assertTrue(true);
	}
}
