package brabra.game.scene.fun;

import brabra.game.Color;
import brabra.game.physic.Collider;
import brabra.game.physic.geo.Box;
import brabra.game.physic.geo.Quaternion;
import brabra.game.physic.geo.Vector;
import brabra.game.scene.weapons.Weaponry;
import processing.core.PShape;

/** STARSHIIIIPPP !!! */
public class Starship extends Box {
	
	private static final float sizeFactor = 15f;
	private static final Vector size = vec(7, 2, 8).multBy(sizeFactor); //for the collider
	private static final boolean displayViseur = true;
	private static final Color viseurColor = new Color("red", "255, 0, 0, 150");
	private Weaponry armement;
	private static PShape starship;
	
	public Starship(Vector location, Quaternion rotation) {
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
			super.inertiaMom = Vector.cube(fact);
			super.inverseInertiaMom = Vector.cube(1/fact);
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
	
	protected void onCollision(Collider other, Vector pos) {
		game.debug.log(presentation()+" a touché "+other.presentation());
	}
}