package brabra.game.scene;

import brabra.game.Observable.NQuaternion;
import brabra.game.Observable.NVector;
import brabra.game.physic.geo.Quaternion;
import brabra.game.physic.geo.Vector;
import brabra.game.physic.geo.Transform.ParentRelationship;
import brabra.game.scene.SceneLoader.Attributes;

/**
 * A movable Object. 
 * you can move it through velocity & rotation velocity (both relative to the parent)
 **/
public class Movable extends Object {

	/** Velocity relative to the parent. */
	public final NVector velocityRel = new NVector();
	/** Rotation velocity relative to the parent. */
	public final NQuaternion rotationRelVel = new NQuaternion();

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
		// flags already set ?
		if (moving || (movableParent != null && movableParent.isMoving()))
			return true;
		else 
			return movableParent != null && (movableParent.isRotating() && !transform.location().minus(movableParent.transform.location()).isZeroEps(false));
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

	public Vector velocity() {
		Vector forMeAbs = isMoving() ? transform.absoluteDirFromLocal(velocityRel) : zero;
		return hasParent() ? add(forMeAbs , parent().velocityAtRel(transform.location())) : forMeAbs;
	}

	/** Return the absolute velocity (from an absolute pos). */
	public Vector velocityAt(Vector posAbs) {
		return velocityAtRel(transform.relative(posAbs));
	}

	/** Return the absolute velocity (from a relative pos). */
	public Vector velocityAtRel(Vector posRel) {
		final Vector fromTransAbs = hasParent() ? parent().velocityAtRel(posRel).plus(velocityRel) : velocityRel;
		return rotationRelVel.isIdentity() ? fromTransAbs : fromTransAbs.plus(rotationRelVel.rotAxisAngle().cross(posRel));
	}

	// --- Setters ---

	public boolean setParent(Object newParent, ParentRelationship parentRel) {
		if (super.setParent(newParent, parentRel)) {
			movableParent = hasParent() ? (Movable) parentThat(p -> p instanceof Movable) : null;
			return true;
		} else
			return false;
	}

	// --- life cycle ---

	public void validate(Attributes atts) {
		super.validate(atts);
		
		final String velocity = atts.getValue("velocity");
		if (velocity != null)
			velocityRel.set(vec(velocity));
		//final String rotVelocity = atts.getValue("rot_velocity");
		//if (velocity != null)
		//	setRotationVelRel(vec(rotVelocity));
	}

	protected void update() {
		// 1. movement
		if (moving || velocityRel.hasChangedCurrent()) {
			if (!velocityRel.isZeroEps(false)) {
				if (!moving) {
					game.debug.log(6, this+" started moving.");
					moving = true;
				}
				transform.move(velocityRel);
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
				transform.rotate(rotationRelVel);
			} else if (rotating) {
				game.debug.log(6, this+" stopped rotating.");
				rotating = false;
			}
			rotationRelVel.update();
			model.notifyChange(Change.RotVelocity);
		}
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
			float angle = rotationRelVel.angle();
			assert(angle != 0);
			rotationRelVel.setAngle(angle * (1 - loss));
			assert(angle != rotationRelVel.angle());
		}
	}
}
