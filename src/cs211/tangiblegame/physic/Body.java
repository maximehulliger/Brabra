package cs211.tangiblegame.physic;

import cs211.tangiblegame.ProMaster;
import cs211.tangiblegame.geo.Quaternion;
import processing.core.*;

public abstract class Body extends ProMaster {
	private static final boolean drawInteraction = true; //forces et impulse
	
	protected float mass = 0;
	protected float inverseMass = 0;
	protected PVector inertiaMom;
	protected PVector inverseInertiaMom;
	protected float restitution = 0.8f; // [0, 1]
	protected boolean affectedByCollision = true;

	private PVector lastLocation = zero.get();
	protected Quaternion lastRotation = new Quaternion();
	private PVector forces;
	private PVector torques;
	
	public final PVector velocity = zero.get();
	public final PVector location;
	public Quaternion rotationVel = new Quaternion();
	public Quaternion rotation = new Quaternion();
	
	public Body parent = null;			
	public boolean transformChanged = true;	//indique si la transformation du body a été modifiée cette frame.

	public Body(PVector location, Quaternion rotation) {
		this.location = location.get();
		this.rotation = rotation.get();
	}
	
	// set la masse du body. si -1, l'objet aura une mass et un moment d'inertie infini.
	// à surcharger (et appeler) pour set le moment d'inertie.
	protected void setMass(float mass) {
		if (mass == -1) {
			this.mass = -1;
			this.inverseMass = 0;
			this.inverseInertiaMom = zero.get();
		} else if (mass <= 0)
			throw new IllegalArgumentException("mass négative ou nulle !");
		else {
			this.mass = mass;
			this.inverseMass = 1/this.mass;
		}
	}

	// applique les forces et update l'etat (+transformChanged)
	public void update() {
		forces = zero.get();
		torques = zero.get();
		addForces();
		
		//1. translation, forces
		if (!forces.equals(zero)) {
			PVector acceleration = PVector.mult( forces, inverseMass );
			velocity.add(acceleration);
		}
		if (!velocity.equals(zero)) {
			location.add(velocity);
		}
		
		//2. rotation, vitesse angulaire, on prend rotation axis comme L/I
		if (!torques.equals(zero)) {
			rotationVel.addAngularMomentum( multMatrix( inverseInertiaMom, torques ) );
		}
		rotation.rotate( rotationVel );
			
		//check changes
		transformChanged = false;
		if (!location.equals(lastLocation)) {
			lastLocation = location.get();
			transformChanged = true;
		} 
		if (!rotation.equals(lastRotation)) {
			lastRotation = rotation.get();
			transformChanged = true;
			//checkAngles();
			/*System.out.println("---------------");
			System.out.println("rotation: \nmag: "+rotation.mag()+"\n vec: "+rotation);
			System.out.println("vitesse Ang.: \nmag: "+angularVelocity.mag()+"\n vec: "+angularVelocity);*/
		}
	}
	
	// ajoute de la quantité de mouvement au body à point (absolu).
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
		
		//pour la rotation, avec la rotation (pour être cohérant avec le moment d'inertie.)
		PVector relPoint = local(absPos);
		PVector relImpulse = PVector.sub(local( PVector.add(impulse, absPos)), relPoint);
		PVector forRot = relPoint.cross(relImpulse);
		rotationVel.addAngularMomentum( multMatrix(inverseInertiaMom, forRot) );
	}
	
	// à surcharger pour réagir à une collision
	public void onCollision(Collider col, PVector impact) {}
	
	//------ Gestion des forces et torques
	
	// méthode à surcharger pour appliquer des forces au body
	protected void addForces() {}

	// permet d'appliquer une force au body à ce point (absolu)
	protected void addForce(PVector absPos, PVector force) {
		if (drawInteraction) {
			PVector to = PVector.add(absPos, PVector.mult(force, 100));
			app.stroke(255);
			app.line(absPos.x, absPos.y, absPos.z, to.x, to.y, to.z);
		}
			
		PVector rel = PVector.sub(absPos, location);
		torques.add( rel.cross(force) );
		
		rel.normalize();
		PVector forceAbs = PVector.mult(rel, force.dot(rel));
		addForce(forceAbs);
	}
	
	// permet d'appliquer une force absolue au centre de masse du body.
	protected void addForce(PVector force) {	
		forces.add(force);
	}
	
	//-- méthodes cuites pour ajouter une force
	
	// applique son poids à l'objet
	protected void pese() {
		if (mass == -1)
			throw new IllegalArgumentException("un objet de mass infini ne devrait pas peser !");
		PVector poids = new PVector(0, -Physic.gravityConstant*mass, 0);
		addForce(poids);
	}
	
	protected void avance(float force) {
		addForce( absolute( PVector.mult(front, 30) ) , absFront(30 + force) );
	}
	
	protected void freine(float perte) {
		freineAbs(perte);
		freineRot(perte);
	}
	
	// applique une force qui s'oppose à la vitesse. 
	protected void freineAbs(float perte) {
		if (isZeroEps(velocity, true))
			return;
		//le frottement, frein. s'oppose à la vitesse :
	    velocity.mult(1-perte);
	}
	
	// applique une force qui s'oppose à la vitesse angulaire.
	protected void freineRot(float perte) {
		/*if (rotationVel.isZeroEps(true))
			return;
		else
			*/rotationVel = Quaternion.slerp(rotationVel, Quaternion.identity, perte);
	}
	
	
	//------ position absolue de points relatifs

	//retourne la position de rel, un point relatif au body en absolu 
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
	
	/*protected static PVector orientation(PVector front) {
		return ProMaster.front.cross(front);
	}*/
	
	protected PVector velocityAt(PVector loc) {
		PVector relVel = velocity.get();
		if (!rotationVel.isZeroEps(false))
			relVel.add( rotationVel.rotAxis().cross(PVector.sub(loc, location)) );
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
		if (parent != null)
			parent.popLocal();
		app.popMatrix();
	    app.popMatrix();
	}
}