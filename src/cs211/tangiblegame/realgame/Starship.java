package cs211.tangiblegame.realgame;

import cs211.tangiblegame.Color;
import cs211.tangiblegame.geo.Cube;
import cs211.tangiblegame.geo.Quaternion;
import cs211.tangiblegame.physic.Collider;
import cs211.tangiblegame.realgame.Weaponry;
import cs211.tangiblegame.realgame.Weaponry.Armed;
import processing.core.PShape;
import processing.core.PVector;

/** STARSHIIIIPPP !!! */
public class Starship extends Cube implements Armed {
	
	private static final float sizeFactor = 15f;
	private static final PVector size = PVector.mult( vec(7, 2, 8), sizeFactor); //for the collider
	private static final boolean displayViseur = true;
	private static final Color viseurColor = new Color("red", "255, 0, 0, 150");
	private Weaponry armement;
	private static PShape starship;
	
	public Starship(PVector location, Quaternion rotation) {
		super(location, rotation, size);
		if (starship == null) {
			starship = app.loadShape("starship.obj");
			starship.scale( sizeFactor );
		}
		setMass(200);
		setName("Starship");
	}
	
	public Weaponry armement() {
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
		if (displayViseur)
			line(zero, front(far), viseurColor);
		
		displayColliderMaybe();
		displayInteractionMaybe();
		translate( vec(0, -10, 20) );
		app.shape(starship);
		popLocal();
	}
	
	protected void onCollision(Collider other, PVector pos) {
		game.debug.log(presentation()+" a touché "+other.presentation());
	}
}