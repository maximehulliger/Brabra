package brabra.game.scene;

import brabra.game.Observable.NQuaternion;
import brabra.game.Observable.NVector;
import brabra.game.XMLLoader.Attributes;
import brabra.game.physic.geo.Quaternion;
import brabra.game.physic.geo.Vector;

/**
 * A movable Object. 
 * you can move it through velocity & rotation velocity (both relative to the parent)
 **/
public class Movable extends Object {

	/** Velocity relative to the parent. */
	protected final NVector velocityRel = new NVector(zero);
	/** Rotation velocity relative to the parent. */
	protected final NQuaternion rotationRelVel = new NQuaternion(identity);
	
	/** The first parent that is a movable or null if nothing moves before this. */
	private Movable movableParent = null;
	/** Indicates if the object was moving last frame. */
	private boolean moving = false, rotating = false;
	

	public Movable(Vector location, Quaternion rotation) {
		super(location, rotation);
	}
	
	public void copy(Object o) {
		super.copy(o);
		Movable om = this.as(Movable.class);
		if (om != null) {
			velocityRel.set(om.velocityRel);
			rotationRelVel.set(om.rotationRelVel);
			movableParent = om.movableParent;
			moving = om.moving;
			rotating = om.rotating;
		}
	}
	
	// --- Getters ---
	
	/** Return true if the object has moved during last frame (by velocity or parent's rotational velocity). */
	public boolean isMoving() {
		return moving || (movableParent != null && 
				(movableParent.isMoving() || (!locationRel.isZeroEps(false) && movableParent.isRotating())));
	}
	
	/** Return true if the object has rotated during last frame (by this or parent's rotational velocity). */
	public boolean isRotating() {
		return rotating || (movableParent != null && movableParent.isRotating());
	}

	/** Return the velocity relative to the parent at the center of mass. */
	public Vector velocityRel() {
		return velocityRel;
	}

	/** Return the rotational velocity relative to the parent. */
	public Quaternion rotationRelVel() {
		return rotationRelVel;
	}
	
	/** Return the absolute velocity at the center of mass. */
	public Vector velocity() {
		if (!isMoving())
			return zero;
		else {
			Vector forMe = absoluteDirFromLocal(velocityRel);
			return hasParent() ? add(forMe , parent().velocityAtRel(locationRel)) : forMe;
		}
	}

	/** Return the absolute velocity (from an absolute pos). */
	public Vector velocityAt(Vector posAbs) {
		return velocityAtRel(relative(posAbs));
	}

	/** Return the absolute velocity (from a relative pos). */
	public Vector velocityAtRel(Vector posRel) {
		return (rotationRel.isZeroEps(false) || posRel.isZeroEps(false))
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

	// --- Setters ---

	public void move(Vector deplAbs) {
		locationRel.add(localDir(deplAbs));
	}
	
	public void rotate(Quaternion rotAbs) {
		rotationRel.rotate(rotAbs);
	}
	
	/** Set the velocity of this object relative to his parent. */
	public void setVelocityRel(Vector velocityRel) {
		this.velocityRel.set(velocityRel);
	}

	/** Set the velocity of this object relative to his parent. */
	public void setRotationVelRel(Vector rotationVelRel) {
		this.rotationRelVel.set(rotationVelRel);
	}
	
	public boolean setParent(Object newParent) {
		if (super.setParent(newParent)) {
			movableParent = (Movable) parentThat(p -> p instanceof Movable);
			return true;
		} else
			return false;
	}

	// --- life cycle ---

	public boolean validate(Attributes atts) {
		if (super.validate(atts)) {
			final String velocity = atts.getValue("velocity");
			if (velocity != null)
				setRotationVelRel(vec(velocity));
			final String rotVelocity = atts.getValue("rot_velocity");
			if (velocity != null)
				setRotationVelRel(vec(rotVelocity));
			return true;
		} else
			return false;
	}
	
	protected boolean update() {
		if (!updated) {
			// 1. movement
			if (moving || velocityRel.hasChangedCurrent()) {
				if (!velocityRel.isZeroEps(false)) {
					if (!moving) {
						game.debug.log(6, this+" started moving.");
						moving = true;
					}
					move(velocityRel);
				} else if (moving) {
					game.debug.log(6, this+" stopped moving.");
					moving = false;
				}
				velocityRel.update();
				model.notifyChange(Change.Velocity);
			}
			// 2. rotation
			if (rotating || rotationRelVel.hasChangedCurrent()) {
				if (!rotationRelVel.isZeroEps(true)) {
					if (!rotating) {
						game.debug.log(6, this+" started rotating.");
						rotating = true;
					}
					rotate(rotationRelVel);
				} else if (rotating) {
					game.debug.log(6, this+" stopped rotating.");
					rotating = false;
				}
				rotationRelVel.update();
				model.notifyChange(Change.RotVelocity);
			}
			return super.update();
		} else
			return false;
	}
	
	// --- cooked methods to brake ---

	/** Force the object to lose some velocity and rotational velocity. loss in [0,1]. reset after eps. */
	public void brake(float loss) {
		brakeDepl(loss);
		brakeRot(loss);
	}
	
	/** Force the object to lose some velocity. loss in [0,1]. reset after eps. */
	public void brakeDepl(float loss) {
		if (isMoving()) {
			velocityRel.mult(1-loss);
			velocityRel.isZeroEps(true);
		}
	}
	
	/** Force the object to lose some rotational velocity. reset after eps. */
	public void brakeRot(float loss) {
		if (isRotating()) {
			rotationRelVel.setAngle(rotationRelVel.angle() * (1 - loss));
			rotationRelVel.isZeroEps(true);
		}
	}
}
