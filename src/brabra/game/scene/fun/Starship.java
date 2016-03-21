package brabra.game.scene.fun;

import brabra.game.Color;
import brabra.game.physic.Collider;
import brabra.game.physic.geo.Cube;
import brabra.game.physic.geo.Quaternion;
import brabra.game.scene.weapons.Weaponry;
import processing.core.PShape;
import processing.core.PVector;

/** STARSHIIIIPPP !!! */
public class Starship extends Cube {
	
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
		displayColliderMaybe();
		displayInteractionMaybe();
		translate( vec(0, -10, 20) );
		if (displayViseur)
			line(zero, front(far), viseurColor);
		app.shape(starship);
		popLocal();
	}
	
	protected void onCollision(Collider other, PVector pos) {
		game.debug.log(presentation()+" a touch� "+other.presentation());
	}
}