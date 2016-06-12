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
	
	private static final boolean displayInteractions = true; //forces & impulse
	private static final Color interactionColor = new Color("white", true);
	
	/** Mass of the body. -2 for ghost, -1 for infinite or bigger than 0 (never equals 0).*/
	protected float mass = -2;
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
		super.validate(atts);
		final String color = atts.getValue("color");
		if (color != null)
			setColor( new Color(color, atts.getValue("stroke")) );
		final String mass = atts.getValue("mass");
		if (mass != null)
			setMass(Float.parseFloat(mass));
		final String life = atts.getValue("life");
		if (life != null)
			setLife(life);
	}
	
	/** applique les forces et update l'etat. return true if this was updated. */
	public void update() {
			// before update: add forces & torques officially
			if (onUpdate != null)
				onUpdate.accept(this);
			
			// apply if needed
			if (inverseMass > 0) {
				// translation
				if (forcesLocToAdd.hasChangedCurrent()) {
					final Vector acceleration = forcesLocToAdd.multBy(inverseMass);
					if (!acceleration.equals(zero))
						velocityRel.add( acceleration );
				}
				// rotation, vitesse angulaire, on prend rotation axis comme L/I
				if (torquesLocToAdd.hasChangedCurrent()) {
					final Vector dL = torquesLocToAdd.multElementsBy(inverseInertiaMom);
					if (!dL.equals(zero))
						addAngularMomentum( dL );
				}
			}
			
			// reset
			if (forcesLocToAdd.hasChangedCurrent())
				forcesLocToAdd.reset(zero);
			if (torquesLocToAdd.hasChangedCurrent())
				torquesLocToAdd.reset(zero);
			
			// apply the movement in Movable
			super.update();
	}
	
	/** Display the interactions... maybe. In local space (should be call after pushLocal()). */
	protected void displayInteractionMaybe() {
		if (displayInteractions && interactionsRel.size()!=0) {
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
	public boolean affectedByPhysic() {
		return inverseMass > 0;
	}
	
	/** Return the mass of the body, bigger than 0 (can be Float.POSITIVE_INFINITY). */
	public float mass() {
		return inverseMass > 0 ? mass : Float.POSITIVE_INFINITY;
	}

	/** Apply a force on the body at this point (absolute). */
	public void applyForce(Vector posAbs, Vector forceAbs) {
		applyForceRel(relative(posAbs), relativeDir(forceAbs));
	}
	
	/** Apply a force on the body at this point (in this object's space). */
	public void applyForceRel(Vector posRel, Vector forceRel) {
		assert(!forceRel.equals(zero));
		final Vector forceLoc = localDirFromRel(forceRel);
		interactionsRel.add(new Line(posRel, add(posRel, forceRel.multBy(4)), true));
		if (posRel.isZeroEps(false) || posRel.cross(forceRel).isZeroEps(false)) {
			// if the force is against (or on) the mass center -> just translation
			forcesLocToAdd.add( forceLoc );
		} else {
			final Vector posLocN = localFromRel(posRel).normalized();
			// for translation, just in absolute
			final float forceFactor = forceLoc.dot(posLocN);
			if (forceFactor != 0)
				forcesLocToAdd.add( posLocN.multBy(forceFactor) );
			// for rotation, we want to stay coherent with inertia moment.
			torquesLocToAdd.add( posRel.cross(forceRel) );
		}
	}

	/** Add momentum to the body at this point (absolute). */
	public void applyImpulse(Vector posAbs, Vector impulseAbs) {
		assert(!impulseAbs.equals(zero));
		final Vector posRel = relative(posAbs);
		final Vector impulseRel = relativeDir(impulseAbs);
		final Vector impulseLoc = localDirFromRel(impulseRel);
		// in local space
		interactionsRel.add(new Line(relative(posRel), relative(add(posRel, impulseRel.multBy(4))), true));		
		// test if the impulse is against (or on) the mass center -> just translation
		if (posRel.isZeroEps(false) || posRel.cross(impulseRel).isZeroEps(false)) {
			velocityRel.add( impulseLoc.multBy(this.inverseMass) );
		} else {
			final Vector posLocN = localFromRel(posRel).normalized();
			// for translation, just in absolute
			final Vector dVel = posLocN.multBy( impulseLoc.dot(posLocN)*this.inverseMass );
			if (!dVel.equals(zero))
				velocityRel.add( dVel );
			// for rotation, we want to stay coherent with inertia moment.
			final Vector dL = inverseInertiaMom.multElementsBy(posRel.cross(impulseRel));
			if (!dL.equals(zero))
				addAngularMomentum( dL );
		}
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
		if (inverseMass > 0)
			applyForce( location(), app.para.gravity().multBy(mass) );
	}
	
	public void avance(float forceFront) {
		assert(forceFront != 0);
		applyForceRel( zero, front(forceFront) );
	}
}