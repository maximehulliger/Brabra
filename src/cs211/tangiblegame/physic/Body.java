package cs211.tangiblegame.physic;

import cs211.tangiblegame.Color;
import cs211.tangiblegame.geo.Quaternion;
import processing.core.*;

/** 
 * An object obeying to the laws of physics. 
 * It has a mass and a moment of inertia (angular mass). 
 * You can apply forces and impulse to it to
 * move it through velocity and rotation velocity (both relative for the moment).
 * TODO: If has a parent, apply it to the parent.
 **/
public class Body extends Object {
	private static final boolean drawInteraction = true; //forces et impulse

	protected int life = -1;
	protected int maxLife = -1;
	protected Color color = Color.basic;
	/** Mass of the body. -1 for infinite or bigger than 0 (never equals 0).*/
	protected float mass = -1;
	protected float inverseMass = 0;
	protected PVector inertiaMom;
	protected PVector inverseInertiaMom;
	protected float restitution = 0.8f; // [0, 1]
	/** If set to true, the body doesn't interact with others (and others don't). */
	protected boolean ghost = false;
	/** If set to false, the body doesn't react to the collision but others do. */
	protected boolean affectedByCollision = true;

	private PVector forces = zero.copy();
	private PVector torques = zero.copy();
	private Runnable addForces = null;
	
	/** create a Body with this location & location and infinite mass. rotation can be null */
	public Body(PVector location, Quaternion rotation) {
		super(location, rotation);
	}

	/** to add force to the body every frame */
	protected void addForces() {}

	/** to react to a collision */
	protected void onCollision(Collider col, PVector impact) {}

	/** applique les forces et update l'etat. return true if this was updated. */
	public boolean update() {
		if (!updated) {
			addForces();
			if (addForces != null)
				addForces.run();
			
			//1. translation
			PVector acceleration = PVector.mult( forces, inverseMass );
			if (!acceleration.equals(zero)) {
				velocityRel.add( acceleration );
			}
			
			//2. rotation, vitesse angulaire, on prend rotation axis comme L/I
			PVector dL = multMatrix( torques, inverseInertiaMom );
			if (!dL.equals(zero)) {
				rotationRelVel.addAngularMomentum( dL );
			}
			
			forces.set(zero);
			torques.set(zero);
			boolean pu = super.update();
			assert(pu);
			return true;
		} else
			return false;
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
		if (mass == -1) {
			this.mass = -1;
			this.affectedByCollision = false;
			this.inverseMass = 0;
			this.inverseInertiaMom = zero.copy();
		} else if (mass == 0) {
			setMass(-1);
			ghost = true;
		} else if (mass < 0)
			throw new IllegalArgumentException("negative mass !");
		else {
			this.mass = mass;
			this.inverseMass = 1/mass;
			this.affectedByCollision = true;
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
			System.err.println(toString()+" should have positive maxLife");
			maxLife = 1;
		}
		this.maxLife = maxLife;
		if (life > maxLife) {
			this.life = maxLife;
			System.err.println(toString()+" can not have more life than maxLife, setting at "+life());
		} else {
			this.life = life;
		}
	}

	public void damage(int damage) {
		if (maxLife < 0)
			game.debug.log(4, this+" is a poor non-living object !");
		else if (life < 0)
			game.debug.log(5, this+" is already dead !");
		else {
			life -= damage;
			if (life < 0 )
				onDeath();
		}
	}
	
	public String life() {
		return life+"/"+maxLife;
	}

	//------ Gestion des impulse, forces et torques
	
	/** Add momentum to the body at this point (absolu). */
	public void applyImpulse(PVector absPos, PVector impulse) {
		assert(!impulse.equals(zero));
		if (drawInteraction) {
			app.stroke(255);
			line(absPos, add(absPos, mult(impulse, 100)));
		}
		
		PVector rel = PVector.sub(absPos, locationAbs);
		if (isZeroEps(rel.cross(impulse), false)) {
			// if the impulse is against (or on) the mass center -> just translation
			velocityRel.add( mult(impulse, this.inverseMass) );
		} else {
			// for translation, just in absolute
			PVector relN = rel.normalize(new PVector());
			PVector dVel = mult(relN, impulse.dot(relN)*this.inverseMass);
			if (!dVel.equals(zero))
				velocityRel.add( dVel );
			// for rotation, we want to stay coherent with inertia moment. (before static now full)
			rel = local(absPos);
			PVector relImpulse = sub(local( add(impulse, absPos) ), rel);
			PVector dL = multMatrix(inverseInertiaMom, rel.cross(relImpulse));
			if (!dL.equals(zero))
				rotationRelVel.addAngularMomentum( dL );
		}
	}
	
	/** Add linear momentum to the body (absolu). */
	public void applyImpulse(PVector impulse) {
		applyImpulse(location(), impulse);
	}
	
	/** Apply a force on the body at this point (absolu). */
	public void addForce(PVector absPos, PVector force) {
		assert(!force.equals(zero));
		if (drawInteraction) {
			app.stroke(255);
			line(absPos, add(absPos, mult(force, 0.2f)));
		}
			
		PVector rel = PVector.sub(absPos, location());
		if (isZeroEps(rel.cross(force), false)) {
			// if the force is against (or on) the mass center -> just translation
			addForce( force );
		} else {
			// for translation, just in absolute
			PVector relN = rel.normalize(new PVector());
			addForce( mult(relN, force.dot(relN)) );
			// for rotation, we want to stay coherent with inertia moment.
			rel = local(absPos);
			PVector relForce = sub(local( add(force, absPos) ), rel);
			addTorque( rel.cross(relForce) );
		}
	}

	/** Apply a force on the body at the mass center (absolu). */
	public void addForce(PVector force) {	
		if (!force.equals(zero))
			forces.add(force);
	}

	/** Apply a torque (rotational force) on the body (absolu). */
	public void addTorque(PVector torque) {	
		if (!torque.equals(zero))
			torques.add(torque);
	}
	
	// --- cooked methods to apply forces
	
	/** apply his weight to the object. */
	public void pese() {
		if (mass == -1)
			//throw new IllegalArgumentException("un objet de mass infini ne devrait pas peser !"); now tolerated :)
			return;
		addForce( up(-game.physic.gravity*mass) );
	}
	
	public void avance(float force) {
		//addForce( absolute(front(150)), absDir(front(force)) );
		addForce( absDir(front(force)) );
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