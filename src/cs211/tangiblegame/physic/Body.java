package cs211.tangiblegame.physic;

import cs211.tangiblegame.ProMaster;
import cs211.tangiblegame.geo.Quaternion;
import processing.core.*;

public class Body extends ProMaster {
	private static final boolean drawInteraction = true; //forces et impulse

	public final PVector location;
	public final PVector velocity = zero.copy();
	public final Quaternion rotation;
	public final Quaternion rotationVel = identity.copy();
	/** Indicate the modification of the body transform during the frame before the update. */
	public boolean transformChanged = true;

	protected String name = "Body";
	protected Body parent = null;
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
	private boolean transformChangedCurrent = false;
	
	/** create a Body with this location & location. location can be null */
	public Body(PVector location, Quaternion rotation) {
		this.location = new BVector(location);
		this.rotation = new BQuaternion(rotation.copy());
	}

	/** to react when the object is removed from the scene */
	public void onDelete() {}

	/** to add force to the body every frame */
	protected void addForces() {}

	/** to react to a collision */
	protected void onCollision(Collider col, PVector impact) {}

	// --- update stuff (+transformChanged)
	
	/** applique les forces et update l'etat */
	public void update() {
		addForces();
		
		//1. translation, forces
		if (!isZeroEps(forces, false)) {
			PVector acceleration = PVector.mult( forces, inverseMass );
			velocity.add(PVector.mult(acceleration,Physic.deltaTime));
		}
		if (!isZeroEps(velocity, true)) {
			location.add(PVector.mult(velocity,Physic.deltaTime));
		}
		
		//2. rotation, vitesse angulaire, on prend rotation axis comme L/I
		if (!isZeroEps(torques, false)) {
			rotationVel.addAngularMomentum( multMatrix( inverseInertiaMom, torques ) );
		}
		if (!rotationVel.isZeroEps(false)) {
			rotation.rotate( rotationVel );
		}
		
		//check changes
		transformChanged = transformChangedCurrent;
		transformChangedCurrent = false;
		forces = zero.copy();
		torques = zero.copy();
		
		/*System.out.println("---------------");
		System.out.println("rotation: \nmag: "+rotation.mag()+"\n vec: "+rotation);
		System.out.println("vitesse Ang.: \nmag: "+angularVelocity.mag()+"\n vec: "+angularVelocity);*/
	}

	/** PVector notifiant le body des changements */
	private class BVector extends PVector {
		private static final long serialVersionUID = 5162673540041216409L;
		public BVector(PVector v) {
			super(v.x,v.y,v.z);
		}
		public PVector set(PVector v) {
			transformChangedCurrent = true;
			return super.set(v);
		}
		public PVector add(PVector v) {
			if (!v.equals(zero)) {
				transformChangedCurrent = true;
				return super.add(v);
			} else
				return this;
		}
	}
	
	/** Quaternion notifiant le body des changements */
	private class BQuaternion extends Quaternion {
		public BQuaternion(Quaternion q) {
			super((q == null) ? identity : q);
		}
		public Quaternion set(Quaternion v) {
			transformChangedCurrent = true;
			return super.set(v);
		}
		public Quaternion rotate(Quaternion r) {
			if (!r.equals(identity)) {
				transformChangedCurrent = true;
				return super.set(r);
			} else
				return this;
		}
	}
	
	// --- some setters

	/** set la masse du body. si -1, l'objet aura une mass et un moment d'inertie infini.
		à surcharger (et appeler) pour set le moment d'inertie. */
	public void setMass(float mass) {
		if (mass == -1) {
			this.mass = -1;
			this.affectedByCollision = false;
			this.inverseMass = 0;
			this.inverseInertiaMom = zero.copy();
		} else if (mass <= 0)
			throw new IllegalArgumentException("mass nÃ©gative ou nulle !");
		else {
			this.mass = mass;
			this.inverseMass = 1/this.mass;
			this.affectedByCollision = true;
		}
	}
	
	public void setColor(Color color) {
		this.color = color;
	}
	
	// --- name
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Body withName(String name) {
		setName(name);
		return this;
	}
	
	public String toString() {
		return name;
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
			System.out.println(toString()+" is a poor non-living object !");
		else if (life < 0)
			System.out.println(toString()+" is already dead !");
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
		if (equalsEps(absPos, location)) {
			velocity.add( PVector.mult(impulse, this.inverseMass) );
			return;
		}
		
		//pour le deplacement, seulement en absolu
		PVector toPos = PVector.sub(absPos, location);
		toPos.normalize();
		PVector forAbs = PVector.mult(toPos, impulse.dot(toPos));
		velocity.add( PVector.mult(forAbs, this.inverseMass) );
		
		//pour la rotation, avec la rotation (pour Ãªtre cohÃ©rant avec le moment d'inertie.)
		PVector relPoint = local(absPos);
		PVector relImpulse = PVector.sub(local( PVector.add(impulse, absPos)), relPoint);
		PVector forRot = relPoint.cross(relImpulse);
		assert(!isZeroEps(forRot, false));
		rotationVel.addAngularMomentum( multMatrix(inverseInertiaMom, forRot) );
	}
	
	/** ajoute de la quantité de mouvement au centre de masse (absolu). */
	public void applyImpulse(PVector impulse) {
		applyImpulse(location, impulse);
	}
	
	/** permet d'appliquer une force au body à ce point (absolu) */
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
		PVector poids = new PVector(0, -Physic.gravity*mass, 0);
		addForce(poids);
	}
	
	public void avance(float force) {
		addForce( absolute( PVector.mult(front, 150) ) , absFront(force) );
	}
	
	public void freine(float perte) {
		freineDepl(perte);
		freineRot(perte);
	}
	
	/** applique une force qui s'oppose à la vitesse. */
	public void freineDepl(float perte) {
		if (isZeroEps(velocity, true))
			return;
		//le frottement, frein. s'oppose Ã  la vitesse :
	    velocity.mult(1-perte);
	}
	
	/** applique une force qui s'oppose à la vitesse angulaire. */
	public void freineRot(float perte) {
		if ( !rotationVel.isZeroEps(false) )
			rotationVel.setAngle(rotationVel.angle() * (1 - perte));
	}
	
	// --- conversion vector global <-> local

	/**retourne la position de rel, un point relatif au body en absolu*/
	public PVector absolute(PVector rel) {
		PVector relAbs = absolute(rel, location, rotation);
		if (parent != null)
			return parent.absolute(relAbs);
		else
			return relAbs;
	}
	
	protected PVector[] absolute(PVector[] rels) {
		PVector[] ret = new PVector[rels.length];
		for (int i=0; i<rels.length; i++)
			ret[i] = absolute(rels[i]);
		return ret;
	}
	protected PVector local(PVector abs) {
		if (parent != null)
			return local(parent.local(abs), location, rotation);
		else
			return local(abs, location, rotation);
	}
	
	/** return the pos in front of the body at dist from location */
	public PVector absFront(float dist) {
		return absolute(PVector.mult(front, dist), zero, rotation);
	}
	
	/** return the pos in front of the body at dist from location */
	public PVector absUp(float dist) {
		return absolute(PVector.mult(up, dist), zero, rotation);
	}
	
	protected PVector velocityAt(PVector loc) {
		PVector relVel = velocity.copy();
		/*PVector rotVelAxis = rotationVel.rotAxis();
		if (!isZeroEps( rotationVel.angle ));
			relVel.add( rotVelAxis.cross(PVector.sub(loc, location)) );*/
		if (parent != null)
			return PVector.add( parent.velocityAt(loc), relVel);
		else
			return relVel;
	}
	
	protected void pushLocal() {
		if (parent != null)
			parent.pushLocal();
		app.pushMatrix();
		translate(location);
		app.pushMatrix();
		rotate(rotation);
	}
	
	protected void popLocal() {
		app.popMatrix();
	    app.popMatrix();
	    if (parent != null)
			parent.popLocal();
	}
}