package brabra.game.physic;

import java.util.ArrayList;
import java.util.List;

import brabra.game.Color;
import brabra.game.XMLLoader.Attributes;
import brabra.game.physic.geo.Line;
import brabra.game.physic.geo.Quaternion;
import brabra.game.scene.Object;
import processing.core.*;

/** 
 * An object obeying to the laws of physics. 
 * It has a mass and a moment of inertia (angular mass). 
 * You can apply forces and impulse (interactions) to it to
 * move it through velocity and rotation velocity (both relative to the parent).
 * TODO: If has a parent, apply it to the parent.
 **/
public class Body extends Object {
	
	private static final boolean displayInteractions = true; //forces et impulse
	private static final Color interactionColor = new Color("white", true);
	
	/** Mass of the body. -2 for ghost, -1 for infinite or bigger than 0 (never equals 0).*/
	protected float mass = -2;
	protected int life = -1;
	protected int maxLife = -1;
	protected Color color = Color.basic;
	protected float inverseMass = 0;
	protected PVector inertiaMom = cube(Float.POSITIVE_INFINITY);
	protected PVector inverseInertiaMom = zero.copy();
	protected float restitution = 0.8f; // [0, 1]

	private PVector forces = zero.copy();
	private PVector torques = zero.copy();
	private Runnable addForces = null;
	private List<Line> interactionsRel = new ArrayList<>();
	
	
	/** create a Body with this location & location and infinite mass. rotation can be null */
	public Body(PVector location, Quaternion rotation) {
		super(location, rotation);
	}

	/** to add force to the body every frame */
	protected void addForces() {}

	/** to react to a collision */
	protected void onCollision(Collider col, PVector impact) {}

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
		final String impulse = atts.getValue("impulse");
		if (impulse != null)
			applyImpulse(vec(impulse));
	}
	
	/** applique les forces et update l'etat. return true if this was updated. */
	public boolean update() {
		if (!updated) {
			addForces();
			if (addForces != null)
				addForces.run();
			//1. translation
			PVector acceleration = PVector.mult( forces, inverseMass );
			if (!acceleration.equals(zero))
				velocityRel.add( acceleration );
			//2. rotation, vitesse angulaire, on prend rotation axis comme L/I
			PVector dL = multMatrix( torques, inverseInertiaMom );
			if (!dL.equals(zero))
				rotationRelVel.addAngularMomentum( dL );
			forces.set(zero);
			torques.set(zero);
			boolean pu = super.update();
			assert(pu);
			return true;
		} else
			return false;
	}
	
	/** Display the interactions... maybe. In local space (should be call after pushLocal()). */
	protected void displayInteractionMaybe() {
		if (displayInteractions) {
			for (Line l : interactionsRel) {
				l.display(interactionColor);
			}
			interactionsRel.clear();
		}
	}
	
	public void onDelete() {
		game.debug.log(6, this+" deleted.");
		super.onDelete();
	}

	// --- some setters
	
	public void addApplyForces(Runnable addForce) {
		final Runnable oldAddForce = this.addForces;
		this.addForces = this.addForces == null ?
				addForce : () -> { oldAddForce.run(); addForce.run(); };
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
		if (mass == -2 || mass == 0) {
			this.mass = -2;
			this.inverseMass = 0;
			this.inverseInertiaMom = zero.copy();
		} else if (mass == -1) {
			this.mass = -1;
			this.inverseMass = 0;
			this.inverseInertiaMom = zero.copy();
		} else if (mass <= 0) {
			game.debug.err("invalide mass: "+mass+", taking -2 (ghost)");
			setMass(-2);
		} else {
			this.mass = mass;
			this.inverseMass = 1/mass;
			this.inverseInertiaMom = zero.copy();
		}
	}
	
	public void setColor(Color color) {
		this.color = color;
	}
	
	// --- life
	
	/** to react to the death from damages */
	protected void onDeath() {}
	
	public void setLife(int life, int maxLife) {
		if (maxLife <= 0) {
			game.debug.err(toString()+" should have a positive maxLife, set to 1 (0 is dead).");
			maxLife = 1;
		}
		if (life < 0) {
			game.debug.err(toString()+" should have a positive life, killing him...");
			life = 0;
			onDeath();
		}
			
		this.maxLife = maxLife;
		if (life > maxLife) {
			this.life = maxLife;
			game.debug.err(toString()+" can not have more life than maxLife, setting at "+life());
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
			game.debug.err("unsuported life format: \""+lifeText+"\". letting to "+life());
	}

	public void damage(int damage) {
		if (maxLife < 0)
			game.debug.log(4, this+" is a poor non-living object !");
		else if (life == 0)
			game.debug.log(5, this+" is already dead !");
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
		return mass > 0;
	}

	/** Add momentum to the body at this point (absolute). */
	public void applyImpulse(PVector posAbs, PVector impulseAbs) {
		assert(!impulseAbs.equals(zero));
		// in local space
		PVector posLoc = local(posAbs);
		PVector impulseLoc = localDir(impulseAbs);
		interactionsRel.add(new Line(relative(posAbs), relative(add(posAbs, mult(impulseAbs, 4))), true));
				
		if (isZeroEps(posLoc, false) || isZeroEps(posLoc.cross(impulseLoc), false)) {
			// if the impulse is against (or on) the mass center -> just translation
			velocityRel.add( mult(impulseLoc, this.inverseMass) );
		} else {
			PVector posLocN = posLoc.normalize(new PVector());
			// for translation, just in absolute
			PVector dVel = mult(posLocN, impulseLoc.dot(posLocN)*this.inverseMass);
			if (!dVel.equals(zero))
				velocityRel.add( dVel );
			// for rotation, we want to stay coherent with inertia moment.
			PVector dL = multMatrix(inverseInertiaMom, posLoc.cross(impulseLoc));
			if (!dL.equals(zero))
				rotationRelVel.addAngularMomentum( dL );
		}
	}
	
	/** Add linear momentum to the body (absolute). */
	public void applyImpulse(PVector impulseAbs) {
		applyImpulse(location(), impulseAbs);
	}

	/** Apply a force on the body at this point (absolu). */
	public void applyForce(PVector posAbs, PVector forceAbs) {
		applyForceLoc(local(posAbs), localDir(forceAbs));
	}
	
	/** Apply a force on the body at this point (in this object's space). */
	public void applyForceRel(PVector posRel, PVector forceRel) {
		applyForceLoc(localFromRel(posRel), localDirFromRel(forceRel));
	}

	/** Add a pure translation force on the body at the mass center (from absolute). */
	public void applyForce(PVector forceAbs) {
		applyForceLoc(localDir(forceAbs));
	}

	/** Add a pure translation force on the body at the mass center (from relative). */
	public void applyForceRel(PVector forceRel) {
		applyForceLoc(localDirFromRel(forceRel));
	}

	/** Main methods to add a force. we work in local space. */
	private void applyForceLoc(PVector posLoc, PVector forceLoc) {
		assert(!forceLoc.equals(zero));
		interactionsRel.add(new Line(relativeFromLocal(posLoc), relativeFromLocal(add(posLoc, mult(forceLoc, 4))), true));
		if (isZeroEps(posLoc, false) || isZeroEps(posLoc.cross(forceLoc), false)) {
			// if the force is against (or on) the mass center -> just translation
			applyForceLoc( forceLoc );
		} else {
			PVector posLocN = posLoc.normalize(new PVector());
			// for translation, just in absolute
			float forceFactor = forceLoc.dot(posLocN);
			if (forceFactor != 0)
				applyForceLoc( mult(posLocN, forceFactor) );
			// for rotation, we want to stay coherent with inertia moment.
			applyTorqueLoc( posLoc.cross(forceLoc) );
		}
	}

	/** add a pure translation force on the body at the mass center (from parent space). */
	private void applyForceLoc(PVector forceLoc) {
		assert(!forceLoc.equals(zero));
		if (!forceLoc.equals(zero))
			forces.add(forceLoc);
	}

	/** add a torque (rotational force) on the body (from parent space). */
	private void applyTorqueLoc(PVector torqueLoc) {
		assert(!torqueLoc.equals(zero));
		if (!torqueLoc.equals(zero))
			torques.add(torqueLoc);
	}
	
	// --- cooked methods to apply forces
	
	/** apply his weight to the object. */
	public void pese() {
		if (mass == -1)
			//throw new IllegalArgumentException("un objet de mass infini ne devrait pas peser !"); now tolerated :)
			return;
		applyForce( up(-game.physic.gravity*mass) );
	}
	
	public void avance(float forceFront) {
		assert(forceFront != 0);
		applyForceRel( front(forceFront) );
	}
	
	/** applique une force qui s'oppose aux vitesse. perte dans [0,1]. reset selon eps. */
	public void freine(float perte) {
		freineDepl(perte);
		freineRot(perte);
	}
	
	/** applique une force qui s'oppose à la vitesse. perte dans [0,1]. reset selon eps. */
	public void freineDepl(float perte) {
		if (isZeroEps(velocityRel, true))
			return;
		//le frottement, frein. s'oppose Ã  la vitesse :
	    velocityRel.mult(1-perte);
	}
	
	/** applique une force qui s'oppose à la vitesse angulaire. perte dans [0,1]. reset selon eps. */
	public void freineRot(float perte) {
		if ( !rotationRelVel.isZeroEps(true) )
			rotationRelVel.setAngle(rotationRelVel.angle() * (1 - perte));
	}
}