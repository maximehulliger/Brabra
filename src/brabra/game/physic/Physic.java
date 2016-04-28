package brabra.game.physic;


import java.util.ArrayList;
import brabra.ProMaster;
import brabra.game.physic.geo.Sphere;
import brabra.game.scene.Scene;

/** Static class taking care of the physic cycle. */
public class Physic extends ProMaster {
	
	/** Small value used to consider a value as zero. */
	public static final float epsilon = 1E-5f;
	
	/** List of all the current collision. */
	public static final ArrayList<Collision> collisions = new ArrayList<>();
	
	// buffers
	private static Collision colBuffer;
	private static Sphere sphereBuffer;
	private static PseudoPolyedre ppBuffer1, ppBuffer2;

	// to pause after 3 exception in magic.
	private static int errCount = 0;

	/** Just... do Magic  :D <p> Actually resolve collisions in the scene. */
	public static void doMagic(Scene scene) {
		app.debug.setCurrentWork("physic magic");
		try {
			//2. on determine et filtre les collisions pour chaque paire possible (c, o).
			forAllPairs(scene.activeColliders(), (c,o)-> {
				if ( (o.affectedByCollision() || c.affectedByCollision())
						&& !c.isRelated(o) 
						&& (o.doCollideFast(c) && c.doCollideFast(o)) ) {
					// easy first: with at least one sphere
					if ((sphereBuffer = c.as(Sphere.class)) != null)
						colBuffer = new CollisionSphere(sphereBuffer, o);
					else if ((sphereBuffer = o.as(Sphere.class)) != null)
						colBuffer = new CollisionSphere(sphereBuffer, c);
					// then polyedron against polyedron
					else if ((ppBuffer1 = c.as(PseudoPolyedre.class)) != null
							&& (ppBuffer2 = o.as(PseudoPolyedre.class)) != null) 
						colBuffer = new CollisionPPolyedre(ppBuffer1, ppBuffer2);
					else
						throw new IllegalArgumentException("colliders pair unhandled: "+c.presentation()+" vs "+o.presentation());
					collisions.add( colBuffer );
		    	}
			});
		
			//3. on r�sout les collisions
			for (Collision col : collisions)
				col.resolve();
			
			//4. on applique les collision aux agents (si n�cessaire)
			for (Collision col : collisions)
				col.apply();

		} catch (Exception e) {
			app.debug.err("physical error :/");
			e.printStackTrace();
			if (++errCount >= 3) {
				app.para.setRunning(false);
				app.debug.msg(1, "physic paused (after 3 errors)");
			}
		}
	}
	
	// --- EPSILON (small value) ---

	/** Return true if f is nearly zero. */
	public static boolean isZeroEps(float f) {
		return equalsEps(f, 0, epsilon);
	}

	/** Return true if f is nearly zero (after given epsilon). */
	public static boolean isZeroEps(float f, float epsilon) {
		return equalsEps(f, 0, epsilon);
	}

	/** Return true if f1 is nearly equal to f2. */
	public static boolean equalsEps(float f1, float f2) {
		return equalsEps(f1, f2, epsilon);
	}

	/** Return true if f1 is nearly equal to f2. */
	public static boolean equalsEps(float f1, float f2, float epsilon) {
		return f1==f2 || isConstrained(f1-f2, -epsilon, epsilon);
	}
}
