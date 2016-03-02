package cs211.tangiblegame.physic;

import cs211.tangiblegame.Color;
import cs211.tangiblegame.TangibleGame;
import cs211.tangiblegame.geo.Quaternion;
import processing.core.*;

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
	protected boolean affectedByCollision = true;

	private PVector forces = zero.copy();
	private PVector torques = zero.copy();
	private Runnable addForces = () -> {};
	
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
		addForces.run();
		
		//1. translation
		if (!forces.equals(zero)) {
			PVector acceleration = PVector.mult( forces, inverseMass );
			velocity.add(mult(acceleration,game.physic.deltaTime));
		}
		
		//2. rotation, vitesse angulaire, on prend rotation axis comme L/I
		PVector dL = multMatrix( inverseInertiaMom, torques );
		if (!dL.equals(zero)) {
			rotationVel.addAngularMomentum( dL );
		}
		
		forces = zero.copy();
		torques = zero.copy();
		super.update();
	}

	// --- some setters
	
	public void addApplyForces(Runnable addForce) {
		final Runnable r = this.addForces;
		this.addForces = () -> {
			r.run();
			addForce.run();
		};
	}

	/** set la masse du body. si -1, l'objet aura une mass et un moment d'inertie infini.
		� surcharger (et appeler) pour set le moment d'inertie. */
	public void setMass(float mass) {
		if (mass == -1) {
			this.mass = -1;
			this.affectedByCollision = false;
			this.inverseMass = 0;
			this.inverseInertiaMom = zero.copy();
		} else if (mass <= 0)
			throw new IllegalArgumentException("mass n�gative ou nulle !");
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

	public void setLife(String lifeText) {
		String[] sub = lifeText.split("/");
		if (sub.length == 2)
			setLife(Integer.parseInt(sub[0]),Integer.parseInt(sub[1]));
		else if (sub.length == 1) {
			int life = Integer.parseInt(sub[0]);
			setLife(life, life);
		} else
			System.err.println("unsuported life format: \""+lifeText+"\"");
	}
	
	public void damage(int damage) {
		if (maxLife < 0)
			if (TangibleGame.verbosity >= 3)
				System.err.println(this+" is a poor non-living object !");
		else if (life < 0)
			if (TangibleGame.verbosity >= 4)
				System.out.println(this+" is already dead !");
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
	
	/** ajoute de la quantité de mouvement au body à point (absolu). */
	public void applyImpulse(PVector absPos, PVector impulse) {
		if (drawInteraction) {
			PVector to = PVector.add(absPos, PVector.mult(impulse, 100));
			app.stroke(255);
			app.line(absPos.x, absPos.y, absPos.z, to.x, to.y, to.z);
		}
		if (equalsEps(absPos, location, false)) {
			velocity.add( PVector.mult(impulse, this.inverseMass) );
			return;
		} else {
			//pour le deplacement, seulement en absolu
			PVector toPos = PVector.sub(absPos, location);
			toPos.normalize();
			PVector forAbs = PVector.mult(toPos, impulse.dot(toPos));
			velocity.add( PVector.mult(forAbs, this.inverseMass) );
			
			//TODO test impulse contre l'objet
			//pour la rotation, avec la rotation (pour être cohérant avec le moment d'inertie.)
			PVector relPoint = local(absPos);
			PVector relImpulse = sub(local( add(impulse, absPos) ), relPoint);
			PVector forRot = relPoint.cross(relImpulse);
			if (!forRot.equals(zero))
				rotationVel.addAngularMomentum( multMatrix(inverseInertiaMom, forRot) );
		}
	}
	
	/** ajoute de la quantit� de mouvement au centre de masse (absolu). */
	public void applyImpulse(PVector impulse) {
		applyImpulse(location, impulse);
	}
	
	/** permet d'appliquer une force au body � ce point (absolu) */
	public void addForce(PVector absPos, PVector force) {
		if (drawInteraction) {
			PVector to = PVector.add(absPos, PVector.mult(force, 0.2f));
			app.stroke(255);
			app.line(absPos.x, absPos.y, absPos.z, to.x, to.y, to.z);
		}
			
		PVector rel = PVector.sub(absPos, location);
		torques.add( rel.cross(force) );
		
		rel.normalize();
		PVector forceAbs = PVector.mult(rel, force.dot(rel));
		addForce(forceAbs);
	}
	
	/** applique une force absolue au centre de masse du body. */
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
	
	public void freine(float perte) {
		freineDepl(perte);
		freineRot(perte);
	}
	
	/** applique une force qui s'oppose � la vitesse. */
	public void freineDepl(float perte) {
		if (isZeroEps(velocity, true))
			return;
		//le frottement, frein. s'oppose à la vitesse :
	    velocity.mult(1-perte);
	}
	
	/** applique une force qui s'oppose � la vitesse angulaire. */
	public void freineRot(float perte) {
		if ( !rotationVel.isZeroEps(false) )
			rotationVel.setAngle(rotationVel.angle() * (1 - perte));
	}
}