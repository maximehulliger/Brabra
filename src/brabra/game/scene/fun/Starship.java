package brabra.game.scene.fun;

import brabra.Debug;
import brabra.game.Color;
import brabra.game.physic.Body;
import brabra.game.physic.geo.Box;
import brabra.game.physic.geo.ProTransform;
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
	
	public Starship() {
		super(size);
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
	
	/** Display the starship and the laser. */
	public void display() {
		pushLocal();
		displayColliderMaybe();
		displayInteractionMaybe();
		ProTransform.translate( vec(0, -10, 20) );
		if (displayViseur)
			line(zero, front(far), viseurColor);
		app.shape(starship);
		popLocal();
	}
	
	public boolean onCollision(Body other, Vector pos) {
		Debug.log(presentation()+" a touché "+other.presentation());
		return true;
	}
}