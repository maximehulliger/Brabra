package cs211.tangiblegame.realgame;

import cs211.tangiblegame.geo.Cube;
import cs211.tangiblegame.geo.Quaternion;
import cs211.tangiblegame.physic.Collider;
import cs211.tangiblegame.realgame.Armement;
import cs211.tangiblegame.realgame.Armement.Armed;
import processing.core.PShape;
import processing.core.PVector;

/** STARSHIIIIPPP !!! */
public class Starship extends Cube implements Armed {
	
	private static final float sizeFactor = 15f;
	private static final PVector size = PVector.mult( vec(7, 2, 8), sizeFactor); //for the collider
	private static final boolean displayViseur = true;
	
	private Armement armement;
	private static PShape starship;
	//private MeteorSpawner champ;
	
	public Starship(PVector location, Quaternion rotation) {
		super(location, rotation, size);
		if (starship == null) {
			starship = app.loadShape("starship.obj");
			starship.scale( sizeFactor );
		}
		setMass(200);
		setName("Starship");
		this.armement = new Armement(this, 0, 1, 1);
		//this.champ = new MeteorSpawner(this, vec(5000, 5000, 8000));
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

	/** Display the starship and the laser. */
	public void display() {
		pushLocal();
		if (displayViseur) {
			app.stroke(255, 0, 0, 150);
			app.line(0, -1, 0, 0, -1, -far);
		}
		translate( vec(0, -10, 20) );
		app.shape(starship);
		popLocal();
	}
	
	protected void onCollision(Collider other, PVector pos) {
		game.debug.log(presentation()+" a touché "+other.presentation());
	}
}