package brabra.game.scene.weapons;

import brabra.game.physic.geo.Ellipsoide;
import processing.core.PVector;

public class Shield extends Ellipsoide {

	public Shield(PVector location, PVector rayons) {
		super(location, identity, rayons);
	}
	
	public void display() {
		app.fill(255, 100);
		super.display();
	}

}
