package brabra.game.physic;


import java.util.ArrayList;
import java.util.List;
import brabra.ProMaster;
import brabra.game.physic.geo.Sphere;


public class Physic extends ProMaster {
	
	public static final float epsilon = 1E-5f;

	public float gravity = 0.8f;
	
	private int errCount = 0;
	
	/** Just... do Magic  :D <p>Actually resolve collisions. */
	public void doMagic() {
		game.debug.setCurrentWork("physic magic");
		try {
			//2. on détermine et filtre les collisions pour chaque paire possible (c, o).
			List<Collision> collisions = new ArrayList<>();
			forAllPairs(game.scene.activeColliders(), (c,o)-> {
				if ( (o.affectedByCollision() || c.affectedByCollision())
						&& !c.isRelated(o) 
						&& (o.doCollideFast(c) && c.doCollideFast(o)) ) {
					Collision col;
					if (c.affectedByCollision() && c instanceof Sphere)
						col = new CollisionSphere((Sphere)c, o);
					else if (o.affectedByCollision() && o instanceof Sphere)
						col = new CollisionSphere((Sphere)o, c);
					else if (c instanceof PseudoPolyedre && o instanceof PseudoPolyedre) {
						PseudoPolyedre ppc, ppo;
						if (c.affectedByCollision()) {
							ppc = (PseudoPolyedre)c;
							ppo = (PseudoPolyedre)o;
						} else {
							ppc = (PseudoPolyedre)o;
							ppo = (PseudoPolyedre)c;
						}
						col = new CollisionPPolyedre(ppc, ppo);
					} else
						throw new IllegalArgumentException("colliders pair unhandled: "+c.presentation()+" vs "+o.presentation());
					collisions.add( col );
		    	}
			});
		
			//3. on résout les collisions
			for (Collision col : collisions)
				col.resolve();
			
			//4. on applique les collision aux agents (si nécessaire)
			for (Collision col : collisions)
				col.apply();

		} catch (Exception e) {
			game.debug.err("physical error :/");
			e.printStackTrace();
			if (++errCount >= 3) {
				game.setRunning(false);
				game.debug.msg(1, "physic paused (after 3 errors)");
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
