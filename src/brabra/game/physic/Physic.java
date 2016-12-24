package brabra.game.physic;

import brabra.ProMaster;
/** Static class taking care of the physic cycle. */
public class Physic extends ProMaster {
	
	/** Small value used to consider a value as zero. */
	public static final float epsilon = 1E-5f;
	
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
