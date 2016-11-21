package brabra.game.physic;


import java.util.ArrayList;

import brabra.Debug;
import brabra.ProMaster;
import brabra.game.physic.geo.Plane;
import brabra.game.physic.geo.Sphere;
import brabra.game.scene.Scene;

/** Static class taking care of the physic cycle. */
public class Physic extends ProMaster {
	
	/** Small value used to consider a value as zero. */
	public static final float epsilon = 1E-5f;
	
	/** List of all the current collision. */
	public static final ArrayList<Collision<?, ?>> collisions = new ArrayList<>();
	
	// to pause after 3 exception in magic.
	private static int errCount = 0;

	/** Just... do Magic  :D <p> Actually resolve collisions in the scene. */
	public static void doMagic(Scene scene) {
		Debug.setCurrentWork("physic magic");
		try {
			collisions.clear();
			
			//2. on determine et filtre les collisions pour chaque paire possible (c, o).
			forAllPairs(scene.activeColliders(), (c1,c2)-> {
				if ( (c1.affectedByPhysic() || c2.affectedByPhysic())
						&& !c1.isRelated(c2) ) {
					// with a sphere first
					if (c1 instanceof Sphere && c2 instanceof Sphere)
						collisions.add( new CollisionSphereSphere((Sphere)c1, (Sphere)c2));
					else if (c1 instanceof Sphere && c2 instanceof Plane)
						collisions.add( new CollisionPlaneSphere((Plane)c2, (Sphere)c1));
					else if (c1 instanceof Plane && c2 instanceof Sphere)
						collisions.add( new CollisionPlaneSphere((Plane)c1, (Sphere)c2));
//					else
//						throw new IllegalArgumentException("colliders pair unhandled: "+c1.presentation()+" vs "+c2.presentation());
		    	}
			});
		
			//3. on r�sout les collisions
			for (Collision<?, ?> col : collisions)
				col.resolve();
			
			//4. on applique les collision aux agents (si n�cessaire)
			for (Collision<?, ?> col : collisions)
				col.apply();
		} catch (Exception e) {
			Debug.err("physical error :/");
			e.printStackTrace();
			if (++errCount >= 3) {
				app.para.setRunning(false);
				Debug.msg(1, "physic paused (after 3 errors)");
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
