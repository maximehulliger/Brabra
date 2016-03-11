package cs211.tangiblegame;

import org.junit.Before;
import org.junit.Test;

import processing.core.PMatrix;
import processing.core.PMatrix3D;

public class MatrixTest extends ProTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() {
		PMatrix m1 = new PMatrix3D();
		m1.rotateX(halfPi);
		println(m1.toString());
		println(m1.get(new float[]{}));
	}
}
