package brabra.game.physic;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import brabra.Debug;
import brabra.game.Color;
import brabra.game.Observable.NVector;
import brabra.game.physic.geo.Line;
import brabra.game.physic.geo.Quaternion;
import brabra.game.physic.geo.Vector;
import brabra.game.scene.Movable;
import brabra.game.scene.Object;
import brabra.game.scene.SceneLoader.Attributes;

/** 
 * An movable object obeying to the laws of physics. 
 * It has a mass and a moment of inertia (angular mass). 
 * You can apply forces and impulse (interactions) to it to move it.
 * TODO: If has a parent, apply it to the parent.
 **/
public abstract class Body extends Movable {
	
	public static final float infiniteMass = -1, ghostMass = -2; //TODO change all -1 & -2
	private static final boolean displayInteractions = true; //forces & impulse
	private static final Color interactionColor = new Color("white", true);
	
	/** Mass of the body. -2 for ghost, -1 for infinite or bigger than 0 (never equals 0).*/
	protected float mass = ghostMass;
	/** Inverse of the body mass. 0 for infinite mass or bigger than 0. */
	protected float inverseMass = 0;
	protected Vector inertiaMom = null;
	protected Vector inverseInertiaMom = null;
	protected Color color = Color.basic;
	/** Coefficient of restitution in [0, 1] for stable simulation. */
	protected float restitution = 0.8f;
	protected int life = -1;
	protected int maxLife = -1;
	
	/** Will be added on next update. */
	private NVector forcesLocToAdd = new NVector(), torquesLocToAdd = new NVector();
	/** Executed before updating the body. */
	private Consumer<Body> onUpdate = null;
	private List<Line> interactionsRel = new ArrayList<>();
	
	
	/** create a Body with this location & location and infinite mass. rotation can be null */
	public Body(Vector location, Quaternion rotation) {
		super(location, rotation);
	}
	
	public void copy(Object o) {
		super.copy(o);
		Body ob = this.as(Body.class);
		if (ob != null) {
			setMass(ob.mass);
			onUpdate = ob.onUpdate;
			maxLife = ob.maxLife;
			life = ob.life;
			color = ob.color;
			restitution = ob.restitution;
		}
	}

	/** to react to a collision */
	protected void onCollision(Collider col, Vector impact) {}

	public void validate(Attributes atts) {
		final String color = atts.getValue("color");
		if (color != null)
			setColor( new Color(color, atts.getValue("stroke")) );
		final String mass = atts.getValue("mass");
		if (mass != null)
			setMass(Float.parseFloat(mass));
		final String life = atts.getValue("life");
		if (life != null)
			setLife(life);
		final String impulse = atts.getValue("impulse");
		if (impulse != null)
			applyImpulse(vec(impulse));
		super.validate(atts);
	}
	
	/** applique les forces et update l'etat. return true if this was updated. */
	public void update() {
			// before update
			if (onUpdate != null)
				onUpdate.accept(this);
			
			// apply if needed
			final boolean touched = forcesLocToAdd.hasChangedCurrent() || torquesLocToAdd.hasChangedCurrent();
			if (inverseMass > 0 && touched) {
				// translation
				Vector acceleration = forcesLocToAdd.multBy(inverseMass);
				if (!acceleration.equals(zero))
					velocityRel.add( acceleration );
				// rotation, vitesse angulaire, on prend rotation axis comme L/I
				Vector dL = torquesLocToAdd.multElementsBy(inverseInertiaMom);
//				TODO: if (!dL.equals(zero))
//					rotationRelVel.addAngularMomentum( dL );
				if (!dL.equals(zero))
					//rotationRelVel.addAngularMomentum( dL );
					addAngularMomentum( dL );
			}
			
			// reset
			if (touched) {
				forcesLocToAdd.reset(zero);
				torquesLocToAdd.reset(zero);
			} 
			
			super.update(); //always true
//======= TODO
//				onUpdate.run();
//			//1. translation
//			Vector acceleration = forcesLocToAdd.multBy(inverseMass);
//			if (!acceleration.equals(zero))
//				velocityRel.add( acceleration );
//			//2. rotation, vitesse angulaire, on prend rotation axis comme L/I
//			Vector dL = torquesLocToAdd.multElementsBy(inverseInertiaMom);
//			if (!dL.equals(zero))
//				addAngularMomentum( dL );
//
//			forcesLocToAdd.set(zero);
//			torquesLocToAdd.set(zero);
//>>>>>>> refs/remotes/origin/master
	}
	
	/** Display the interactions... maybe. In local space (should be call after pushLocal()). */
	protected void displayInteractionMaybe() {
		if (displayInteractions) {
			for (Line l : interactionsRel)
				l.display(interactionColor);
			interactionsRel.clear();
		}
	}
	
	public void onDelete() {
		Debug.log(6, this+" deleted.");
		super.onDelete();
	}

	// --- some setters
	
	/** To execute some code before this body is updated (for example to apply some forces). */
	public void addOnUpdate(Consumer<Body> onUpdate) {
		final Consumer<Body> oldAddForce = this.onUpdate;
		this.onUpdate = this.onUpdate == null ?
				onUpdate : b -> { oldAddForce.accept(b); onUpdate.accept(b); };
	}

	/** 
	 * 	Set the body mass. 
	 * 	If mass is -1, the body will have an infinite mass and inertia moment 
	 * 	and won't be affected by collisions (but others will).
	 * 	If mass is 0, the body becomes a ghost (with infinite mass -> same as -1) 
	 * 	and don't react to physic (nor with others).
	 *	Should be overload (and called) to set the inertia moment (depending on the shape). 
	 **/
	public void setMass(float mass) {
		if (mass < -1) {
			Debug.err("invalide mass: "+mass+", taking 0 (ghost)");
			setMass(0);
		} else if (mass == -1) {
			this.mass = -1;
			this.inverseMass = 0;
			this.inertiaMom = Vector.cube(Float.POSITIVE_INFINITY);
			this.inverseInertiaMom = zero.copy();
		} else if (mass == 0) {
			setMass(-1);
			this.mass = -2; //-> ghost
		} else { //mass > 0
			this.mass = mass;
			this.inverseMass = 1/mass;
			// to be set by concrete class
			this.inertiaMom = null;
			this.inverseInertiaMom = null;
		}
		model.notifyChange(Change.Mass);
	}
	
	public void setColor(Color color) {
		this.color = color;
	}
	
	// --- life
	
	/** to react to the death from damages */
	protected void onDeath() {}
	
	public void setLife(int life, int maxLife) {
		if (maxLife <= 0) {
			Debug.err(toString()+" should have a positive maxLife, set to 1 (0 is dead).");
			maxLife = 1;
		}
		if (life < 0) {
			Debug.err(toString()+" should have a positive life, killing him...");
			life = 0;
			onDeath();
		}
			
		this.maxLife = maxLife;
		if (life > maxLife) {
			this.life = maxLife;
			Debug.err(toString()+" can not have more life than maxLife, setting at "+life());
		} else {
			this.life = life;
		}
	}

	private void setLife(String lifeText) {
		final String[] sub = lifeText.split("/");
		if (sub.length == 2)
			setLife(Integer.parseInt(sub[0]),Integer.parseInt(sub[1]));
		else if (sub.length == 1) {
			int life = Integer.parseInt(sub[0]);
			setLife(life, life);
		} else
			Debug.err("unsuported life format: \""+lifeText+"\". letting to "+life());
	}

	public void damage(int damage) {
		if (maxLife < 0)
			Debug.log(4, this+" is a poor non-living object !");
		else if (life == 0)
			Debug.log(5, this+" is already dead !");
		else {
			life -= damage;
			if (life <= 0 ) {
				life = 0;
				onDeath();
			}
		}
	}
	
	public String life() {
		return life+"/"+maxLife;
	}

	// --- Physic management:  impulse, forces and torques ---

	/** If true, the body doesn't interact with others (and others don't). */
	public boolean ghost() {
		return mass < -1;
	}
	
	/** If false, the body doesn't react to the collision but others do. */
	public boolean affectedByCollision() {
		return inverseMass > 0;
	}
	
	/** Return the mass of the body, bigger than 0 (can be Float.POSITIVE_INFINITY). */
	public float mass() {
		return inverseMass > 0 ? mass : Float.POSITIVE_INFINITY;
	}

	/** Add momentum to the body at this point (absolute). */
	public void applyImpulse(Vector posAbs, Vector impulseAbs) {
		assert(!impulseAbs.equals(zero));
		// in local space
		Vector posLoc = local(posAbs);
		Vector impulseLoc = localDir(impulseAbs);
		interactionsRel.add(new Line(relative(posAbs), relative(add(posAbs, impulseAbs.multBy(4))), true));		
		// test if the impulse is against (or on) the mass center -> just translation
		if (posLoc.isZeroEps(false) || posLoc.cross(impulseLoc).isZeroEps(false)) {
			velocityRel.add( impulseLoc.multBy(this.inverseMass) );
		} else {
			Vector posLocN = posLoc.normalized();
			// for translation, just in absolute
			Vector dVel = posLocN.multBy(impulseLoc.dot(posLocN)*this.inverseMass);
			if (!dVel.equals(zero))
				velocityRel.add( dVel );
			// for rotation, we want to stay coherent with inertia moment.
			Vector dL = inverseInertiaMom.multElementsBy(posLoc.cross(impulseLoc));
			if (!dL.equals(zero))
				addAngularMomentum( dL );
		}
	}
	
	/** Add linear momentum to the body (absolute). */
	public void applyImpulse(Vector impulseAbs) {
		applyImpulse(location(), impulseAbs);
	}

	/** Apply a force on the body at this point (absolu). */
	public void applyForce(Vector posAbs, Vector forceAbs) {
		applyForceLoc(local(posAbs), localDir(forceAbs));
	}
	
	/** Apply a force on the body at this point (in this object's space). */
	public void applyForceRel(Vector posRel, Vector forceRel) {
		applyForceLoc(localFromRel(posRel), localDirFromRel(forceRel));
	}

	/** Add a pure translation force on the body at the mass center (from absolute). */
	public void applyForce(Vector forceAbs) {
		applyForceLoc(localDir(forceAbs));
	}

	/** Add a pure translation force on the body at the mass center (from relative). */
	public void applyForceRel(Vector forceRel) {
		applyForceLoc(localDirFromRel(forceRel));
	}

	/** Main methods to add a force. we work in local space. */
	private void applyForceLoc(Vector posLoc, Vector forceLoc) {
		assert(!forceLoc.equals(zero));
		interactionsRel.add(new Line(relativeFromLocal(posLoc), relativeFromLocal(add(posLoc, forceLoc.multBy(4))), true));
		if (posLoc.isZeroEps(false) || posLoc.cross(forceLoc).isZeroEps(false)) {
			// if the force is against (or on) the mass center -> just translation
			applyForceLoc( forceLoc );
		} else {
			Vector posLocN = posLoc.normalized();
			// for translation, just in absolute
			float forceFactor = forceLoc.dot(posLocN);
			if (forceFactor != 0)
				applyForceLoc( posLocN.multBy(forceFactor) );
			// for rotation, we want to stay coherent with inertia moment.
			applyTorqueLoc( posLoc.cross(forceLoc) );
		}
	}

	/** add a pure translation force on the body at the mass center (from parent space). */
	private void applyForceLoc(Vector forceLoc) {
		assert(!forceLoc.equals(zero));
		if (!forceLoc.equals(zero))
			forcesLocToAdd.add(forceLoc);
	}

	/** add a torque (rotational force) on the body (from parent space). */
	private void applyTorqueLoc(Vector torqueLoc) {
		assert(!torqueLoc.equals(zero));
		if (!torqueLoc.equals(zero))
			torquesLocToAdd.add(torqueLoc);
	}
	
	private void addAngularMomentum(Vector dL) {
		assert (!dL.equals(zero));
		if (rotationRelVel.isIdentity())
			rotationRelVel.set(dL, dL.mag());
		else {
			final Vector newRotAxis = rotationRelVel.rotAxisAngle().plus(dL);
			rotationRelVel.set(newRotAxis, newRotAxis.mag());
		}
	}
	
	// --- cooked methods to apply forces
	
	/** apply his weight to the object. */
	public void pese() {
		if (mass == -1)
			//throw new IllegalArgumentException("un objet de mass infini ne devrait pas peser !"); now tolerated :)
			return;
		applyForce( app.para.gravity().multBy(mass) );
	}
	
	public void avance(float forceFront) {
		assert(forceFront != 0);
		applyForceRel( front(forceFront) );
	}
}