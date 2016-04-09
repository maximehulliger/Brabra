package brabra;

import static org.junit.Assert.*;

import java.util.concurrent.locks.ReentrantLock;

import brabra.ProMaster;
import brabra.Brabra;
import brabra.game.physic.Physic;
import brabra.game.physic.geo.Quaternion;
import brabra.game.physic.geo.Vector;
import processing.core.PApplet;

public class ProTest extends ProMaster {

	public static void initApp() {
		if (ProMaster.app == null) {
			TestTangibleGame testApp = new TestTangibleGame();
			app = testApp;
			(new Thread(
					() -> PApplet.runSketch(new String[] {TestTangibleGame.class.getName()}, app)
					)).start();
			assert(testApp.ready.isLocked());	
			testApp.ready.lock();
			testApp.ready.unlock();
			boolean ok = false;
			while (!ok) {
				try {
					synchronized (app) {
						app.pushMatrix();
						app.popMatrix();
					}
					ok = true;
				} catch (Exception e) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			}
		}
	}

	public static void assertEqualsEps(String msg, Quaternion q1, Quaternion q2) {
		
		//assertEquals(msg, q1.angle(), q2.angle(), Quaternion.epsilonAngle);
		//assertEqualsEps(msg, q1.rotAxis(), q2.rotAxis(), Quaternion.epsilonRotAxis);
		if (!q1.equalsEps(q2, false))
			assertEquals(msg, q1, q2);
	}

	public static void assertEqualsEps(String msg, Vector p1, Vector p2) {
		assertEqualsEps(msg, p1, p2, Physic.epsilon);
	}

	public static void assertEqualsEps(String msg, Vector p1, Vector p2, float epsilon) {
		if (p1 == null)
			assertEquals(p1, p2);
		else if (!(p1.equalsEps(p2, false, epsilon)))
			assertEquals(msg, p1, p2);
	}

	public Vector[] someVectors(int n) {
		Vector[] ret = new Vector[n];
		for (int i=0; i<n; i++)
			ret[i] = Vector.randomVec(1);
		return ret;
	}

	private static class TestTangibleGame extends Brabra {
		/** released once everything is setup. */
		public final ReentrantLock ready = new ReentrantLock();
		
		public TestTangibleGame() {
			ready.lock();
			debug.testMode = true;
			imgAnalysis = false;
			toolWindow = false;
		}

		public void settings() {
			size(0, 0, "processing.opengl.PGraphics3D");
		}
		
		public void setup() {
			frame.setVisible(false);
			setView(View.None);
		}
		
		public void draw() {
			if (ready.isHeldByCurrentThread()){
				System.out.println("ready");
				ready.unlock();
			}
		}
	}
}
