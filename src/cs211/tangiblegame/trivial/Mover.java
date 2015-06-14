package cs211.tangiblegame.trivial;

import cs211.tangiblegame.ProMaster;
import processing.core.PApplet;
import processing.core.PVector;

public class Mover extends ProMaster {
	//-- parametres
	boolean perteEnergieApresChocActivee = true;
	float ratioEnergieApresChoc = 0.8f; //ratio de restitution de l'énergie après un choc statique
	float gravityConstant = 0.7f;
	float frictionMagnitude = 0.1f;
	float empietementMaxContre = 0.4f; //en dessous de ces valeurs, le mover est considéré contre un obstacle
	float vitesseMaxContre = 0.1f;     //vitesse normale de collision

	//-- interne
	private static final PVector tTer = TrivialGame.tailleTerrain;
	private float radius;
	private PVector location;
	private PVector velocity;
	private PVector acceleration;
	private final TrivialGame trivialGame;

	public Mover(float radius, float mass, TrivialGame trivialGame) {
		location = new PVector(0,0,0);
		velocity = new PVector(0,0,0);
		acceleration = new PVector(0,0,0);
		this.radius = radius;
		this.trivialGame = trivialGame;
	}

	void update() {

		// frottement, frein. s'oppose à la vitesse :
		PVector frein = velocity.get();
		frein.normalize();
		frein.mult(-frictionMagnitude*gravityConstant);

		// accélération :
		acceleration.z = 
				PApplet.sin( trivialGame.platRot.x ) * gravityConstant +
				frein.z;
		acceleration.x = 
				- PApplet.sin( trivialGame.platRot.z ) * gravityConstant +
				frein.x;
		acceleration.y =
				frein.y;

		// update la vitesse puis la position :
		velocity.add(acceleration);
		location.add(velocity);

		// contrôle la position :
		checkEdges();
		checkCylinderCollision();
	}

	void display() {
		app.pushMatrix();
		app.fill(0,0,255);
		app.translate(location.x, radius + location.y, location.z);
		app.sphere(radius);
		app.popMatrix();
	}

	void checkEdges() {
		boolean collision = false;
		if (location.x > tTer.x/2) {
			location.x = tTer.x/2;
			velocity.x *= -1;
			collision = true;
		}
		else if (location.x < -tTer.x/2) {
			location.x = -tTer.x/2;
			velocity.x *= -1;
			collision = true;
		}
		if (location.z > tTer.z/2) {
			location.z = tTer.z/2;
			velocity.z *= -1;
			collision = true;
		}
		else if (location.z < -tTer.z/2) {
			location.z = -tTer.z/2;
			velocity.z *= -1;
			collision = true;
		}
	}

	void checkCylinderCollision() {
		for (PVector v : trivialGame.cylinders.cylindersPos) {
			if (collide2D(v, Cylinders.cylinderRadius, location, radius)) {
				//il y a une collision; on modifie la vitesse du mover.
				//V' = V − 2(V · n)n
				PVector n = PVector.sub(v, location);
				n.normalize();

				PVector vn2n = PVector.mult(n, velocity.dot(n)*2);
				velocity.sub(vn2n);
				perdEnergie(velocity, ratioEnergieApresChoc);

				//on update sa position
				//on le pousse hors de la balle selon n (selon tolérance pour optimiser)
				float empietement = Cylinders.cylinderRadius + radius - v.dist(location);
				if (empietement >= empietementMaxContre) {
					PVector correction = PVector.mult(n, empietement);
					location.sub(correction);
				}
			}
		} 
	}

	//utilise x and z, centre de 2 spheres et leurs rayons
	private boolean collide2D(PVector p1, float r1, PVector p2, float r2) {
		PVector v = p1.get();
		v.sub(p2);
		v.y = 0;
		return v.magSq() <= (r1+r2)*(r1+r2);
	}

	//retourne une vitesse amortie. prend un vecteur vitesse et le ration de restitution de l'energie après le choc.
	private void perdEnergie(PVector vitesse, float ratioRestitution) {
		float nv = PApplet.sqrt(vitesse.magSq() * ratioRestitution);
		vitesse.limit(nv);
	}

}
