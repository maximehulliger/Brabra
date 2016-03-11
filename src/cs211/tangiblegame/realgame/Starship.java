package cs211.tangiblegame.realgame;

import cs211.tangiblegame.geo.Plane;
import cs211.tangiblegame.geo.Quaternion;
import cs211.tangiblegame.realgame.Armement;
import cs211.tangiblegame.realgame.Armement.Armed;
import processing.core.PShape;
import processing.core.PVector;

/** STARSHIIIIPPP !!! */
public class Starship extends Plane implements Armed//Cube
{
	public static final float sizeFactor = 15f;
	private static final PVector size = PVector.mult( vec(7, 2, 8), sizeFactor); //for the collider
	private static final boolean displayViseur = true;
	
	MeteorSpawner champ;
	public Armement armement;
	public static PShape starship;
	
	public Starship(PVector location, Quaternion rotation) {
		super(location, rotation, 200, size);
		//PVector champSize = vec(5000, 5000, 8000);
		//this.champ = new MeteorSpawner(this, vec(0, 0, -champSize.z/6), champSize);
		this.armement = new Armement(this, 0, 1, 1);
		setName("Starship");
	}
	
	public Armement armement() {
		return armement;
	}
	
	public void setMass(float mass) {
		super.setMass(mass);
		if (inverseMass > 0) {
			float fact = mass*(sq(size.x) + sq(size.y) + sq(size.z))/7;
			float invFact = 1/fact;
			super.inertiaMom = new PVector(fact, fact, fact);
			super.inverseInertiaMom = new PVector(invFact, invFact, invFact);
		}
	}

	public void display() {
		app.noStroke();
		//2. le vaisseau (+viseur)
		pushLocal();
		if (displayViseur) {
			app.stroke(255, 0, 0, 150);
			app.line(0, -1, 0, 0, -1, -100000);
		}
		translate( vec(0, -10, 20) );
		app.shape(starship);
		popLocal();
		if (drawCollider) {
			app.fill(255, 0, 0, 100);
			super.display();
		}
	}
	
	//protected void addForces() {}
}