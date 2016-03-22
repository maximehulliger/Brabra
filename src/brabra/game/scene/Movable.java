package brabra.game.scene;

import brabra.game.physic.geo.Quaternion;
import processing.core.PVector;

/**
 * A movable Object. 
 * you can move it through velocity & rotation velocity (both relative to the parent)
 **/
public class Movable extends Object {

	/** Velocity relative to the parent. */
	protected final NVector velocityRel = new NVector(zero);
	/** Rotation velocity relative to the parent. */
	protected final NQuaternion rotationRelVel = new NQuaternion(identity);

	/** Indicates if the object was moving last frame. */
	private boolean moving = false, rotating = false;


	public Movable(PVector location, Quaternion rotation) {
		super(location, rotation);
		// TODO Auto-generated constructor stub
	}

	// --- Getters ---
	
	/** Return the absolute velocity at the center of mass. */
	public PVector velocity() {
		PVector forMe = absoluteDirFromLocal(velocityRel);
		return hasParent() ? add(forMe , parent().velocityAtRel(locationRel)) : forMe;
	}

	/** Return the absolute velocity (from an absolute pos). */
	public PVector velocityAt(PVector posAbs) {
		return velocityAtRel(relative(posAbs));
	}

	/** Return the absolute velocity (from a relative pos). */
	public PVector velocityAtRel(PVector posRel) {
		return (rotationRel.isZeroEps(false) || isZeroEps(posRel, false))
			? velocity() : add(velocity(), rotationRelVel.rotAxisAngle().cross(posRel));
	}
	
	protected String state(boolean onlyChange) {
		boolean vel, rotVel;
		if (onlyChange) {
			vel = velocityRel.equals(zero);
			rotVel = rotationRelVel.isIdentity();
		} else
			vel = rotVel = false;
		return super.state(onlyChange)
				+ (vel ? "" : "\nvitesse rel: "+velocityRel+" -> "+velocityRel.mag()+" unit/frame.") 
				+ (rotVel	? "" : "\nvitesse Ang.: "+rotationRelVel);
	}
	
	protected boolean update() {
		if (!updated) {
			// 1. movement
			if (moving || velocityRel.hasChangedCurrent()) {
				if (!isZeroEps(velocityRel, false)) {
					if (!moving) {
						game.debug.log(6, this+" started moving.");
						moving = true;
					}
					locationRel.add( velocityRel );
					locationAbs.set(absolute(zero));
				} else if (moving) {
					game.debug.log(6, this+" stopped moving.");
					moving = false;
				}
			}
			// 2. rotation
			if (rotating || rotationRelVel.hasChangedCurrent()) {
				if (!rotationRelVel.isZeroEps(true)) {
					if (!rotating) {
						game.debug.log(6, this+" started rotating.");
						rotating = true;
					}
					rotationRel.rotate( rotationRelVel );
				} else if (rotating) {
					game.debug.log(6, this+" stopped rotating.");
					rotating = false;
				}
			}
			// 3. update changes
			velocityRel.update();
			rotationRelVel.update();
			return super.update();
		} else
			return false;
	}
	
	// --- cooked methods to brake ---

	/** applique une force qui s'oppose aux vitesse. perte dans [0,1]. reset selon eps. */
	public void brake(float loss) {
		brakeDepl(loss);
		brakeRot(loss);
	}
	
	/** applique une force qui s'oppose à la vitesse. perte dans [0,1]. reset selon eps. */
	public void brakeDepl(float loss) {
		if (isZeroEps(velocityRel, true))
			return;
		//le frottement, frein. s'oppose Ã  la vitesse :
	    velocityRel.mult(1-loss);
	}
	
	/** applique une force qui s'oppose à la vitesse angulaire. perte dans [0,1]. reset selon eps. */
	public void brakeRot(float loss) {
		if ( !rotationRelVel.isZeroEps(true) )
			rotationRelVel.setAngle(rotationRelVel.angle() * (1 - loss));
	}
}
