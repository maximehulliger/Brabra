package brabra;

import static org.junit.Assert.*;

import java.util.concurrent.locks.ReentrantLock;

import brabra.ProMaster;
import brabra.Brabra;
import brabra.game.physic.geo.Quaternion;
import brabra.game.physic.geo.Vector;
import processing.core.PApplet;

public class ProTest extends ProMaster {

	public static final Vector[] directions = new Vector[] {
			up, down, front, behind, right, left };
	
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
		if (!q1.equalsEps(q2, false))
			assertEquals(msg, q1, q2);
	}

	public static void assertEqualsEpsAxis(String msg, Quaternion q1, Quaternion q2) {
		if (!q1.equalsEpsAxis(q2, false))
			assertEquals(msg, q1, q2);
	}

	public static void assertEqualsEps(String msg, Vector p1, Vector p2) {
		if (!(p1.equalsEps(p2, false)))
			assertEquals(msg, p1, p2);
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
