package cs211.tangiblegame.physic;

import cs211.tangiblegame.Color;
import cs211.tangiblegame.geo.Quaternion;
import processing.core.*;

/** 
 * An object obeying to the laws of physics. 
 * It has a mass and a moment of inertia (angular mass). 
 * You can apply forces and impulse
 **/
public class Body extends Object {
	private static final boolean drawInteraction = true; //forces et impulse

	protected int life = -1;
	protected int maxLife = -1;
	protected Color color = Color.basic;
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
	
	/** create a Body with this location & location. location can be null */
	public Body(PVector location, Quaternion rotation) {
		super(location, rotation);
	}

	/** to add force to the body every frame */
	protected void addForces() {}

	/** to react to a collision */
	protected void onCollision(Collider col, PVector impact) {}

	/** applique les forces et update l'etat */
	public void update() {
		addForces();
		if (addForces != null)
			addForces.run();
		
		//1. translation
		if (!forces.equals(zero)) {
			PVector acceleration = PVector.mult( forces, inverseMass );
			velocityRel.add( acceleration );
		}
		
		//2. rotation, vitesse angulaire, on prend rotation axis comme L/I
		PVector dL = multMatrix( inverseInertiaMom, torques );
		if (!dL.equals(zero)) {
			rotationRelVel.addAngularMomentum( dL );
		}
		
		forces = zero.copy();
		torques = zero.copy();
		super.update();
	}

	// --- some setters
	
	public void addApplyForces(Runnable addForce) {
		final Runnable oldAddForce = this.addForces;
		this.addForces = this.addForces == null ?
				addForce : () -> { oldAddForce.run(); addForce.run(); };
	}

	/** set la masse du body. si -1, l'objet aura une mass et un moment d'inertie infini.
		à surcharger (et appeler) pour set le moment d'inertie. */
	public void setMass(float mass) {
		if (mass == -1) {
			this.mass = -1;
			this.affectedByCollision = false;
			this.inverseMass = 0;
			this.inverseInertiaMom = zero.copy();
		} else if (mass <= 0)
			throw new IllegalArgumentException("mass négative ou nulle !");
		else {
			this.mass = mass;
			this.inverseMass = 1/this.mass;
			this.affectedByCollision = true;
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
	
	/** ajoute de la quantitÃ© de mouvement au body Ã  point (absolu). */
	public void applyImpulse(PVector absPos, PVector impulse) {
		if (drawInteraction) {
			PVector to = PVector.add(absPos, PVector.mult(impulse, 100));
			app.stroke(255);
			app.line(absPos.x, absPos.y, absPos.z, to.x, to.y, to.z);
		}
		if (equalsEps(absPos, locationAbs, false)) {
			velocityRel.add( PVector.mult(impulse, this.inverseMass) );
			return;
		} else {
			//pour le deplacement, seulement en absolu
			PVector toPos = PVector.sub(absPos, locationAbs);
			toPos.normalize();
			PVector forAbs = PVector.mult(toPos, impulse.dot(toPos));
			velocityRel.add( PVector.mult(forAbs, this.inverseMass) );
			
			//TODO test impulse contre l'objet
			//pour la rotation, avec la rotation (pour Ãªtre cohÃ©rant avec le moment d'inertie.)
			PVector relPoint = local(absPos);
			PVector relImpulse = sub(local( add(impulse, absPos) ), relPoint);
			PVector forRot = relPoint.cross(relImpulse);
			if (!forRot.equals(zero))
				rotationRelVel.addAngularMomentum( multMatrix(inverseInertiaMom, forRot) );
		}
	}
	
	/** ajoute de la quantité de mouvement au centre de masse (absolu). */
	public void applyImpulse(PVector impulse) {
		applyImpulse(locationAbs, impulse);
	}
	
	/** permet d'appliquer une force au body à ce point (absolu). force should be mult my physic.deltaTime. */
	public void addForce(PVector absPos, PVector force) {
		if (drawInteraction) {
			PVector to = PVector.add(absPos, PVector.mult(force, 0.2f));
			app.stroke(255);
			app.line(absPos.x, absPos.y, absPos.z, to.x, to.y, to.z);
		}
			
		PVector rel = PVector.sub(absPos, locationAbs);
		torques.add( rel.cross(force) );
		
		rel.normalize();
		PVector forceAbs = PVector.mult(rel, force.dot(rel));
		addForce(forceAbs);
	}
	
	/** applique une force absolue au centre de masse du body. force should be mult my physic.deltaTime. */
	public void addForce(PVector force) {	
		forces.add(force);
	}
	
	// --- cooked methods to apply forces
	
	/** apply his weight to the object. */
	public void pese() {
		if (mass == -1)
			throw new IllegalArgumentException("un objet de mass infini ne devrait pas peser !");
		PVector poids = new PVector(0, -game.physic.gravity*mass, 0);
		addForce(poids);
	}
	
	public void avance(float force) {
		addForce( absolute( PVector.mult(front, 150) ) , absFront(force) );
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